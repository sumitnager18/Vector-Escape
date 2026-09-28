import { SoundManager } from '../audio/SoundManager';
import { HapticManager } from '../haptics/HapticManager';

export interface GameSettingsData {
  soundEnabled: boolean;
  hapticsEnabled: boolean;
  reducedMotion: boolean;
  hapticIntensity: 'soft' | 'medium';
}

export interface LevelRecord {
  stars: number;
  bestMoves: number;
  completedAt: string;
}

export interface DailyRecord {
  date: string;
  completed: boolean;
  stars: number;
  moves: number;
}

export interface PlayerProgressData {
  unlockedLevel: number;
  currentCampaignLevel: number;
  levelRecords: Record<number, LevelRecord>;
  dailyRecords: Record<string, DailyRecord>;
  totalStars: number;
  settings: GameSettingsData;
}

const STORAGE_KEY = 'vector_escape_player_data_v1';

export class StorageManager {
  private static defaultData: PlayerProgressData = {
    unlockedLevel: 1,
    currentCampaignLevel: 1,
    levelRecords: {},
    dailyRecords: {},
    totalStars: 0,
    settings: {
      soundEnabled: true,
      hapticsEnabled: true,
      reducedMotion: false,
      hapticIntensity: 'soft'
    }
  };

  static load(): PlayerProgressData {
    if (typeof window === 'undefined') return { ...this.defaultData };

    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) {
        this.save(this.defaultData);
        return { ...this.defaultData };
      }
      const parsed = JSON.parse(raw);
      // Synchronize audio and haptic systems
      if (parsed.settings) {
        SoundManager.setEnabled(parsed.settings.soundEnabled ?? true);
        HapticManager.setEnabled(parsed.settings.hapticsEnabled ?? true);
      }
      return {
        ...this.defaultData,
        ...parsed,
        settings: {
          ...this.defaultData.settings,
          ...(parsed.settings || {})
        }
      };
    } catch {
      return { ...this.defaultData };
    }
  }

  static save(data: PlayerProgressData): void {
    if (typeof window === 'undefined') return;
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
    } catch {
      // Ignore quota errors
    }
  }

  static recordLevelClear(levelNumber: number, stars: number, moves: number): PlayerProgressData {
    const data = this.load();
    const existing = data.levelRecords[levelNumber];
    const prevStars = existing ? existing.stars : 0;
    const bestStars = Math.max(prevStars, stars);
    const bestMoves = existing ? Math.min(existing.bestMoves, moves) : moves;

    data.levelRecords[levelNumber] = {
      stars: bestStars,
      bestMoves,
      completedAt: new Date().toISOString()
    };

    // Unlock next level if this was the highest unlocked
    if (levelNumber >= data.unlockedLevel && levelNumber < 50) {
      data.unlockedLevel = levelNumber + 1;
    }
    data.currentCampaignLevel = Math.min(levelNumber + 1, 50);

    // Recompute total stars
    let total = 0;
    for (const rec of Object.values(data.levelRecords)) {
      total += rec.stars;
    }
    data.totalStars = total;

    this.save(data);
    return data;
  }

  static recordDailyClear(date: string, stars: number, moves: number): PlayerProgressData {
    const data = this.load();
    data.dailyRecords[date] = {
      date,
      completed: true,
      stars,
      moves
    };
    this.save(data);
    return data;
  }

  static updateSettings(settings: Partial<GameSettingsData>): PlayerProgressData {
    const data = this.load();
    data.settings = { ...data.settings, ...settings };
    if (settings.soundEnabled !== undefined) {
      SoundManager.setEnabled(settings.soundEnabled);
    }
    if (settings.hapticsEnabled !== undefined) {
      HapticManager.setEnabled(settings.hapticsEnabled);
    }
    this.save(data);
    return data;
  }

  static resetProgress(): PlayerProgressData {
    const fresh: PlayerProgressData = {
      ...this.defaultData,
      settings: this.load().settings // preserve user audio/haptics settings
    };
    this.save(fresh);
    return fresh;
  }
}
