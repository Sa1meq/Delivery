package com.example.delivery;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.delivery.adapter.OnboardingAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class OnboardingActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_FIRST_LAUNCH = "firstLaunch";

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private MaterialButton btnNext;
    private MaterialButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        if (!isFirstLaunch()) {
            navigateToAuthorization();
            return;
        }

        setContentView(R.layout.activity_onboarding);
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);

        viewPager.setAdapter(new OnboardingAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {}).attach();

        // Обработка нажатия на кнопку "Далее"
        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < 3) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                setFirstLaunchComplete();
                navigateToAuthorization();
            }
        });

        // Обработка нажатия на кнопку "Назад"
        btnBack.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() > 0) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            }
        });

        // Слушатель изменения страниц
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateUI(position);
                viewPager.setPageTransformer(new MarginPageTransformer(16));
            }
        });

        // Инициализация UI для первой страницы
        updateUI(0);
    }

    private void updateUI(int position) {
        // Показываем/скрываем кнопку "Назад"
        btnBack.setVisibility(position > 0 ? View.VISIBLE : View.GONE);

        // Меняем текст кнопки "Далее" на последней странице
        if (position == 3) {
            btnNext.setText("Начать");
            btnNext.setIcon(null); // Убираем иконку стрелки
        } else {
            btnNext.setText("Далее");
            btnNext.setIconResource(R.drawable.baseline_arrow_forward_24);
        }
    }

    private boolean isFirstLaunch() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return preferences.getBoolean(KEY_FIRST_LAUNCH, true);
    }

    private void setFirstLaunchComplete() {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(KEY_FIRST_LAUNCH, false);
        editor.apply();
    }

    private void navigateToAuthorization() {
        Intent intent = new Intent(this, Registration.class);
        startActivity(intent);
        finish();
    }
}