from flask import Flask, request, Response
import subprocess

app = Flask(__name__)

@app.route("/plantuml", methods=["POST"])
def plantuml():
    uml_code = request.data.decode('utf-8')
    try:
        # Call PlantUML with stdin input, output to stdout
        result = subprocess.run(
            ['./cachedio.sh', 'java -jar plantuml.jar -pipe'],
            input=uml_code.encode('utf-8'),
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            check=True
        )
        return Response(result.stdout, mimetype='image/png')
    except subprocess.CalledProcessError as e:
        return Response(f"Error: {e.stderr.decode('utf-8')}", status=500)

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
