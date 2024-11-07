package com.example.delivery;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.delivery.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserProfile extends AppCompatActivity {
    private TextView userNameTextView;
    private TextView profileTitleTextView;
    private TextView orderHistoryButton, activeOrdersButton, supportButton, rechargeBalanceButton, becomeCourierButton, aboutServiceButton;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        userNameTextView = findViewById(R.id.papa);
        orderHistoryButton = findViewById(R.id.orderHistoryButton);

        Log.d("UserProfile", "onCreate: userNameTextView = " + userNameTextView);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        userRepository = new UserRepository(db, auth);


        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();

            userRepository.getUserById(userId).thenAccept(user -> {
                if (user != null) {
                    userNameTextView.setText(user.getName());
                } else {
                    userNameTextView.setText("Неизвестный пользователь");
                }
            }).exceptionally(throwable -> {
                userNameTextView.setText("Ошибка загрузки данных");
                return null;
            });
        } else {
            userNameTextView.setText("Пользователь не авторизован");
        }
    }
}
