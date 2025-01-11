package com.example.delivery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AdminPanel extends AppCompatActivity {

    private Button verificationButton;
    private Button chatButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        verificationButton = findViewById(R.id.verificationButton);
        chatButton = findViewById(R.id.chatButton);

        verificationButton.setOnClickListener(v -> navigateToVerification());
//        chatButton.setOnClickListener(v -> navigateToChats());
    }

    private void navigateToVerification() {
        // Переход на экран верификации курьеров
        Intent intent = new Intent(AdminPanel.this, VerificationActivity.class);
        startActivity(intent);
    }

//    private void navigateToChats() {
//        // Переход на экран чатов
//        Intent intent = new Intent(AdminPanel.this, AdminChatListActivity.class);
//        startActivity(intent);
//    }
}
