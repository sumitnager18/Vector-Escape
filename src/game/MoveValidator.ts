import { Arrow } from './Arrow';
import { BoardState } from './BoardState';
import { Direction } from './Direction';

export interface PathCell {
  row: number;
  col: number;
}

export class MoveValidator {
  /**
   * Returns true if all cells between arrow and board edge along its direction are empty.
   */
  static isMoveLegal(board: BoardState, arrow: Arrow): boolean {
    const { dr, dc } = Direction.getVector(arrow.direction);
    let currR = arrow.row + dr;
    let currC = arrow.col + dc;

    while (board.isValidPosition(currR, currC)) {
      if (!board.isCellEmpty(currR, currC)) {
        return false;
      }
      currR += dr;
      currC += dc;
    }

    return true;
  }

  /**
   * Returns all arrows currently legal to escape on the board.
   */
  static getLegalMoves(board: BoardState): Arrow[] {
    return board.arrows.filter(arrow => MoveValidator.isMoveLegal(board, arrow));
  }

  /**
   * Returns the first blocking arrow in the direction of the given arrow, or null if legal.
   */
  static getFirstBlockingArrow(board: BoardState, arrow: Arrow): Arrow | null {
    const { dr, dc } = Direction.getVector(arrow.direction);
    let currR = arrow.row + dr;
    let currC = arrow.col + dc;

    while (board.isValidPosition(currR, currC)) {
      const blocking = board.getArrowAt(currR, currC);
      if (blocking !== null) {
        return blocking;
      }
      currR += dr;
      currC += dc;
    }

    return null;
  }

  /**
   * Returns all cells from immediately after the arrow up to the edge.
   */
  static getFullLaneCells(board: BoardState, arrow: Arrow): PathCell[] {
    const cells: PathCell[] = [];
    const { dr, dc } = Direction.getVector(arrow.direction);
    let currR = arrow.row + dr;
    let currC = arrow.col + dc;

    while (board.isValidPosition(currR, currC)) {
      cells.push({ row: currR, col: currC });
      currR += dr;
      currC += dc;
    }

    return cells;
  }

  /**
   * Returns the trajectory cells up to the first blocking arrow (inclusive of blocker's cell),
   * or to the edge if unblocked.
   */
  static getPreviewCells(board: BoardState, arrow: Arrow): { cells: PathCell[]; blocked: boolean; blocker: Arrow | null } {
    const cells: PathCell[] = [];
    const { dr, dc } = Direction.getVector(arrow.direction);
    let currR = arrow.row + dr;
    let currC = arrow.col + dc;

    while (board.isValidPosition(currR, currC)) {
      const occupant = board.getArrowAt(currR, currC);
      cells.push({ row: currR, col: currC });
      if (occupant !== null) {
        return { cells, blocked: true, blocker: occupant };
      }
      currR += dr;
      currC += dc;
    }

    return { cells, blocked: false, blocker: null };
  }
}
