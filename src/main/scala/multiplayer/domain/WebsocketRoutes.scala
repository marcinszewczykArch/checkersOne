package multiplayer.domain

import enumeratum._

sealed abstract class WebsocketRoutes(val tag: String) extends EnumEntry {}

object WebsocketRoutes extends Enum[WebsocketRoutes] {
  val values: IndexedSeq[WebsocketRoutes] = findValues

  case object State extends WebsocketRoutes("/state ")
  case object Move  extends WebsocketRoutes("/move ")
  case object Chat  extends WebsocketRoutes("/chat ")
  case object Error extends WebsocketRoutes("/error ")
  case object None  extends WebsocketRoutes("")
}
