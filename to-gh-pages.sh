#!/usr/bin/env bash
set -euv

DIR="$(pwd)"
TARGET="$(dirname "${BASH_SOURCE[0]}")/target"

mkdir -p "$TARGET"
cd "$TARGET"


if [ -d gh-pages ]; then
  echo "Removing former gh-pages clone"
  rm -rf gh-pages
fi

echo "Cloning"
git clone "https://${GH_TOKEN}@github.com/$TRAVIS_REPO_SLUG.git" -b gh-pages gh-pages
cd gh-pages

git config user.name "Travis-CI"
git config user.email "invalid@travis-ci.com"

GH_PAGES_DEST="${GH_PAGES_DEST:-"index.html"}"
cp "$DIR/stats.html" "$GH_PAGES_DEST"
git add -- "$GH_PAGES_DEST"

MSG="Update Sonatype statistics"

if git status | grep "nothing to commit" >/dev/null 2>&1; then
  echo "Nothing changed"
else
  git commit -m "$MSG"

  echo "Pushing changes"
  git push origin gh-pages
fi
