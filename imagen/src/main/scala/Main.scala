import cats.effect._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.ember.server._
import org.http4s.headers.`Content-Type`
import org.http4s.MediaType
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.Logger
import scala.sys.process._
import java.io._
import com.comcast.ip4s._
import org.typelevel.log4cats.slf4j.loggerFactoryforSync

import java.io.ByteArrayOutputStream
import java.util.zip.Inflater

object PlantUMLDecoder {

  // PlantUML Base64 characters
  private val charTable =
    "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_"

  private val charMap: Map[Char, Int] = charTable.zipWithIndex.toMap

  // Decode a single character to 6-bit value
  private def decode6bit(c: Char): Int = charMap.getOrElse(
    c,
    throw new IllegalArgumentException(s"Invalid character: $c")
  )

  // Decode 4 Base64 chars to 3 bytes
  private def decode4chars(
      c1: Char,
      c2: Char,
      c3: Char,
      c4: Char
  ): Array[Byte] = {
    val b1 = (decode6bit(c1) << 2) | (decode6bit(c2) >> 4)
    val b2 = ((decode6bit(c2) & 0xf) << 4) | (decode6bit(c3) >> 2)
    val b3 = ((decode6bit(c3) & 0x3) << 6) | decode6bit(c4)
    Array(b1.toByte, b2.toByte, b3.toByte)
  }

  // Decode full PlantUML string to bytes
  def decodePlantUML(encoded: String): Array[Byte] = {
    val baos = new ByteArrayOutputStream
    val len = encoded.length
    var i = 0
    while (i < len) {
      val c1 = encoded(i)
      val c2 = if (i + 1 < len) encoded(i + 1) else '0'
      val c3 = if (i + 2 < len) encoded(i + 2) else '0'
      val c4 = if (i + 3 < len) encoded(i + 3) else '0'
      val bytes = decode4chars(c1, c2, c3, c4)
      baos.write(bytes)
      i += 4
    }
    baos.toByteArray
  }

  // Inflate raw deflate bytes to original text
  def inflateRaw(data: Array[Byte]): String = {
    val inflater = new Inflater(true) // 'true' for raw deflate
    inflater.setInput(data)
    val out = new ByteArrayOutputStream()
    val buffer = new Array[Byte](1024)
    while (!inflater.finished()) {
      val count = inflater.inflate(buffer)
      out.write(buffer, 0, count)
    }
    out.toString("UTF-8")
  }

  // Convenience method
  def decodeToText(encoded: String): String = {
    val bytes = decodePlantUML(encoded)
    inflateRaw(bytes)
  }
}

object Main extends IOApp.Simple {

  val routes = HttpRoutes.of[IO] {
    case req @ GET -> Root / "plantuml" / "svg" / encoded =>
      val body = PlantUMLDecoder.decodeToText(encoded)
      for {
        svg <- IO.blocking {
          val process = Process(
            Seq(
              "/cachedio.sh",
              "java",
              "-jar",
              "/plantuml.jar",
              "-pipe",
              "-tsvg"
            )
          )
          val baos = new ByteArrayOutputStream()

          val io = new ProcessIO(
            in => {
              in.write(body.getBytes)
              in.close()
            },
            out => {
              out.transferTo(baos)
            },
            err => {
              err.transferTo(System.err)
            }
          )

          val p = process.run(io)
          p.exitValue()
          baos.toByteArray
        }
        resp <- Ok(svg).map(
          _.withContentType(`Content-Type`(MediaType.image.`svg+xml`))
        )
      } yield resp
  }

  val httpApp = routes.orNotFound

  override val run: IO[Unit] =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"5000")
      .withHttpApp(httpApp)
      .build
      .useForever
}
