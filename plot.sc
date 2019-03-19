
import $ivy.`com.github.tototoshi::scala-csv:1.3.5`
import $ivy.`com.twitter::algebird-core:0.13.0`
import $ivy.`org.plotly-scala::plotly-render:0.5.2`

import java.io.File
import java.nio.file.{Files, Paths}
import java.time._

import com.twitter.algebird.Operators._
import plotly._
import plotly.element._
import plotly.layout._
import plotly.Plotly._
import upickle.default._
import ujson.{read => _, _}

import com.github.tototoshi.csv._


case class UniqueIpData(total: Int)
case class UniqueIpResp(data: UniqueIpData)
implicit val uniqueIpDataRW: ReadWriter[UniqueIpData] = macroRW
implicit val uniqueIpRespRW: ReadWriter[UniqueIpResp] = macroRW

def csvScatter(dir: File, name: String, filterOut: Set[YearMonth] = Set()) = {

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
    .map { case (ym, _, n) => ym -> n }
    .sumByKey
    .toVector
    .sortBy(_._1)
  
  def byMonth0 = byMonth.filter { case (ym, _) => !filterOut(ym) }
  
  def x = byMonth0.map(_._1).map { m =>
    plotly.element.LocalDateTime(m.getYear, m.getMonthValue, 1, 0, 0, 0)
  }
  def y = byMonth0.map(_._2)

  Bar(x, y, name = name)
}

def uniqueIpScatter(dir: File, name: String) = {

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

  Bar(x, y, name = name)
}

val plotBase = Paths.get("plots")
Files.createDirectories(plotBase)

val dataBase = Paths.get("data")

val downloadsDest = plotBase.resolve("downloads.html")
val uniqueIpsDest = plotBase.resolve("unique-ips.html")

Files.deleteIfExists(downloadsDest)
Files.deleteIfExists(uniqueIpsDest)

Seq(
  csvScatter(dataBase.resolve("stats").toFile, "# downloads")
).plot(
  path = downloadsDest.toString,
  addSuffixIfExists = false
)

Seq(
  uniqueIpScatter(dataBase.resolve("unique-ips").toFile, "Unique IPs")
).plot(
  path = uniqueIpsDest.toString,
  addSuffixIfExists = false
)


