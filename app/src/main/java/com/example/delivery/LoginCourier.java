package com.example.delivery;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.delivery.model.Courier;
import com.example.delivery.repository.CourierRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.CompletableFuture;

public class LoginCourier extends AppCompatActivity {

    private EditText phoneEditText;
    private Button loginButton;

    private CourierRepository courierRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_courier);
        phoneEditText = findViewById(R.id.editTextPhone);
        loginButton = findViewById(R.id.loginButton);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        courierRepository = new CourierRepository(db);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginCourier();
            }
        });
    }

    private void loginCourier() {
        String phone = phoneEditText.getText().toString().trim();

        if (validateInputs(phone)) {
            courierRepository.getCourierByPhone(phone).thenAccept(existingCourier -> {
                if (existingCourier != null) {
                    runOnUiThread(() -> Toast.makeText(LoginCourier.this, "Login successful!", Toast.LENGTH_SHORT).show());
                    Intent intent = new Intent(LoginCourier.this, CourierProfile.class);
                    startActivity(intent);
                    finish();
                } else {
                    runOnUiThread(() -> Toast.makeText(LoginCourier.this, "Courier not found!", Toast.LENGTH_SHORT).show());
                }
            }).exceptionally(e -> {
                runOnUiThread(() -> Toast.makeText(LoginCourier.this, "Error checking phone: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                return null;
            });
        }
    }

    private boolean validateInputs(String phone) {
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Phone requiered", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
