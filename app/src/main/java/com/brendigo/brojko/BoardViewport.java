package com.brendigo.brojko;

/** Pure viewport policy; rendering and activity state remain outside this class. */
final class BoardViewport {
    final boolean referencePortraitLayout,compactBoardLayout;
    final float gridY,scale,maxScroll,scrollOffset,offX,offY;
    private BoardViewport(boolean referencePortraitLayout,boolean compactBoardLayout,
            float gridY,float scale,float maxScroll,float scrollOffset,float offX,float offY){
        this.referencePortraitLayout=referencePortraitLayout;
        this.compactBoardLayout=compactBoardLayout;this.gridY=gridY;this.scale=scale;
        this.maxScroll=maxScroll;this.scrollOffset=scrollOffset;this.offX=offX;this.offY=offY;
    }

    private static boolean compactDetail(String page){
        return "language".equals(page)||"help".equals(page)||"backup".equals(page)||
            "privacy".equals(page)||"about".equals(page)||"historyDetail".equals(page);
    }

    static BoardViewport calculate(String page,String gameMode,int gameSize,float usableWidth,
            float usableHeight,int paddingLeft,int paddingTop,float density,boolean accessibleHistory,
            int historyCount,float scrollOffset,boolean pageChanged){
        boolean inGame=page.equals("game")||page.equals("pause")||
            page.equals("win")||page.equals("over")||page.equals("confirm")||
            page.equals("confirmEnd")||page.equals("dailyWin")||page.equals("dailyFail");
        // Themes has a compact bottom navigation within the 880-unit design.
        // Fit it to the available window INCLUDING system bar insets; only
        // genuinely taller content (statistics/history) must always scroll.
        // These four screens have supplied, distinct owner reference
        // compositions at 941x1672 (400x710 design units). Tall portrait
        // phones must not switch to the unrelated 880-unit arrangement:
        // it substantially changes the Stats chart/history geometry and
        // Themes cards. Preserve the source Canvas composition and
        // center it on the available viewport, without stretching.
        // All other pages retain their own existing breakpoints.
        // Three additional supplied owner references are fully drawn
        // on the 400x710 portrait plane. Tall phones previously
        // switched them to an unrelated 880-unit layout, changing
        // the Missions cards, Daily challenge and Classic gameplay.
        // Scope Classic to the actual 4x4 mode: 5x5 and Daily gameplay
        // keep their own independent responsive behavior.
        boolean ownerClassicGame=page.equals("game")&&gameSize>0&&
            "classic".equals(gameMode)&&gameSize==4;
        boolean tallOwnerPortrait=(page.equals("splash")||
            page.equals("home")||page.equals("themes")||
            page.equals("stats")||page.equals("missions")||
            page.equals("daily")||ownerClassicGame)&&
            usableHeight>usableWidth&&usableHeight/usableWidth>=1.85f;
        boolean referencePortraitLayout=
            usableHeight/usableWidth<1.85f||tallOwnerPortrait;
        boolean compactBoardLayout=inGame&&referencePortraitLayout;
        float gridY=compactBoardLayout&&gameSize>0&&gameSize==4?159:(compactBoardLayout?171:222);
        boolean extended=page.equals("stats");
        // Keep buttons touch-sized on 16:9 screens; permit scrolling
        // outside the board rather than scaling the whole game to tiny size.
        // Splash uses its own width-fitted short-portrait arrangement.
        // Using the 880-unit tall layout shrank the reference by ~19%.
        // Decorative edge tiles may be cropped, but logo/slogan must fit.
        boolean compact=referencePortraitLayout||
            (!page.equals("splash")&&extended);
        // Landscape phones and resizable tablets must keep the whole
        // interaction region reachable; portrait stays pixel-identical.
        boolean landscape=usableWidth>usableHeight;
        // Landscape Splash must fit the complete 880-unit scene.
        // Other landscape screens retain touch-sized controls and scroll
        // if their 880-unit content is taller than the available window.
        // Landscape Home uses larger touch-friendly shortcut cards.
        // It can scroll vertically instead of shrinking the buttons.
        float landscapeScale=Math.min(usableWidth/400f,
            usableHeight/(page.equals("splash")?880f:
                page.equals("home")?580f:720f));
        if(landscape&&page.equals("home")){
            // Very short split-screen and foldable windows previously made
            // all four 88-unit Home shortcuts too small to touch. Grow
            // them toward 48 dp, but never crop the 400-unit canvas or
            // make the 120-unit shortcut card taller than the viewport.
            // Vertical scrolling, not horizontal clipping, absorbs the
            // extra height. Portrait reference pixels are unchanged.
            float desiredTouchScale=48f*density/88f;
            landscapeScale=Math.min(usableWidth/400f,
                Math.min(usableHeight/120f,
                    Math.max(landscapeScale,desiredTouchScale)));
        }
        float scale=landscape?landscapeScale:
            (compact?usableWidth/400f:
                Math.min(usableWidth/400f,usableHeight/880f));
        // A 320x480 compact phone cannot fit the 628-unit slogan with
        // pure width scaling. Cap only Splash; the 941x1672 design
        // reference and its native positions remain unchanged.
        if(page.equals("splash")&&compact)
            scale=Math.min(scale,usableHeight/640f);
        float contentHeight=page.equals("stats")?
            (referencePortraitLayout?
                (accessibleHistory?
                    Math.max(710f,545f+Math.min(5,historyCount)*48f):
                    710f)
                :1120f):
            // All Home shortcuts end at y=690 plus their card shadows.
            // The old 880-unit height caused ~170 units of empty scrolling
            // at the original 941x1672 reference viewport. Preserve real
            // scrolling on shorter devices without cropping any shortcut.
            ((page.equals("pause")||page.equals("confirm")||
                page.equals("confirmEnd"))&&referencePortraitLayout?654f:
            (page.equals("over")&&referencePortraitLayout?705f:
            ((page.equals("dailyWin")||page.equals("dailyFail"))&&
                referencePortraitLayout?700f:
            (page.equals("home")&&referencePortraitLayout?710f:
            ((page.equals("settings")||page.equals("themes")||
                page.equals("missions")||page.equals("daily"))&&
                referencePortraitLayout?710f:880f)))));
        // The owner Classic 4x4 artwork ends at y~671 of its
        // 710-unit reference scene. The inherited 880-unit content
        // height caused a phantom 169-unit scroll on 941x1672,
        // despite all gameplay actions already fitting. Keep real
        // overflow scroll on genuinely short screens.
        if((ownerClassicGame||compactDetail(page))&&referencePortraitLayout)
            contentHeight=710f;
        // Splash must never scroll; its compact lettering fits a 360x600
        // viewport while longer non-splash pages still scroll as needed.
        float maxScroll=page.equals("splash")?0:
            ((compact||landscape)?
                Math.max(0,contentHeight-usableHeight/scale):0);
        if(pageChanged)scrollOffset=0;
        scrollOffset=Math.max(0,Math.min(maxScroll,scrollOffset));
        float offX=paddingLeft+(usableWidth-400*scale)/2f;
        float offY=(compact||landscape&&!page.equals("splash"))?
            paddingTop-scrollOffset*scale:
            paddingTop+(usableHeight-880*scale)/2f;
        // Reference 941x1672 is unchanged (tallOwnerPortrait=false).
        // On taller portrait phones, center the full 710-unit source
        // scene when it fits. Compact split-screen and short windows
        // continue to scroll instead of clipping interactive controls.
        if(tallOwnerPortrait&&maxScroll==0f)
            offY+=Math.max(0f,(usableHeight-710f*scale)/2f);
        return new BoardViewport(referencePortraitLayout,compactBoardLayout,gridY,scale,
            maxScroll,scrollOffset,offX,offY);
    }
}
