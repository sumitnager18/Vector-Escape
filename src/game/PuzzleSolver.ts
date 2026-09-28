import { Arrow } from './Arrow';
import { BoardState } from './BoardState';
import { MoveValidator } from './MoveValidator';

export interface SolverResult {
  isSolvable: boolean;
  solutionOrder: string[]; // arrow ids in order of removal
  depth: number;
  totalStatesVisited: number;
  minInitialLegalMoves: number;
}

export class PuzzleSolver {
  /**
   * Solves the given board, returning whether it can be completely cleared and the solution sequence.
   */
  static solve(board: BoardState, maxStates: number = 10000): SolverResult {
    const visited = new Set<string>();
    let totalStates = 0;
    let initialLegalCount = MoveValidator.getLegalMoves(board).length;

    // Fast-path for already cleared
    if (board.isCleared) {
      return {
        isSolvable: true,
        solutionOrder: [],
        depth: 0,
        totalStatesVisited: 1,
        minInitialLegalMoves: 0
      };
    }

    // If initially no legal moves, unsolvable immediately
    if (initialLegalCount === 0) {
      return {
        isSolvable: false,
        solutionOrder: [],
        depth: 0,
        totalStatesVisited: 1,
        minInitialLegalMoves: 0
      };
    }

    function search(currBoard: BoardState, path: string[]): string[] | null {
      totalStates++;
      if (totalStates > maxStates) return null;

      if (currBoard.isCleared) {
        return path;
      }

      const stateKey = currBoard.toKey();
      if (visited.has(stateKey)) {
        return null;
      }
      visited.add(stateKey);

      const legalMoves = MoveValidator.getLegalMoves(currBoard);
      if (legalMoves.length === 0) {
        return null;
      }

      // Try each legal move
      for (const move of legalMoves) {
        const nextBoard = currBoard.removeArrow(move.id);
        const result = search(nextBoard, [...path, move.id]);
        if (result !== null) {
          return result;
        }
      }

      return null;
    }

    const solution = search(board, []);

    return {
      isSolvable: solution !== null,
      solutionOrder: solution ?? [],
      depth: solution ? solution.length : 0,
      totalStatesVisited: totalStates,
      minInitialLegalMoves: initialLegalCount
    };
  }

  /**
   * Check if a board is solvable without collecting the full path.
   */
  static isSolvable(board: BoardState): boolean {
    return PuzzleSolver.solve(board).isSolvable;
  }
}
