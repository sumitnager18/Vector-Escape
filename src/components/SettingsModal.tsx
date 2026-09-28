import React, { useState } from 'react';
import { X, Volume2, VolumeX, Vibrate, Eye, AlertTriangle, RotateCcw, Check } from 'lucide-react';
import { GameSettingsData } from '../storage/StorageManager';

interface SettingsModalProps {
  settings: GameSettingsData;
  onUpdateSettings: (newSettings: Partial<GameSettingsData>) => void;
  onResetProgress: () => void;
  onClose: () => void;
}

export const SettingsModal: React.FC<SettingsModalProps> = ({
  settings,
  onUpdateSettings,
  onResetProgress,
  onClose
}) => {
  const [showConfirmReset, setShowConfirmReset] = useState(false);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-in fade-in duration-200">
      <div className="w-full max-w-sm rounded-3xl p-6 bg-gradient-to-b from-[#111A2E] to-[#0A101D] border border-cyan-500/30 shadow-[0_20px_50px_rgba(0,0,0,0.9),0_0_30px_rgba(0,240,255,0.15)] flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-cyan-500/20 mb-5">
          <h2 className="text-base font-bold tracking-widest text-slate-100 uppercase">
            SYSTEM SETTINGS
          </h2>
          <button
            onClick={onClose}
            aria-label="Close settings"
            className="p-2 rounded-xl text-slate-400 hover:text-slate-100 hover:bg-slate-800 transition-all outline-none"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Settings List */}
        <div className="flex flex-col gap-4 mb-6">
          {/* Sound Toggle */}
          <div className="flex items-center justify-between p-3.5 rounded-2xl bg-[#091120] border border-slate-800">
            <div className="flex items-center gap-3">
              {settings.soundEnabled ? (
                <Volume2 className="w-5 h-5 text-cyan-400" />
              ) : (
                <VolumeX className="w-5 h-5 text-slate-500" />
              )}
              <div className="flex flex-col">
                <span className="text-sm font-semibold text-slate-200">Audio Synthesizer</span>
                <span className="text-[11px] text-slate-400">Procedural tones & whooshes</span>
              </div>
            </div>
            <button
              onClick={() => onUpdateSettings({ soundEnabled: !settings.soundEnabled })}
              className={`w-12 h-7 rounded-full p-1 transition-colors ${
                settings.soundEnabled ? 'bg-cyan-500' : 'bg-slate-800'
              }`}
            >
              <div
                className={`w-5 h-5 rounded-full bg-slate-950 transition-transform ${
                  settings.soundEnabled ? 'translate-x-5' : 'translate-x-0'
                }`}
              />
            </button>
          </div>

          {/* Haptics Toggle */}
          <div className="flex items-center justify-between p-3.5 rounded-2xl bg-[#091120] border border-slate-800">
            <div className="flex items-center gap-3">
              <Vibrate className={`w-5 h-5 ${settings.hapticsEnabled ? 'text-violet-400' : 'text-slate-500'}`} />
              <div className="flex flex-col">
                <span className="text-sm font-semibold text-slate-200">Haptic Vibration</span>
                <span className="text-[11px] text-slate-400">Tactile physical impulses</span>
              </div>
            </div>
            <button
              onClick={() => onUpdateSettings({ hapticsEnabled: !settings.hapticsEnabled })}
              className={`w-12 h-7 rounded-full p-1 transition-colors ${
                settings.hapticsEnabled ? 'bg-violet-500' : 'bg-slate-800'
              }`}
            >
              <div
                className={`w-5 h-5 rounded-full bg-slate-950 transition-transform ${
                  settings.hapticsEnabled ? 'translate-x-5' : 'translate-x-0'
                }`}
              />
            </button>
          </div>

          {/* Reduced Motion Toggle */}
          <div className="flex items-center justify-between p-3.5 rounded-2xl bg-[#091120] border border-slate-800">
            <div className="flex items-center gap-3">
              <Eye className={`w-5 h-5 ${settings.reducedMotion ? 'text-emerald-400' : 'text-slate-500'}`} />
              <div className="flex flex-col">
                <span className="text-sm font-semibold text-slate-200">Reduced Motion</span>
                <span className="text-[11px] text-slate-400">Minimize flight trajectories</span>
              </div>
            </div>
            <button
              onClick={() => onUpdateSettings({ reducedMotion: !settings.reducedMotion })}
              className={`w-12 h-7 rounded-full p-1 transition-colors ${
                settings.reducedMotion ? 'bg-emerald-500' : 'bg-slate-800'
              }`}
            >
              <div
                className={`w-5 h-5 rounded-full bg-slate-950 transition-transform ${
                  settings.reducedMotion ? 'translate-x-5' : 'translate-x-0'
                }`}
              />
            </button>
          </div>
        </div>

        {/* Reset Progress Section */}
        <div className="pt-2 border-t border-slate-800/80">
          {!showConfirmReset ? (
            <button
              onClick={() => setShowConfirmReset(true)}
              className="w-full py-3 px-4 rounded-2xl bg-red-950/20 border border-red-500/20 hover:border-red-500/40 text-red-400 text-xs font-mono flex items-center justify-center gap-2 active:scale-95 transition-all"
            >
              <RotateCcw className="w-4 h-4" />
              <span>RESET CAMPAIGN PROGRESS</span>
            </button>
          ) : (
            <div className="flex flex-col gap-2 p-3 rounded-2xl bg-red-950/40 border border-red-500/40 animate-in fade-in">
              <div className="flex items-center gap-2 text-xs font-mono text-red-300">
                <AlertTriangle className="w-4 h-4 flex-shrink-0 text-red-400" />
                <span>Erase all 50 level stars and unlocked progress?</span>
              </div>
              <div className="flex items-center gap-2 mt-1">
                <button
                  onClick={() => {
                    onResetProgress();
                    setShowConfirmReset(false);
                  }}
                  className="flex-1 py-2 px-3 rounded-xl bg-red-600 hover:bg-red-500 text-white font-mono text-xs font-bold flex items-center justify-center gap-1 active:scale-95 transition-all"
                >
                  <Check className="w-3.5 h-3.5" />
                  <span>CONFIRM RESET</span>
                </button>
                <button
                  onClick={() => setShowConfirmReset(false)}
                  className="flex-1 py-2 px-3 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 font-mono text-xs active:scale-95 transition-all"
                >
                  CANCEL
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
