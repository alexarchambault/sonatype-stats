#!/usr/bin/env bash
set -euv


DIR="$(dirname "${BASH_SOURCE[0]}")"

"$DIR/sonatype-stats.sh"

"$DIR/plot.sh"

"$DIR/upload.sh"

"$DIR/to-gh-pages.sh"
