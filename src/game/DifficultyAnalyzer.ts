import { BoardState } from './BoardState';
import { MoveValidator } from './MoveValidator';
import { PuzzleSolver } from './PuzzleSolver';

export type DifficultyLevel = 'EASY' | 'MEDIUM' | 'HARD' | 'EXPERT';

export interface DifficultyMetrics {
  arrowCount: number;
  density: number;
  initialLegalMoves: number;
  dependencyDepth: number;
  difficulty: DifficultyLevel;
  difficultyScore: number;
}

export class DifficultyAnalyzer {
  static analyze(board: BoardState): DifficultyMetrics {
    const totalCells = board.rows * board.cols;
    const arrowCount = board.remainingCount;
    const density = totalCells > 0 ? arrowCount / totalCells : 0;
    const initialLegal = MoveValidator.getLegalMoves(board).length;
    const solution = PuzzleSolver.solve(board);

    // Compute composite difficulty score
    // Higher arrows, higher density, deeper dependency, fewer initial free exits = higher difficulty
    const depth = solution.depth;
    const freedomRatio = arrowCount > 0 ? initialLegal / arrowCount : 1;

    let score = (arrowCount * 3) + (density * 40) + (depth * 2) - (freedomRatio * 15);
    if (score < 0) score = 0;

    let difficulty: DifficultyLevel = 'EASY';
    if (score >= 48 || arrowCount >= 18 || (density >= 0.55 && depth >= 15)) {
      difficulty = 'EXPERT';
    } else if (score >= 32 || arrowCount >= 12 || depth >= 10) {
      difficulty = 'HARD';
    } else if (score >= 18 || arrowCount >= 7 || depth >= 5) {
      difficulty = 'MEDIUM';
    } else {
      difficulty = 'EASY';
    }

    return {
      arrowCount,
      density,
      initialLegalMoves: initialLegal,
      dependencyDepth: depth,
      difficulty,
      difficultyScore: Math.round(score)
    };
  }
}
