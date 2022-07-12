package checkers.domain

import checkers.domain.Side.White

final case class GameState(
    status: GameStatus = GameStatus.Ongoing,
    movesNow: Side,
    board: Board,
    nextMoveBy: Option[Pawn] = None
  ) {

  //todo: to produce Option?
//  def getNewState(move: PawnMove): GameState = {
//
//    val oldPawn: Option[Pawn] = board.pawnAt(move.from)
//
//    val newPawn: Option[Pawn] = oldPawn match {
//      case None       => None
//      case Some(pawn) =>
//        val newSide     = pawn.side
//        val newPosition = move.to
//        //todo: if there is another pawn to smash pawnType can not become a queen! Here we should assume the pawnType remains unchanged.
//        val newType     = pawn.pawnType match {
//            case PawnType.Queen   => PawnType.Queen
//            case PawnType.Regular => (move.from.x, move.to.x) match {
//              case (_, 0) if pawn.side == White => PawnType.Queen
//              case (_, 7) if pawn.side == Red   => PawnType.Queen
//              case _                            => PawnType.Regular
//            }
//        }
//
//      Some(Pawn(newSide, newType, newPosition))
//    }
//
//    val smashedPawn: Option[Pawn] = getSmashedPawn(move)
//
//    val newBoard: Board = Board(
//      board
//        .pawnsArray
//        .filterNot(_ == oldPawn.orNull)
//        .filterNot(_ == smashedPawn.orNull)
//        .appended(newPawn.orNull))
//
//
//
//    val newRound: Side = checkNewRound(move)
//
//    val newStatus: GameStatus = {
//      if (!newBoard.pawnsArray.exists(_.side == White))
//        GameStatus.Win(Red)
//      else if (!newBoard.pawnsArray.exists(_.side == Red))
//        GameStatus.Win(White)
////    else if (15 moves with queen without smashing) todo: add this condition
////      GameStatus.Draw
//      else
//        GameStatus.Ongoing
//    }
//
//    val newNextMoveBy: Option[Pawn] = {
//      if (newRound == movesNow) {
//        newBoard.pawnAt(move.to)
//      } else
//      //todo: here check if newPawn should change to queen
//        None
//    }
//
//    GameState(newStatus, newRound, newBoard, newNextMoveBy)
//  }

  //todo: check for queen missing
//  def isNextToSmash(move: PawnMove): Boolean = {
//    val otherColour: Side = movesNow.opposite
//    val pawnType: Option[PawnType] = board.pawnAt(move.from).map(_.pawnType)
//    //delete pawn from moveFrom
//    //set moveFrom to MoveToValue
//    //isSthToSmash()
//
//    pawnType match {
//      case Some(Regular) =>  //todo: change into for-comprehension
//        if (getSmashedPawn(move).isEmpty)
//          false
//        else if (board.pawnExists(move.to.upRight(),   otherColour) && board.positionIsAvailable(move.to.doubleUpRight()))
//          true
//        else if (board.pawnExists(move.to.upLeft(),    otherColour) && board.positionIsAvailable(move.to.doubleUpLeft()))
//          true
//        else if (board.pawnExists(move.to.downRight(), otherColour) && board.positionIsAvailable(move.to.doubleDownRight()))
//          true
//        else if (board.pawnExists(move.to.downLeft(),  otherColour) && board.positionIsAvailable(move.to.doubleDownLeft()))
//          true
//        else
//          false
//
//      case Some(Queen) => { //todo: change into for-comprehension
//        if (getSmashedPawn(move).isEmpty)
//          false
//        else
//          false //todo: to be implemented
//      }
//      case None => false
//    }
//  }

//  def getSmashedPawn(move: PawnMove): Option[Pawn] = {
//
//    board.pawnAt(move.from).map(_.pawnType) match {
//      case Some(Regular) => {
//        if (move.to == move.from.doubleUpRight())
//          board.pawnAt(move.from.upRight())
//        else if (move.to == move.from.doubleDownRight())
//          board.pawnAt(move.from.downRight())
//        else if (move.to == move.from.doubleUpLeft())
//          board.pawnAt(move.from.upLeft())
//        else if (move.to == move.from.doubleDownLeft())
//          board.pawnAt(move.from.downLeft())
//        else
//          None
//      }
//      case Some(Queen) => { //todo duplicated code from ValidateMove.validateMoveTypeQueen, should be connected somehow
//        val otherSide = movesNow.opposite
//
//        //check if move is diagonal
//        if ((move.to.x - move.from.x).abs != (move.to.y - move.from.y).abs)
//          return None
//
//        val deltaX = move.to.x - move.from.x
//        val deltaY = move.to.y - move.from.y
//
//        val pawnsOnTheWay: List[Pawn] = for {
//          dx <- if (deltaX > 0) List.range(1, deltaX) else List.range(deltaX, 0)
//          dy <- if (deltaY > 0) List.range(1, deltaY) else List.range(deltaY, 0)
//          if dx.abs == dy.abs
//          pawnPosition = PawnPosition(move.from.x + dx, move.from.y + dy)
//          pawn = board.pawnAt(pawnPosition)
//          if pawn.isDefined
//        } yield pawn.get
//
//        if (pawnsOnTheWay.size == 1 && pawnsOnTheWay.head.side == otherSide)
//          Some(pawnsOnTheWay.head)
//        else
//          None
//      }
//      case None => None
//    }
//  }

//  def checkNewRound(move: PawnMove): Side = {
//    if (isNextToSmash(move))
//      movesNow
//    else
//      movesNow.opposite
//  }
//todo: check for queen missing
//  def isSthToSmash: Boolean = {
//    val colour: Side = this.movesNow
//    val board: Board = this.board
//
//    //check for regular pawns
//    val checkForRegular: Boolean =
//      board.pawnsArray
//        .filter(_.side == colour)
//        .filter(_.pawnType == PawnType.Regular)
//        .exists(o => board.pawnsArray.exists(p =>
//          p.side != colour && (
//            (p.position == o.position.upRight()   && this.board.positionIsAvailable(p.position.upRight()))   ||
//            (p.position == o.position.upLeft()    && this.board.positionIsAvailable(p.position.upLeft()))    ||
//            (p.position == o.position.downRight() && this.board.positionIsAvailable(p.position.downRight())) ||
//            (p.position == o.position.downLeft()  && this.board.positionIsAvailable(p.position.downLeft())))
//    ))
//
//    //todo: check for queen
//    val queensToCheck =
//      board.pawnsArray
//      .filter(_.side == colour)
//      .filter(_.pawnType == Queen)
//
//    def getQueenMoveOptions {
//      for {
//        queen <- queensToCheck
//        pawnPositionFrom = queen.position
//
//        delta <- List.range(2, 7).concat(List.range(-7, 2))
//        deltaX = delta
//        deltaY = delta
//        if deltaX.abs == deltaY.abs
//        pawnPositionTo = PawnPosition(queen.position.x + deltaX, queen.position.y + deltaY)
//        if pawnPositionTo.isOnTheBoard
//        if board.positionIsAvailable(pawnPositionTo)
//
//        move = PawnMove(pawnPositionFrom, pawnPositionTo)
//        if validateMoveTypeQueen(this, move) == Right(WithSmash)
//
//      } yield PawnMove(pawnPositionFrom, pawnPositionTo)
//    }
//
//
////    def getPawnsOnTheWayOfQueen(queen: Pawn, deltaX: Int, deltaY: Int): List[Pawn] = {
////      for {
////        dx <- if (deltaX > 0) List.range(1, deltaX) else List.range(deltaX, 0)
////        dy <- if (deltaY > 0) List.range(1, deltaY) else List.range(deltaY, 0)
////        if dx.abs == dy.abs
////        pawnPosition = PawnPosition(queen.position.x + dx, queen.position.y + dy)
////        pawn = board.pawnAt(pawnPosition)
////        if pawn.isDefined
////      } yield pawn.get
////    }
////
////    val checkForQueen =
////      queensToCheck
////        .map(getQueenMoveOptions(_)
////          .map(o => getPawnsOnTheWayOfQueen(o._1, o._2, o._3))
////      ).exists(_.size == 1)
//
//    checkForRegular || checkForQueen
//  }
}

object GameState {
  def initial: GameState =
    GameState(
      status = GameStatus.Ongoing,
      movesNow = White,
      board = Board.initial,
      nextMoveBy = None
    )

  def fromString(board: String, movesNow: String, nextMoveBy: String = "None", status: String = "ongoing"): Option[GameState] = {

    val newMovesNow: Option[Side]         = Side.fromString(movesNow)
    val newBoard: Option[Board]           = Board.fromString(board)
    val newGameStatus: Option[GameStatus] = GameStatus.fromString(status)
    val newNextMoveBy: Option[Pawn]       = for {
      board: Board                <- Board.fromString(board)
      index: Int                  <- nextMoveBy.toIntOption
      pawnPosition: PawnPosition  <- PawnPosition.fromIndex(index)
      pawn: Pawn                  <- board.pawnAt(pawnPosition)
    } yield pawn

    (newGameStatus, newMovesNow, newBoard, newNextMoveBy) match {
      case (Some(gameStatus), Some(movesNow), Some(board), nextMoveBy) => Some(GameState(gameStatus, movesNow, board, nextMoveBy)) //todo: nextMoveBy is an Option
      case _                                                           => None
    }

  }

}
