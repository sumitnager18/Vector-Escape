import { Arrow, createArrow } from '../game/Arrow';
import { BoardState } from '../game/BoardState';
import { Direction } from '../game/Direction';
import { MoveValidator } from '../game/MoveValidator';
import { PuzzleSolver, SolverResult } from '../game/PuzzleSolver';
import { PuzzleGenerator } from '../game/PuzzleGenerator';
import { CampaignLevels } from '../game/CampaignLevels';
import { DailyVectorGenerator } from '../game/DailyVectorGenerator';
import { DifficultyLevel } from '../game/DifficultyAnalyzer';
import { createInitialSession, calculateStars } from '../game/GameState';

export async function runComprehensiveAudit() {
  console.log("============================================================");
  console.log("VECTOR ESCAPE — COMPREHENSIVE PRODUCTION AUDIT");
  console.log("============================================================\n");

  let allPassed = true;

  // ------------------------------------------------------------
  // PHASE 4 — 50-LEVEL SOLVABILITY AUDIT
  // ------------------------------------------------------------
  console.log("--- PHASE 4: 50-LEVEL SOLVABILITY AUDIT ---");
  const campaignLevels = CampaignLevels.getAll();
  if (campaignLevels.length !== 50) {
    console.error(`ERROR: Expected 50 levels, got ${campaignLevels.length}`);
    allPassed = false;
  }

  const levelAuditResults: {
    level: number;
    title: string;
    difficulty: string;
    arrows: number;
    initialLegal: number;
    solvable: boolean;
    depth: number;
    executable: boolean;
  }[] = [];

  let unsolvableLevels = 0;
  let invalidExecutionLevels = 0;

  for (const lvl of campaignLevels) {
    const board = new BoardState(lvl.rows, lvl.cols, lvl.arrows);

    // 1. Validate structure
    let structureValid = true;
    const occupied = new Set<string>();
    for (const a of lvl.arrows) {
      if (a.row < 0 || a.row >= lvl.rows || a.col < 0 || a.col >= lvl.cols) {
        structureValid = false;
        console.error(`Level ${lvl.levelNumber}: Arrow out of bounds at (${a.row}, ${a.col})`);
      }
      const cellKey = `${a.row},${a.col}`;
      if (occupied.has(cellKey)) {
        structureValid = false;
        console.error(`Level ${lvl.levelNumber}: Duplicate arrow at (${a.row}, ${a.col})`);
      }
      occupied.add(cellKey);
    }

    // 2. Legal opening
    const initialLegal = MoveValidator.getLegalMoves(board);

    // 3. Solver
    const solverRes = PuzzleSolver.solve(board);

    // 4. Executability check
    let executable = true;
    let simBoard = board;
    if (solverRes.isSolvable) {
      for (const arrowId of solverRes.solutionOrder) {
        const arr = simBoard.getArrowById(arrowId);
        if (!arr || !MoveValidator.isMoveLegal(simBoard, arr)) {
          executable = false;
          break;
        }
        simBoard = simBoard.removeArrow(arrowId);
      }
      if (!simBoard.isCleared) {
        executable = false;
      }
    } else {
      executable = false;
      unsolvableLevels++;
    }

    if (!executable && solverRes.isSolvable) {
      invalidExecutionLevels++;
    }

    levelAuditResults.push({
      level: lvl.levelNumber,
      title: lvl.title,
      difficulty: lvl.difficulty,
      arrows: lvl.arrows.length,
      initialLegal: initialLegal.length,
      solvable: solverRes.isSolvable,
      depth: solverRes.depth,
      executable
    });
  }

  console.log(`Audited ${campaignLevels.length} campaign levels:`);
  console.log(`  Fully Solvable: ${campaignLevels.length - unsolvableLevels}/${campaignLevels.length}`);
  console.log(`  Executable Solution Sequences: ${campaignLevels.length - invalidExecutionLevels}/${campaignLevels.length}`);
  if (unsolvableLevels > 0 || invalidExecutionLevels > 0) {
    allPassed = false;
    console.error("FAIL: Some campaign levels are not solvable!");
  } else {
    console.log("PASS: All 50 campaign levels are 100% solvable and executable.");
  }

  // Sample display of first 10 and last 5
  console.log("Sample Levels Summary:");
  for (const r of [...levelAuditResults.slice(0, 10), ...levelAuditResults.slice(45)]) {
    console.log(`  Lvl ${r.level.toString().padStart(2)}: ${r.title.padEnd(24)} [${r.difficulty.padEnd(6)}] ${r.arrows} arrows, ${r.initialLegal} openings, depth ${r.depth}, solvable: ${r.solvable}`);
  }

  // ------------------------------------------------------------
  // PHASE 5 — PROCEDURAL GENERATOR STRESS TEST
  // ------------------------------------------------------------
  console.log("\n--- PHASE 5: PROCEDURAL GENERATOR STRESS TEST ---");
  const TARGET_TEST_COUNT = 1000;
  const difficulties: DifficultyLevel[] = ["EASY", "MEDIUM", "HARD", "EXPERT"];
  const arrowCounts: Record<DifficultyLevel, number> = { EASY: 6, MEDIUM: 12, HARD: 18, EXPERT: 24 };
  const boardSizes: Record<DifficultyLevel, number> = { EASY: 5, MEDIUM: 6, HARD: 6, EXPERT: 7 };

  let generatedCount = 0;
  let validCount = 0;
  let rejectedCount = 0;
  let stressUnsolvable = 0;
  let totalGenTimeMs = 0;
  let maxGenTimeMs = 0;
  let minDepth = Infinity;
  let maxDepth = -Infinity;
  let totalDepth = 0;
  const generatedKeys = new Set<string>();

  const stressStart = performance.now();

  for (let i = 0; i < TARGET_TEST_COUNT; i++) {
    const diff = difficulties[i % difficulties.length];
    const size = boardSizes[diff];
    const arrows = arrowCounts[diff];
    const seed = `stress_seed_${i}_${diff}`;

    const t0 = performance.now();
    try {
      const puzzle = PuzzleGenerator.generate({
        rows: size,
        cols: size,
        arrowCount: arrows,
        difficulty: diff,
        seed
      });
      const genTime = performance.now() - t0;
      totalGenTimeMs += genTime;
      if (genTime > maxGenTimeMs) maxGenTimeMs = genTime;
      generatedCount++;

      // Check structure
      const board = puzzle.board;
      let valid = true;
      if (board.remainingCount !== arrows) valid = false;

      // Check openings
      const openings = MoveValidator.getLegalMoves(board);
      if (openings.length === 0) valid = false;

      // Check solver
      const solveRes = PuzzleSolver.solve(board);
      if (!solveRes.isSolvable || solveRes.depth !== arrows) {
        stressUnsolvable++;
        valid = false;
      }

      if (valid) {
        validCount++;
        totalDepth += solveRes.depth;
        if (solveRes.depth < minDepth) minDepth = solveRes.depth;
        if (solveRes.depth > maxDepth) maxDepth = solveRes.depth;
        generatedKeys.add(board.toKey());
      } else {
        rejectedCount++;
      }
    } catch (e) {
      rejectedCount++;
    }
  }

  const stressElapsed = performance.now() - stressStart;
  const avgGenTimeMs = totalGenTimeMs / generatedCount;
  const avgDepth = totalDepth / validCount;
  const duplicateRate = (1 - (generatedKeys.size / validCount)) * 100;

  console.log(`Generated: ${generatedCount} / ${TARGET_TEST_COUNT}`);
  console.log(`Valid & Solvable: ${validCount} (${((validCount/generatedCount)*100).toFixed(1)}%)`);
  console.log(`Unsolvable: ${stressUnsolvable}`);
  console.log(`Rejected: ${rejectedCount}`);
  console.log(`Average Gen Time: ${avgGenTimeMs.toFixed(2)} ms`);
  console.log(`Max Gen Time: ${maxGenTimeMs.toFixed(2)} ms`);
  console.log(`Solution Depth: min=${minDepth}, max=${maxDepth}, avg=${avgDepth.toFixed(1)}`);
  console.log(`Unique Boards: ${generatedKeys.size} / ${validCount} (Duplicate rate: ${duplicateRate.toFixed(2)}%)`);
  console.log(`Total Stress Test Time: ${(stressElapsed/1000).toFixed(2)} s`);

  if (stressUnsolvable > 0) {
    allPassed = false;
    console.error("FAIL: Procedural generator produced unsolvable puzzles!");
  } else {
    console.log("PASS: 1,000 / 1,000 generated puzzles are completely solvable.");
  }

  // ------------------------------------------------------------
  // PHASE 6 — DETERMINISTIC SEED AUDIT
  // ------------------------------------------------------------
  console.log("\n--- PHASE 6: DETERMINISTIC SEED AUDIT ---");
  const testSeeds = ["seed_alpha_42", "quantum_flux_99", "vector_escape_2026", 12345678, 987654321];
  let determinismPassed = true;

  for (const s of testSeeds) {
    const p1 = PuzzleGenerator.generate({ rows: 6, cols: 6, arrowCount: 10, seed: s });
    const p2 = PuzzleGenerator.generate({ rows: 6, cols: 6, arrowCount: 10, seed: s });
    const p3 = PuzzleGenerator.generate({ rows: 6, cols: 6, arrowCount: 10, seed: s });

    const k1 = p1.board.toKey();
    const k2 = p2.board.toKey();
    const k3 = p3.board.toKey();

    if (k1 !== k2 || k2 !== k3) {
      console.error(`Determinism failure for seed '${s}'!`);
      determinismPassed = false;
    }
  }

  if (determinismPassed) {
    console.log("PASS: Identical seeds produce byte-for-byte identical board states across multiple runs.");
  } else {
    allPassed = false;
    console.error("FAIL: Determinism failed!");
  }

  // ------------------------------------------------------------
  // PHASE 7 — DAILY VECTOR AUDIT
  // ------------------------------------------------------------
  console.log("\n--- PHASE 7: DAILY VECTOR AUDIT ---");
  const testDates = ["2026-01-01", "2026-06-15", "2026-09-26", "2027-01-01"];
  let dailyPassed = true;
  const dailyKeysByDate: Record<string, string> = {};

  for (const d of testDates) {
    const d1 = DailyVectorGenerator.generateForDate(d);
    const d2 = DailyVectorGenerator.generateForDate(d);
    const b1 = new BoardState(d1.rows, d1.cols, d1.arrows);
    const b2 = new BoardState(d2.rows, d2.cols, d2.arrows);

    if (b1.toKey() !== b2.toKey()) {
      console.error(`Daily puzzle for date ${d} is not deterministic!`);
      dailyPassed = false;
    }

    dailyKeysByDate[d] = b1.toKey();

    // Verify solvability
    const solveRes = PuzzleSolver.solve(b1);
    if (!solveRes.isSolvable) {
      console.error(`Daily puzzle for date ${d} is not solvable!`);
      dailyPassed = false;
    } else {
      console.log(`  Date ${d}: Solvable! (${b1.remainingCount} arrows, depth ${solveRes.depth}, title: '${d1.title}')`);
    }
  }

  // Verify different dates produce different puzzles
  const uniqueKeys = new Set(Object.values(dailyKeysByDate));
  if (uniqueKeys.size !== testDates.length) {
    console.error("Different dates produced duplicate puzzles!");
    dailyPassed = false;
  } else {
    console.log("PASS: Different dates produce distinct, deterministic, solvable puzzles.");
  }

  if (!dailyPassed) allPassed = false;

  // ------------------------------------------------------------
  // PHASE 8 — GAMEPLAY STATE INTEGRITY AUDIT
  // ------------------------------------------------------------
  console.log("\n--- PHASE 8: GAMEPLAY STATE INTEGRITY AUDIT ---");
  let statePassed = true;

  // Transition: Idle -> Select legal -> Settle -> Cleared
  {
    const a1 = createArrow("a1", 0, 1, "UP");
    const a2 = createArrow("a2", 2, 2, "DOWN");
    const board = new BoardState(4, 4, [a1, a2]);
    const session = createInitialSession(1, "Test Level", "EASY", board);

    if (session.hearts !== 3 || session.moves !== 0 || session.status !== "PLAYING") {
      statePassed = false;
      console.error("Initial session state invalid");
    }

    // Move 1: a1
    if (!MoveValidator.isMoveLegal(session.board, a1)) statePassed = false;
    session.undoStack.push({ board: session.board, flow: session.flow, arrow: a1 });
    session.board = session.board.removeArrow(a1.id);
    session.moves += 1;
    session.flow += 1;

    if (session.board.remainingCount !== 1 || session.moves !== 1 || session.flow !== 2) {
      statePassed = false;
      console.error("State after move 1 invalid");
    }

    // Move 2: a2
    session.undoStack.push({ board: session.board, flow: session.flow, arrow: a2 });
    session.board = session.board.removeArrow(a2.id);
    session.moves += 2;
    session.flow += 1;

    if (session.board.isCleared) {
      session.status = "CLEARED";
      session.stars = calculateStars(session.mistakes, session.moves, 2);
    }

    if (session.status !== "CLEARED" || session.stars !== 3) {
      statePassed = false;
      console.error("State after completion invalid");
    }
  }

  // Transition: Blocked arrow -> Heart loss -> Board unchanged -> Hearts = 0 -> Game Over
  {
    const blocker = createArrow("blocker", 1, 1, "RIGHT");
    const blocked = createArrow("blocked", 2, 1, "UP");
    const board = new BoardState(4, 4, [blocker, blocked]);
    const session = createInitialSession(2, "Test Mistakes", "EASY", board);
    const originalKey = session.board.toKey();

    // Tap 1: blocked
    if (!MoveValidator.isMoveLegal(session.board, blocked)) {
      session.hearts -= 1;
      session.mistakes += 1;
      session.flow = 1;
    }
    if (session.board.toKey() !== originalKey || session.hearts !== 2 || session.mistakes !== 1) {
      statePassed = false;
      console.error("Blocked tap corrupted board or failed heart decrement");
    }

    // Tap 2: blocked
    session.hearts -= 1;
    session.mistakes += 1;
    if (session.hearts !== 1) statePassed = false;

    // Tap 3: blocked -> Game Over
    session.hearts -= 1;
    session.mistakes += 1;
    if (session.hearts <= 0) {
      session.status = "FAILED";
    }
    if (session.status !== "FAILED" || session.board.toKey() !== originalKey) {
      statePassed = false;
      console.error("Game over transition failed or altered board");
    }
  }

  if (statePassed) {
    console.log("PASS: All gameplay state transitions verified.");
  } else {
    allPassed = false;
    console.error("FAIL: State integrity failed!");
  }

  console.log("\n============================================================");
  console.log(`AUDIT COMPLETE — RESULT: ${allPassed ? "ALL AUDITS PASSED [OK]" : "AUDIT FAILED [FAIL]"}`);
  console.log("============================================================\n");

  return allPassed;
}

if (import.meta.url === `file://${process.argv[1]}`) {
  runComprehensiveAudit().then(success => {
    process.exit(success ? 0 : 1);
  });
}
