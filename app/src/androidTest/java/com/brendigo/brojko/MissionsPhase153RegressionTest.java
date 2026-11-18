package com.brendigo.brojko;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Phase 153: actual Android Canvas renders, not substituted reference images.
 * The owner's PNG is private, so these are functional and material regression
 * checks; they do not claim pixel-perfect equivalence.
 */
@RunWith(AndroidJUnit4.class)
public final class MissionsPhase153RegressionTest {
    @Rule public ActivityScenarioRule<MainActivity> rule =
        new ActivityScenarioRule<>(MainActivity.class);

    private static Bitmap draw(MainActivity activity, int width, int height) {
        MainActivity.BoardScreen view = activity.new BoardScreen(activity);
        view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
        view.layout(0, 0, width, height);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(image));
        boolean back = false;
        for (MainActivity.BoardScreen.Tap tap : view.taps) {
            if ("Natrag".equals(tap.label)) back = true;
        }
        assertTrue("Missions must retain accessible Back at " +
            width + "x" + height, back);
        return image;
    }

    @Test public void allThemesAndViewportsKeepRealProgressUnmodified() {
        rule.getScenario().onActivity(activity -> {
            String oldPage = activity.page;
            int oldTheme = activity.theme;
            TileGame originalGame = activity.game;
            int merges = activity.todayMerges;
            int points = activity.weekPoints;
            int record = activity.recordTile;
            int best = activity.best;
            int stars = activity.stars;
            int flags = activity.rewardFlags;
            try {
                activity.page = "missions";
                int[][] sizes = {{320,480},{360,800},{941,1672},{1080,2400}};
                for (int theme = 0; theme < 3; theme++) {
                    activity.theme = theme;
                    for (int[] size : sizes) {
                        Bitmap image = draw(activity, size[0], size[1]);
                        image.recycle();
                        assertSame("Rendering cannot replace saved game",originalGame,activity.game);
                        assertEquals("Daily mission counter changed",merges,activity.todayMerges);
                        assertEquals("Weekly mission points changed",points,activity.weekPoints);
                        assertEquals("Record tile changed",record,activity.recordTile);
                        assertEquals("Best score changed",best,activity.best);
                        assertEquals("Currency changed",stars,activity.stars);
                        assertEquals("Reward flags changed",flags,activity.rewardFlags);
                    }
                }
            } finally {
                activity.page = oldPage;
                activity.theme = oldTheme;
                activity.updateBars();
                activity.screen.invalidate();
            }
        });
    }

    @Test public void sixReferencePortraitGoldStarsHaveFaceAndRelief() {
        rule.getScenario().onActivity(activity -> {
            String oldPage = activity.page;
            int oldTheme = activity.theme;
            try {
                activity.page = "missions";
                activity.theme = 0;
                Bitmap image = draw(activity, 941, 1672);
                try {
                    // These broad bands are based on already audited positions.
                    // No owner-image RGB MAE is inferred from this Android-only test.
                    int[][] rows = {{415,510},{610,695},{890,975},
                        {1065,1155},{1340,1460},{1520,1630}};
                    for (int row = 0; row < rows.length; row++) {
                        int face = 0, relief = 0;
                        for (int y = rows[row][0]; y < rows[row][1]; y++) {
                            for (int x = 775; x < 865; x++) {
                                int rgb = image.getPixel(x, y);
                                int red=Color.red(rgb), green=Color.green(rgb),
                                    blue=Color.blue(rgb);
                                if (red >= 245 && green >= 175 && blue <= 125) face++;
                                if (red >= 205 && green >= 100 && green <= 170 &&
                                    blue <= 95) relief++;
                            }
                        }
                        assertTrue("Gold gradient face missing at reward " + row,face > 12);
                        assertTrue("Gold relief missing at reward " + row,relief > 12);
                    }
                } finally {
                    image.recycle();
                }
            } finally {
                activity.page = oldPage;
                activity.theme = oldTheme;
                activity.updateBars();
                activity.screen.invalidate();
            }
        });
    }
}
