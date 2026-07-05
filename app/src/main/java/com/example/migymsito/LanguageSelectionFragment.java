package com.example.migymsito;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import java.util.Locale;

public class LanguageSelectionFragment extends Fragment {

    private static final String PREFS_NAME = "AppConfig";
    private static final String KEY_LANGUAGE = "selected_language";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.language_selection_activity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        View toolbar = getActivity().findViewById(R.id.include_toolbar);
        if (toolbar != null) toolbar.setVisibility(View.GONE);

        view.findViewById(R.id.btnSpanish).setOnClickListener(v -> selectLanguage("es"));
        view.findViewById(R.id.btnEnglish).setOnClickListener(v -> selectLanguage("en"));
    }

    private void selectLanguage(String langCode) {
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, langCode).apply();

        updateLocale(langCode);

        Navigation.findNavController(requireView()).navigate(R.id.tutorialFragment);
    }

    private void updateLocale(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
        
        // Restart activity to apply language changes globally
        getActivity().recreate();
    }
}
