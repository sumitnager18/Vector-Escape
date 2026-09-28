import React, { useEffect, useRef } from 'react';
import { Play, List, Calendar, Sparkles, Settings, Code, Star, ChevronRight, ShieldCheck } from 'lucide-react';
import { PlayerProgressData } from '../storage/StorageManager';

interface MainMenuProps {
  progress: PlayerProgressData;
  onPlayCurrent: () => void;
  onOpenLevels: () => void;
  onOpenDaily: () => void;
  onOpenPractice: () => void;
  onOpenSettings: () => void;
  onOpenInspector: () => void;
}

export const MainMenu: React.FC<MainMenuProps> = ({
  progress,
  onPlayCurrent,
  onOpenLevels,
  onOpenDaily,
  onOpenPractice,
  onOpenSettings,
  onOpenInspector
}) => {
  const bgCanvasRef = useRef<HTMLCanvasElement>(null);

  // Subtle animated background vector grid / slow flowing directional particles
  useEffect(() => {
    const canvas = bgCanvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let animId: number;
    let width = (canvas.width = canvas.parentElement?.clientWidth || 400);
    let height = (canvas.height = canvas.parentElement?.clientHeight || 700);

    const particles: { x: number; y: number; vx: number; vy: number; len: number; alpha: number }[] = [];
    for (let i = 0; i < 28; i++) {
      particles.push({
        x: Math.random() * width,
        y: Math.random() * height,
        vx: (Math.random() - 0.5) * 0.4,
        vy: -0.3 - Math.random() * 0.5,
        len: 12 + Math.random() * 20,
        alpha: 0.1 + Math.random() * 0.25
      });
    }

    const render = () => {
      ctx.clearRect(0, 0, width, height);

      // Draw faint isometric grid lines
      ctx.strokeStyle = 'rgba(0, 240, 255, 0.04)';
      ctx.lineWidth = 1;
      const step = 45;
      for (let x = 0; x < width; x += step) {
        ctx.beginPath();
        ctx.moveTo(x, 0);
        ctx.lineTo(x, height);
        ctx.stroke();
      }
      for (let y = 0; y < height; y += step) {
        ctx.beginPath();
        ctx.moveTo(0, y);
        ctx.lineTo(width, y);
        ctx.stroke();
      }

      // Draw upward drifting vector particles
      for (const p of particles) {
        p.x += p.vx;
        p.y += p.vy;
        if (p.y < -30) {
          p.y = height + 10;
          p.x = Math.random() * width;
        }

        ctx.save();
        ctx.strokeStyle = `rgba(0, 240, 255, ${p.alpha})`;
        ctx.lineWidth = 1.5;
        ctx.beginPath();
        ctx.moveTo(p.x, p.y);
        ctx.lineTo(p.x, p.y + p.len);
        ctx.stroke();

        // Tiny glowing arrow tip
        ctx.fillStyle = `rgba(0, 240, 255, ${p.alpha * 1.5})`;
        ctx.beginPath();
        ctx.arc(p.x, p.y, 1.8, 0, Math.PI * 2);
        ctx.fill();
        ctx.restore();
      }

      animId = requestAnimationFrame(render);
    };

    animId = requestAnimationFrame(render);

    const handleResize = () => {
      if (canvas.parentElement) {
        width = canvas.width = canvas.parentElement.clientWidth;
        height = canvas.height = canvas.parentElement.clientHeight;
      }
    };
    window.addEventListener('resize', handleResize);

    return () => {
      cancelAnimationFrame(animId);
      window.removeEventListener('resize', handleResize);
    };
  }, []);

  return (
    <div className="relative w-full max-w-md mx-auto flex flex-col h-full py-6 px-4 justify-between select-none">
      {/* Background canvas */}
      <canvas
        ref={bgCanvasRef}
        className="absolute inset-0 w-full h-full pointer-events-none z-0"
      />

      {/* Top Header & Branding */}
      <div className="relative z-10 flex flex-col items-center text-center mt-3">
        {/* Sleek Geometric Device Emblem */}
        <div className="relative w-16 h-16 rounded-3xl p-3 bg-gradient-to-br from-cyan-950 via-[#0E172A] to-violet-950 border border-cyan-400/40 shadow-[0_0_30px_rgba(0,240,255,0.3)] flex items-center justify-center mb-3">
          <svg viewBox="0 0 40 40" className="w-9 h-9">
            <path
              d="M 12 24 L 20 12 L 28 24"
              fill="none"
              stroke="#00F0FF"
              strokeWidth="3.5"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
            <line
              x1="20"
              y1="12"
              x2="20"
              y2="32"
              stroke="#00F0FF"
              strokeWidth="3.5"
              strokeLinecap="round"
            />
            <circle cx="20" cy="30" r="2.5" fill="#8B5CF6" />
          </svg>
        </div>

        <h1 className="text-2xl font-black tracking-[0.25em] text-slate-100 uppercase">
          VECTOR ESCAPE
        </h1>
        <p className="text-[11px] font-mono tracking-widest text-cyan-400 mt-1 uppercase">
          CLEAR THE FIELD
        </p>

        {/* Campaign Star Counter badge */}
        <div className="flex items-center gap-1.5 mt-3 px-3 py-1 rounded-full bg-[#0E172A]/90 border border-slate-700/80 text-xs font-mono text-amber-300">
          <Star className="w-3.5 h-3.5 fill-amber-400 text-amber-400" />
          <span className="font-bold">{progress.totalStars}</span>
          <span className="text-slate-500">/ 150 STARS</span>
          <span className="text-slate-600">|</span>
          <span className="text-cyan-300">SECTOR {progress.currentCampaignLevel}</span>
        </div>
      </div>

      {/* Primary Center Action: CONTINUE / PLAY */}
      <div className="relative z-10 my-auto py-2 flex flex-col items-center w-full gap-2">
        <button
          onClick={onPlayCurrent}
          className="group relative w-full py-4.5 px-6 rounded-3xl bg-gradient-to-r from-cyan-500 via-blue-500 to-violet-600 text-slate-950 font-black font-mono tracking-widest text-base uppercase flex items-center justify-center gap-3 shadow-[0_0_35px_rgba(0,240,255,0.45)] hover:shadow-[0_0_45px_rgba(0,240,255,0.6)] active:scale-98 transition-all cursor-pointer"
        >
          <div className="w-10 h-10 rounded-2xl bg-black/20 flex items-center justify-center text-slate-950">
            <Play className="w-5 h-5 fill-current ml-0.5" />
          </div>
          <div className="flex flex-col text-left">
            <span>
              {progress.currentCampaignLevel === 1 ? 'PLAY' : 'CONTINUE'}
            </span>
            <span className="text-[10px] tracking-normal font-sans font-medium text-slate-900 opacity-80">
              {progress.currentCampaignLevel === 1 ? 'Start Sector 1' : `Resume Sector ${progress.currentCampaignLevel}`}
            </span>
          </div>
          <ChevronRight className="w-5 h-5 ml-auto text-slate-900 group-hover:translate-x-1 transition-transform" />
        </button>

        {progress.currentCampaignLevel > 1 && (
          <button
            onClick={onOpenLevels}
            className="w-full py-2.5 px-4 rounded-2xl bg-[#0A111F]/80 border border-slate-800 hover:border-cyan-500/30 text-slate-400 hover:text-slate-200 text-xs font-mono tracking-wider flex items-center justify-center gap-2 active:scale-98 transition-all cursor-pointer"
          >
            <span>RESTART FROM SECTOR 1</span>
          </button>
        )}
      </div>

      {/* Navigation Menu Grid */}
      <div className="relative z-10 flex flex-col gap-2.5 w-full mb-3">
        {/* Levels Grid Selector */}
        <button
          onClick={onOpenLevels}
          className="p-3.5 px-4 rounded-2xl bg-[#0E172A]/80 border border-slate-700/70 hover:border-cyan-500/40 text-slate-200 flex items-center justify-between transition-all active:scale-98 cursor-pointer"
        >
          <div className="flex items-center gap-3">
            <div className="p-2 rounded-xl bg-cyan-950/60 border border-cyan-500/30 text-cyan-400">
              <List className="w-4 h-4" />
            </div>
            <div className="flex flex-col text-left">
              <span className="text-xs font-bold font-mono tracking-wider">CAMPAIGN MATRIX</span>
              <span className="text-[10px] text-slate-400">50 Progressive Handcrafted & Procedural Levels</span>
            </div>
          </div>
          <ChevronRight className="w-4 h-4 text-slate-500" />
        </button>

        {/* Daily Vector */}
        <button
          onClick={onOpenDaily}
          className="p-3.5 px-4 rounded-2xl bg-[#0E172A]/80 border border-slate-700/70 hover:border-cyan-500/40 text-slate-200 flex items-center justify-between transition-all active:scale-98 cursor-pointer"
        >
          <div className="flex items-center gap-3">
            <div className="p-2 rounded-xl bg-violet-950/60 border border-violet-500/30 text-violet-400">
              <Calendar className="w-4 h-4" />
            </div>
            <div className="flex flex-col text-left">
              <span className="text-xs font-bold font-mono tracking-wider">DAILY VECTOR</span>
              <span className="text-[10px] text-slate-400">Offline Deterministic Calendar Challenge</span>
            </div>
          </div>
          <ChevronRight className="w-4 h-4 text-slate-500" />
        </button>

        {/* Practice Mode */}
        <button
          onClick={onOpenPractice}
          className="p-3.5 px-4 rounded-2xl bg-[#0E172A]/80 border border-slate-700/70 hover:border-cyan-500/40 text-slate-200 flex items-center justify-between transition-all active:scale-98 cursor-pointer"
        >
          <div className="flex items-center gap-3">
            <div className="p-2 rounded-xl bg-emerald-950/60 border border-emerald-500/30 text-emerald-400">
              <Sparkles className="w-4 h-4" />
            </div>
            <div className="flex flex-col text-left">
              <span className="text-xs font-bold font-mono tracking-wider">PRACTICE LAB</span>
              <span className="text-[10px] text-slate-400">Endless Solvable Puzzles (Easy to Expert)</span>
            </div>
          </div>
          <ChevronRight className="w-4 h-4 text-slate-500" />
        </button>
      </div>

      {/* Bottom Footer Toolbar */}
      <div className="relative z-10 flex items-center justify-between pt-2 border-t border-slate-800/80">
        {/* Settings button */}
        <button
          onClick={onOpenSettings}
          className="flex items-center gap-1.5 py-2 px-3 rounded-xl bg-[#0B1322] border border-slate-800 text-slate-400 hover:text-slate-200 hover:border-slate-700 text-xs font-mono transition-all active:scale-95"
        >
          <Settings className="w-4 h-4" />
          <span>SETTINGS</span>
        </button>

        {/* Android Native Code & Tests Inspector */}
        <button
          onClick={onOpenInspector}
          className="flex items-center gap-1.5 py-2 px-3 rounded-xl bg-cyan-950/40 border border-cyan-500/30 text-cyan-300 hover:bg-cyan-900/40 text-xs font-mono transition-all active:scale-95 shadow-[0_0_12px_rgba(0,240,255,0.15)]"
        >
          <Code className="w-4 h-4 text-cyan-400" />
          <span>ANDROID / TESTS</span>
        </button>
      </div>
    </div>
  );
};
