FROM openjdk:21-bookworm

RUN apt-get update && apt-get install -y pv curl vim jq linux-perf wget \
    graphviz jq git fonts-cantarell 

COPY ./cachedio.sh /cachedio.sh
COPY ./plantuml-bsd-1.2025.9.jar /plantuml.jar

USER daemon

