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
 * Owner's two 941x1672 screens: native geometry and live actions.
 * Capture from the actual Canvas. Never substitute a reference bitmap.
 */
@RunWith(AndroidJUnit4.class)
public final class SplashHomeGeometryRegressionTest {
    @Rule public ActivityScenarioRule<MainActivity> rule =
        new ActivityScenarioRule<>(MainActivity.class);

    private Bitmap render(MainActivity activity, String page) {
        MainActivity.BoardScreen screen = activity.new BoardScreen(activity);
        screen.measure(View.MeasureSpec.makeMeasureSpec(941, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(1672, View.MeasureSpec.EXACTLY));
        screen.layout(0, 0, 941, 1672);
        activity.page = page;
        activity.theme = 0;
        Bitmap result = Bitmap.createBitmap(941, 1672, Bitmap.Config.ARGB_8888);
        screen.draw(new Canvas(result));
        if ("splash".equals(page)) {
            assertTrue("Splash is decorative, not clickable", screen.taps.isEmpty());
            assertEquals("Splash never scrolls", 0f, screen.maxScroll, 0f);
        } else {
            boolean classic = false, relaxed = false, daily = false, play = false;
            for (MainActivity.BoardScreen.Tap tap : screen.taps) {
                classic |= tap.label.contains("Klasično");
                relaxed |= tap.label.contains("Opušteno");
                daily |= tap.label.contains("Dnevni izazov");
                play |= tap.label.contains("Nova igra") || tap.label.contains("Nastavi igru");
            }
            assertTrue("All three real game-mode actions remain", classic && relaxed && daily);
            assertTrue("Live New Game / Resume action remains", play);
        }
        return result;
    }

    @Test public void splashPaintedCornerTilesKeepOwnerGeometry() {
        rule.getScenario().onActivity(activity -> {
            String previousPage = activity.page;
            int previousTheme = activity.theme;
            Bitmap image = null;
            try {
                image = render(activity, "splash");
                // These source pixels lie outside the coloured tile faces.
                // Before the localized fixes both were filled by oversized tiles.
                int upperCoralOutside = image.getPixel(240, 135);
                int tealRightOutside = image.getPixel(811, 310);
                assertTrue("Coral 2 face must start lower, like the reference",
                    Color.green(upperCoralOutside) > 208);
                assertTrue("Teal 4 must not overshoot its right source edge",
                    Color.green(tealRightOutside) > 225);
                int coralInside = image.getPixel(200, 270);
                int tealInside = image.getPixel(600, 280);
                assertTrue("Raised coral 2 stays visible",
                    Color.red(coralInside) > Color.green(coralInside) + 45);
                assertTrue("Raised turquoise 4 stays visible",
                    Color.green(tealInside) > Color.red(tealInside) + 40);
            } finally {
                activity.page = previousPage;
                activity.theme = previousTheme;
                if (image != null) image.recycle();
            }
        });
    }

    @Test public void homeClassicMiniatureKeepsSourcePitchAndLiveControls() {
        rule.getScenario().onActivity(activity -> {
            String previousPage = activity.page;
            int previousTheme = activity.theme;
            Bitmap image = null;
            try {
                image = render(activity, "home");
                // User's first coral cell reaches native x121 at y973.
                // Former 59px tile pitch left this pixel on blank card.
                int firstTileEdge = image.getPixel(121, 973);
                assertTrue("Classic thumbnail left cell must reach source x121",
                    Color.red(firstTileEdge) > 210
                    && Color.green(firstTileEdge) < 200
                    && Color.blue(firstTileEdge) < 160);
                // The warm card gutter between tiles is still present.
                int gutter = image.getPixel(125, 970);
                assertTrue("Classic cells remain visibly separated",
                    Color.red(gutter) > 210 && Color.green(gutter) > 190);
            } finally {
                activity.page = previousPage;
                activity.theme = previousTheme;
                if (image != null) image.recycle();
            }
        });
    }
}
