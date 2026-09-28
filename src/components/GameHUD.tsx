import React from 'react';
import { ArrowLeft, RotateCcw, Undo2, Lightbulb, Heart, Zap, Sparkles } from 'lucide-react';
import { DifficultyLevel } from '../game/DifficultyAnalyzer';

interface GameHUDProps {
  levelNumber: number;
  levelTitle: string;
  difficulty: DifficultyLevel;
  hearts: number;
  maxHearts: number;
  moves: number;
  flow: number;
  remainingArrows: number;
  canUndo: boolean;
  onBack: () => void;
  onRestart: () => void;
  onUndo: () => void;
  onHint: () => void;
  mode: 'CAMPAIGN' | 'DAILY' | 'PRACTICE';
}

export const GameHUD: React.FC<GameHUDProps> = ({
  levelNumber,
  levelTitle,
  difficulty,
  hearts,
  maxHearts,
  moves,
  flow,
  remainingArrows,
  canUndo,
  onBack,
  onRestart,
  onUndo,
  onHint,
  mode
}) => {
  const diffColors: Record<DifficultyLevel, { text: string; bg: string; border: string }> = {
    EASY: { text: 'text-emerald-400', bg: 'bg-emerald-950/60', border: 'border-emerald-500/30' },
    MEDIUM: { text: 'text-cyan-400', bg: 'bg-cyan-950/60', border: 'border-cyan-500/30' },
    HARD: { text: 'text-amber-400', bg: 'bg-amber-950/60', border: 'border-amber-500/30' },
    EXPERT: { text: 'text-purple-400', bg: 'bg-purple-950/60', border: 'border-purple-500/30' }
  };

  const badge = diffColors[difficulty] || diffColors.EASY;

  return (
    <div className="w-full max-w-md mx-auto flex flex-col gap-3 px-3">
      {/* Top Header Row */}
      <header className="flex items-center justify-between py-2 border-b border-cyan-500/10" aria-label="Game Status Bar">
        {/* Back Button */}
        <button
          onClick={onBack}
          aria-label="Return to menu"
          className="p-2.5 rounded-2xl bg-[#0F172A]/80 border border-slate-700/60 text-slate-300 hover:text-cyan-300 hover:border-cyan-500/40 active:scale-95 transition-all outline-none"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>

        {/* Level & Difficulty info */}
        <div className="flex flex-col items-center">
          <div className="flex items-center gap-2">
            <span className="text-base font-bold tracking-wider text-slate-100 uppercase">
              {mode === 'DAILY' ? 'DAILY VECTOR' : mode === 'PRACTICE' ? 'PRACTICE' : `LEVEL ${levelNumber}`}
            </span>
            <span
              className={`text-[10px] font-mono px-2 py-0.5 rounded-full border ${badge.bg} ${badge.border} ${badge.text} tracking-wider font-semibold uppercase`}
            >
              {difficulty}
            </span>
          </div>
          <span className="text-xs font-mono text-slate-400 truncate max-w-[170px]">
            {levelTitle}
          </span>
        </div>

        {/* Hearts display (if not practice mode) */}
        <div className="flex items-center gap-1.5 p-1.5 px-2.5 rounded-2xl bg-[#0F172A]/80 border border-slate-700/60">
          {mode === 'PRACTICE' ? (
            <div className="flex items-center gap-1 text-xs font-mono text-cyan-400">
              <Zap className="w-3.5 h-3.5" />
              <span>FREE</span>
            </div>
          ) : (
            Array.from({ length: maxHearts }).map((_, idx) => {
              const active = idx < hearts;
              return (
                <Heart
                  key={idx}
                  className={`w-4 h-4 transition-all duration-300 ${
                    active
                      ? 'fill-red-500 text-red-500 drop-shadow-[0_0_8px_rgba(239,68,68,0.7)] scale-100'
                      : 'fill-transparent text-slate-600 scale-90'
                  }`}
                />
              );
            })
          )}
        </div>
      </header>

      {/* Stats ribbon: Remaining, Moves, Flow Multiplier */}
      <div className="flex items-center justify-between px-3 py-1.5 rounded-xl bg-[#0B1322]/80 border border-cyan-500/15 backdrop-blur-md">
        {/* Remaining arrows */}
        <div className="flex items-center gap-1.5">
          <span className="text-xs text-slate-400">VECTORS:</span>
          <span className="text-sm font-bold font-mono text-cyan-300">
            {remainingArrows}
          </span>
        </div>

        {/* Moves Counter */}
        <div className="flex items-center gap-1.5">
          <span className="text-xs text-slate-400">MOVES:</span>
          <span className="text-sm font-bold font-mono text-slate-200">
            {moves}
          </span>
        </div>

        {/* FLOW Multiplier */}
        <div
          className={`flex items-center gap-1 px-2 py-0.5 rounded-lg transition-all ${
            flow > 1
              ? 'bg-gradient-to-r from-cyan-950 to-violet-950 border border-cyan-400/40 shadow-[0_0_10px_rgba(0,240,255,0.2)] scale-105'
              : 'opacity-40'
          }`}
        >
          <Sparkles className="w-3 h-3 text-cyan-400" />
          <span className="text-xs font-mono font-bold tracking-wider text-cyan-300">
            FLOW ×{flow}
          </span>
        </div>
      </div>

      {/* Bottom Action Controls */}
      <div className="flex items-center justify-between gap-3 pt-2">
        {/* Undo */}
        <button
          onClick={onUndo}
          disabled={!canUndo}
          aria-label="Undo previous move"
          className={`flex-1 py-3 px-3 rounded-2xl border flex items-center justify-center gap-2 text-xs font-mono tracking-wider font-semibold transition-all active:scale-95 outline-none ${
            canUndo
              ? 'bg-[#0E172A] border-slate-700 text-slate-200 hover:border-cyan-400/40 hover:text-cyan-300'
              : 'bg-[#0A101D]/50 border-slate-800 text-slate-600 cursor-not-allowed'
          }`}
        >
          <Undo2 className="w-4 h-4" />
          <span>UNDO</span>
        </button>

        {/* Hint */}
        <button
          onClick={onHint}
          aria-label="Show hint"
          className="flex-1 py-3 px-3 rounded-2xl bg-[#0E172A] border border-slate-700 text-slate-200 hover:border-emerald-400/40 hover:text-emerald-300 flex items-center justify-center gap-2 text-xs font-mono tracking-wider font-semibold transition-all active:scale-95 outline-none"
        >
          <Lightbulb className="w-4 h-4 text-emerald-400" />
          <span>HINT</span>
        </button>

        {/* Restart */}
        <button
          onClick={onRestart}
          aria-label="Restart level"
          className="flex-1 py-3 px-3 rounded-2xl bg-[#0E172A] border border-slate-700 text-slate-200 hover:border-amber-400/40 hover:text-amber-300 flex items-center justify-center gap-2 text-xs font-mono tracking-wider font-semibold transition-all active:scale-95 outline-none"
        >
          <RotateCcw className="w-4 h-4 text-amber-400" />
          <span>RESET</span>
        </button>
      </div>
    </div>
  );
};
