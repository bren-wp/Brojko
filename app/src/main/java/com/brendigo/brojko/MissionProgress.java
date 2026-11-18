package com.brendigo.brojko;

/**
 * Presentation-only normalization for mission counters.
 *
 * Gameplay, reward flags and persisted progress must keep their real values.
 * Invalid/negative imported progress is clamped only when drawing a row.
 */
final class MissionProgress {
    private MissionProgress() {}

    static int displayValue(int value, int target, boolean binaryMilestone) {
        if (target <= 0) return 0;
        if (binaryMilestone) return value >= target ? 1 : 0;
        return Math.max(0, Math.min(value, target));
    }

    static int displayTarget(int target, boolean binaryMilestone) {
        return binaryMilestone ? 1 : Math.max(0, target);
    }

    static float completionFraction(int value, int target, boolean binaryMilestone) {
        if (target <= 0) return 0f;
        if (binaryMilestone) return value >= target ? 1f : 0f;
        return (float) displayValue(value, target, false) / (float) target;
    }
}
