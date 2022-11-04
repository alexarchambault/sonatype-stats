
import sttp.client3.quick._
import upickle.default._

import java.nio.file.Paths
import java.time.{YearMonth, ZoneOffset}

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
      val projResp = simpleHttpClient.send {
        basicRequest
          .auth.basic(Params.sonatypeUser, Params.sonatypePassword)
          .header("Accept", "application/json")
          .get(uri"https://oss.sonatype.org/service/local/stats/projects")
      }

      if (!projResp.isSuccess)
        sys.error("Error getting project list: " + projResp.statusText)

      val respJson = ujson.read(projResp.body.right.get)

      read[Seq[Responses.Elem]](respJson("data"))
        .map(e => e.name -> e.id)
        .toMap
    }

    projectIds(Params.proj)
  }

  val cutOff = start.minusMonths(4L)

  val base = Paths.get("data")
}
