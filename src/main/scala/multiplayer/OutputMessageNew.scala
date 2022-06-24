package multiplayer

import multiplayer.players.domain.Player
import org.http4s.BuildInfo

/*
 * Trait for any message that can be sent downstream to one or more users
 */
trait OutputMessageNew {
  def forPlayer(targetPlayer: Player): Boolean
  def toString: String
}

case class WelcomeUser(player: Player) extends OutputMessageNew {
  override def forPlayer(targetPlayer: Player): Boolean = targetPlayer == player
  override def toString: String                         = s"Welcome to ChatServer version ${BuildInfo.version} - an example for http4s"
}

case class SendToUser(player: Player, text: String) extends OutputMessageNew {
  override def forPlayer(targetPlayer: Player): Boolean = targetPlayer == player
  override def toString: String                         = text
}

case class SendToUsers(users: Set[Player], text: String) extends OutputMessageNew {
  override def forPlayer(targetUser: Player): Boolean = users.contains(targetUser)
  override def toString: String                       = text
}

case object KeepAlive extends OutputMessageNew {
  override def forPlayer(targetPlayer: Player) = true
  override def toString: String                = ""
}
