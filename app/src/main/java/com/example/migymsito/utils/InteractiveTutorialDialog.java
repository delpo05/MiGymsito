package com.example.migymsito.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.migymsito.R;
import com.google.android.material.button.MaterialButton;

public class InteractiveTutorialDialog {

    public interface OnTutorialActionListener {
        void onCreateRoutineRequested();
        void onTutorialDismissed();
    }

    private static final String PREFS_NAME = "AppConfig";
    public static final String KEY_GUIDE_SEEN = "interactive_guide_seen";
    private static final int TOTAL_STEPS = 6;

    private final Context context;
    private final OnTutorialActionListener listener;
    private Dialog dialog;

    private ImageView ivSlideImage;
    private TextView tvSlideTitle;
    private TextView tvSlideDescription;
    private MaterialButton btnPrev;
    private MaterialButton btnNext;

    private View stepIndicator1;
    private View stepIndicator2;
    private View stepIndicator3;
    private View stepIndicator4;
    private View stepIndicator5;
    private View stepIndicator6;

    private int currentStep = 1;

    public InteractiveTutorialDialog(@NonNull Context context, OnTutorialActionListener listener) {
        this.context = context;
        this.listener = listener;
        initDialog();
    }

    private void initDialog() {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_interactive_tutorial);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }

        TextView btnSaltar = dialog.findViewById(R.id.btnSaltar);
        ivSlideImage = dialog.findViewById(R.id.ivSlideImage);
        tvSlideTitle = dialog.findViewById(R.id.tvSlideTitle);
        tvSlideDescription = dialog.findViewById(R.id.tvSlideDescription);
        btnPrev = dialog.findViewById(R.id.btnPrev);
        btnNext = dialog.findViewById(R.id.btnNext);

        stepIndicator1 = dialog.findViewById(R.id.stepIndicator1);
        stepIndicator2 = dialog.findViewById(R.id.stepIndicator2);
        stepIndicator3 = dialog.findViewById(R.id.stepIndicator3);
        stepIndicator4 = dialog.findViewById(R.id.stepIndicator4);
        stepIndicator5 = dialog.findViewById(R.id.stepIndicator5);
        stepIndicator6 = dialog.findViewById(R.id.stepIndicator6);

        if (btnSaltar != null) {
            btnSaltar.setOnClickListener(v -> dismissAndNotify());
        }

        if (btnPrev != null) {
            btnPrev.setOnClickListener(v -> {
                if (currentStep > 1) {
                    currentStep--;
                    updateStepUI();
                }
            });
        }

        if (btnNext != null) {
            btnNext.setOnClickListener(v -> {
                if (currentStep < TOTAL_STEPS) {
                    currentStep++;
                    updateStepUI();
                } else {
                    markGuideAsSeen();
                    dialog.dismiss();
                    if (listener != null) {
                        listener.onCreateRoutineRequested();
                    }
                }
            });
        }

        updateStepUI();
    }

    private void updateStepUI() {
        int activeColor = ContextCompat.getColor(context, R.color.blanco);
        int inactiveColor = ContextCompat.getColor(context, R.color.gris_oscuro_texto);

        if (stepIndicator1 != null) stepIndicator1.setBackgroundColor(currentStep >= 1 ? activeColor : inactiveColor);
        if (stepIndicator2 != null) stepIndicator2.setBackgroundColor(currentStep >= 2 ? activeColor : inactiveColor);
        if (stepIndicator3 != null) stepIndicator3.setBackgroundColor(currentStep >= 3 ? activeColor : inactiveColor);
        if (stepIndicator4 != null) stepIndicator4.setBackgroundColor(currentStep >= 4 ? activeColor : inactiveColor);
        if (stepIndicator5 != null) stepIndicator5.setBackgroundColor(currentStep >= 5 ? activeColor : inactiveColor);
        if (stepIndicator6 != null) stepIndicator6.setBackgroundColor(currentStep >= 6 ? activeColor : inactiveColor);

        if (btnPrev != null) btnPrev.setVisibility(currentStep > 1 ? View.VISIBLE : View.INVISIBLE);

        if (btnNext != null) {
            if (currentStep == TOTAL_STEPS) {
                btnNext.setText(R.string.entendido_a_entrenar);
            } else {
                btnNext.setText(R.string.siguiente);
            }
        }

        if (ivSlideImage != null && tvSlideTitle != null && tvSlideDescription != null) {
            switch (currentStep) {
                case 1:
                    ivSlideImage.clearColorFilter();
                    ivSlideImage.setImageResource(R.drawable.nueva_rutina);
                    tvSlideTitle.setText(R.string.guide_paso_1_titulo);
                    tvSlideDescription.setText(R.string.guide_paso_1_desc);
                    break;
                case 2:
                    ivSlideImage.clearColorFilter();
                    ivSlideImage.setImageResource(R.drawable.anadir_seccion);
                    tvSlideTitle.setText(R.string.guide_paso_2_titulo);
                    tvSlideDescription.setText(R.string.guide_paso_2_desc);
                    break;
                case 3:
                    ivSlideImage.clearColorFilter();
                    ivSlideImage.setImageResource(R.drawable.anadir_ejercicio);
                    tvSlideTitle.setText(R.string.guide_paso_3_titulo);
                    tvSlideDescription.setText(R.string.guide_paso_3_desc);
                    break;
                case 4:
                    ivSlideImage.clearColorFilter();
                    ivSlideImage.setImageResource(R.drawable.comparativa_rendimeinto);
                    tvSlideTitle.setText(R.string.guide_paso_4_titulo);
                    tvSlideDescription.setText(R.string.guide_paso_4_desc);
                    break;
                case 5:
                    ivSlideImage.clearColorFilter();
                    ivSlideImage.setImageResource(R.drawable.grafico_mi_progreso);
                    tvSlideTitle.setText(R.string.guide_paso_5_titulo);
                    tvSlideDescription.setText(R.string.guide_paso_5_desc);
                    break;
                case 6:
                    ivSlideImage.setImageResource(R.drawable.ic_export);
                    ivSlideImage.setColorFilter(ContextCompat.getColor(context, R.color.blanco));
                    tvSlideTitle.setText(R.string.guide_paso_6_titulo);
                    tvSlideDescription.setText(R.string.guide_paso_6_desc);
                    break;
            }
        }
    }

    private void dismissAndNotify() {
        markGuideAsSeen();
        dialog.dismiss();
        if (listener != null) {
            listener.onTutorialDismissed();
        }
    }

    public void markGuideAsSeen() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_GUIDE_SEEN, true).apply();
    }

    public void show() {
        if (dialog != null && !dialog.isShowing()) {
            currentStep = 1;
            updateStepUI();
            dialog.show();
        }
    }

    public static boolean hasSeenGuide(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_GUIDE_SEEN, false);
    }
}
