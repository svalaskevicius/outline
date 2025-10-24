#!/bin/sh

set -x
set -e

cd "$(dirname "$0")"

tag="$(date '+%Y-%m-%dT%H-%M-%S')"

docker build -t "imagen-base:$tag" -f "$(pwd)/Dockerfile" .
