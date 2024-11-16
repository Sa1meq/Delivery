package com.example.delivery;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.delivery.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserProfile extends AppCompatActivity {
    private TextView userNameTextView;
    private TextView orderHistoryButton, activeOrdersButton, exitButton, placeOrder, becomeCourierButton, rechargeBalanceButton, balanceText;
    private UserRepository userRepository;
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView avatarImage;
    private AlertDialog loadingDialog;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER_ME = "rememberMe";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        userNameTextView = findViewById(R.id.papa);
        orderHistoryButton = findViewById(R.id.orderHistoryButton);
        activeOrdersButton = findViewById(R.id.activeOrdersButton);
        placeOrder = findViewById(R.id.placeOrders);
        becomeCourierButton = findViewById(R.id.becomeCourierButton);
        avatarImage = findViewById(R.id.avatarImage);
        rechargeBalanceButton = findViewById(R.id.rechargeBalanceButton);
        balanceText = findViewById(R.id.balanceText);
        exitButton = findViewById(R.id.exitButton);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        userRepository = new UserRepository(db, auth);

        FirebaseUser firebaseUser = auth.getCurrentUser();

        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();

            userRepository.getUserById(userId).thenAccept(user -> {
                if (user != null) {
                    userNameTextView.setText(user.getName());

                    // Установка баланса с форматированием
                    try {
                        double balance = Double.parseDouble(user.getBalance());
                        String formattedBalance = String.format("%.2f", balance);
                        runOnUiThread(() -> balanceText.setText(formattedBalance + " BYN"));
                    } catch (NumberFormatException e) {
                        runOnUiThread(() -> balanceText.setText("Ошибка баланса"));
                    }

                    String avatarUrl = user.getAvatarUrl();
                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        runOnUiThread(() -> Glide.with(UserProfile.this)
                                .load(avatarUrl)
                                .circleCrop()
                                .placeholder(R.drawable.ic_avatar)
                                .into(avatarImage));
                    } else {
                        avatarImage.setImageResource(R.drawable.ic_avatar);
                    }
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

        avatarImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        orderHistoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfile.this, UserOrdersHistory.class);
            startActivity(intent);
        });

        placeOrder.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfile.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        becomeCourierButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfile.this, RegisterCourier.class);
            startActivity(intent);
            finish();
        });

        rechargeBalanceButton.setOnClickListener(v -> {
            if (firebaseUser != null) {
                showRechargeBalanceDialog(firebaseUser.getUid());
            } else {
                Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
            }
        });

        activeOrdersButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfile.this, UserActiveOrders.class);
            startActivity(intent);
            finish();
        });
        exitButton.setOnClickListener(v -> {
            if (firebaseUser != null) {
                clearUserCredentials();

                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(UserProfile.this, Authorization.class);
                startActivity(intent);
                finish();
                Toast.makeText(UserProfile.this, "Выход выполнен", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(UserProfile.this, "Ошибка выхода. Попробуйте снова.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadAvatarToCloudinary(imageUri);
        }
    }

    private void uploadAvatarToCloudinary(Uri imageUri) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = auth.getCurrentUser();

        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            CloudinaryUploader uploader = new CloudinaryUploader(this);

            showLoadingDialog();

            uploader.uploadImage(imageUri, userId, imageUrl -> {
                if (imageUrl != null) {
                    updateAvatarInFirestore(userId, imageUrl);
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(UserProfile.this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
                        hideLoadingDialog();
                    });
                }
            });
        }
    }

    private void updateAvatarInFirestore(String userId, String imageUrl) {
        userRepository.getUserById(userId).thenAccept(user -> {
            if (user != null) {
                user.setAvatarUrl(imageUrl);
                userRepository.updateUser(userId, user).thenAccept(success -> {
                    runOnUiThread(() -> {
                        hideLoadingDialog();
                        Glide.with(UserProfile.this)
                                .load(imageUrl)
                                .circleCrop()
                                .placeholder(R.drawable.ic_avatar)
                                .into(avatarImage);

                        Toast.makeText(UserProfile.this, "Изображение успешно обновлено", Toast.LENGTH_SHORT).show();
                    });
                }).exceptionally(throwable -> {
                    runOnUiThread(() -> {
                        hideLoadingDialog();
                        Toast.makeText(UserProfile.this, "Ошибка обновления Firestore", Toast.LENGTH_SHORT).show();
                    });
                    return null;
                });
            }
        });
    }

    private void showLoadingDialog() {
        if (loadingDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);

            View view = getLayoutInflater().inflate(R.layout.dialog_loading, null);
            builder.setView(view);

            loadingDialog = builder.create();
        }

        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void showRechargeBalanceDialog(String userId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_recharge_balance, null);
        builder.setView(dialogView);

        EditText inputAmount = dialogView.findViewById(R.id.inputAmount);
        inputAmount.setFilters(new InputFilter[]{new InputFilter.LengthFilter(7)});

        TextView confirmButton = dialogView.findViewById(R.id.confirmButton);
        TextView cancelButton = dialogView.findViewById(R.id.cancelButton);

        AlertDialog dialog = builder.create();

        confirmButton.setOnClickListener(v -> {
            String amountStr = inputAmount.getText().toString().trim();
            if (!amountStr.isEmpty()) {
                try {
                    double amount = Double.parseDouble(amountStr);
                    if (amount > 0) {
                        updateBalance(userId, amount);
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Введите корректную сумму", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Введите корректное число", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Поле не может быть пустым", Toast.LENGTH_SHORT).show();
            }
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void updateBalance(String userId, double amount) {
        userRepository.getUserById(userId).thenAccept(user -> {
            if (user != null) {
                double currentBalance = Double.parseDouble(user.getBalance());
                double newBalance = currentBalance + amount;
                user.setBalance(String.valueOf(newBalance));

                userRepository.updateUser(userId, user).thenAccept(success -> {
                    if (success) {
                        runOnUiThread(() -> {
                            String formattedBalance = String.format("%.2f", newBalance);
                            balanceText.setText(formattedBalance + " BYN");
                            Toast.makeText(this, "Баланс успешно пополнен", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Ошибка обновления баланса", Toast.LENGTH_SHORT).show();
                        });
                    }
                }).exceptionally(throwable -> {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Ошибка соединения с сервером", Toast.LENGTH_SHORT).show();
                    });
                    return null;
                });
            }
        });
    }
    private void clearUserCredentials() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_PASSWORD);
        editor.putBoolean(KEY_REMEMBER_ME, false);
        editor.apply();
    }

}
