import { Arrow } from './Arrow';
import { BoardState } from './BoardState';
import { DifficultyLevel } from './DifficultyAnalyzer';

export type GameStatus = 'PLAYING' | 'CLEARED' | 'FAILED';

export interface UndoStep {
  board: BoardState;
  flow: number;
  arrow: Arrow;
}

export interface GameSessionState {
  levelNumber: number;
  levelTitle: string;
  difficulty: DifficultyLevel;
  board: BoardState;
  initialBoard: BoardState;
  hearts: number;
  maxHearts: number;
  moves: number;
  mistakes: number;
  flow: number;
  maxFlow: number;
  status: GameStatus;
  stars: number;
  undoStack: UndoStep[];
  mode: 'CAMPAIGN' | 'DAILY' | 'PRACTICE';
}

export function createInitialSession(
  levelNumber: number,
  title: string,
  difficulty: DifficultyLevel,
  initialBoard: BoardState,
  mode: 'CAMPAIGN' | 'DAILY' | 'PRACTICE' = 'CAMPAIGN'
): GameSessionState {
  return {
    levelNumber,
    levelTitle: title,
    difficulty,
    board: initialBoard.clone(),
    initialBoard: initialBoard.clone(),
    hearts: 3,
    maxHearts: 3,
    moves: 0,
    mistakes: 0,
    flow: 1,
    maxFlow: 1,
    status: 'PLAYING',
    stars: 0,
    undoStack: [],
    mode
  };
}

export function calculateStars(mistakes: number, moves: number, initialArrowCount: number): number {
  if (mistakes === 0) return 3;
  if (mistakes <= 1) return 2;
  return 1;
}
