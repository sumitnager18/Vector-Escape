import React, { useState } from 'react';
import { ArrowLeft, Play, Dices, Sparkles, CheckCircle2 } from 'lucide-react';
import { DifficultyLevel } from '../game/DifficultyAnalyzer';

interface PracticeModeProps {
  onStartPractice: (difficulty: DifficultyLevel, customSeed?: string) => void;
  onBack: () => void;
}

export const PracticeMode: React.FC<PracticeModeProps> = ({
  onStartPractice,
  onBack
}) => {
  const [selectedDifficulty, setSelectedDifficulty] = useState<DifficultyLevel>('MEDIUM');
  const [customSeed, setCustomSeed] = useState<string>('');

  const difficulties: { level: DifficultyLevel; name: string; desc: string; color: string }[] = [
    {
      level: 'EASY',
      name: 'EASY',
      desc: '5x5 grid, 6–8 vectors, light dependencies',
      color: 'border-emerald-500/40 text-emerald-400 bg-emerald-950/20'
    },
    {
      level: 'MEDIUM',
      name: 'MEDIUM',
      desc: '6x6 grid, 12–15 vectors, balanced branching',
      color: 'border-cyan-500/40 text-cyan-400 bg-cyan-950/20'
    },
    {
      level: 'HARD',
      name: 'HARD',
      desc: '6x6 grid, 18–22 vectors, deep chain blockages',
      color: 'border-amber-500/40 text-amber-400 bg-amber-950/20'
    },
    {
      level: 'EXPERT',
      name: 'EXPERT',
      desc: '6x6 grid, 25+ vectors, intricate multi-layer lock',
      color: 'border-purple-500/40 text-purple-400 bg-purple-950/20'
    }
  ];

  const rollSeed = () => {
    const randomSeed = `vector_${Math.floor(100000 + Math.random() * 900000)}`;
    setCustomSeed(randomSeed);
  };

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
            PRACTICE LAB
          </h2>
          <span className="text-[11px] font-mono text-cyan-400">
            INFINITE PROCEDURAL SYNTHESIS
          </span>
        </div>

        <div className="w-10" />
      </div>

      {/* Difficulty Selector Cards */}
      <div className="flex flex-col gap-2.5 mb-6">
        <span className="text-xs font-mono text-slate-400 tracking-wider">
          SELECT DIFFICULTY COMPLEXITY:
        </span>
        {difficulties.map(diff => {
          const isSelected = selectedDifficulty === diff.level;
          return (
            <button
              key={diff.level}
              onClick={() => setSelectedDifficulty(diff.level)}
              className={`p-4 rounded-2xl border text-left flex items-center justify-between transition-all outline-none cursor-pointer ${
                isSelected
                  ? `${diff.color} ring-1 ring-cyan-400/50 shadow-[0_0_15px_rgba(0,240,255,0.15)]`
                  : 'bg-[#0E172A]/70 border-slate-800 text-slate-400 hover:border-slate-700'
              }`}
            >
              <div className="flex flex-col">
                <span className="text-sm font-bold font-mono tracking-wider">
                  {diff.name}
                </span>
                <span className="text-xs text-slate-400 mt-0.5">
                  {diff.desc}
                </span>
              </div>
              {isSelected && <CheckCircle2 className="w-5 h-5 text-cyan-400" />}
            </button>
          );
        })}
      </div>

      {/* Custom Seed Input (Optional) */}
      <div className="flex flex-col gap-2 mb-8">
        <div className="flex items-center justify-between">
          <span className="text-xs font-mono text-slate-400">
            PROCEDURAL SEED (OPTIONAL):
          </span>
          <button
            onClick={rollSeed}
            className="flex items-center gap-1 text-[11px] font-mono text-cyan-400 hover:text-cyan-300 cursor-pointer"
          >
            <Dices className="w-3.5 h-3.5" />
            <span>RANDOM ROLL</span>
          </button>
        </div>
        <input
          type="text"
          value={customSeed}
          onChange={e => setCustomSeed(e.target.value)}
          placeholder="Leave blank for fresh random puzzle"
          className="w-full px-4 py-3 rounded-2xl bg-[#091120] border border-slate-800 focus:border-cyan-400 text-slate-200 font-mono text-xs outline-none placeholder:text-slate-600"
        />
      </div>

      {/* Start Button */}
      <button
        onClick={() => onStartPractice(selectedDifficulty, customSeed)}
        className="w-full py-4 px-6 rounded-2xl bg-gradient-to-r from-cyan-500 to-violet-600 hover:from-cyan-400 hover:to-violet-500 text-slate-950 font-bold font-mono tracking-wider flex items-center justify-center gap-2 shadow-[0_0_24px_rgba(0,240,255,0.4)] active:scale-98 transition-all cursor-pointer mt-auto"
      >
        <Sparkles className="w-5 h-5 fill-current" />
        <span>SYNTHESIZE PUZZLE</span>
      </button>
    </div>
  );
};
