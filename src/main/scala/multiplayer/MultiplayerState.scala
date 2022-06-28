package multiplayer

import cats.implicits._
import checkers.CheckersCodecs.gameStateEncoder
import checkers.domain.{GameState, PawnMove, Side, ValidateMove}
import io.circe.syntax.EncoderOps
import multiplayer.players.domain.Player
import multiplayer.rooms.domain.Room

object MultiplayerState {
  // Default constructor
  def apply(): MultiplayerState = MultiplayerState(List.empty, List.empty)
}

case class MultiplayerState(players: List[Player], rooms: List[Room]) {

  def process(msg: InputMessage): (MultiplayerState, Seq[OutputMessage]) = msg match {

    case Chat(player: Player, text: String) => findRoomByPlayer(player) match {
      case Some(room) => (this, sendToRoom(room, s"${player.name}: $text"))
      case None       => (this, Seq(SendToUser(player, s"${player.name}: $text")))
    }

    case PlayersInGame(player: Player) => addToGame(player)

    case EnterRoom(player, toRoom) => findRoomByPlayer(player) match {
      case None       => addToRoom(player, toRoom)
      case Some(room) => (this, Seq(SendToUser(player, s"You are already in the room! Leave '${room.name}' first")))
    }

    case LeaveRoom(player)  => findRoomByPlayer(player) match {
      case Some(_)    => removeFromCurrentRoom(player)
      case None       => (this, Seq(SendToUser(player, "You are not currently in a room")))
    }

    case Disconnect(player)   => disconnectPlayer(player)

    case ListRooms(player)    => (this, Seq(SendToUser(player, roomsToString())))

    case ListPlayers(player)  => (this, Seq(SendToUser(player, playersToString())))

    case MakeMove(player, board, currentColour, moveFrom, moveTo) => findRoomByPlayer(player) match {
      case Some(room) =>
        //example input with move: "/move woooooooooooooooooooooooooooooow w 31 27"
        val state: GameState = GameState.fromString(board, currentColour)
        val move: PawnMove = PawnMove.fromString(moveFrom, moveTo)

        ValidateMove.apply().apply(move, state) match {
          case Right(newState)        =>
            (this, sendToRoom(room, newState.asJson.toString)) //todo: to return http response

          case Left(validationError)  =>
            (this, sendToRoom(room, validationError.show)) //todo: to return http response
        }

      case None =>
        (this, Seq(SendToUser(player, "You are not currently in a room")))
    }

    case Error(player) =>
      (this, Seq(SendToUser(player, "incorrect ws input")))
  }

  private def sendToRoom(room: Room, text: String): Seq[OutputMessage] = {
    Seq(SendToUsers(room.players, text))
  }

  private def roomsToString(): String = {
    rooms.map(_.name).mkString("Rooms:\n\t", "\n\t", "")
  }

  private def playersToString(): String = {
    players
      .map(o => o.name + " - " + findRoomByPlayer(o).getOrElse(Room("-")).name)
      .mkString("Players:\n\t", "\n\t", "")
  }

  private def findRoomByPlayer(player: Player): Option[Room] = rooms.find(_.players.contains(player))

  private def findRoomByName(roomName: String): Option[Room] = rooms.find(_.name==roomName)

  private def removeFromCurrentRoom(player: Player): (MultiplayerState, Seq[OutputMessage]) = findRoomByPlayer(player) match {
    case Some(room) =>
      val nextMembers: List[Player] = room.players.filterNot(_ == player)

      val nextState: MultiplayerState =
        if (nextMembers.isEmpty)
          MultiplayerState(players, rooms.filterNot(_ == room)) //remove room from state
        else
          MultiplayerState(players, rooms.filterNot(_ == room) :+ Room(room.name, nextMembers))  //replace room in state

      // Send to "previous" room population to include the leaving user
      (nextState, sendToRoom(room, s"${player.name} has left '${room.name}' room"))

    case None =>
      (this, Nil)
  }

  private def disconnectPlayer(player: Player): (MultiplayerState, Seq[OutputMessage]) = findRoomByPlayer(player) match {
    case Some(room) =>
      val newPlayers: List[Player] = room.players.filterNot(_ == player)
      val listOfPlayers: String = players.filterNot(_ == player).map(_.name).mkString(",")

      val nextState: MultiplayerState =
        if (newPlayers.isEmpty)
          MultiplayerState(players.filterNot(_ == player), rooms.filterNot(_ == room)) //remove room from state
        else
          MultiplayerState(players.filterNot(_ == player), rooms.filterNot(_ == room) :+ Room(room.name, newPlayers))  //replace room in state

      val combinedMessage: Seq[OutputMessage] =
        sendToRoom(room, s"${player.name} has left game").concat(
          Seq(SendToUsers(newPlayers :+ player, s"/pla $listOfPlayers")))

      (nextState, combinedMessage)

    case None =>

      val newPlayers: List[Player] = players.filterNot(_ == player)
      val listOfPlayers: String = newPlayers.map(_.name).mkString(",")
      val nextState: MultiplayerState = MultiplayerState(newPlayers, this.rooms)
      (nextState, Seq(SendToUsers(newPlayers :+ player, s"/pla $listOfPlayers")))
  }

  private def addToRoom(player: Player, roomName: String): (MultiplayerState, Seq[OutputMessage]) = {
    val room: Room = findRoomByName(roomName).getOrElse(Room(roomName, List())) //take from rooms or create new room
    val nextMembers: List[Player] = room.players :+ player

    if (nextMembers.size >2) {
      (this, Seq(SendToUser(player, "This room is full, try to find another one")))

    } else {
      val newPlayers = (players :+ player).distinct
      val newRoom = Room(room.name, nextMembers)
      val newRooms = rooms.filterNot(_ == room) :+ newRoom

      val nextState: MultiplayerState = MultiplayerState(newPlayers, newRooms)

      // Send to "next" room population to include the joining user
      (nextState, nextState.sendToRoom(newRoom, s"${player.name} has joined ${room.name}"))
    }
  }

  private def addToGame(player: Player): (MultiplayerState, Seq[OutputMessage]) = {
      val newPlayers = (players :+ player).distinct
      val listOfPlayers: String = newPlayers.map(_.name).mkString(",")

      val nextState: MultiplayerState = MultiplayerState(newPlayers, this.rooms)

      // Send to "next" room population to include the joining user
      (nextState, Seq(SendToUsers(newPlayers :+ player, s"/pla $listOfPlayers")))
  }

}