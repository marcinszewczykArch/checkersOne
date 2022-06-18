package service

import domain.EColour.EColour
import domain.EMoveType.EMoveType
import domain.{EMoveType, _}
import io.circe.Json
import io.circe.syntax.EncoderOps
import io.circe.generic.auto._


object CheckersService {

  type ErrorMessage = String

  def stateDecoder(state: GameState): Json = {
    case class State(board: String, currentColour: String)

    val boardArray: Array[(Int, EColour)] = state.board.map(o => (positionToIndex(o.position), o.colour))
    val board: String = (0 to 31)
      .map(n => boardArray
            .find(_._1 == n)
            .map(_._2.toString)
            .getOrElse("o"))
      .mkString("")

    State(board, state.colour.toString).asJson
  }

  def stateEncoder(boardString: String, roundString: String): GameState = {

    val board: Array[Pawn] = boardString
      .split("")
      .zipWithIndex
      .filter(o => o._1 != "o")
      .map(o => (stringToColour(o._1), indexToPosition(o._2)))
      .map(o => Pawn(o._1, o._2))


    val round: EColour.Value = roundString match {
      case "r" => EColour.r
      case "w" => EColour.w
    }

    domain.GameState(board, round: EColour)
  }

  def moveEncoder(from: String, to: String): PawnMove = PawnMove(indexToPosition(from.toInt), indexToPosition(to.toInt))

  def isSthToSmash(state: GameState): Boolean = {
    val colour: EColour = state.colour
    val board: Array[Pawn] = state.board

    board.filter(_.colour == colour).exists(o => board.exists(p =>
      p.colour != colour && (
        (p.position == o.position.upRight()   && state.positionIsAvailable(p.position.upRight()))   ||
        (p.position == o.position.upLeft()    && state.positionIsAvailable(p.position.upLeft()))    ||
        (p.position == o.position.downRight() && state.positionIsAvailable(p.position.downRight())) ||
        (p.position == o.position.downLeft()  && state.positionIsAvailable(p.position.downLeft())))
    ))
  }

  def validateMove(state: GameState, move: PawnMove): Either[ErrorMessage, GameState] = {

    val fx = move.from.x
    val fy = move.from.y
    val tx = move.to.x
    val ty = move.to.y

    val moveType: Either[ErrorMessage, EMoveType] = {
      if
        (!state.pawnExists(move.from, state.colour))                                            Left("Wrong pawn chosen")
      else if
        (!state.positionIsAvailable(move.to))                                                   Left("Destination position is not available")

      else if ((tx, ty) == (fx - 1, fy + 1) &&
        state.colour == EColour.w)                                                              Right(EMoveType.SINGLE)
      else if ((tx, ty) == (fx - 1, fy - 1) &&
        state.colour == EColour.w)                                                              Right(EMoveType.SINGLE)
      else if ((tx, ty) == (fx + 1, fy + 1) &&
        state.colour == EColour.r)                                                              Right(EMoveType.SINGLE)
      else if ((tx, ty) == (fx + 1, fy - 1) &&
        state.colour == EColour.r)                                                              Right(EMoveType.SINGLE)

      else if ((tx, ty) == (fx + 2, fy + 2) &&
        state.pawnExists(PawnPosition(move.from.x + 1, move.from.y + 1), state.otherColour()))  Right(EMoveType.WITH_SMASH)
      else if ((tx, ty) == (fx - 2, fy - 2) &&
        state.pawnExists(PawnPosition(move.from.x - 1, move.from.y - 1), state.otherColour()))  Right(EMoveType.WITH_SMASH)
      else if ((tx, ty) == (fx + 2, fy - 2) &&
        state.pawnExists(PawnPosition(move.from.x + 1, move.from.y - 1), state.otherColour()))  Right(EMoveType.WITH_SMASH)
      else if ((tx, ty) == (fx - 2, fy + 2) &&
        state.pawnExists(PawnPosition(move.from.x - 1, move.from.y + 1), state.otherColour()))  Right(EMoveType.WITH_SMASH)

      else                                                                                      Left("Illegal move")
    }

    val sthToSmash = isSthToSmash(state)

    moveType match {
      case Right(EMoveType.SINGLE)     if !sthToSmash => Right(state.getNewState(move))
      case Right(EMoveType.SINGLE)     if sthToSmash  => Left("You have to take your opponent's piece!")
      case Right(EMoveType.WITH_SMASH) if sthToSmash  => Right(state.getNewState(move))
      case Left(error)                                => Left(error)
      case _                                          => Left("Illegal move")
    }
  }

  def indexToPosition(index: Int): PawnPosition = index match {
    case 0 => PawnPosition(0, 1)
    case 1 => PawnPosition(0, 3)
    case 2 => PawnPosition(0, 5)
    case 3 => PawnPosition(0, 7)
    case 4 => PawnPosition(1, 0)
    case 5 => PawnPosition(1, 2)
    case 6 => PawnPosition(1, 4)
    case 7 => PawnPosition(1, 6)
    case 8 => PawnPosition(2, 1)
    case 9 => PawnPosition(2, 3)
    case 10 => PawnPosition(2, 5)
    case 11 => PawnPosition(2, 7)
    case 12 => PawnPosition(3, 0)
    case 13 => PawnPosition(3, 2)
    case 14 => PawnPosition(3, 4)
    case 15 => PawnPosition(3, 6)
    case 16 => PawnPosition(4, 1)
    case 17 => PawnPosition(4, 3)
    case 18 => PawnPosition(4, 5)
    case 19 => PawnPosition(4, 7)
    case 20 => PawnPosition(5, 0)
    case 21 => PawnPosition(5, 2)
    case 22 => PawnPosition(5, 4)
    case 23 => PawnPosition(5, 6)
    case 24 => PawnPosition(6, 1)
    case 25 => PawnPosition(6, 3)
    case 26 => PawnPosition(6, 5)
    case 27 => PawnPosition(6, 7)
    case 28 => PawnPosition(7, 0)
    case 29 => PawnPosition(7, 2)
    case 30 => PawnPosition(7, 4)
    case 31 => PawnPosition(7, 6)
  }

  def positionToIndex(position: PawnPosition): Int = position match {
    case PawnPosition(0, 1) => 0
    case PawnPosition(0, 3) => 1
    case PawnPosition(0, 5) => 2
    case PawnPosition(0, 7) => 3
    case PawnPosition(1, 0) => 4
    case PawnPosition(1, 2) => 5
    case PawnPosition(1, 4) => 6
    case PawnPosition(1, 6) => 7
    case PawnPosition(2, 1) => 8
    case PawnPosition(2, 3) => 9
    case PawnPosition(2, 5) => 10
    case PawnPosition(2, 7) => 11
    case PawnPosition(3, 0) => 12
    case PawnPosition(3, 2) => 13
    case PawnPosition(3, 4) => 14
    case PawnPosition(3, 6) => 15
    case PawnPosition(4, 1) => 16
    case PawnPosition(4, 3) => 17
    case PawnPosition(4, 5) => 18
    case PawnPosition(4, 7) => 19
    case PawnPosition(5, 0) => 20
    case PawnPosition(5, 2) => 21
    case PawnPosition(5, 4) => 22
    case PawnPosition(5, 6) => 23
    case PawnPosition(6, 1) => 24
    case PawnPosition(6, 3) => 25
    case PawnPosition(6, 5) => 26
    case PawnPosition(6, 7) => 27
    case PawnPosition(7, 0) => 28
    case PawnPosition(7, 2) => 29
    case PawnPosition(7, 4) => 30
    case PawnPosition(7, 6) => 31
  }

  def stringToColour(colour: String): EColour = colour match {
    case "r" => EColour.r
    case "R" => EColour.R
    case "w" => EColour.w
    case "W" => EColour.W
  }

}
