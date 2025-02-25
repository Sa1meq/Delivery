package com.example.delivery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.delivery.model.Courier;
import com.example.delivery.repository.CourierRepository;
import com.google.firebase.firestore.FirebaseFirestore;

public class CourierDetailsActivity extends AppCompatActivity {

    private TextView firstNameTextView, surNameTextView, phoneTextView, typeTextView,
            licenseCategoriesTextView, experienceTextView, previousJobsTextView, additionalInfoTextView;

    private ImageView avatarImageView;

    private CourierRepository courierRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courier_details);

        // Инициализация View
        avatarImageView = findViewById(R.id.imageViewCourierAvatar);
        firstNameTextView = findViewById(R.id.textViewCourierName);
        surNameTextView = findViewById(R.id.textViewCourierSurName);
        phoneTextView = findViewById(R.id.textViewCourierPhone);
        typeTextView = findViewById(R.id.textViewCourierType);
        licenseCategoriesTextView = findViewById(R.id.textViewLicenseCategories);
        experienceTextView = findViewById(R.id.textViewExperience);
        previousJobsTextView = findViewById(R.id.textViewPreviousJobs);
        additionalInfoTextView = findViewById(R.id.textViewAdditionalInfo);

        courierRepository = new CourierRepository(FirebaseFirestore.getInstance());

        String courierId = getIntent().getStringExtra("COURIER_ID");
        loadCourierDetails(courierId);

        ImageView backImageView = findViewById(R.id.backImageView);
        backImageView.setOnClickListener(v -> {
            Intent intent = new Intent(CourierDetailsActivity.this, VerificationActivity.class);
            startActivity(intent);
            finish();
        });

    }

    private void loadCourierDetails(String courierId) {
        courierRepository.getCourierById(courierId).thenAccept(courier -> {
            displayCourierDetails(courier);
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    private void displayCourierDetails(Courier courier) {
        firstNameTextView.setText(courier.getFirstName());
        surNameTextView.setText(courier.getSurName());
        phoneTextView.setText(courier.getPhone());
        typeTextView.setText("Тип курьера: " + courier.getTypeOfCourier());
        licenseCategoriesTextView.setText("Категории: " + courier.getDrivingLicenseCategories());
        experienceTextView.setText("Опыт: " + (courier.isHasExperience() ? "Да" : "Нет"));
        previousJobsTextView.setText("Предыдущие места работы: " + courier.getPreviousJobs());
        additionalInfoTextView.setText("Дополнительная информация: " + courier.getAdditionalInfo());
    }
}