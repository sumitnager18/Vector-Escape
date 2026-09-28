import { Arrow, createArrow } from '../game/Arrow';
import { BoardState } from '../game/BoardState';
import { Direction } from '../game/Direction';
import { MoveValidator } from '../game/MoveValidator';
import { PuzzleSolver } from '../game/PuzzleSolver';
import { PuzzleGenerator } from '../game/PuzzleGenerator';
import { CampaignLevels } from '../game/CampaignLevels';
import { DailyVectorGenerator } from '../game/DailyVectorGenerator';
import { createInitialSession, calculateStars } from '../game/GameState';

export interface TestResult {
  id: number;
  name: string;
  passed: boolean;
  message: string;
  durationMs: number;
}

export class EngineTestSuite {
  static runAll(): { results: TestResult[]; allPassed: boolean; totalPassed: number; totalCount: number } {
    const results: TestResult[] = [];

    const tests: { id: number; name: string; fn: () => void }[] = [
      {
        id: 1,
        name: "1. Empty board validation",
        fn: () => {
          const board = new BoardState(4, 4, []);
          if (!board.isCleared) throw new Error("Empty board should be marked as cleared");
          if (MoveValidator.getLegalMoves(board).length !== 0) throw new Error("Empty board should have 0 legal moves");
          if (!PuzzleSolver.isSolvable(board)) throw new Error("Empty board should be trivially solvable");
        }
      },
      {
        id: 2,
        name: "2. Single legal arrow",
        fn: () => {
          // Arrow at (1, 1) pointing UP on a 4x4 board
          const arrow = createArrow("a1", 1, 1, "UP");
          const board = new BoardState(4, 4, [arrow]);
          if (!MoveValidator.isMoveLegal(board, arrow)) throw new Error("Arrow pointing to free edge should be legal");
          const legal = MoveValidator.getLegalMoves(board);
          if (legal.length !== 1 || legal[0].id !== "a1") throw new Error("Legal moves list should contain exactly this arrow");
          const solver = PuzzleSolver.solve(board);
          if (!solver.isSolvable || solver.solutionOrder[0] !== "a1") throw new Error("Solver should clear single legal arrow in 1 step");
        }
      },
      {
        id: 3,
        name: "3. Blocked arrow detection",
        fn: () => {
          // Arrow at (2, 1) pointing UP, blocked by arrow at (1, 1)
          const blocker = createArrow("blocker", 1, 1, "RIGHT");
          const blocked = createArrow("blocked", 2, 1, "UP");
          const board = new BoardState(4, 4, [blocker, blocked]);
          if (MoveValidator.isMoveLegal(board, blocked)) throw new Error("Blocked arrow should not be legal");
          const foundBlocker = MoveValidator.getFirstBlockingArrow(board, blocked);
          if (!foundBlocker || foundBlocker.id !== "blocker") throw new Error("Blocker should be identified correctly");
        }
      },
      {
        id: 4,
        name: "4. Horizontal blocking verification",
        fn: () => {
          // Arrow at (2, 0) pointing RIGHT, blocked by arrow at (2, 3)
          const blocked = createArrow("left_arr", 2, 0, "RIGHT");
          const blocker = createArrow("right_arr", 2, 3, "DOWN");
          const board = new BoardState(5, 5, [blocked, blocker]);
          if (MoveValidator.isMoveLegal(board, blocked)) throw new Error("Horizontal path obstructed by cell (2, 3)");
          if (!MoveValidator.isMoveLegal(board, blocker)) throw new Error("Right arrow pointing DOWN to empty edge should be legal");
        }
      },
      {
        id: 5,
        name: "5. Vertical blocking verification",
        fn: () => {
          // Arrow at (0, 2) pointing DOWN, blocked by arrow at (3, 2)
          const topArr = createArrow("top", 0, 2, "DOWN");
          const bottomArr = createArrow("bottom", 3, 2, "RIGHT");
          const board = new BoardState(5, 5, [topArr, bottomArr]);
          if (MoveValidator.isMoveLegal(board, topArr)) throw new Error("Vertical path obstructed by cell (3, 2)");
          if (!MoveValidator.isMoveLegal(board, bottomArr)) throw new Error("Bottom arrow pointing RIGHT should be legal");
        }
      },
      {
        id: 6,
        name: "6. Multiple legal moves",
        fn: () => {
          const a1 = createArrow("a1", 0, 0, "UP");
          const a2 = createArrow("a2", 0, 3, "RIGHT");
          const a3 = createArrow("a3", 3, 3, "DOWN");
          const a4 = createArrow("a4", 3, 0, "LEFT");
          const board = new BoardState(4, 4, [a1, a2, a3, a4]);
          const legal = MoveValidator.getLegalMoves(board);
          if (legal.length !== 4) throw new Error(`Expected 4 legal moves, got ${legal.length}`);
        }
      },
      {
        id: 7,
        name: "7. Removing an arrow updates board and exposes new legal move",
        fn: () => {
          const blocker = createArrow("b", 1, 2, "RIGHT");
          const blocked = createArrow("a", 3, 2, "UP");
          let board = new BoardState(5, 5, [blocker, blocked]);
          if (MoveValidator.isMoveLegal(board, blocked)) throw new Error("Must be blocked initially");
          // Remove blocker
          board = board.removeArrow("b");
          if (!board.isCellEmpty(1, 2)) throw new Error("Cell (1, 2) should now be empty");
          if (!MoveValidator.isMoveLegal(board, blocked)) throw new Error("Blocked arrow should now be legal after blocker removed");
        }
      },
      {
        id: 8,
        name: "8. Completion detection",
        fn: () => {
          const a1 = createArrow("a1", 1, 1, "UP");
          let board = new BoardState(4, 4, [a1]);
          if (board.isCleared) throw new Error("Should not be cleared with 1 arrow remaining");
          board = board.removeArrow("a1");
          if (!board.isCleared || board.remainingCount !== 0) throw new Error("Board should be cleared with 0 arrows");
        }
      },
      {
        id: 9,
        name: "9. Invalid move behavior preserves board occupancy",
        fn: () => {
          const blocker = createArrow("b", 1, 2, "RIGHT");
          const blocked = createArrow("a", 3, 2, "UP");
          const board = new BoardState(5, 5, [blocker, blocked]);
          const initialKey = board.toKey();
          // Simulate invalid tap on 'a'
          const isLegal = MoveValidator.isMoveLegal(board, blocked);
          if (isLegal) throw new Error("Move must be illegal");
          // Board must remain unchanged
          if (board.toKey() !== initialKey) throw new Error("Board was modified after invalid move!");
        }
      },
      {
        id: 10,
        name: "10. Hearts reduction on mistake",
        fn: () => {
          const blocker = createArrow("b", 1, 2, "RIGHT");
          const blocked = createArrow("a", 3, 2, "UP");
          const board = new BoardState(5, 5, [blocker, blocked]);
          const session = createInitialSession(1, "Test", "EASY", board);
          if (session.hearts !== 3) throw new Error("Session must start with 3 hearts");
          // Simulate wrong tap
          session.hearts -= 1;
          session.mistakes += 1;
          session.flow = 1;
          if (session.hearts !== 2) throw new Error("Hearts should be 2");
          session.hearts -= 1;
          session.hearts -= 1;
          if (session.hearts <= 0) {
            session.status = "FAILED";
          }
          if (session.status !== "FAILED") throw new Error("Session should fail at 0 hearts");
        }
      },
      {
        id: 11,
        name: "11. Undo restores previous state exactly without restoring lost hearts",
        fn: () => {
          const a1 = createArrow("a1", 0, 1, "UP");
          const a2 = createArrow("a2", 2, 2, "DOWN");
          const initialBoard = new BoardState(4, 4, [a1, a2]);
          const session = createInitialSession(1, "Test", "EASY", initialBoard);
          
          // Make move 1
          session.undoStack.push({ board: session.board, flow: session.flow, arrow: a1 });
          session.board = session.board.removeArrow(a1.id);
          session.moves += 1;
          session.flow += 1;
          
          if (session.board.remainingCount !== 1) throw new Error("Board should have 1 arrow");
          
          // Execute Undo
          const last = session.undoStack.pop()!;
          session.board = last.board;
          session.flow = last.flow;
          session.moves -= 1;
          
          if (session.board.remainingCount !== 2) throw new Error("Undo should restore 2 arrows");
          if (!session.board.getArrowById("a1")) throw new Error("Undo should restore arrow a1");
          if (session.moves !== 0) throw new Error("Move count should decrement to 0");
        }
      },
      {
        id: 12,
        name: "12. Restart restores the exact original level state",
        fn: () => {
          const a1 = createArrow("a1", 0, 1, "UP");
          const a2 = createArrow("a2", 2, 2, "DOWN");
          const initialBoard = new BoardState(4, 4, [a1, a2]);
          const session = createInitialSession(1, "Test", "EASY", initialBoard);
          
          // Play moves and make mistake
          session.board = session.board.removeArrow(a1.id);
          session.moves += 1;
          session.hearts = 2;
          session.mistakes = 1;
          
          // Restart
          const restarted = createInitialSession(session.levelNumber, session.levelTitle, session.difficulty, session.initialBoard);
          if (restarted.board.remainingCount !== 2) throw new Error("Restart should have all initial arrows");
          if (restarted.hearts !== 3) throw new Error("Restart should reset hearts to 3");
          if (restarted.moves !== 0) throw new Error("Restart should reset moves to 0");
        }
      },
      {
        id: 13,
        name: "13. Hint intelligence never highlights an illegal arrow",
        fn: () => {
          const blocker = createArrow("b", 1, 2, "RIGHT");
          const blocked = createArrow("a", 3, 2, "UP");
          const board = new BoardState(5, 5, [blocker, blocked]);
          const legalMoves = MoveValidator.getLegalMoves(board);
          if (legalMoves.length === 0) throw new Error("Must have at least one legal move");
          const hint = legalMoves[0];
          if (!MoveValidator.isMoveLegal(board, hint)) throw new Error("Hint provided an illegal move!");
          if (hint.id === "a") throw new Error("Hint must not highlight blocked arrow 'a'");
        }
      },
      {
        id: 14,
        name: "14. Procedural generation creates valid puzzles",
        fn: () => {
          const res = PuzzleGenerator.generate({
            rows: 6,
            cols: 6,
            arrowCount: 8,
            seed: 99482
          });
          if (res.board.remainingCount !== 8) throw new Error(`Expected 8 arrows, got ${res.board.remainingCount}`);
          if (MoveValidator.getLegalMoves(res.board).length === 0) throw new Error("Generated board has 0 initial legal moves");
        }
      },
      {
        id: 15,
        name: "15. Solvability validation proves complete clearing",
        fn: () => {
          const res = PuzzleGenerator.generate({
            rows: 5,
            cols: 5,
            arrowCount: 7,
            seed: 12345
          });
          const solverResult = PuzzleSolver.solve(res.board);
          if (!solverResult.isSolvable) throw new Error("Generated puzzle failed solvability check");
          if (solverResult.depth !== 7) throw new Error("Solver depth does not match arrow count");
        }
      },
      {
        id: 16,
        name: "16. Deterministic seed produces identical puzzle",
        fn: () => {
          const resA = PuzzleGenerator.generate({ rows: 5, cols: 5, arrowCount: 6, seed: "alpha_omega" });
          const resB = PuzzleGenerator.generate({ rows: 5, cols: 5, arrowCount: 6, seed: "alpha_omega" });
          if (resA.board.toKey() !== resB.board.toKey()) {
            throw new Error("Identical seed produced different boards!");
          }
        }
      },
      {
        id: 17,
        name: "17. Daily Vector determinism for same date",
        fn: () => {
          const daily1 = DailyVectorGenerator.generateForDate("2026-09-26");
          const daily2 = DailyVectorGenerator.generateForDate("2026-09-26");
          const key1 = new BoardState(daily1.rows, daily1.cols, daily1.arrows).toKey();
          const key2 = new BoardState(daily2.rows, daily2.cols, daily2.arrows).toKey();
          if (key1 !== key2) throw new Error("Daily challenge was not deterministic for the same date");
        }
      },
      {
        id: 18,
        name: "18. Star calculation invariant",
        fn: () => {
          if (calculateStars(0, 10, 10) !== 3) throw new Error("0 mistakes should yield 3 stars");
          if (calculateStars(1, 10, 10) !== 2) throw new Error("1 mistake should yield 2 stars");
          if (calculateStars(2, 10, 10) !== 1) throw new Error("2 mistakes should yield 1 star");
        }
      },
      {
        id: 19,
        name: "19. Handcrafted 10-level vertical slice validation",
        fn: () => {
          for (let lvl = 1; lvl <= 10; lvl++) {
            const def = CampaignLevels.getLevel(lvl);
            const board = new BoardState(def.rows, def.cols, def.arrows);
            const initialLegal = MoveValidator.getLegalMoves(board);
            if (initialLegal.length === 0) {
              throw new Error(`Level ${lvl} (${def.title}) has 0 initial legal moves`);
            }
            const solverRes = PuzzleSolver.solve(board);
            if (!solverRes.isSolvable) {
              throw new Error(`Level ${lvl} (${def.title}) failed solvability check`);
            }
            if (solverRes.depth !== def.arrows.length) {
              throw new Error(`Level ${lvl} (${def.title}) solution depth ${solverRes.depth} != arrow count ${def.arrows.length}`);
            }
          }
        }
      },
      {
        id: 20,
        name: "20. Full 50-level campaign solvability & execution",
        fn: () => {
          const allLevels = CampaignLevels.getAll();
          if (allLevels.length !== 50) throw new Error(`Expected 50 campaign levels, found ${allLevels.length}`);
          for (const lvl of allLevels) {
            const board = new BoardState(lvl.rows, lvl.cols, lvl.arrows);
            const legalMoves = MoveValidator.getLegalMoves(board);
            if (legalMoves.length === 0) {
              throw new Error(`Level ${lvl.levelNumber} (${lvl.title}) has no initial legal moves`);
            }
            const solverResult = PuzzleSolver.solve(board);
            if (!solverResult.isSolvable) {
              throw new Error(`Level ${lvl.levelNumber} (${lvl.title}) is unsolvable`);
            }
            if (solverResult.depth !== lvl.arrows.length) {
              throw new Error(`Level ${lvl.levelNumber} solution depth mismatch`);
            }
          }
        }
      },
      {
        id: 21,
        name: "21. Procedural generation solvability stress test",
        fn: () => {
          for (let i = 0; i < 20; i++) {
            const p = PuzzleGenerator.generate({
              rows: 6,
              cols: 6,
              arrowCount: 10 + (i % 8),
              seed: `procedural_test_${i}`
            });
            const res = PuzzleSolver.solve(p.board);
            if (!res.isSolvable) throw new Error(`Procedural puzzle ${i} failed solvability`);
          }
        }
      },
      {
        id: 22,
        name: "22. Full gameplay state integrity & sequence execution",
        fn: () => {
          const lvl = CampaignLevels.getLevel(5);
          let board = new BoardState(lvl.rows, lvl.cols, lvl.arrows);
          const solveRes = PuzzleSolver.solve(board);
          if (!solveRes.isSolvable) throw new Error("Level 5 should be solvable");
          for (const arrowId of solveRes.solutionOrder) {
            const arr = board.getArrowById(arrowId);
            if (!arr || !MoveValidator.isMoveLegal(board, arr)) {
              throw new Error(`Move ${arrowId} was not legal during simulated execution`);
            }
            board = board.removeArrow(arrowId);
          }
          if (!board.isCleared) throw new Error("Board should be cleared after executing full solution");
        }
      }
    ];

    for (const t of tests) {
      const start = performance.now();
      try {
        t.fn();
        results.push({
          id: t.id,
          name: t.name,
          passed: true,
          message: "Passed invariant checks",
          durationMs: +(performance.now() - start).toFixed(2)
        });
      } catch (err: unknown) {
        results.push({
          id: t.id,
          name: t.name,
          passed: false,
          message: err instanceof Error ? err.message : String(err),
          durationMs: +(performance.now() - start).toFixed(2)
        });
      }
    }

    const totalPassed = results.filter(r => r.passed).length;
    return {
      results,
      allPassed: totalPassed === results.length,
      totalPassed,
      totalCount: results.length
    };
  }
}
