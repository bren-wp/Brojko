package com.brendigo.brojko;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** Display safeguards: never rewrite gameplay or saved mission state. */
public final class MissionProgressTest {
    @Test public void normalMissionProgressKeepsActualFractionAndCounter() {
        assertEquals(12, MissionProgress.displayValue(12, 20, false));
        assertEquals(20, MissionProgress.displayTarget(20, false));
        assertEquals(0.6f, MissionProgress.completionFraction(12, 20, false), 0.00001f);
        assertEquals(6250, MissionProgress.displayValue(6250, 10000, false));
        assertEquals(0.625f, MissionProgress.completionFraction(6250, 10000, false), 0.00001f);
    }

    @Test public void corruptedNegativeProgressNeverDrawsNegativeCounter() {
        assertEquals(0, MissionProgress.displayValue(-7, 20, false));
        assertEquals(0f, MissionProgress.completionFraction(-7, 20, false), 0f);
        assertEquals(0, MissionProgress.displayValue(-5, 1024, true));
    }

    @Test public void progressBeyondGoalIsCappedOnlyInPresentation() {
        assertEquals(20, MissionProgress.displayValue(25, 20, false));
        assertEquals(1f, MissionProgress.completionFraction(25, 20, false), 0f);
        assertEquals(10000, MissionProgress.displayValue(Integer.MAX_VALUE, 10000, false));
    }

    @Test public void tileAchievementIsBinaryNotHalfFilled() {
        assertEquals(0, MissionProgress.displayValue(512, 1024, true));
        assertEquals(0f, MissionProgress.completionFraction(512, 1024, true), 0f);
        assertEquals(1, MissionProgress.displayValue(1024, 1024, true));
        assertEquals(1, MissionProgress.displayValue(2048, 1024, true));
        assertEquals(1, MissionProgress.displayTarget(1024, true));
        assertEquals(1f, MissionProgress.completionFraction(2048, 1024, true), 0f);
    }

    @Test public void zeroOrInvalidTargetCannotDivideByZero() {
        assertEquals(0, MissionProgress.displayValue(50, 0, false));
        assertEquals(0, MissionProgress.displayTarget(-1, false));
        assertEquals(0f, MissionProgress.completionFraction(50, 0, false), 0f);
        assertEquals(0f, MissionProgress.completionFraction(50, -1, true), 0f);
    }
}
