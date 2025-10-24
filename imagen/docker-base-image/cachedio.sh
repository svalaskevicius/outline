#!/usr/bin/env bash

C="$(cat -)"
A="$*"
K="$(echo "$A$C" | sha256sum  | cut -f1 -d' ')"
mkdir -p /tmp/work/cachedio
F="/tmp/work/cachedio/$K"
if test -f "$F" ; then
  cat "$F"
else 
  echo "$C" | "$@" > "$F"
  cat "$F"
fi

