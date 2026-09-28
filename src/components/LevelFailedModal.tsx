import React from 'react';
import { RotateCcw, List, ShieldAlert } from 'lucide-react';

interface LevelFailedModalProps {
  levelNumber: number;
  onTryAgain: () => void;
  onLevelSelect: () => void;
}

export const LevelFailedModal: React.FC<LevelFailedModalProps> = ({
  levelNumber,
  onTryAgain,
  onLevelSelect
}) => {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-in fade-in duration-200">
      <div className="w-full max-w-sm rounded-3xl p-6 bg-gradient-to-b from-[#1E1118] to-[#0D080D] border border-red-500/30 shadow-[0_20px_50px_rgba(0,0,0,0.9),0_0_30px_rgba(239,68,68,0.2)] flex flex-col items-center text-center">
        {/* Shield Alert Icon */}
        <div className="w-16 h-16 rounded-2xl bg-red-950/80 border border-red-500/40 flex items-center justify-center mb-4 shadow-[0_0_24px_rgba(239,68,68,0.3)]">
          <ShieldAlert className="w-8 h-8 text-red-400" />
        </div>

        <h2 className="text-xl font-bold tracking-widest text-slate-100 uppercase mb-1">
          CONTAINMENT BREACH
        </h2>
        <p className="text-xs font-mono text-red-400 mb-6">
          ALL HEARTS DEPLETED IN SECTOR {levelNumber}
        </p>

        <p className="text-xs text-slate-400 mb-6 leading-relaxed">
          Lane collision blocked the vector matrix. Analyze the obstacle lanes before launching.
        </p>

        {/* Action Buttons */}
        <div className="w-full flex flex-col gap-2.5">
          <button
            onClick={onTryAgain}
            className="w-full py-3.5 px-4 rounded-2xl bg-gradient-to-r from-red-600 to-amber-600 hover:from-red-500 hover:to-amber-500 text-slate-950 font-bold font-mono tracking-wider flex items-center justify-center gap-2 shadow-[0_0_20px_rgba(239,68,68,0.4)] active:scale-98 transition-all"
          >
            <RotateCcw className="w-4 h-4" />
            <span>TRY AGAIN</span>
          </button>

          <button
            onClick={onLevelSelect}
            className="w-full py-3 px-4 rounded-2xl bg-[#140D17] border border-slate-700/80 hover:border-slate-500 text-slate-300 font-mono text-xs flex items-center justify-center gap-2 active:scale-95 transition-all"
          >
            <List className="w-4 h-4 text-slate-400" />
            <span>LEVEL SELECT</span>
          </button>
        </div>
      </div>
    </div>
  );
};
