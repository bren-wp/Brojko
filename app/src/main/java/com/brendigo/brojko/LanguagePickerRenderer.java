package com.brendigo.brojko;

/** Canvas language page; no preference or session mutation occurs in the renderer. */
final class LanguagePickerRenderer {
    private static final int PAGE_SIZE=4;
    private int pageIndex=-1;
    void render(MainActivity.BoardScreen board,MainActivity activity){
        boolean compact=board.referencePortraitLayout;
        board.title(board.uiText(R.string.settings_language));
        board.fitted(board.uiText(R.string.language_interface),200,174,21,340,board.muted,true);
        board.fitted(activity.uiLanguage.nativeName,200,204,22,340,board.ink,true);
        board.button(board.uiText(R.string.language_search),"globe",25,222,350,49,board.card,false,
            ()->LanguageSearchDialog.show(activity,board.localizedResources,activity::selectLanguage));
        LocaleManager.Language[] languages=LocaleManager.Language.values();
        int pages=(languages.length+PAGE_SIZE-1)/PAGE_SIZE;
        if(pageIndex<0)pageIndex=activity.uiLanguage.ordinal()/PAGE_SIZE;
        pageIndex=Math.max(0,Math.min(pageIndex,pages-1));
        int first=pageIndex*PAGE_SIZE;
        for(int i=first;i<Math.min(first+PAGE_SIZE,languages.length);i++){
            LocaleManager.Language language=languages[i];
            float y=(compact?278:286)+(i-first)*(compact?68:79);
            float h=compact?58:68;
            boolean selected=language==activity.uiLanguage;
            board.card(25,y,350,h,20,board.card);
            if(selected)board.icon("check",62,y+h/2,27,MainActivity.TEAL);
            else board.icon("globe",62,y+h/2,23,board.muted);
            board.leftFitted(language.nativeName,101,y+h/2+8,23,225,board.ink,true);
            String label=selected?board.localizedResources.getString(R.string.language_selected,language.nativeName):language.nativeName;
            board.taps.add(board.new Tap(25,y,375,y+h,label,()->activity.selectLanguage(language)));
        }
        if(pageIndex>0)board.button(board.uiText(R.string.language_previous),"back",25,compact?560:624,165,58,board.card,false,
            ()->{pageIndex--;board.invalidate();});
        if(pageIndex+1<pages)board.button(board.uiText(R.string.language_next),"chevron-right",210,compact?560:624,165,58,board.card,false,
            ()->{pageIndex++;board.invalidate();});
        board.button(board.uiText(R.string.language_back),"back",25,compact?632:720,350,compact?66:70,MainActivity.CORAL,true,()->activity.navigate("settings"));
    }
}
