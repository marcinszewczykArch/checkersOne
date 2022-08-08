package multiPlayer.domain

import cats.effect.IO
import io.circe.syntax.EncoderOps
import multiPlayer.MultiPlayerCodecs.multiplayerStateEncoder

trait OutputMessage {
  def forPlayer(targetPlayer: Player): Boolean
  def toString: String
}

case class SendToUser(
  player: Player,
  prefix: WebsocketRoutes,
  text: String
) extends OutputMessage {
  override def forPlayer(targetPlayer: Player): Boolean = targetPlayer == player
  override def toString: String                         = prefix.tag + text
}

case class SendToUsers(
  players: List[Player],
  prefix: WebsocketRoutes,
  text: String
) extends OutputMessage {
  override def forPlayer(targetPlayer: Player): Boolean = players.contains(targetPlayer)
  override def toString: String                         = prefix.tag + text
}

case class KeepAlive(
  state: IO[MultiplayerState]
) extends OutputMessage {
  override def forPlayer(targetPlayer: Player) = true
  override def toString: String                = WebsocketRoutes.StateRoute.tag + state.map(_.asJson.toString()).unsafeRunSync()
}
