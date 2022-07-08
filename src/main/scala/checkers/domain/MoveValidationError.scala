package checkers.domain

import enumeratum._
import cats.Show

sealed trait MoveValidationError extends EnumEntry

object MoveValidationError extends Enum[MoveValidationError] {
  val values: IndexedSeq[MoveValidationError] = findValues

  case object WrongPawnColor extends MoveValidationError

  case object DestinationNotAvailable extends MoveValidationError

  case object NoPawnAtStartingPosition extends MoveValidationError

  case object IdenticalStartAndDestinationPosition extends MoveValidationError

  case object OpponentPawnToTake extends MoveValidationError

  case object ContinueMultipleSmashing extends MoveValidationError

  case object IllegalMove extends MoveValidationError

  case object MoveIsNotDiagonal extends MoveValidationError


  implicit val showMoveValidationError: Show[MoveValidationError] = {
    case WrongPawnColor                         => "Wrong piece color"
    case DestinationNotAvailable                => "Destination is not available"
    case NoPawnAtStartingPosition               => "No piece at starting position"
    case IdenticalStartAndDestinationPosition   => "Move's start and destination can not be identical"
    case OpponentPawnToTake                     => "You have to take your opponent's pawn"
    case ContinueMultipleSmashing               => "You have to continue your multiple smashing with the same pawn"
    case MoveIsNotDiagonal                      => "Move is not diagonal"
    case IllegalMove                            => "Illegal move"
  }
}