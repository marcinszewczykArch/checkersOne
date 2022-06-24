package multiplayer

import cats.implicits._
import checkers.CheckersCodecs.gameStateEncoder
import checkers.domain.{GameState, PawnMove, ValidateMove}
import io.circe.syntax.EncoderOps
import multiplayer.players.domain.Player
import multiplayer.rooms.domain.Room

object ChatStateNew {
  // Default constructor
  def apply(): ChatStateNew = ChatStateNew(Map.empty, Map.empty)
}

case class ChatStateNew(
                         userRooms: Map[Player, Room],
                         roomMembers: Map[Room, Set[Player]]
                       ) {

  def process(msg: InputMessageNew): (ChatStateNew, Seq[OutputMessageNew]) = msg match {

    case Chat(player: Player, text: String) => userRooms.get(player) match {
      case Some(room) =>

        val textArray: Array[String] = text.split("/")

          //example input with move: "woooooooooooooooooooooooooooooow/w/31/27/"
        if (textArray.length == 5) {

            val board = textArray(0)
            val currentColour = textArray(1)
            val moveFrom = textArray(2)
            val moveTo = textArray(3)

          val state: GameState = GameState.fromString(board, currentColour)
          val move: PawnMove = PawnMove.fromString(moveFrom, moveTo)

          ValidateMove.apply().apply(move, state) match {
            case Right(newState)        => (this, sendToRoom(room, newState.asJson.toString)) //todo: to return http response
            case Left(validationError)  => (this, sendToRoom(room, validationError.show)) //todo: to return http response
          }

        } else {
        (this, sendToRoom(room, s"${player.name.value}: $text"))
      }

      case None =>
        (this, Seq(SendToUser(player, "You are not currently in a room")))
    }

    case EnterRoom(user, toRoom) => userRooms.get(user) match {
      case None =>
        // First time in - welcome and enter
        val (finalState, enterMessages) = addToRoom(user, toRoom)

        (finalState, Seq(WelcomeUser(user)) ++ enterMessages)

      case Some(currentRoom) if currentRoom == toRoom =>
        (this, Seq(SendToUser(user, "You are already in that room!")))

      case Some(_) =>
        // Already in - move from one room to another
        val (intermediateState, leaveMessages) = removeFromCurrentRoom(user)
        val (finalState, enterMessages)        = intermediateState.addToRoom(user, toRoom)

        (finalState, leaveMessages ++ enterMessages)
    }

    case Disconnect(user) => removeFromCurrentRoom(user)
  }

  private def sendToRoom(room: Room, text: String): Seq[OutputMessageNew] = {
    roomMembers
      .get(room)
      .map(SendToUsers(_, text))
      .toSeq
  }

  private def removeFromCurrentRoom(user: Player): (ChatStateNew, Seq[OutputMessageNew]) = userRooms.get(user) match {
    case Some(room) =>
      val nextMembers: Set[Player] = roomMembers.getOrElse(room, Set()) - user
      val nextState: ChatStateNew =
        if (nextMembers.isEmpty)
          ChatStateNew(userRooms - user, roomMembers - room)
        else
          ChatStateNew(userRooms - user, roomMembers + (room -> nextMembers))

      // Send to "previous" room population to include the leaving user
      (nextState, sendToRoom(room, s"$user has left $room"))
    case None =>
      (this, Nil)
  }

  private def addToRoom(player: Player, room: Room): (ChatStateNew, Seq[OutputMessageNew]) = {
    val nextMembers = roomMembers.getOrElse(room, Set()) + player
    val nextState   = ChatStateNew(userRooms + (player -> room), roomMembers + (room -> nextMembers))

    // Send to "next" room population to include the joining user
    (nextState, nextState.sendToRoom(room, s"${player.name.value} has joined ${room.name.value}"))
  }


}