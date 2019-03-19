Fetch / update the stats with
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
