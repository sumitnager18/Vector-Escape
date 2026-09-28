export type DirectionType = 'UP' | 'RIGHT' | 'DOWN' | 'LEFT';

export interface DirectionVector {
  dr: number;
  dc: number;
  angleDeg: number;
}

export const Direction = {
  UP: 'UP' as const,
  RIGHT: 'RIGHT' as const,
  DOWN: 'DOWN' as const,
  LEFT: 'LEFT' as const,

  getVector(dir: DirectionType): DirectionVector {
    switch (dir) {
      case 'UP':
        return { dr: -1, dc: 0, angleDeg: 0 };
      case 'RIGHT':
        return { dr: 0, dc: 1, angleDeg: 90 };
      case 'DOWN':
        return { dr: 1, dc: 0, angleDeg: 180 };
      case 'LEFT':
        return { dr: 0, dc: -1, angleDeg: 270 };
    }
  },

  getAll(): DirectionType[] {
    return ['UP', 'RIGHT', 'DOWN', 'LEFT'];
  }
};
