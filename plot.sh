#!/usr/bin/env bash
set -e

TARGET="$(dirname "${BASH_SOURCE[0]}")/target"

mkdir -p "$TARGET"

if [ ! -x "$TARGET/coursier" ]; then
  curl -Lo "$TARGET/coursier" https://git.io/coursier-cli
  chmod +x "$TARGET/coursier"
fi

"$TARGET/coursier" launch com.lihaoyi:ammonite_2.12.8:1.6.4 \
  -M ammonite.Main \
  -- \
    "$(dirname "${BASH_SOURCE[0]}")/plot.sc"
