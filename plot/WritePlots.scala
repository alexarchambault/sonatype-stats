//> using scala "2.13"

//> using lib "com.github.alexarchambault::case-app:2.1.0-M19"
//> using lib "com.github.tototoshi::scala-csv:1.3.10"
//> using lib "com.twitter::algebird-core:0.13.9"
//> using lib "org.plotly-scala::plotly-render:0.8.4"
//> using lib "com.lihaoyi::upickle:1.6.0"

import caseapp._
import plotly._
import plotly.element._
import plotly.layout._

import java.nio.file.{Files, Paths}

object WritePlots {

  final case class Options(
    nameFilter: String = "",
    @ExtraName("o")
      output: String = "stats.html"
  )

  def main(args: Array[String]): Unit = {

    val (options, args0) = CaseApp.process[Options](args)

    val dataBase = args0.all match {
      case Seq() => sys.error("Expected data dir as argument")
      case Seq(dir) => Paths.get(dir)
      case _ => sys.error("Expected a single data dir as argument")
    }

    val filterNames = Some(options.nameFilter).filter(_.trim.nonEmpty)

    val downloadsTraces = Seq(
      Plots.csvScatter(dataBase.resolve("stats").toFile, "# downloads", filterNames)
    )

    val uniqueIpsTraces =
      if (filterNames.isEmpty)
        Seq(
          Plots.uniqueIpScatter(dataBase.resolve("unique-ips").toFile, "Unique IPs")
        )
      else
        Nil


    val dlDivId = "downloads"
    val ipDivId = "uniqueips"

    val layout = Layout()

    val config = Config()

    val html =
      s"""<!DOCTYPE html>
         |<html>
         |<head>
         |<title>${layout.title.getOrElse("plotly chart")}</title>
         |<script src="https://cdn.plot.ly/plotly-${Plotly.plotlyVersion}.min.js"></script>
         |</head>
         |<body>
         |<div id="$dlDivId"></div>
         |<div id="$ipDivId"></div>
         |<script>
         |${Plotly.jsSnippet(dlDivId, downloadsTraces, layout, config)}
         |${Plotly.jsSnippet(ipDivId, uniqueIpsTraces, layout, config)}
         |</script>
         |</body>
         |</html>
         |""".stripMargin

    val dest = Paths.get(options.output)
    Files.write(dest, html.getBytes("UTF-8"))
    System.err.println(s"Wrote $dest")
  }
}
