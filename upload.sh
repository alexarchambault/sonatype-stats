#!/usr/bin/env bash
set -euv

git config user.name "Travis-CI"
git config user.email "invalid@travis-ci.com"

git remote add writable "https://${GH_TOKEN}@github.com/$TRAVIS_REPO_SLUG.git"

git add -- stats.html data

MSG="Update Sonatype statistics"

if git status | grep "nothing to commit" >/dev/null 2>&1; then
  echo "Nothing changed"
else
  git commit -m "$MSG"

  echo "Pushing changes"
  git push writable HEAD:"$TRAVIS_BRANCH"
fi
