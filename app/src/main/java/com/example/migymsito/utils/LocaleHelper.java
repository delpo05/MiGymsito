package com.example.migymsito.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class LocaleHelper {

    private static final String CONFIG_PREFS = "AppConfig";
    private static final String KEY_LANGUAGE = "selected_language";

    public static void applyLocale(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(CONFIG_PREFS, Context.MODE_PRIVATE);
        String lang = prefs.getString(KEY_LANGUAGE, null);
        if (lang != null) {
            Locale locale = new Locale(lang);
            Locale.setDefault(locale);
            Resources resources = context.getResources();
            Configuration config = resources.getConfiguration();
            config.setLocale(locale);
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }
    }
}
