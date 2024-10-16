//package com.example.delivery;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.delivery.repository.UserRepository;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//public class CompleteProfileActivity extends AppCompatActivity {
//
//    private EditText nameEditText, nicknameEditText, passwordEditText;
//    private Button completeProfileButton;
//    private UserRepository userRepository;
//    private String email;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_complete_profile);
//
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        userRepository = new UserRepository(FirebaseFirestore.getInstance());
//
//        nameEditText = findViewById(R.id.nicknameEditText);
//        nicknameEditText = findViewById(R.id.nicknameEditText);
//        passwordEditText = findViewById(R.id.passwordEditText);
//        completeProfileButton = findViewById(R.id.registerButton);
//
//        email = getIntent().getStringExtra("email");
//
//        completeProfileButton.setOnClickListener(v -> completeProfile());
//    }
//
//    private void completeProfile() {
//        String name = nameEditText.getText().toString();
//        String nickname = nicknameEditText.getText().toString();
//        String password = passwordEditText.getText().toString();
//
//        userRepository.addUser(name, email, password).thenAccept(user -> {
//            Toast.makeText(CompleteProfileActivity.this, "Профиль успешно заполнен", Toast.LENGTH_SHORT).show();
//            startActivity(new Intent(CompleteProfileActivity.this, MainActivity.class));
//            finish();
//        }).exceptionally(e -> {
//            Toast.makeText(CompleteProfileActivity.this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//            return null;
//        });
//    }
//}
