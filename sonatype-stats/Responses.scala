
import upickle.default._

object Responses {

  case class UniqueIpData(total: Int)
  implicit val uniqueIpDataRW: ReadWriter[UniqueIpData] = macroRW
  case class UniqueIpResp(data: UniqueIpData)
  implicit val uniqueIpRespRW: ReadWriter[UniqueIpResp] = macroRW

  case class Elem(id: String, name: String)
  implicit val elemRW: ReadWriter[Elem] = macroRW

}
