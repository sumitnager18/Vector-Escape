import React, { useEffect } from 'react';
import { Star, RotateCcw, ArrowRight, List, Trophy, Heart, Sparkles } from 'lucide-react';
import confetti from 'canvas-confetti';

interface LevelCompletionModalProps {
  levelNumber: number;
  moves: number;
  mistakes: number;
  heartsRemaining: number;
  stars: number;
  onNextLevel: () => void;
  onReplay: () => void;
  onLevelSelect: () => void;
  hasNextLevel: boolean;
  reducedMotion?: boolean;
}

export const LevelCompletionModal: React.FC<LevelCompletionModalProps> = ({
  levelNumber,
  moves,
  mistakes,
  heartsRemaining,
  stars,
  onNextLevel,
  onReplay,
  onLevelSelect,
  hasNextLevel,
  reducedMotion = false
}) => {
  useEffect(() => {
    if (!reducedMotion) {
      try {
        confetti({
          particleCount: 50,
          spread: 60,
          origin: { y: 0.6 },
          colors: ['#00F0FF', '#8B5CF6', '#10B981']
        });
      } catch {
        // Ignore confetti if not supported
      }
    }
  }, [reducedMotion]);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-md animate-in fade-in duration-200">
      <div className="w-full max-w-sm rounded-3xl p-6 bg-gradient-to-b from-[#111A2E] to-[#0A101D] border border-cyan-500/30 shadow-[0_20px_50px_rgba(0,0,0,0.9),0_0_30px_rgba(0,240,255,0.15)] flex flex-col items-center text-center">
        {/* Glowing Trophy Icon */}
        <div className="w-16 h-16 rounded-2xl bg-cyan-950/80 border border-cyan-400/40 flex items-center justify-center mb-4 shadow-[0_0_24px_rgba(0,240,255,0.3)]">
          <Trophy className="w-8 h-8 text-cyan-300" />
        </div>

        <h2 className="text-xl font-bold tracking-widest text-slate-100 uppercase mb-1">
          LEVEL CLEARED
        </h2>
        <p className="text-xs font-mono text-cyan-400 mb-3">
          SECTOR {levelNumber} RESOLVED
        </p>

        {/* Subtle PERFECT celebration tag if zero mistakes */}
        {mistakes === 0 && (
          <div className="flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-950/80 border border-emerald-400/40 text-emerald-300 font-mono text-[11px] font-bold tracking-widest uppercase mb-4 shadow-[0_0_12px_rgba(16,185,129,0.3)] animate-pulse">
            <Sparkles className="w-3.5 h-3.5 text-emerald-400" />
            <span>PERFECT CLEAR</span>
          </div>
        )}

        {/* Star Rating Display */}
        <div className="flex items-center gap-2 mb-6">
          {[1, 2, 3].map(starNum => {
            const earned = starNum <= stars;
            return (
              <div
                key={starNum}
                className={`p-2 rounded-2xl border transition-all duration-300 ${
                  earned
                    ? 'bg-amber-950/40 border-amber-400/60 shadow-[0_0_15px_rgba(245,158,11,0.5)] scale-110'
                    : 'bg-slate-900/40 border-slate-800 opacity-30'
                }`}
              >
                <Star
                  className={`w-6 h-6 ${
                    earned ? 'fill-amber-400 text-amber-400' : 'text-slate-600'
                  }`}
                />
              </div>
            );
          })}
        </div>

        {/* Stats Grid */}
        <div className="w-full grid grid-cols-3 gap-2 p-3 rounded-2xl bg-[#070D18] border border-slate-800/80 mb-6 font-mono text-xs">
          <div className="flex flex-col items-center">
            <span className="text-slate-400 text-[10px]">MOVES</span>
            <span className="text-base font-bold text-cyan-300">{moves}</span>
          </div>
          <div className="flex flex-col items-center border-x border-slate-800">
            <span className="text-slate-400 text-[10px]">ERRORS</span>
            <span className="text-base font-bold text-amber-400">{mistakes}</span>
          </div>
          <div className="flex flex-col items-center">
            <span className="text-slate-400 text-[10px]">HEARTS</span>
            <div className="flex items-center gap-0.5 mt-0.5">
              <span className="text-base font-bold text-red-400">{heartsRemaining}</span>
              <Heart className="w-3.5 h-3.5 fill-red-500 text-red-500" />
            </div>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="w-full flex flex-col gap-2.5">
          {hasNextLevel && (
            <button
              onClick={onNextLevel}
              className="w-full py-3.5 px-4 rounded-2xl bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-slate-950 font-bold font-mono tracking-wider flex items-center justify-center gap-2 shadow-[0_0_20px_rgba(0,240,255,0.4)] active:scale-98 transition-all"
            >
              <span>NEXT SECTOR</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          )}

          <div className="w-full flex items-center gap-2">
            <button
              onClick={onReplay}
              className="flex-1 py-3 px-3 rounded-2xl bg-[#0E172A] border border-slate-700/80 hover:border-slate-500 text-slate-300 font-mono text-xs flex items-center justify-center gap-2 active:scale-95 transition-all"
            >
              <RotateCcw className="w-4 h-4 text-cyan-400" />
              <span>REPLAY</span>
            </button>
            <button
              onClick={onLevelSelect}
              className="flex-1 py-3 px-3 rounded-2xl bg-[#0E172A] border border-slate-700/80 hover:border-slate-500 text-slate-300 font-mono text-xs flex items-center justify-center gap-2 active:scale-95 transition-all"
            >
              <List className="w-4 h-4 text-violet-400" />
              <span>SELECT</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
