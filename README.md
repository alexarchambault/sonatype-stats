The scripts in this repository allow to fetch and regularly update project
statistics from Sonatype. You can run them either [locally](#test-it-locally),
or from a [cron job on Travis CI](#cron-job-on-travis-ci).

## Test it locally

Fetch / update stats with
```bash
$ SONATYPE_PROJECT=io.get-coursier \
  SONATYPE_USERNAME=… SONATYPE_PASSWORD=… \
  ./sonatype-stats.sh
```
(replace `io.get-coursier` with your own Sonatype organization)
This fetches CSV and JSON files from `oss.sonatype.org`, and places them
under `data/`.

Create or update the plots with
```bash
$ ./plot.sh
```
This creates a `stats.html` web page, that displays the number of downloads
and of unique IPs that hit your project, for each month.

Open `stats.html` in your browser to visualize it.

## Cron job on Travis CI

### Clone this repo

Clone this repository, and set up your clone in Travis CI.

### Travis CI settings

In your clone settings on Travis CI, add the following environment variables (these are secret by default):
- `SONATYPE_USERNAME`: your Sonatype username, or the name part of your Sonatype token,
- `SONATYPE_PASSWORD`: your Sonatype password, or the password part of your Sonatype token,
- `SONATYPE_PROJECT`: the Sonatype project you want statistics for (should be the organization you publish under, like `com.github.user`),
- `GH_TOKEN`: a GitHub personal access token. Create one on GitHub, by going into Settings > Developer settings > Personal access tokens (just the public repo rights should be enough).

### Cron job

From GitHub, navigate to your clone, then to its `stats` branch. Add an empty
file, to trigger a Travis CI job for the `stats` branch.

Then in the Travis CI settings of your clone, in the cron section,
add a daily cron for the `stats` branch.

### GitHub page

Once the Travis CI job ran once, a page with the total downloads and unique IPs,
per month, should have been pushed to the `gh-pages` branch of your clone.

Navigate to it, at
```
https://your-user-name.github.io/sonatype-stats
```

