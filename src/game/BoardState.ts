import { Arrow } from './Arrow';

export class BoardState {
  readonly rows: number;
  readonly cols: number;
  readonly arrows: ReadonlyArray<Arrow>;
  private readonly grid: (Arrow | null)[][];

  constructor(rows: number, cols: number, arrows: Arrow[]) {
    this.rows = rows;
    this.cols = cols;
    this.arrows = [...arrows];

    // Build fast lookup grid
    this.grid = Array.from({ length: rows }, () => Array(cols).fill(null));
    for (const arrow of arrows) {
      if (this.isValidPosition(arrow.row, arrow.col)) {
        this.grid[arrow.row][arrow.col] = arrow;
      }
    }
  }

  isValidPosition(row: number, col: number): boolean {
    return row >= 0 && row < this.rows && col >= 0 && col < this.cols;
  }

  isCellEmpty(row: number, col: number): boolean {
    if (!this.isValidPosition(row, col)) return true;
    return this.grid[row][col] === null;
  }

  getArrowAt(row: number, col: number): Arrow | null {
    if (!this.isValidPosition(row, col)) return null;
    return this.grid[row][col];
  }

  getArrowById(id: string): Arrow | null {
    return this.arrows.find(a => a.id === id) || null;
  }

  get remainingCount(): number {
    return this.arrows.length;
  }

  get isCleared(): boolean {
    return this.arrows.length === 0;
  }

  removeArrow(arrowId: string): BoardState {
    const nextArrows = this.arrows.filter(a => a.id !== arrowId);
    return new BoardState(this.rows, this.cols, nextArrows);
  }

  addArrow(arrow: Arrow): BoardState {
    // If an arrow already exists at this location, replace it, otherwise append
    const filtered = this.arrows.filter(a => !(a.row === arrow.row && a.col === arrow.col));
    return new BoardState(this.rows, this.cols, [...filtered, arrow]);
  }

  clone(): BoardState {
    return new BoardState(this.rows, this.cols, this.arrows.map(a => ({ ...a })));
  }

  toKey(): string {
    return this.arrows
      .map(a => `${a.row},${a.col},${a.direction}`)
      .sort()
      .join('|');
  }
}
