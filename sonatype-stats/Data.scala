
import sttp.client3.quick._

import java.nio.file.{Files, Path}
import java.time.{YearMonth, ZoneOffset}

final case class Data(
  base: Path,
  ext: String,
  empty: String => Boolean,
  name: String,
  tpe: String,
  projId: String,
  organization: String
) {

  def fileFor(monthYear: YearMonth): Path = {
    val year = monthYear.getYear
    val month = monthYear.getMonth.getValue
    base.resolve(f"$year%04d/$month%02d.$ext")
  }

  def exists(monthYear: YearMonth): Boolean =
    Files.isRegularFile(fileFor(monthYear))

  def write(monthYear: YearMonth, content: String): Unit = {
    System.err.println(s"Writing $monthYear (${content.length} B)")
    val f = fileFor(monthYear)
    Files.createDirectories(f.getParent)
    Files.write(f, content.getBytes("UTF-8"))
  }

  def urlFor(monthYear: YearMonth) = {
    val year = monthYear.getYear
    val month = monthYear.getMonth.getValue

    uri"https://oss.sonatype.org/service/local/stats/$name?p=$projId&g=$organization&a=&t=$tpe&from=${f"$year%04d$month%02d"}&nom=1"
  }

  def process(monthYears: Iterator[YearMonth]): Iterator[(YearMonth, Boolean)] =
    monthYears
      .filter { monthYear =>
        !exists(monthYear)
      }
      .map { monthYear =>

        val u = urlFor(monthYear)

        System.err.println(s"Getting $monthYear: $u")

        val statResp = simpleHttpClient.send {
          basicRequest
            .auth.basic(Params.sonatypeUser, Params.sonatypePassword)
            .header("Accept", "application/json")
            .get(u)
        }

        if (!statResp.isSuccess)
          sys.error("Error getting project stats: " + statResp.statusText)

        val stats = statResp.body.right.get.trim

        val empty0 = empty(stats)
        if (empty0)
          System.err.println(s"Empty response at $monthYear")
        else
          write(monthYear, stats)

        monthYear -> !empty0
      }
}
