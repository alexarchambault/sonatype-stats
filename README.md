# Cron job on Travis CI

## Clone this repo

Clone this repository, and set up your clone in Travis CI.

## Travis CI settings

In your clone settings on Travis CI, add the following environment variables (these are secret by default):
- `SONATYPE_USERNAME`: your Sonatype username, or the name part of your Sonatype token,
- `SONATYPE_PASSWORD`: your Sonatype password, or the password part of your Sonatype token,
- `SONATYPE_PROJECT`: the Sonatype project you want statistics for (should be the organization you publish under, like `com.github.user`),
- `GH_TOKEN`: a GitHub personal access token. Create one on GitHub, by going into Settings > Developer settings > Personal access tokens (just the public repo rights should be enough).

## Cron job

In the Travis CI settings of your clone, add a daily project for the `stats` branch.

# Test it locally

Fetch / update stats with
```bash
$ SONATYPE_PROJECT=io.get-coursier \
  SONATYPE_USERNAME=… SONATYPE_PASSWORD=… \
  ./sonatype-stats.sh
```
(replace `io.get-coursier` with your own Sonatype organization)

Update the plots with
```bash
$ ./plot.sh
```

Then open `stats.html` in your browser (or do whatever you want with what's under `data/`).

