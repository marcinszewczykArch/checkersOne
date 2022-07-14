package multiplayer.domain

import cats.implicits._
import checkers.CheckersCodecs.gameStateEncoder
import checkers.domain.{GameState, PawnMove, ValidateMove}
import io.circe.syntax.EncoderOps
import multiplayer.MultiplayerCodecs.multiplayerStateEncoder

object MultiplayerState {
  // Default constructor
  def apply(): MultiplayerState = MultiplayerState(List.empty, List.empty)
}

case class MultiplayerState(players: List[Player], rooms: List[Room]) {

  def process(msg: InputMessage): (MultiplayerState, Seq[OutputMessage]) =
    msg match {

      case EnterGame(player: Player)                                => enterGame(player)
      case LeaveGame(player: Player)                                => leaveGame(player)
      case EnterRoom(player: Player, toRoom)                        => enterRoom(player, toRoom)
      case LeaveRoom(player)                                        => leaveRoom(player)
      case Chat(player: Player, text)                               => sendChatMsg(player, text)
      case MakeMove(player, board, currentColour, moveFrom, moveTo) =>
        makeMove(player, board, currentColour, moveFrom, moveTo)
      case Error(player)                                            => sendErrorMsg(player)
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
          sendToRoom(room, WebsocketRoutes.Chat, s"${player.name} has left game").concat(sendStateToAll(newState))
        (newState, message)

      case None       =>
        val newState = MultiplayerState(players.filterNot(_ == player), this.rooms)
        (newState, sendStateToAll(newState))
    }
  }

  private def excludePlayerFromRoom(player: Player, room: Room): List[Room] = {
    val newRoomPlayers: List[Player] = room.players.filterNot(_ == player)

    if (newRoomPlayers.isEmpty)
      rooms.filterNot(_ == room)
    else
      rooms.filterNot(_ == room) :+ Room(room.name, newRoomPlayers)
  }

  private def enterRoom(player: Player, roomName: String): (MultiplayerState, Seq[OutputMessage]) =
    findRoomByPlayer(player) match {
      case Some(_) =>
        (this, Seq(SendToUser(player, WebsocketRoutes.Error, "You are already in the room. Leave current room first")))

      case None    =>
        val room: Room                = findRoomByName(roomName).getOrElse(Room(roomName, List())) //take from rooms or create new room
        val nextMembers: List[Player] = room.players :+ player

        if (nextMembers.size > 2)
          (this, Seq(SendToUser(player, WebsocketRoutes.Error, "This room is full, try to find another one")))
        else {
          val newRoom  = Room(room.name, nextMembers)
          val newRooms = rooms.filterNot(_ == room) :+ newRoom
          val newState = MultiplayerState(players, newRooms)

          val message: Seq[OutputMessage] = sendToRoom(newRoom, WebsocketRoutes.Chat, s"${player.name} has joined room")
            .concat(sendStateToAll(newState))
          (newState, message)
        }
    }

  private def sendStateToAll(state: MultiplayerState): Seq[OutputMessage] =
    Seq(SendToUsers(state.players, WebsocketRoutes.State, state.asJson.toString()))

  private def findRoomByName(roomName: String): Option[Room] = rooms.find(_.name == roomName)

  private def leaveRoom(player: Player): (MultiplayerState, Seq[OutputMessage]) =
    findRoomByPlayer(player) match {
      case Some(room) =>
        val newRooms: List[Room]       = excludePlayerFromRoom(player, room)
        val newState: MultiplayerState =
          MultiplayerState(players, newRooms) //replace room in state //replace room in state

        val message: Seq[OutputMessage] =
          sendToRoom(room, WebsocketRoutes.Chat, s"${player.name} has left room").concat(sendStateToAll(newState))
        (newState, message)

      case None       =>
        (this, Nil)
    }

  private def sendChatMsg(player: Player, text: String) =
    findRoomByPlayer(player) match {
      case Some(room) => (this, sendToRoom(room, WebsocketRoutes.Chat, s"${player.name}: $text"))
      case None       => (this, Seq(SendToUser(player, WebsocketRoutes.None, text)))
    }

  //helpers
  private def sendToRoom(
    room: Room,
    prefix: WebsocketRoutes,
    text: String
  ): Seq[OutputMessage] =
    Seq(SendToUsers(room.players, prefix, text))

  private def findRoomByPlayer(player: Player): Option[Room] = rooms.find(_.players.contains(player))

  private def sendErrorMsg(player: Player) = (this, Seq(SendToUser(player, WebsocketRoutes.None, "incorrect ws input")))

  private def makeMove(
    player: Player,
    board: String,
    currentColour: String,
    moveFrom: String,
    moveTo: String
  ): (MultiplayerState, Seq[OutputMessage]) =
    findRoomByPlayer(player) match {
      case Some(room) =>
        val state: GameState =
          GameState.fromString(board, currentColour).get //todo: add moveFrom to multiplayer & deal with .get
        val move: PawnMove = PawnMove.fromString(moveFrom, moveTo).get //todo: deal with .get

        ValidateMove.apply().apply(move, state) match {
          case Right(newGameState)   => (this, sendToRoom(room, WebsocketRoutes.Move, newGameState.asJson.toString))
          case Left(validationError) => (this, Seq(SendToUser(player, WebsocketRoutes.Error, validationError.show)))
        }

      case None       => (this, Seq(SendToUser(player, WebsocketRoutes.None, "you are not in room")))
    }

}
