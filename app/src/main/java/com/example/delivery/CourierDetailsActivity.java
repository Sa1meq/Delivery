package com.example.delivery;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.delivery.model.Courier;
import com.example.delivery.repository.CourierRepository;
import com.google.firebase.firestore.FirebaseFirestore;

public class CourierDetailsActivity extends AppCompatActivity {

    private TextView firstNameTextView, surNameTextView, phoneTextView, hobbiesTextView,
            previousJobsTextView, licenseCategoriesTextView, experienceTextView, additionalInfoTextView;

    private CourierRepository courierRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courier_details);

        firstNameTextView = findViewById(R.id.textViewCourierName);
        surNameTextView = findViewById(R.id.textViewCourierSurName);
        phoneTextView = findViewById(R.id.textViewCourierPhone);
        hobbiesTextView = findViewById(R.id.textViewCourierHobby);
        licenseCategoriesTextView = findViewById(R.id.textViewLicenseCategories);

        courierRepository = new CourierRepository(FirebaseFirestore.getInstance());

        String courierId = getIntent().getStringExtra("COURIER_ID");
        loadCourierDetails(courierId);
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
        hobbiesTextView.setText(courier.getHobbies());
        previousJobsTextView.setText(courier.getPreviousJobs());
        licenseCategoriesTextView.setText(courier.getDrivingLicenseCategories());
        experienceTextView.setText(courier.isHasExperience() ? "Есть опыт" : "Нет опыта");
        additionalInfoTextView.setText(courier.getAdditionalInfo());
    }
}
