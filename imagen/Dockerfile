FROM python:3.11-slim

RUN apt-get update && \
    apt-get install -y openjdk-21-jdk-headless wget curl && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

RUN wget https://github.com/plantuml/plantuml/releases/latest/download/plantuml.jar

COPY cachedio.sh .
COPY app.py .

RUN pip install --no-cache-dir flask

EXPOSE 5000

CMD ["python", "app.py"]
