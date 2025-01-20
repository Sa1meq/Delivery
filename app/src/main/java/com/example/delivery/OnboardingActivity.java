package com.example.delivery;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        if (!isFirstLaunch()) {
            navigateToAuthorization();
            return;
        }

        setContentView(R.layout.activity_onboarding);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        btnNext = findViewById(R.id.btnNext);

        viewPager.setAdapter(new OnboardingAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
        }).attach();

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < 3) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                setFirstLaunchComplete();
                navigateToAuthorization();
            }
        });
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
        Intent intent = new Intent(this, Authorization.class);
        startActivity(intent);
        finish();
    }
}
