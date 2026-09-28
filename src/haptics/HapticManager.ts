/**
 * Haptic Feedback Manager using Navigator Vibration API.
 */
export class HapticManager {
  private static enabled: boolean = true;

  static setEnabled(enabled: boolean) {
    this.enabled = enabled;
  }

  static isEnabled(): boolean {
    return this.enabled;
  }

  static lightTap() {
    if (!this.enabled || typeof navigator === 'undefined' || !navigator.vibrate) return;
    try {
      navigator.vibrate(12);
    } catch {
      // Ignore
    }
  }

  static success() {
    if (!this.enabled || typeof navigator === 'undefined' || !navigator.vibrate) return;
    try {
      navigator.vibrate(25);
    } catch {
      // Ignore
    }
  }

  static blocked() {
    if (!this.enabled || typeof navigator === 'undefined' || !navigator.vibrate) return;
    try {
      navigator.vibrate([40, 30, 50]);
    } catch {
      // Ignore
    }
  }

  static levelComplete() {
    if (!this.enabled || typeof navigator === 'undefined' || !navigator.vibrate) return;
    try {
      navigator.vibrate([30, 40, 50, 40, 90]);
    } catch {
      // Ignore
    }
  }
}
