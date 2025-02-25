package com.example.delivery;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.delivery.adapter.AdminPanelAdapter;
import com.example.delivery.model.AdminCardItem;

import java.util.Arrays;
import java.util.List;

public class AdminPanel extends AppCompatActivity {

    private RecyclerView adminRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        adminRecyclerView = findViewById(R.id.adminRecyclerView);

        // Данные для карточек
        List<AdminCardItem> items = Arrays.asList(
                new AdminCardItem(R.drawable.ic_verification, "Верификация курьеров"),
                new AdminCardItem(R.drawable.ic_chat, "Техническая поддержка"),
                new AdminCardItem(R.drawable.ic_users, "Управление пользователями")
        );

        // Настройка адаптера и RecyclerView
        AdminPanelAdapter adapter = new AdminPanelAdapter(this, items, position -> handleCardClick(position));
        adminRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adminRecyclerView.setAdapter(adapter);

        ImageView backImageView = findViewById(R.id.backImageView);
        backImageView.setOnClickListener(v -> {
            Intent intent = new Intent(AdminPanel.this, UserProfile.class);
            startActivity(intent);
            finish();
        });
    }

    private void handleCardClick(int position) {
        switch (position) {
            case 0: // Верификация курьеров
                navigateToVerification();
                break;
            case 1: // Чаты
                navigateToChats();
                break;
            case 2: // Управление пользователями
                navigateToUserManagement();
                break;
            default:
                break;
        }
    }

    private void navigateToVerification() {
        Intent intent = new Intent(AdminPanel.this, VerificationActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToChats() {
        Intent intent = new Intent(AdminPanel.this, AdminChatListActivity.class);
        startActivity(intent);
        finish();
    }


    private void navigateToUserManagement() {
        Intent intent = new Intent(AdminPanel.this, UserCourierActivity.class);
        startActivity(intent);
    }


}
