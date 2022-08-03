package multiPlayer.domain

import cats.implicits._
import checkers.domain.{GameState, PawnMove, ValidateMove}
import io.circe.syntax.EncoderOps
import multiPlayer.MultiPlayerCodecs.multiplayerStateEncoder

object MultiplayerState {

 val initial: MultiplayerState = MultiplayerState(List.empty, List.empty)

}

case class MultiplayerState(players: List[Player], rooms: List[Room]) {

  def process(msg: InputMessage): (MultiplayerState, Seq[OutputMessage]) =
    msg match {

      case EnterGame(player: Player)         => enterGame(player)
      case LeaveGame(player: Player)         => leaveGame(player)
      case EnterRoom(player: Player, toRoom) => enterRoom(player, toRoom)
      case LeaveRoom(player)                 => leaveRoom(player)
      case Chat(player: Player, text)        => sendChatMsg(player, text)
      case MakeMove(player, from, to)        => makeMove(player, from, to)
      case Error(player)                     => sendErrorMsg(player)
    }

  private def enterGame(player: Player): (MultiplayerState, Seq[OutputMessage]) = {
    val newState = MultiplayerState(players :+ player, this.rooms)
    (newState, sendStateToAll(newState))
  }

  private def leaveGame(player: Player): (MultiplayerState, Seq[OutputMessage]) = {
    val newPlayers: List[Player] = players.filterNot(_ == player)

    findRoomByPlayer(player) match {
      case Some(room) =>
        val newRooms: List[Room]        = excludePlayerFromRoom(player, room)
        val newState: MultiplayerState  = MultiplayerState(newPlayers, newRooms) //replace room in state
        val message: Seq[OutputMessage] =
          sendToRoom(room, WebsocketRoutes.ChatRoute, s"${player.name} has left game").concat(sendStateToAll(newState))
        (newState, message)

      case None       =>
        val newState = MultiplayerState(players.filterNot(_ == player), this.rooms)
        (newState, sendStateToAll(newState))
    }
  }

  private def enterRoom(player: Player, roomName: String): (MultiplayerState, Seq[OutputMessage]) =
    findRoomByPlayer(player) match {
      case Some(_) =>
        (
          this,
          Seq(SendToUser(player, WebsocketRoutes.ErrorRoute, "You are already in the room. Leave current room first"))
        )

      case None    =>
        val room: Room =
          findRoomByName(roomName).getOrElse(
            Room(roomName, List(), GameState.initial)
          ) //take from rooms or create new room
        val nextMembers: List[Player] = room.players :+ player

        if (nextMembers.size > 2)
          (this, Seq(SendToUser(player, WebsocketRoutes.ErrorRoute, "This room is full, try to find another one")))
        else {
          val newRoom  = Room(room.name, nextMembers, room.gameState)
          val newRooms = rooms.filterNot(_ == room) :+ newRoom
          val newState = MultiplayerState(players, newRooms)

          val message: Seq[OutputMessage] =
            sendToRoom(newRoom, WebsocketRoutes.ChatRoute, s"${player.name} has joined room")
              .concat(sendStateToAll(newState))
          (newState, message)
        }
    }

  private def findRoomByName(roomName: String): Option[Room] = rooms.find(_.name == roomName)

  private def leaveRoom(player: Player): (MultiplayerState, Seq[OutputMessage]) =
    findRoomByPlayer(player) match {
      case Some(room) =>
        val newRooms: List[Room]       = excludePlayerFromRoom(player, room)
        val newState: MultiplayerState =
          MultiplayerState(players, newRooms) //replace room in state

        val message: Seq[OutputMessage] =
          sendToRoom(room, WebsocketRoutes.ChatRoute, s"${player.name} has left room").concat(sendStateToAll(newState))
        (newState, message)

      case None       =>
        (this, Nil)
    }

  private def excludePlayerFromRoom(player: Player, room: Room): List[Room] = {
    val newRoomPlayers: List[Player] = room.players.filterNot(_ == player)

    if (newRoomPlayers.isEmpty)
      rooms.filterNot(_ == room)
    else
      rooms.filterNot(_ == room) :+ Room(room.name, newRoomPlayers, room.gameState)
  }

  private def sendStateToAll(state: MultiplayerState): Seq[OutputMessage] =
    Seq(SendToUsers(state.players, WebsocketRoutes.StateRoute, state.asJson.toString()))

  //helpers
  private def sendToRoom(
    room: Room,
    prefix: WebsocketRoutes,
    text: String
  ): Seq[OutputMessage] =
    Seq(SendToUsers(room.players, prefix, text))

  private def findRoomByPlayer(player: Player): Option[Room] = rooms.find(_.players.contains(player))

  private def sendChatMsg(player: Player, text: String) =
    findRoomByPlayer(player) match {
      case Some(room) => (this, sendToRoom(room, WebsocketRoutes.ChatRoute, s"${player.name}: $text"))
      case None       => (this, Seq(SendToUser(player, WebsocketRoutes.None, text)))
    }

  private def sendErrorMsg(player: Player) = (this, Seq(SendToUser(player, WebsocketRoutes.None, "incorrect ws input")))

  //todo: refactor
  private def makeMove(
    player: Player,
    moveFrom: String,
    moveTo: String
  ): (MultiplayerState, Seq[OutputMessage]) =
    findRoomByPlayer(player) match {

      case Some(room) =>
        if (room.players.size == 2)
          PawnMove.fromString(moveFrom, moveTo) match {

            case None       => (this, Seq(SendToUser(player, WebsocketRoutes.ErrorRoute, "move input incorrect")))
            case Some(move) =>
              ValidateMove.apply().apply(move, room.gameState) match {

                case Right(newGameState)   =>
                  val newRooms: List[Room]       = rooms.filterNot(_ == room) :+ Room(room.name, room.players, newGameState)
                  val newState: MultiplayerState = MultiplayerState(players, newRooms)
                  (newState, sendStateToAll(newState))
                case Left(validationError) =>
                  (this, Seq(SendToUser(player, WebsocketRoutes.ErrorRoute, validationError.show)))
              }
          }
        else
          (this, Seq(SendToUser(player, WebsocketRoutes.ErrorRoute, "wait for opponent")))
      case None       => (this, Seq(SendToUser(player, WebsocketRoutes.None, "you are not in room")))
    }

}
