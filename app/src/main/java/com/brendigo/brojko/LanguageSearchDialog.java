package com.brendigo.brojko;

import android.app.AlertDialog;
import android.content.res.Resources;
import android.text.Editable;
import android.text.InputFilter;
import android.view.View;
import java.text.Normalizer;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Locale;

/** Native keyboard and accessible filtered list for the Canvas language picker. */
final class LanguageSearchDialog {
    interface Selection { void choose(LocaleManager.Language language); }

    private LanguageSearchDialog() {}

    static String normalized(String value){
        return Normalizer.normalize(value,Normalizer.Form.NFD)
            .replaceAll("\\p{M}+","").trim().toLowerCase(Locale.ROOT);
    }

    static void show(MainActivity activity,Resources resources,Selection selection){
        LinearLayout content=new LinearLayout(activity);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setLayoutDirection(LocaleManager.isRtl(activity.uiLocale)?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);
        content.setTextDirection(View.TEXT_DIRECTION_LOCALE);
        int padding=(int)(16*resources.getDisplayMetrics().density);
        content.setPadding(padding,0,padding,padding);
        EditText search=new EditText(activity);
        search.setSingleLine(true);
        search.setFilters(new InputFilter[]{new InputFilter.LengthFilter(80)});
        search.setHint(resources.getString(R.string.language_search));
        search.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        content.addView(search,new LinearLayout.LayoutParams(-1,-2));
        ListView list=new ListView(activity);
        TextView empty=new TextView(activity);
        empty.setText(resources.getString(R.string.language_no_results));
        empty.setPadding(0,padding,0,padding);
        content.addView(empty,new LinearLayout.LayoutParams(-1,-2));
        content.addView(list,new LinearLayout.LayoutParams(-1,
            Math.round(220*resources.getDisplayMetrics().density)));
        list.setEmptyView(empty);
        ArrayList<LocaleManager.Language> matches=new ArrayList<>();
        ArrayAdapter<String> adapter=new ArrayAdapter<>(activity,
            android.R.layout.simple_list_item_1,new ArrayList<>());
        list.setAdapter(adapter);
        Runnable update=()->{
            String term=normalized(search.getText().toString());
            matches.clear();adapter.clear();
            for(LocaleManager.Language language:LocaleManager.Language.values()){
                String translated=language.locale().getDisplayLanguage(activity.uiLocale);
                if(normalized(language.nativeName).contains(term)||
                    normalized(translated).contains(term)||language.tag.contains(term)){
                    matches.add(language);adapter.add(language.nativeName);
                }
            }
            adapter.notifyDataSetChanged();
        };
        search.addTextChangedListener(new TextWatcher(){
            @Override public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            @Override public void onTextChanged(CharSequence s,int start,int before,int count){update.run();}
            @Override public void afterTextChanged(Editable s){}
        });
        AlertDialog dialog=new AlertDialog.Builder(activity)
            .setTitle(resources.getString(R.string.language_search))
            .setView(content)
            .setNegativeButton(resources.getString(R.string.action_cancel),null)
            .create();
        list.setOnItemClickListener((parent,view,position,id)->{
            if(position>=0&&position<matches.size()){
                LocaleManager.Language language=matches.get(position);
                dialog.dismiss();
                selection.choose(language);
            }
        });
        update.run();
        dialog.show();
    }
}
