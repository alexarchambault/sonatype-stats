
import upickle.default._

case class UniqueIpData(total: Int)

object UniqueIpData {
  implicit val uniqueIpDataRW: ReadWriter[UniqueIpData] = macroRW
}
