import React, { useState, useEffect, useMemo, useCallback } from 'react';
import { Arrow } from './game/Arrow';
import { BoardState } from './game/BoardState';
import { Direction } from './game/Direction';
import { MoveValidator } from './game/MoveValidator';
import { PuzzleSolver } from './game/PuzzleSolver';
import { PuzzleGenerator } from './game/PuzzleGenerator';
import { CampaignLevels, LevelDefinition } from './game/CampaignLevels';
import { DailyVectorGenerator } from './game/DailyVectorGenerator';
import { DifficultyLevel } from './game/DifficultyAnalyzer';
import { GameSessionState, createInitialSession, calculateStars } from './game/GameState';
import { SoundManager } from './audio/SoundManager';
import { HapticManager } from './haptics/HapticManager';
import { StorageManager, PlayerProgressData, GameSettingsData } from './storage/StorageManager';

import { VectorBoard } from './components/VectorBoard';
import { GameHUD } from './components/GameHUD';
import { MainMenu } from './components/MainMenu';
import { LevelSelect } from './components/LevelSelect';
import { DailyChallenge } from './components/DailyChallenge';
import { PracticeMode } from './components/PracticeMode';
import { SettingsModal } from './components/SettingsModal';
import { LevelCompletionModal } from './components/LevelCompletionModal';
import { LevelFailedModal } from './components/LevelFailedModal';
import { AndroidProjectViewer } from './components/AndroidProjectViewer';

type CurrentScreen = 'MENU' | 'GAME' | 'LEVEL_SELECT' | 'DAILY_CHALLENGE' | 'PRACTICE_SELECT';

export default function App() {
  // Persistence state
  const [progress, setProgress] = useState<PlayerProgressData>(() => StorageManager.load());

  // Navigation state
  const [currentScreen, setCurrentScreen] = useState<CurrentScreen>('MENU');
  const [showSettings, setShowSettings] = useState(false);
  const [showAndroidInspector, setShowAndroidInspector] = useState(false);

  // Active game session
  const [session, setSession] = useState<GameSessionState | null>(null);
  const [activeHintArrowId, setActiveHintArrowId] = useState<string | null>(null);

  // Modals inside game
  const [showClearedModal, setShowClearedModal] = useState(false);
  const [showFailedModal, setShowFailedModal] = useState(false);

  // Cache campaign levels
  const campaignLevels = useMemo(() => CampaignLevels.getAll(), []);

  // Update storage & audio/haptic preferences
  const handleUpdateSettings = (newSettings: Partial<GameSettingsData>) => {
    const updated = StorageManager.updateSettings(newSettings);
    setProgress(updated);
  };

  const handleResetProgress = () => {
    const reset = StorageManager.resetProgress();
    setProgress(reset);
  };

  // Launch a campaign level
  const startCampaignLevel = useCallback((levelNumber: number) => {
    const def = CampaignLevels.getLevel(levelNumber);
    const initialBoard = new BoardState(def.rows, def.cols, def.arrows);
    const newSession = createInitialSession(def.levelNumber, def.title, def.difficulty, initialBoard, 'CAMPAIGN');
    setSession(newSession);
    setActiveHintArrowId(null);
    setShowClearedModal(false);
    setShowFailedModal(false);
    setCurrentScreen('GAME');
  }, []);

  // Launch Daily Vector
  const startDailyVector = useCallback((dateStr: string) => {
    const def = DailyVectorGenerator.generateForDate(dateStr);
    const initialBoard = new BoardState(def.rows, def.cols, def.arrows);
    const newSession = createInitialSession(0, def.title, def.difficulty, initialBoard, 'DAILY');
    setSession(newSession);
    setActiveHintArrowId(null);
    setShowClearedModal(false);
    setShowFailedModal(false);
    setCurrentScreen('GAME');
  }, []);

  // Launch Practice Mode with custom or random seed
  const startPractice = useCallback((difficulty: DifficultyLevel, customSeed?: string) => {
    let rows = 6;
    let cols = 6;
    let arrowCount = 14;

    if (difficulty === 'EASY') {
      rows = 5;
      cols = 5;
      arrowCount = 8;
    } else if (difficulty === 'MEDIUM') {
      rows = 6;
      cols = 6;
      arrowCount = 14;
    } else if (difficulty === 'HARD') {
      rows = 6;
      cols = 6;
      arrowCount = 20;
    } else {
      rows = 6;
      cols = 6;
      arrowCount = 26;
    }

    const seed = customSeed && customSeed.trim().length > 0 ? customSeed.trim() : `practice_${Date.now()}`;
    const generated = PuzzleGenerator.generate({
      rows,
      cols,
      arrowCount,
      difficulty,
      seed
    });

    const initialBoard = generated.board;
    const newSession = createInitialSession(
      0,
      `Procedural ${difficulty}`,
      difficulty,
      initialBoard,
      'PRACTICE'
    );
    setSession(newSession);
    setActiveHintArrowId(null);
    setShowClearedModal(false);
    setShowFailedModal(false);
    setCurrentScreen('GAME');
  }, []);

  // Handle successful arrow escape
  const handleMoveSuccess = useCallback(
    (arrow: Arrow) => {
      if (!session || session.status !== 'PLAYING') return;

      // Clear any active hint
      setActiveHintArrowId(null);

      // Record in undo stack
      const nextUndo = [...session.undoStack, { board: session.board, flow: session.flow, arrow }];
      const nextBoard = session.board.removeArrow(arrow.id);
      const nextMoves = session.moves + 1;
      const nextFlow = session.flow + 1;
      const nextMaxFlow = Math.max(session.maxFlow, nextFlow);

      if (nextBoard.isCleared) {
        // LEVEL CLEARED!
        SoundManager.playLevelCleared();
        HapticManager.levelComplete();

        const earnedStars = calculateStars(session.mistakes, nextMoves, session.initialBoard.remainingCount);

        setSession({
          ...session,
          board: nextBoard,
          moves: nextMoves,
          flow: nextFlow,
          maxFlow: nextMaxFlow,
          status: 'CLEARED',
          stars: earnedStars,
          undoStack: nextUndo
        });

        // Persist records
        if (session.mode === 'CAMPAIGN') {
          const updated = StorageManager.recordLevelClear(session.levelNumber, earnedStars, nextMoves);
          setProgress(updated);
        } else if (session.mode === 'DAILY') {
          const todayStr = DailyVectorGenerator.getTodayDateString();
          const updated = StorageManager.recordDailyClear(todayStr, earnedStars, nextMoves);
          setProgress(updated);
        }

        setTimeout(() => {
          setShowClearedModal(true);
        }, 300);
      } else {
        setSession({
          ...session,
          board: nextBoard,
          moves: nextMoves,
          flow: nextFlow,
          maxFlow: nextMaxFlow,
          undoStack: nextUndo
        });
      }
    },
    [session]
  );

  // Handle blocked arrow tap
  const handleMoveInvalid = useCallback(
    (arrow: Arrow, _blocker: Arrow | null) => {
      if (!session || session.status !== 'PLAYING') return;

      setActiveHintArrowId(null);
      const nextMistakes = session.mistakes + 1;
      // In practice mode, hearts never deplete
      const nextHearts = session.mode === 'PRACTICE' ? session.hearts : session.hearts - 1;

      if (nextHearts <= 0) {
        // LEVEL FAILED
        setSession({
          ...session,
          mistakes: nextMistakes,
          hearts: 0,
          flow: 1,
          status: 'FAILED'
        });
        setTimeout(() => {
          setShowFailedModal(true);
        }, 350);
      } else {
        setSession({
          ...session,
          mistakes: nextMistakes,
          hearts: nextHearts,
          flow: 1 // Flow resets on error
        });
      }
    },
    [session]
  );

  // Undo move
  const handleUndo = useCallback(() => {
    if (!session || session.undoStack.length === 0 || session.status !== 'PLAYING') return;

    SoundManager.playTap();
    HapticManager.lightTap();
    setActiveHintArrowId(null);

    const stack = [...session.undoStack];
    const lastStep = stack.pop()!;

    setSession({
      ...session,
      board: lastStep.board,
      flow: lastStep.flow,
      moves: Math.max(0, session.moves - 1),
      undoStack: stack
    });
  }, [session]);

  // Restart level
  const handleRestart = useCallback(() => {
    if (!session) return;
    SoundManager.playTap();
    HapticManager.lightTap();
    setActiveHintArrowId(null);
    setShowClearedModal(false);
    setShowFailedModal(false);

    const restarted = createInitialSession(
      session.levelNumber,
      session.levelTitle,
      session.difficulty,
      session.initialBoard,
      session.mode
    );
    setSession(restarted);
  }, [session]);

  // Intelligent Hint
  const handleHint = useCallback(() => {
    if (!session || session.status !== 'PLAYING') return;

    const legal = MoveValidator.getLegalMoves(session.board);
    if (legal.length === 0) return;

    // Pick first genuinely legal move from solver's optimal sequence if available
    const solverRes = PuzzleSolver.solve(session.board);
    let chosenId = legal[0].id;
    if (solverRes.isSolvable && solverRes.solutionOrder.length > 0) {
      chosenId = solverRes.solutionOrder[0];
    }

    SoundManager.playHint();
    HapticManager.lightTap();
    setActiveHintArrowId(chosenId);

    // Auto-clear hint highlight after 3 seconds
    setTimeout(() => {
      setActiveHintArrowId(curr => (curr === chosenId ? null : curr));
    }, 3200);
  }, [session]);

  // Advance to next campaign level
  const handleNextLevel = () => {
    setShowClearedModal(false);
    if (!session) return;
    const nextLvl = session.levelNumber + 1;
    if (nextLvl <= 50) {
      startCampaignLevel(nextLvl);
    } else {
      setCurrentScreen('LEVEL_SELECT');
    }
  };

  return (
    <div className="min-h-screen w-full bg-[#05080F] text-slate-100 flex items-center justify-center p-0 sm:p-4 select-none font-sans overflow-hidden">
      {/* Sleek Device Mockup Frame on Desktop / Full Screen on Mobile */}
      <div className="relative w-full max-w-[460px] h-[100dvh] sm:h-[880px] max-h-[100dvh] sm:max-h-[92vh] sm:rounded-[42px] bg-[#080D17] border-0 sm:border-[6px] sm:border-[#1E293B] shadow-[0_25px_60px_rgba(0,0,0,0.9),0_0_50px_rgba(0,240,255,0.1)] flex flex-col overflow-hidden">
        {/* Android Status Bar Simulation */}
        <div className="w-full flex items-center justify-between px-6 pt-3 pb-1 text-[11px] font-mono text-slate-400 select-none z-30">
          <div className="flex items-center gap-1.5">
            <span className="font-semibold text-slate-300">VECTOR OS</span>
          </div>
          {/* Subtle speaker notch on top bezel */}
          <div className="w-16 h-1 rounded-full bg-slate-800" />
          <div className="flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-cyan-400 shadow-[0_0_6px_#00F0FF]" />
            <span>60 FPS</span>
          </div>
        </div>

        {/* Screen Controller */}
        <main className="relative flex-1 w-full flex flex-col overflow-hidden z-20">
          {currentScreen === 'MENU' && (
            <MainMenu
              progress={progress}
              onPlayCurrent={() => startCampaignLevel(progress.currentCampaignLevel)}
              onOpenLevels={() => setCurrentScreen('LEVEL_SELECT')}
              onOpenDaily={() => setCurrentScreen('DAILY_CHALLENGE')}
              onOpenPractice={() => setCurrentScreen('PRACTICE_SELECT')}
              onOpenSettings={() => setShowSettings(true)}
              onOpenInspector={() => setShowAndroidInspector(true)}
            />
          )}

          {currentScreen === 'LEVEL_SELECT' && (
            <LevelSelect
              levels={campaignLevels}
              progress={progress}
              onSelectLevel={lvl => startCampaignLevel(lvl)}
              onBack={() => setCurrentScreen('MENU')}
            />
          )}

          {currentScreen === 'DAILY_CHALLENGE' && (
            <DailyChallenge
              progress={progress}
              onPlayDaily={dateStr => startDailyVector(dateStr)}
              onBack={() => setCurrentScreen('MENU')}
            />
          )}

          {currentScreen === 'PRACTICE_SELECT' && (
            <PracticeMode
              onStartPractice={(diff, seed) => startPractice(diff, seed)}
              onBack={() => setCurrentScreen('MENU')}
            />
          )}

          {currentScreen === 'GAME' && session && (
            <div className="w-full h-full flex flex-col justify-between py-2 overflow-hidden">
              <GameHUD
                levelNumber={session.levelNumber}
                levelTitle={session.levelTitle}
                difficulty={session.difficulty}
                hearts={session.hearts}
                maxHearts={session.maxHearts}
                moves={session.moves}
                flow={session.flow}
                remainingArrows={session.board.remainingCount}
                canUndo={session.undoStack.length > 0}
                onBack={() => {
                  if (session.mode === 'CAMPAIGN') {
                    setCurrentScreen('LEVEL_SELECT');
                  } else if (session.mode === 'DAILY') {
                    setCurrentScreen('DAILY_CHALLENGE');
                  } else {
                    setCurrentScreen('PRACTICE_SELECT');
                  }
                }}
                onRestart={handleRestart}
                onUndo={handleUndo}
                onHint={handleHint}
                mode={session.mode}
              />

              <div className="flex-1 flex items-center justify-center py-2">
                <VectorBoard
                  board={session.board}
                  onMoveSuccess={handleMoveSuccess}
                  onMoveInvalid={handleMoveInvalid}
                  activeHintArrowId={activeHintArrowId}
                  disabled={session.status !== 'PLAYING'}
                  reducedMotion={progress.settings.reducedMotion}
                />
              </div>

              {/* Tutorial tip banner if available */}
              {session.levelNumber <= 5 && session.mode === 'CAMPAIGN' && (
                <div className="mx-4 mb-2 p-2.5 rounded-xl bg-cyan-950/40 border border-cyan-500/20 text-center">
                  <span className="text-[11px] font-mono text-cyan-300">
                    {CampaignLevels.getLevel(session.levelNumber)?.tutorialTip}
                  </span>
                </div>
              )}
            </div>
          )}
        </main>

        {/* Android Home Indicator Pill */}
        <div className="w-full flex items-center justify-center pb-2 pt-1 z-30 pointer-events-none">
          <div className="w-28 h-1 rounded-full bg-slate-700/60" />
        </div>
      </div>

      {/* Modals & Dialogs */}
      {showClearedModal && session && (
        <LevelCompletionModal
          levelNumber={session.levelNumber}
          moves={session.moves}
          mistakes={session.mistakes}
          heartsRemaining={session.hearts}
          stars={session.stars}
          onNextLevel={handleNextLevel}
          onReplay={handleRestart}
          onLevelSelect={() => {
            setShowClearedModal(false);
            setCurrentScreen('LEVEL_SELECT');
          }}
          hasNextLevel={session.mode === 'CAMPAIGN' && session.levelNumber < 50}
          reducedMotion={progress.settings.reducedMotion}
        />
      )}

      {showFailedModal && session && (
        <LevelFailedModal
          levelNumber={session.levelNumber}
          onTryAgain={handleRestart}
          onLevelSelect={() => {
            setShowFailedModal(false);
            setCurrentScreen('LEVEL_SELECT');
          }}
        />
      )}

      {showSettings && (
        <SettingsModal
          settings={progress.settings}
          onUpdateSettings={handleUpdateSettings}
          onResetProgress={handleResetProgress}
          onClose={() => setShowSettings(false)}
        />
      )}

      {showAndroidInspector && (
        <AndroidProjectViewer onClose={() => setShowAndroidInspector(false)} />
      )}
    </div>
  );
}
