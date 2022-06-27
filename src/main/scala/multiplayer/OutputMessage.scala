package multiplayer

import multiplayer.players.domain.Player

trait OutputMessage {
  def forPlayer(targetPlayer: Player): Boolean
  def toString: String
}

case class SendToUser(player: Player, text: String) extends OutputMessage {
  override def forPlayer(targetPlayer: Player): Boolean = targetPlayer == player
  override def toString: String                         = text
}

case class SendToUsers(players: List[Player], text: String) extends OutputMessage {
  override def forPlayer(targetPlayer: Player): Boolean = players.contains(targetPlayer)
  override def toString: String                         = text
}


