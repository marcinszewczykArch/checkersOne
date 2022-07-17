package multiplayer.domain

import enumeratum._

sealed abstract class WebsocketRoutes(val tag: String) extends EnumEntry {}

object WebsocketRoutes extends Enum[WebsocketRoutes] {
  val values: IndexedSeq[WebsocketRoutes] = findValues

  case object StateRoute     extends WebsocketRoutes("/state ")
  case object MoveRoute      extends WebsocketRoutes("/move ")
  case object ChatRoute      extends WebsocketRoutes("/chat ")
  case object ErrorRoute     extends WebsocketRoutes("/error ")
  case object RoomRoute      extends WebsocketRoutes("/room ")
  case object LeaveRoomRoute extends WebsocketRoutes("/leaveRoom ")
  case object None           extends WebsocketRoutes("")
}
