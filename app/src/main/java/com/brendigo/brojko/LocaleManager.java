package com.brendigo.brojko;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.text.TextUtils;
import android.view.View;
import androidx.annotation.RequiresApi;
import java.util.Locale;

/** Single source of truth for app languages; never expose incomplete translations. */
final class LocaleManager {
    static final String PREFS="brojko_language";
    static final String KEY_TAG="language_tag";
    private static final String KEY_PLATFORM_MIGRATED="platform_migrated";

    enum Language {
        ENGLISH("en","English"), CROATIAN("hr","Hrvatski"),
        GERMAN("de","Deutsch"), FRENCH("fr","Français"),
        ARABIC("ar","العربية"), SPANISH("es","Español"),
        HEBREW("he","עברית"), ITALIAN("it","Italiano"),
        PORTUGUESE_BRAZIL("pt-BR","Português (Brasil)"),
        SLOVENIAN("sl","Slovenščina"), POLISH("pl","Polski"), CZECH("cs","Čeština"),
        JAPANESE("ja","日本語"), KOREAN("ko","한국어"),
        CHINESE_SIMPLIFIED("zh-Hans","简体中文"),
        SLOVAK("sk","Slovenčina"), DUTCH("nl","Nederlands"), UKRAINIAN("uk","Українська");

        final String tag;
        final String nativeName;
        Language(String tag,String nativeName){this.tag=tag;this.nativeName=nativeName;}
        Locale locale(){return Locale.forLanguageTag("hr".equals(tag)?"hr-HR":"en".equals(tag)?"en-US":tag);}
    }

    private LocaleManager() {}

    static Locale legacyUiLocale(){return Locale.forLanguageTag("hr-HR");}
    static boolean usesCroatianArtwork(Locale locale){return "hr".equals(locale.getLanguage());}
    static boolean isRtl(Locale locale){return TextUtils.getLayoutDirectionFromLocale(locale)==View.LAYOUT_DIRECTION_RTL;}

    static Language findLanguage(String tag){
        if(tag==null||tag.isEmpty())return null;
        Locale requested=Locale.forLanguageTag(tag);
        for(Language language:Language.values())
            if(language.locale().getLanguage().equals(requested.getLanguage()))return language;
        return null;
    }

    static Language languageOrEnglish(String tag){
        Language language=findLanguage(tag);
        return language==null?Language.ENGLISH:language;
    }

    private static SharedPreferences preferences(Context context){
        return context.getSharedPreferences(PREFS,Context.MODE_PRIVATE);
    }

    /** On OS upgrade, migrate an explicit pre-33 selection once. */
    static void migratePlatformPreference(Context context){
        if(Build.VERSION.SDK_INT<33)return;
        SharedPreferences prefs=preferences(context);
        if(prefs.getBoolean(KEY_PLATFORM_MIGRATED,false))return;
        Language saved=findLanguage(prefs.getString(KEY_TAG,""));
        if(saved!=null&&Platform.tag(context).isEmpty()&&!Platform.set(context,saved.tag))return;
        prefs.edit().putBoolean(KEY_PLATFORM_MIGRATED,true).apply();
    }

    static Language selected(Context context){
        // Android Settings is authoritative on API 33+. An empty selection
        // intentionally means the product's English default, not device locale.
        if(Build.VERSION.SDK_INT>=33)return languageOrEnglish(Platform.tag(context));
        return languageOrEnglish(preferences(context).getString(KEY_TAG,""));
    }

    static boolean select(Context context,Language language){
        if(language==null)return false;
        if(Build.VERSION.SDK_INT>=33)return Platform.set(context,language.tag);
        return preferences(context).edit().putString(KEY_TAG,language.tag).commit();
    }

    static Resources resourcesFor(Context context,Locale locale){
        Configuration configuration=new Configuration(context.getResources().getConfiguration());
        configuration.setLocales(new LocaleList(locale));
        configuration.setLayoutDirection(locale);
        return context.createConfigurationContext(configuration).getResources();
    }

    @RequiresApi(33)
    private static final class Platform {
        static String tag(Context context){
            android.app.LocaleManager manager=context.getSystemService(android.app.LocaleManager.class);
            if(manager==null)return "";
            LocaleList locales=manager.getApplicationLocales();
            return locales.isEmpty()?"":locales.get(0).toLanguageTag();
        }
        static boolean set(Context context,String tag){
            android.app.LocaleManager manager=context.getSystemService(android.app.LocaleManager.class);
            if(manager==null)return false;
            manager.setApplicationLocales(LocaleList.forLanguageTags(tag));
            return true;
        }
    }
}
