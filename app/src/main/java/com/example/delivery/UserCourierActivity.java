package com.example.delivery;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.delivery.adapter.UserCourierAdapter;
import com.example.delivery.model.Courier;
import com.example.delivery.model.User;
import com.example.delivery.repository.CourierRepository;
import com.example.delivery.repository.UserRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UserCourierActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserCourierAdapter adapter;
    private CourierRepository courierRepository;
    private UserRepository userRepository;
    private int currentTab = R.id.menu_users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_courier);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        courierRepository = new CourierRepository(FirebaseFirestore.getInstance());
        userRepository = new UserRepository(FirebaseFirestore.getInstance(), null);

        BottomNavigationView navigationView = findViewById(R.id.bottomNavigation);
        navigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);

        loadUsers();
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        currentTab = item.getItemId();
        if (item.getItemId() == R.id.menu_users) {
            loadUsers();
            return true;
        } else if (item.getItemId() == R.id.menu_couriers) {
            loadCouriers();
            return true;
        }
        return false;
    }

    private void loadUsers() {
        userRepository.usersCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<User> users = task.getResult().toObjects(User.class);
                adapter = new UserCourierAdapter(users, null, (user, courier, requestCode) -> {
                    Intent intent = new Intent(this, DetailActivity.class);
                    intent.putExtra("type", "user");
                    intent.putExtra("name", user.getName());
                    intent.putExtra("userId", user.getId());
                    intent.putExtra("email", user.getEmail());
                    intent.putExtra("balance", user.getBalance());
                    intent.putExtra("avatarUrl", user.getAvatarUrl());
                    startActivityForResult(intent, requestCode);
                });
                recyclerView.setAdapter(adapter);
            } else {
                Toast.makeText(this, "Ошибка загрузки пользователей", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCouriers() {
        courierRepository.courierCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Courier> couriers = task.getResult().toObjects(Courier.class);
                adapter = new UserCourierAdapter(null, couriers, (user, courier, requestCode) -> {
                    Intent intent = new Intent(this, DetailActivity.class);
                    intent.putExtra("type", "courier");
                    intent.putExtra("userId", courier.getId()); // Только ID, остальное будем грузить при открытии
                    startActivityForResult(intent, requestCode);
                });
                recyclerView.setAdapter(adapter);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (currentTab == R.id.menu_users) {
                loadUsers();
            } else {
                loadCouriers();
            }
        }
    }
}