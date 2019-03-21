#!/usr/bin/env amm

import $ivy.`com.softwaremill.sttp::core:1.5.10`

import java.nio.file._
import java.time.{YearMonth, ZoneOffset}

import com.softwaremill.sttp.quick._
import upickle.default._
import ujson.{read => _, _}

object Responses {

  case class UniqueIpData(total: Int)
  implicit val uniqueIpDataRW: ReadWriter[UniqueIpData] = macroRW
  case class UniqueIpResp(data: UniqueIpData)
  implicit val uniqueIpRespRW: ReadWriter[UniqueIpResp] = macroRW

  case class Elem(id: String, name: String)
  implicit val elemRW: ReadWriter[Elem] = macroRW

}

import Responses._

object Params {

  // organization one was granted write access to
  val proj = sys.env.getOrElse(
    "SONATYPE_PROJECT",
    sys.error("SONATYPE_PROJECT not set")
  )
  // actual organization used for publishing (must have proj as prefix)
  val organization = sys.env.getOrElse("SONATYPE_PROJECT", proj) 

  val sonatypeUser = sys.env.getOrElse(
    "SONATYPE_USERNAME",
    sys.error("SONATYPE_USERNAME not set")
  )
  val sonatypePassword: String = sys.env.getOrElse(
    "SONATYPE_PASSWORD",
    sys.error("SONATYPE_PASSWORD not set")
  )

  val start = YearMonth.now(ZoneOffset.UTC)

  val projId = {

    val projectIds = {
      val projResp = sttp
      .auth.basic(Params.sonatypeUser, Params.sonatypePassword)
      .header("Accept", "application/json")
      .get(uri"https://oss.sonatype.org/service/local/stats/projects")
      .send()

      if (!projResp.isSuccess)
        sys.error("Error getting project list: " + projResp.statusText)

      val respJson = ujson.read(projResp.body.right.get)

      read[Seq[Elem]](respJson("data"))
        .map(e => e.name -> e.id)
        .toMap
    }

    projectIds(Params.proj)
  }

  val cutOff = start.minusMonths(4L)

  val base = Paths.get("data")
}

case class Data(
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

        val statResp = sttp
          .auth.basic(Params.sonatypeUser, Params.sonatypePassword)
          .header("Accept", "application/json")
          .get(u)
          .send()

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

val statsData = Data(
  Params.base.resolve("stats"),
  "csv",
  _.isEmpty,
  "slices_csv",
  "raw",
  Params.projId,
  Params.organization
)

val uniqueIpsData = Data(
  Params.base.resolve("unique-ips"),
  "json",
  s => read[UniqueIpResp](ujson.read(s)).data.total <= 0,
  "timeline",
  "ip",
  Params.projId,
  Params.organization
)

for (data <- Seq(statsData, uniqueIpsData)) {
  val it = Iterator.iterate(Params.start)(_.minusMonths(1L))
  val processed = data.process(it)
    .takeWhile {
      case (monthYear, nonEmpty) =>
        nonEmpty || monthYear.compareTo(Params.cutOff) >= 0
    }
    .length

  System.err.println(s"Processed $processed months in ${data.base} for type ${data.tpe}")
}

