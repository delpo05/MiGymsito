package com.example.migymsito;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.migymsito.utils.LocaleHelper;
import com.google.android.material.button.MaterialButton;

public class TutorialActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AppConfig";
    private static final String KEY_TUTORIAL_DONE = "tutorial_done";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial_activity);

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        MaterialButton btnNext = findViewById(R.id.btnNext);

        TutorialAdapter adapter = new TutorialAdapter();
        viewPager.setAdapter(adapter);



        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == adapter.getItemCount() - 1) {
                    btnNext.setText(R.string.empezar);
                } else {
                    btnNext.setText(R.string.siguiente);
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < adapter.getItemCount() - 1) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                finishTutorial();
            }
        });
    }

    private void finishTutorial() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_TUTORIAL_DONE, true).apply();

        Intent intent = new Intent(this, RegistroSesionActivity.class);
        startActivity(intent);
        finish();
    }

    private static class TutorialAdapter extends RecyclerView.Adapter<TutorialAdapter.ViewHolder> {

        private final int[] titles = {
                R.string.tutorial_title_1,
                R.string.tutorial_title_2,
                R.string.tutorial_title_3,
                R.string.tutorial_title_4
        };

        private final int[] descriptions = {
                R.string.tutorial_desc_1,
                R.string.tutorial_desc_2,
                R.string.tutorial_desc_3,
                R.string.tutorial_desc_4
        };

        private final int[] images = {
                R.drawable.logo_gym2,
                R.drawable.image_excercise,
                R.drawable.image_stats,
                R.drawable.image_compare
        };

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tutorial_slide, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.tvTitle.setText(titles[position]);
            holder.tvDescription.setText(descriptions[position]);
            holder.ivTutorial.setImageResource(images[position]);

            // Si es la primera diapositiva (Logo), mantenemos el tamaño original
            // Para el resto de diapositivas (Imágenes del tutorial), aumentamos el tamaño
            ViewGroup.LayoutParams params = holder.ivTutorial.getLayoutParams();
            if (position == 0) {
                params.width = (int) (250 * holder.itemView.getContext().getResources().getDisplayMetrics().density);
                params.height = (int) (250 * holder.itemView.getContext().getResources().getDisplayMetrics().density);
            } else {
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params.height = (int) (550 * holder.itemView.getContext().getResources().getDisplayMetrics().density);
            }
            holder.ivTutorial.setLayoutParams(params);
        }

        @Override
        public int getItemCount() {
            return titles.length;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDescription;
            ImageView ivTutorial;

            ViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvDescription = itemView.findViewById(R.id.tvDescription);
                ivTutorial = itemView.findViewById(R.id.ivTutorial);
            }
        }
    }
}
