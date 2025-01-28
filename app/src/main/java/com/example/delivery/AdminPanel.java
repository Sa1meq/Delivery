package com.example.delivery;

import android.content.Intent;
import android.os.Bundle;

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
                new AdminCardItem(R.drawable.ic_users, "Управление пользователями"),
                new AdminCardItem(R.drawable.ic_complaints, "Жалобы пользователей")
        );

        // Настройка адаптера и RecyclerView
        AdminPanelAdapter adapter = new AdminPanelAdapter(this, items, position -> handleCardClick(position));
        adminRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adminRecyclerView.setAdapter(adapter);
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
            case 3: // Жалобы
//                navigateToComplaints();
                break;
            default:
                break;
        }
    }

    private void navigateToVerification() {
        Intent intent = new Intent(AdminPanel.this, VerificationActivity.class);
        startActivity(intent);
    }

    private void navigateToChats() {
        Intent intent = new Intent(AdminPanel.this, AdminChatListActivity.class);
        startActivity(intent);
    }


    private void navigateToUserManagement() {
        Intent intent = new Intent(AdminPanel.this, UserCourierActivity.class);
        startActivity(intent);
    }
}

//    private void navigateToComplaints() {
//        // Переход на экран жалоб
//        Intent intent = new Intent(AdminPanel.this, ComplaintsActivity.class);
//        startActivity(intent);
//    }
//}
