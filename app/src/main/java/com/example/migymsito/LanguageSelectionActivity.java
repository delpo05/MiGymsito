package com.example.migymsito;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class LanguageSelectionActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AppConfig";
    private static final String KEY_LANGUAGE = "selected_language";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.language_selection_activity);

        findViewById(R.id.btnSpanish).setOnClickListener(v -> selectLanguage("es"));
        findViewById(R.id.btnEnglish).setOnClickListener(v -> selectLanguage("en"));
    }

    private void selectLanguage(String langCode) {
        // Guardar preferencia
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, langCode).apply();

        // Cambiar locale
        updateLocale(langCode);

        // Ir al tutorial
        Intent intent = new Intent(this, TutorialActivity.class);
        startActivity(intent);
        finish();
    }

    private void updateLocale(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
}
