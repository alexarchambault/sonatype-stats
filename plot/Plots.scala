
import com.github.tototoshi.csv._
import com.twitter.algebird.Operators._
import plotly._
import plotly.element._
import plotly.layout._
import upickle.default._

import java.io.File
import java.nio.file.Files
import java.time._

object Plots {

  private def blobPattern(s: String) =
    ("\\Q" + s.replace("*", "\\E.*\\Q") + "\\E").r

  def csvScatter(dir: File, name: String, filterNames: Option[String] = None, filterOutMonths: Set[YearMonth] = Set()) = {

    val patternOpt = filterNames.map(blobPattern(_).pattern)

    val data = for {

      year <- 2015 to Year.now(ZoneOffset.UTC).getValue
      month <- 1 to 12

      f = new File(dir, f"$year/$month%02d.csv")
      if f.exists()

      ym = YearMonth.of(year, month)

      elem <- CSVReader.open(f)
        .iterator
        .map(l => (ym, l(0), l(1).toInt))
        .toVector

    } yield elem

    val byMonth = data
      .filter {
        patternOpt match {
          case None => _ => true
          case Some(p) => t => p.matcher(t._2).matches()
        }
      }
      .map { case (ym, _, n) => ym -> n }
      .sumByKey
      .toVector
      .sortBy(_._1)

    def byMonth0 = byMonth.filter { case (ym, _) => !filterOutMonths(ym) }

    def x = byMonth0.map(_._1).map { m =>
      plotly.element.LocalDateTime(m.getYear, m.getMonthValue, 1, 0, 0, 0)
    }
    def y = byMonth0.map(_._2)

    Bar(x, y)
      .withName(name)
  }

  def uniqueIpData(dir: File): (Seq[plotly.element.LocalDateTime], Seq[Int]) = {

    val data = for {

      year <- 2015 to Year.now(ZoneOffset.UTC).getValue
      month <- 1 to 12

      f = new File(dir, f"$year/$month%02d.json")
      if f.exists()

      ym = YearMonth.of(year, month)

    } yield {
      val s = new String(Files.readAllBytes(f.toPath), "UTF-8")
      val resp = read[UniqueIpResp](ujson.read(s))
      ym -> resp.data.total
    }

    def x = data.map(_._1).map { m =>
      plotly.element.LocalDateTime(m.getYear, m.getMonthValue, 1, 0, 0, 0)
    }
    def y = data.map(_._2)

    (x, y)
  }

  def uniqueIpScatter(dir: File, name: String): Bar = {
    val (x, y) = uniqueIpData(dir)
    Bar(x, y)
      .withName(name)
  }

}
