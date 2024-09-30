package com.example.delivery;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.delivery.model.User;
import com.example.delivery.repository.UserRepository;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Pattern;

public class Registration extends AppCompatActivity {
    private UserRepository userRepository;
    private EditText editName;
    private EditText editEmail;
    private EditText editPassword;
    private Button registerButton;
    private SignInButton googleSignInButton;
    private TextView loginTextView;

    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;

    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);

        userRepository = new UserRepository(FirebaseFirestore.getInstance());
        mAuth = FirebaseAuth.getInstance();
        editName = findViewById(R.id.nicknameEditText);
        editEmail = findViewById(R.id.emailEditText);
        editPassword = findViewById(R.id.passwordEditText);
        registerButton = findViewById(R.id.registerButton);
        googleSignInButton = findViewById(R.id.googleSignInButton);
        loginTextView = findViewById(R.id.loginTextView);

        registerButton.setOnClickListener(this::onClickRegistration);
        loginTextView.setOnClickListener(v -> {
            Intent intent = new Intent(Registration.this, Authorization.class);
            startActivity(intent);
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInButton.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    public void onClickRegistration(View view) {
        String name = editName.getText().toString();
        String email = editEmail.getText().toString();
        String password = editPassword.getText().toString();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        if (!Pattern.compile(emailPattern).matcher(email).matches()) {
            Toast.makeText(this, "Неправильный формат электронной почты", Toast.LENGTH_SHORT).show();
            return;
        }

        userRepository.addUser(name, email, password).thenAccept(user -> {
            if (user != null) {
                Toast.makeText(this, "Регистрация прошла успешно!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Registration.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Ошибка регистрации", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();
            firebaseAuthWithGoogle(idToken);
        } catch (ApiException e) {
            Toast.makeText(this, "Ошибка входа: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        userRepository.addUser(user.getDisplayName(), user.getEmail(), "")
                                .thenAccept(addedUser -> {
                                    Toast.makeText(Registration.this, "Вход через Google успешен", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(Registration.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                });
                    } else {
                        Toast.makeText(this, "Ошибка аутентификации: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
