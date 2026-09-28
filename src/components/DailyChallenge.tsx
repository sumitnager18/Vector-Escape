import React, { useMemo } from 'react';
import { ArrowLeft, Calendar, Play, RotateCcw, Star, CheckCircle2, ShieldCheck } from 'lucide-react';
import { DailyVectorGenerator } from '../game/DailyVectorGenerator';
import { PlayerProgressData } from '../storage/StorageManager';

interface DailyChallengeProps {
  progress: PlayerProgressData;
  onPlayDaily: (dateStr: string) => void;
  onBack: () => void;
}

export const DailyChallenge: React.FC<DailyChallengeProps> = ({
  progress,
  onPlayDaily,
  onBack
}) => {
  const todayStr = useMemo(() => DailyVectorGenerator.getTodayDateString(), []);
  const todayPuzzle = useMemo(() => DailyVectorGenerator.generateForDate(todayStr), [todayStr]);
  const record = progress.dailyRecords[todayStr];
  const isCompleted = record?.completed ?? false;

  return (
    <div className="w-full max-w-md mx-auto flex flex-col h-full py-4 px-4">
      {/* Header */}
      <div className="flex items-center justify-between pb-4 border-b border-cyan-500/15 mb-6">
        <button
          onClick={onBack}
          aria-label="Return to main menu"
          className="p-2.5 rounded-2xl bg-[#0F172A]/80 border border-slate-700/60 text-slate-300 hover:text-cyan-300 hover:border-cyan-500/40 active:scale-95 transition-all outline-none"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>

        <div className="flex flex-col items-center">
          <h2 className="text-base font-bold tracking-widest text-slate-100 uppercase">
            DAILY VECTOR
          </h2>
          <span className="text-[11px] font-mono text-cyan-400">
            OFFLINE CALENDAR MATRIX
          </span>
        </div>

        <div className="w-10" />
      </div>

      {/* Main Daily Card */}
      <div className="relative rounded-3xl p-6 bg-gradient-to-b from-[#111A2E] to-[#0A101D] border border-cyan-500/30 shadow-[0_12px_40px_rgba(0,0,0,0.8),0_0_20px_rgba(0,240,255,0.1)] flex flex-col items-center text-center mb-6">
        {/* Date Badge */}
        <div className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-cyan-950/80 border border-cyan-400/40 text-cyan-300 font-mono text-xs mb-4">
          <Calendar className="w-3.5 h-3.5" />
          <span>{todayStr}</span>
        </div>

        <h3 className="text-xl font-bold tracking-wider text-slate-100 uppercase mb-1">
          TODAY'S ANOMALY
        </h3>
        <p className="text-xs font-mono text-slate-400 mb-6">
          Unique deterministic puzzle generated for this calendar date.
        </p>

        {/* Matrix Specs */}
        <div className="w-full grid grid-cols-3 gap-2 p-3 rounded-2xl bg-[#070D18] border border-slate-800/80 mb-6 font-mono text-xs">
          <div className="flex flex-col items-center">
            <span className="text-slate-400 text-[10px]">DIFFICULTY</span>
            <span className="text-xs font-bold text-cyan-300 mt-1 uppercase">
              {todayPuzzle.difficulty}
            </span>
          </div>
          <div className="flex flex-col items-center border-x border-slate-800">
            <span className="text-slate-400 text-[10px]">VECTORS</span>
            <span className="text-base font-bold text-slate-200">
              {todayPuzzle.arrows.length}
            </span>
          </div>
          <div className="flex flex-col items-center">
            <span className="text-slate-400 text-[10px]">STATUS</span>
            <div className="flex items-center gap-1 mt-1">
              {isCompleted ? (
                <span className="text-xs font-bold text-emerald-400 flex items-center gap-1">
                  <CheckCircle2 className="w-3.5 h-3.5" />
                  DONE
                </span>
              ) : (
                <span className="text-xs font-bold text-amber-400">READY</span>
              )}
            </div>
          </div>
        </div>

        {/* Star Rating if completed */}
        {isCompleted && (
          <div className="flex items-center gap-2 mb-6">
            {[1, 2, 3].map(starNum => (
              <Star
                key={starNum}
                className={`w-6 h-6 ${
                  starNum <= (record?.stars ?? 0)
                    ? 'fill-amber-400 text-amber-400 drop-shadow-[0_0_8px_rgba(245,158,11,0.5)]'
                    : 'text-slate-700'
                }`}
              />
            ))}
          </div>
        )}

        {/* Play / Replay Button */}
        <button
          onClick={() => onPlayDaily(todayStr)}
          className="w-full py-4 px-6 rounded-2xl bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-slate-950 font-bold font-mono tracking-wider flex items-center justify-center gap-2 shadow-[0_0_24px_rgba(0,240,255,0.4)] active:scale-98 transition-all cursor-pointer"
        >
          {isCompleted ? (
            <>
              <RotateCcw className="w-5 h-5" />
              <span>REPLAY TODAY'S VECTOR</span>
            </>
          ) : (
            <>
              <Play className="w-5 h-5 fill-current" />
              <span>ENGAGE DAILY VECTOR</span>
            </>
          )}
        </button>
      </div>

      {/* Offline Guarantee Banner */}
      <div className="flex items-center gap-2 px-4 py-3 rounded-2xl bg-[#091120] border border-cyan-500/20 text-xs font-mono text-slate-400">
        <ShieldCheck className="w-4 h-4 text-cyan-400 flex-shrink-0" />
        <span>100% Offline & Deterministic: Same date produces identical puzzle.</span>
      </div>
    </div>
  );
};
