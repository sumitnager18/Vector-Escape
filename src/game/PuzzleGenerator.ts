import { Arrow, createArrow } from './Arrow';
import { BoardState } from './BoardState';
import { Direction, DirectionType } from './Direction';
import { MoveValidator } from './MoveValidator';
import { PuzzleSolver } from './PuzzleSolver';
import { DifficultyLevel } from './DifficultyAnalyzer';

/**
 * Deterministic pseudo-random number generator (Mulberry32).
 */
export class SeededRng {
  private state: number;

  constructor(seed: number | string) {
    if (typeof seed === 'string') {
      let hash = 0;
      for (let i = 0; i < seed.length; i++) {
        hash = (hash << 5) - hash + seed.charCodeAt(i);
        hash |= 0;
      }
      this.state = hash >>> 0;
    } else {
      this.state = seed >>> 0;
    }
    if (this.state === 0) this.state = 123456789;
  }

  next(): number {
    let t = (this.state += 0x6d2b79f5);
    t = Math.imul(t ^ (t >>> 15), t | 1);
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  }

  nextInt(min: number, max: number): number {
    return Math.floor(min + this.next() * (max - min + 1));
  }

  choice<T>(list: T[]): T {
    return list[Math.floor(this.next() * list.length)];
  }

  shuffle<T>(array: T[]): T[] {
    const arr = [...array];
    for (let i = arr.length - 1; i > 0; i--) {
      const j = Math.floor(this.next() * (i + 1));
      [arr[i], arr[j]] = [arr[j], arr[i]];
    }
    return arr;
  }
}

export interface GeneratorOptions {
  rows: number;
  cols: number;
  arrowCount: number;
  difficulty?: DifficultyLevel;
  seed: number | string;
  maxRetries?: number;
}

export class PuzzleGenerator {
  /**
   * Generates a guaranteed solvable puzzle deterministically.
   */
  static generate(options: GeneratorOptions): { board: BoardState; seed: string; attempts: number } {
    const { rows, cols, arrowCount, seed, maxRetries = 30 } = options;
    const rng = new SeededRng(seed);
    const targetCount = Math.min(arrowCount, rows * cols - 1);

    for (let attempt = 1; attempt <= maxRetries; attempt++) {
      // Create empty board
      let currentBoard = new BoardState(rows, cols, []);
      const placedArrows: Arrow[] = [];
      let arrowIdCounter = 1;

      // Backward placement strategy:
      // In reverse generation, an arrow that has a clear path to edge in the CURRENT board
      // will be guaranteed legal at this stage when solving forward!
      for (let step = 0; step < targetCount; step++) {
        // Collect candidate placements: empty cell + direction where path to boundary is CLEAR on current board
        const candidatePlacements: { row: number; col: number; dir: DirectionType }[] = [];

        for (let r = 0; r < rows; r++) {
          for (let c = 0; c < cols; c++) {
            if (currentBoard.isCellEmpty(r, c)) {
              for (const dir of Direction.getAll()) {
                const tempArrow = createArrow(`temp`, r, c, dir);
                // Check if tempArrow can escape right now on currentBoard
                if (MoveValidator.isMoveLegal(currentBoard, tempArrow)) {
                  candidatePlacements.push({ row: r, col: c, dir });
                }
              }
            }
          }
        }

        if (candidatePlacements.length === 0) {
          // Cannot find a valid candidate in this attempt, break and retry
          break;
        }

        // Pick one candidate deterministically
        const chosen = rng.choice(candidatePlacements);
        const newArrow = createArrow(`a_${arrowIdCounter++}`, chosen.row, chosen.col, chosen.dir);
        placedArrows.push(newArrow);
        currentBoard = currentBoard.addArrow(newArrow);
      }

      // Check if we reached target count and verify with solver
      if (placedArrows.length === targetCount) {
        const candidateBoard = new BoardState(rows, cols, placedArrows);
        const solverResult = PuzzleSolver.solve(candidateBoard);

        if (
          solverResult.isSolvable &&
          solverResult.depth === targetCount &&
          solverResult.minInitialLegalMoves >= 1
        ) {
          return {
            board: candidateBoard,
            seed: String(seed),
            attempts: attempt
          };
        }
      }
    }

    // Fallback: guaranteed solvable hand-crafted procedural fallback
    const fallbackBoard = PuzzleGenerator.generateFallback(rows, cols, targetCount, rng);
    return {
      board: fallbackBoard,
      seed: String(seed),
      attempts: maxRetries + 1
    };
  }

  /**
   * Fail-safe generator that builds a guaranteed solvable chain of arrows.
   */
  private static generateFallback(rows: number, cols: number, count: number, rng: SeededRng): BoardState {
    const arrows: Arrow[] = [];
    let id = 1;

    // Place border-exiting arrows first
    for (let i = 0; i < count; i++) {
      const r = i % rows;
      const c = Math.floor(i / rows) % cols;
      let dir: DirectionType = 'UP';
      if (r === 0) dir = 'UP';
      else if (r === rows - 1) dir = 'DOWN';
      else if (c === 0) dir = 'LEFT';
      else if (c === cols - 1) dir = 'RIGHT';
      else {
        dir = rng.choice(['UP', 'DOWN', 'LEFT', 'RIGHT'] as DirectionType[]);
      }
      arrows.push(createArrow(`fb_${id++}`, r, c, dir));
    }

    const testBoard = new BoardState(rows, cols, arrows);
    if (PuzzleSolver.isSolvable(testBoard)) {
      return testBoard;
    }

    // Absolutely minimal guaranteed solvable board: arrows pointing strictly outwards to closest edge
    const safeArrows: Arrow[] = [];
    let safeId = 1;
    for (let r = 0; r < rows && safeArrows.length < count; r++) {
      for (let c = 0; c < cols && safeArrows.length < count; c++) {
        let dir: DirectionType = 'UP';
        const distUp = r;
        const distDown = rows - 1 - r;
        const distLeft = c;
        const distRight = cols - 1 - c;
        const minDist = Math.min(distUp, distDown, distLeft, distRight);

        if (minDist === distUp) dir = 'UP';
        else if (minDist === distDown) dir = 'DOWN';
        else if (minDist === distLeft) dir = 'LEFT';
        else dir = 'RIGHT';

        safeArrows.push(createArrow(`safe_${safeId++}`, r, c, dir));
      }
    }

    return new BoardState(rows, cols, safeArrows);
  }
}
