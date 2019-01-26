package chess

/**
 * Utility methods for helping to determine whether a situation is a draw or a draw
 * on a player flagging.
 *
 * See http://www.e4ec.org/immr.html
 */
object InsufficientMatingMaterial {

  def nonKingPieces(board: Board) = board.pieces filter (_._2.role != King)

  def bishopsOnOppositeColors(board: Board) =
    (board.pieces filter (_._2.role == Bishop) map (_._1.color) toList).distinct.size == 2

  /**
   * Returns true when remaining non-King pieces are only bishops that cannot
   * capture each other and cannot checkmate
   */
  def bishopsOnDifferentColor(board: Board) = {
    val notKingPieces = nonKingPieces(board) toList

    def bishopsOnSameColor = notKingPieces.map(_._1.color).distinct.size == 1
    def bishopsAreSameColor = notKingPieces.map(_._2.color).distinct.size == 1

    if (notKingPieces.exists(_._2.role != Bishop)) false
    else if (bishopsAreSameColor) notKingPieces.size < 3 || bishopsOnSameColor
    else {
      val whitePlayerBishops = notKingPieces.filter(_._2.color == Color.White)
      val blackPlayerBishops = notKingPieces.filter(_._2.color == Color.Black)

      !whitePlayerBishops.exists {
        case (pos, _) => blackPlayerBishops.exists(_._1.color == pos.color)
      }
    }
  }

  /*
   * Returns true if a pawn cannot progress forward because it is blocked by a pawn
   */
  def pawnBlockedByPawn(pawn: Actor, board: Board) = pawn.moves.isEmpty && {
    val blockingPosition = Actor.posAheadOfPawn(pawn.pos, pawn.piece.color)
    blockingPosition.flatMap(board.actorAt(_)).exists(_.piece.is(Pawn))
  }

  /*
   * Determines whether a board position is an automatic draw due to neither player
   * being able to mate the other as informed by the traditional chess rules.
   */
  def apply(board: Board) = {
    lazy val bishopsOnly = board.pieces forall { p => (p._2 is King) || (p._2 is Bishop) }
    val minorsOnly = board.pieces forall { p => (p._2 is King) || (p._2 is Bishop) || (p._2 is Knight) }

    minorsOnly && (board.pieces.size <= 3 || (bishopsOnly && !bishopsOnOppositeColors(board)))
  }

  def apply(board: Board, color: Color) =
    board rolesOf color filter (King !=) match {
      case Nil => true
      case List(Knight) => board rolesOf !color filter (King !=) filter (Queen !=) isEmpty
      case List(Bishop) => !(board.rolesOf(!color).exists(r => r == Knight || r == Pawn) || bishopsOnOppositeColors(board))
      case _ => false
    }
}
