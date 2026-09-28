import { BoardState } from './BoardState';
import { DifficultyLevel } from './DifficultyAnalyzer';
import { LevelDefinition } from './CampaignLevels';
import { PuzzleGenerator } from './PuzzleGenerator';

export class DailyVectorGenerator {
  static getTodayDateString(): string {
    const now = new Date();
    const y = now.getFullYear();
    const m = String(now.getMonth() + 1).padStart(2, '0');
    const d = String(now.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

  static generateForDate(dateStr: string): LevelDefinition {
    // Generate seed from date string: e.g. "daily_vector_2026-09-26"
    const seed = `daily_vector_${dateStr}`;
    
    // Rotate difficulty based on day of week or day of month
    const parts = dateStr.split('-');
    const day = parseInt(parts[2] || '1', 10);
    const dayMod = day % 4;
    
    let difficulty: DifficultyLevel = 'MEDIUM';
    let arrowCount = 14;
    let rows = 6;
    let cols = 6;

    if (dayMod === 0) {
      difficulty = 'EASY';
      arrowCount = 10;
      rows = 5;
      cols = 5;
    } else if (dayMod === 1) {
      difficulty = 'MEDIUM';
      arrowCount = 15;
    } else if (dayMod === 2) {
      difficulty = 'HARD';
      arrowCount = 20;
    } else {
      difficulty = 'EXPERT';
      arrowCount = 25;
    }

    const { board } = PuzzleGenerator.generate({
      rows,
      cols,
      arrowCount,
      difficulty,
      seed
    });

    return {
      levelNumber: 0,
      title: `Daily Vector: ${dateStr}`,
      rows: board.rows,
      cols: board.cols,
      difficulty,
      arrows: [...board.arrows],
      parMoves: board.arrows.length
    };
  }
}
