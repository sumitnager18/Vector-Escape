import { DirectionType } from './Direction';

export interface Arrow {
  id: string;
  row: number;
  col: number;
  direction: DirectionType;
}

export function createArrow(id: string, row: number, col: number, direction: DirectionType): Arrow {
  return { id, row, col, direction };
}
