
import upickle.default._

case class UniqueIpResp(data: UniqueIpData)

object UniqueIpResp {
  implicit val uniqueIpRespRW: ReadWriter[UniqueIpResp] = macroRW
}
