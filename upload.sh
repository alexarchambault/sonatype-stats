#!/usr/bin/env bash
set -euv

git config user.name "Travis-CI"
git config user.email "invalid@travis-ci.com"

git add -- stats.html data

MSG="Update Sonatype statistics"

if git status | grep "nothing to commit" >/dev/null 2>&1; then
  echo "Nothing changed"
else
  git commit -m "$MSG"

  echo "Pushing changes"
  git push origin gh-pages
fi
