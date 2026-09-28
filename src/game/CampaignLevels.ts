import { Arrow, createArrow } from './Arrow';
import { BoardState } from './BoardState';
import { DifficultyLevel } from './DifficultyAnalyzer';
import { PuzzleGenerator } from './PuzzleGenerator';
import { PuzzleSolver } from './PuzzleSolver';

export interface LevelDefinition {
  levelNumber: number;
  title: string;
  rows: number;
  cols: number;
  difficulty: DifficultyLevel;
  arrows: Arrow[];
  parMoves: number;
  tutorialTip?: string;
}

export class CampaignLevels {
  private static cachedLevels: LevelDefinition[] | null = null;

  static getAll(): LevelDefinition[] {
    if (this.cachedLevels) {
      return this.cachedLevels;
    }

    const levels: LevelDefinition[] = [];

    // Level 1: Single obvious arrow (First Opening)
    levels.push({
      levelNumber: 1,
      title: "First Opening",
      rows: 4,
      cols: 4,
      difficulty: "EASY",
      tutorialTip: "Tap an arrow whose escape lane to the board edge is completely clear.",
      arrows: [
        createArrow("l1_1", 1, 2, "UP")
      ],
      parMoves: 1
    });

    // Level 2: Two arrows (Dual Escape)
    levels.push({
      levelNumber: 2,
      title: "Two Vectors",
      rows: 4,
      cols: 4,
      difficulty: "EASY",
      tutorialTip: "Two distinct vectors. Each escapes along its pointing axis.",
      arrows: [
        createArrow("l2_1", 2, 1, "LEFT"),
        createArrow("l2_2", 1, 2, "RIGHT")
      ],
      parMoves: 2
    });

    // Level 3: One Blocker (The Blocker)
    levels.push({
      levelNumber: 3,
      title: "The Blocker",
      rows: 4,
      cols: 4,
      difficulty: "EASY",
      tutorialTip: "Vector A is blocked by Vector B. Clear Vector B first!",
      arrows: [
        createArrow("l3_1", 2, 1, "UP"),
        createArrow("l3_2", 1, 1, "RIGHT")
      ],
      parMoves: 2
    });

    // Level 4: Simple Dependency (Chain sequence)
    levels.push({
      levelNumber: 4,
      title: "Simple Dependency",
      rows: 4,
      cols: 4,
      difficulty: "EASY",
      tutorialTip: "A 3-step sequence: clear from the outer edge inward.",
      arrows: [
        createArrow("l4_1", 3, 2, "UP"),
        createArrow("l4_2", 2, 2, "UP"),
        createArrow("l4_3", 1, 2, "LEFT")
      ],
      parMoves: 3
    });

    // Level 5: Two Possible Choices (The Crossing)
    levels.push({
      levelNumber: 5,
      title: "The Crossing",
      rows: 5,
      cols: 5,
      difficulty: "EASY",
      tutorialTip: "Multiple valid opening vectors. Choose your sequence.",
      arrows: [
        createArrow("l5_1", 2, 2, "UP"),
        createArrow("l5_2", 1, 2, "LEFT"),
        createArrow("l5_3", 3, 2, "DOWN"),
        createArrow("l5_4", 2, 3, "RIGHT")
      ],
      parMoves: 4
    });

    // Level 6: Mixed Directions
    levels.push({
      levelNumber: 6,
      title: "Mixed Directions",
      rows: 5,
      cols: 5,
      difficulty: "MEDIUM",
      tutorialTip: "Four orthogonal directions interacting across the grid.",
      arrows: [
        createArrow("l6_1", 1, 1, "UP"),
        createArrow("l6_2", 3, 3, "DOWN"),
        createArrow("l6_3", 1, 3, "LEFT"),
        createArrow("l6_4", 3, 1, "RIGHT"),
        createArrow("l6_5", 2, 2, "UP"),
        createArrow("l6_6", 1, 2, "UP")
      ],
      parMoves: 6
    });

    // Level 7: Small Chain
    levels.push({
      levelNumber: 7,
      title: "Small Chain",
      rows: 5,
      cols: 5,
      difficulty: "MEDIUM",
      tutorialTip: "Unravel the cascading dependency lane by lane.",
      arrows: [
        createArrow("l7_1", 4, 1, "UP"),
        createArrow("l7_2", 3, 1, "UP"),
        createArrow("l7_3", 2, 1, "RIGHT"),
        createArrow("l7_4", 2, 3, "DOWN"),
        createArrow("l7_5", 3, 3, "RIGHT"),
        createArrow("l7_6", 1, 4, "UP")
      ],
      parMoves: 6
    });

    // Level 8: Cross-Dependency
    levels.push({
      levelNumber: 8,
      title: "Cross-Dependency",
      rows: 5,
      cols: 5,
      difficulty: "MEDIUM",
      tutorialTip: "Perpendicular paths cross at the center junction.",
      arrows: [
        createArrow("l8_1", 2, 1, "RIGHT"),
        createArrow("l8_2", 2, 2, "DOWN"),
        createArrow("l8_3", 3, 2, "RIGHT"),
        createArrow("l8_4", 1, 2, "DOWN"),
        createArrow("l8_5", 2, 3, "UP"),
        createArrow("l8_6", 4, 2, "LEFT")
      ],
      parMoves: 6
    });

    // Level 9: Multiple Legal Choices With Branching Consequences
    levels.push({
      levelNumber: 9,
      title: "Branching Order",
      rows: 5,
      cols: 5,
      difficulty: "HARD",
      tutorialTip: "Analyze the consequences of each branch before launching.",
      arrows: [
        createArrow("l9_1", 1, 1, "LEFT"),
        createArrow("l9_2", 3, 3, "RIGHT"),
        createArrow("l9_3", 2, 1, "UP"),
        createArrow("l9_4", 1, 2, "LEFT"),
        createArrow("l9_5", 2, 3, "DOWN"),
        createArrow("l9_6", 3, 2, "RIGHT"),
        createArrow("l9_7", 2, 2, "UP"),
        createArrow("l9_8", 3, 1, "UP")
      ],
      parMoves: 8
    });

    // Level 10: First Genuinely Satisfying Multi-Step Puzzle
    levels.push({
      levelNumber: 10,
      title: "The Interlock",
      rows: 6,
      cols: 6,
      difficulty: "HARD",
      tutorialTip: "The master interlock. Precision clearance required.",
      arrows: [
        createArrow("l10_1", 0, 2, "UP"),
        createArrow("l10_2", 1, 2, "UP"),
        createArrow("l10_3", 2, 2, "RIGHT"),
        createArrow("l10_4", 2, 4, "DOWN"),
        createArrow("l10_5", 4, 4, "LEFT"),
        createArrow("l10_6", 4, 3, "DOWN"),
        createArrow("l10_7", 5, 3, "DOWN"),
        createArrow("l10_8", 3, 1, "LEFT"),
        createArrow("l10_9", 3, 3, "LEFT")
      ],
      parMoves: 9
    });

    // Generate Levels 11 through 50 with deterministic procedural generation
    for (let levelNum = 11; levelNum <= 50; levelNum++) {
      let rows = 6;
      let cols = 6;
      let arrowCount = 6;
      let difficulty: DifficultyLevel = "EASY";

      if (levelNum <= 10) {
        rows = 5;
        cols = 5;
        arrowCount = 6 + (levelNum - 6); // 6 to 10
        difficulty = "EASY";
      } else if (levelNum <= 20) {
        rows = 6;
        cols = 6;
        arrowCount = 10 + Math.floor((levelNum - 11) * 0.6); // 10 to 15
        difficulty = "MEDIUM";
      } else if (levelNum <= 30) {
        rows = 6;
        cols = 6;
        arrowCount = 15 + Math.floor((levelNum - 21) * 0.7); // 15 to 21
        difficulty = "MEDIUM";
      } else if (levelNum <= 40) {
        rows = 6;
        cols = 6;
        arrowCount = 20 + Math.floor((levelNum - 31) * 0.6); // 20 to 25
        difficulty = "HARD";
      } else {
        rows = 6;
        cols = 6;
        arrowCount = 24 + Math.floor((levelNum - 41) * 0.6); // 24 to 29
        difficulty = "EXPERT";
      }

      // Generate deterministically using level number as seed
      const seed = `vector_escape_campaign_lvl_${levelNum}_v2`;
      const generated = PuzzleGenerator.generate({
        rows,
        cols,
        arrowCount,
        difficulty,
        seed
      });

      const solverRes = PuzzleSolver.solve(generated.board);
      const verifiedArrows: Arrow[] = [...generated.board.arrows];

      levels.push({
        levelNumber: levelNum,
        title: CampaignLevels.getLevelTitle(levelNum, difficulty),
        rows,
        cols,
        difficulty,
        arrows: verifiedArrows,
        parMoves: verifiedArrows.length
      });
    }

    this.cachedLevels = levels;
    return levels;
  }

  static getLevel(levelNumber: number): LevelDefinition {
    const all = this.getAll();
    const index = Math.max(1, Math.min(levelNumber, all.length)) - 1;
    return all[index];
  }

  private static getLevelTitle(level: number, diff: DifficultyLevel): string {
    const titlesByDiff: Record<DifficultyLevel, string[]> = {
      EASY: ["Nexus", "Gateway", "Horizon", "Orbit", "Drift", "Pulse", "Beam", "Vector"],
      MEDIUM: ["Labyrinth", "Resonance", "Chamber", "Prism", "Gridlock", "Catalyst", "Axiom", "Sector"],
      HARD: ["Vortex", "Singularity", "Monolith", "Entropy", "Hypercube", "Cascade", "Matrix", "Synapse"],
      EXPERT: ["Oblivion", "Zenith", "Quantum Gate", "Event Horizon", "Omega Loop", "Continuum", "Eclipse", "Apex Vector"]
    };
    const list = titlesByDiff[diff];
    const name = list[level % list.length];
    return `${name} ${level}`;
  }
}
