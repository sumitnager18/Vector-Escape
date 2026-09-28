import React from 'react';
import { ArrowLeft, Lock, Star, Sparkles } from 'lucide-react';
import { LevelDefinition } from '../game/CampaignLevels';
import { PlayerProgressData } from '../storage/StorageManager';

interface LevelSelectProps {
  levels: LevelDefinition[];
  progress: PlayerProgressData;
  onSelectLevel: (levelNumber: number) => void;
  onBack: () => void;
}

export const LevelSelect: React.FC<LevelSelectProps> = ({
  levels,
  progress,
  onSelectLevel,
  onBack
}) => {
  return (
    <div className="w-full max-w-md mx-auto flex flex-col h-full py-4 px-3">
      {/* Header */}
      <div className="flex items-center justify-between pb-4 border-b border-cyan-500/15 mb-4">
        <button
          onClick={onBack}
          aria-label="Return to main menu"
          className="p-2.5 rounded-2xl bg-[#0F172A]/80 border border-slate-700/60 text-slate-300 hover:text-cyan-300 hover:border-cyan-500/40 active:scale-95 transition-all outline-none"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>

        <div className="flex flex-col items-center">
          <h2 className="text-base font-bold tracking-widest text-slate-100 uppercase">
            SECTOR MATRIX
          </h2>
          <span className="text-[11px] font-mono text-cyan-400">
            50 CAMPAIGN LEVELS
          </span>
        </div>

        {/* Stars counter */}
        <div className="flex items-center gap-1 px-3 py-1.5 rounded-2xl bg-[#0F172A]/80 border border-amber-500/30 text-amber-300 font-mono text-xs">
          <Star className="w-4 h-4 fill-amber-400 text-amber-400" />
          <span className="font-bold">{progress.totalStars}</span>
          <span className="text-slate-500">/ 150</span>
        </div>
      </div>

      {/* Grid of 50 Levels */}
      <div className="flex-1 overflow-y-auto pr-1 pb-6 grid grid-cols-5 gap-2.5">
        {levels.map(lvl => {
          const isUnlocked = lvl.levelNumber <= progress.unlockedLevel;
          const isCurrent = lvl.levelNumber === progress.unlockedLevel;
          const record = progress.levelRecords[lvl.levelNumber];
          const starsEarned = record ? record.stars : 0;
          const diffDot = lvl.difficulty === 'EASY' ? 'bg-emerald-400' : lvl.difficulty === 'MEDIUM' ? 'bg-cyan-400' : lvl.difficulty === 'HARD' ? 'bg-amber-400' : 'bg-purple-400';

          return (
            <button
              key={lvl.levelNumber}
              disabled={!isUnlocked}
              onClick={() => onSelectLevel(lvl.levelNumber)}
              className={`relative aspect-square rounded-2xl flex flex-col items-center justify-center p-1 transition-all outline-none ${
                isUnlocked
                  ? isCurrent
                    ? 'bg-gradient-to-b from-cyan-900/60 to-slate-900 border-2 border-cyan-400 shadow-[0_0_16px_rgba(0,240,255,0.4)] scale-102 cursor-pointer'
                    : 'bg-[#0E172A] border border-slate-700/70 hover:border-cyan-500/50 hover:bg-[#132038] cursor-pointer active:scale-95'
                  : 'bg-[#080D17]/60 border border-slate-900 text-slate-700 cursor-not-allowed'
              }`}
            >
              {isUnlocked ? (
                <>
                  <span className={`absolute top-1.5 left-1.5 w-1.5 h-1.5 rounded-full ${diffDot} opacity-70`} />
                  <span
                    className={`font-mono text-sm font-bold ${
                      isCurrent ? 'text-cyan-300' : 'text-slate-200'
                    }`}
                  >
                    {lvl.levelNumber}
                  </span>

                  {/* Star indicators */}
                  <div className="flex items-center gap-0.5 mt-1">
                    {[1, 2, 3].map(s => (
                      <Star
                        key={s}
                        className={`w-2.5 h-2.5 ${
                          s <= starsEarned
                            ? 'fill-amber-400 text-amber-400'
                            : 'text-slate-700'
                        }`}
                      />
                    ))}
                  </div>

                  {isCurrent && (
                    <span className="absolute -top-1 -right-1 flex h-2.5 w-2.5">
                      <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-cyan-400 opacity-75"></span>
                      <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-cyan-500"></span>
                    </span>
                  )}
                </>
              ) : (
                <Lock className="w-4 h-4 text-slate-700" />
              )}
            </button>
          );
        })}
      </div>
    </div>
  );
};
