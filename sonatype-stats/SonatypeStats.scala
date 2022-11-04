//> using scala "2.13"
//> using lib "com.softwaremill.sttp.client3::core:3.8.3"
//> using lib "com.lihaoyi::upickle:1.6.0"

import upickle.default._

object SonatypeStats {
  def main(args: Array[String]): Unit = {

    val statsData = Data(
      Params.base.resolve("stats"),
      "csv",
      _.isEmpty,
      "slices_csv",
      "raw",
      Params.projId,
      Params.organization
    )

    val perArtifactUniqueIpsData = Data(
      Params.base.resolve("per-artifact-unique-ips"),
      "csv",
      _.isEmpty,
      "slices_csv",
      "ip",
      Params.projId,
      Params.organization
    )

    val uniqueIpsData = Data(
      Params.base.resolve("unique-ips"),
      "json",
      s => read[Responses.UniqueIpResp](ujson.read(s)).data.total <= 0,
      "timeline",
      "ip",
      Params.projId,
      Params.organization
    )

    for (data <- Seq(statsData, perArtifactUniqueIpsData, uniqueIpsData)) {
      val it = Iterator.iterate(Params.start)(_.minusMonths(1L))
      val processed = data.process(it)
        .takeWhile {
          case (monthYear, nonEmpty) =>
            nonEmpty || monthYear.compareTo(Params.cutOff) >= 0
        }
        .length

      System.err.println(s"Processed $processed months in ${data.base} for type ${data.tpe}")
    }
  }
}
