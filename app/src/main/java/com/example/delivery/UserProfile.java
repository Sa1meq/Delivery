package com.example.delivery;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.example.delivery.model.RouteOrder;
import com.example.delivery.repository.CourierRepository;
import com.example.delivery.repository.RouteOrderRepository;
import com.example.delivery.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserProfile extends AppCompatActivity {
    private TextView userNameTextView;
    private TextView orderHistoryButton, activeOrdersButton, exitButton, placeOrder, becomeCourierButton, rechargeBalanceButton, balanceText, aboutServiceButton, adminPanelButton, supportButton;
    private UserRepository userRepository;
    private CourierRepository courierRepository;
    private RouteOrderRepository routeOrderRepository;
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView avatarImage, notificationIcon;
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
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        userNameTextView = findViewById(R.id.papa);
        orderHistoryButton = findViewById(R.id.orderHistoryButton);
        activeOrdersButton = findViewById(R.id.activeOrdersButton);
        placeOrder = findViewById(R.id.placeOrders);
        becomeCourierButton = findViewById(R.id.becomeCourierButton);
        supportButton = findViewById(R.id.supportButton);
        avatarImage = findViewById(R.id.avatarImage);
        notificationIcon = findViewById(R.id.notificationIcon);
        rechargeBalanceButton = findViewById(R.id.rechargeBalanceButton);
        exitButton = findViewById(R.id.exitButton);
        aboutServiceButton = findViewById(R.id.aboutServiceButton);
        adminPanelButton = findViewById(R.id.adminPanelButton);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        userRepository = new UserRepository(db, auth);
        courierRepository = new CourierRepository(FirebaseFirestore.getInstance());
        routeOrderRepository = new RouteOrderRepository();

        FirebaseUser firebaseUser = auth.getCurrentUser();


        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            userRepository.getUserById(userId).thenAccept(user -> {
                if (user != null) {
                    userNameTextView.setText(user.getName());
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
                    if (user.isAdmin()) {
                        runOnUiThread(() -> adminPanelButton.setVisibility(View.VISIBLE));
                    } else {
                        runOnUiThread(() -> adminPanelButton.setVisibility(View.GONE));
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
        adminPanelButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfile.this, AdminPanel.class);
            startActivity(intent);
            finish();
        });
        orderHistoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfile.this, UserOrdersHistory.class);
            startActivity(intent);
        });
        supportButton.setOnClickListener(view -> {
            Intent intent = new Intent(UserProfile.this, SupportActivity.class);
            startActivity(intent);
            finish();
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
                Intent intent = new Intent(UserProfile.this, CardActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
            }
        });

        activeOrdersButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfile.this, UserActiveOrders.class);
            startActivity(intent);
            finish();
        });
        notificationIcon.setOnClickListener(v -> {
            routeOrderRepository.getOrdersToRate(firebaseUser.getUid()).thenAccept(orders -> {
                if (orders != null && !orders.isEmpty()) {
                    showRatingDialog(orders.get(0));
                } else {
                    Toast.makeText(UserProfile.this, "Ошибка получения заказа", Toast.LENGTH_SHORT).show();
                }
            });
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
        aboutServiceButton.setOnClickListener(v -> {
            showAboutDialog();
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




    private void clearUserCredentials() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_PASSWORD);
        editor.putBoolean(KEY_REMEMBER_ME, false);
        editor.apply();
    }

    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_about_service, null);
        builder.setView(dialogView);

        ImageView appIcon = dialogView.findViewById(R.id.appIconImageView);
        TextView appName = dialogView.findViewById(R.id.appNameTextView);
        TextView appInfo = dialogView.findViewById(R.id.appInfoTextView);
        ImageView closeButton = dialogView.findViewById(R.id.closeButton);

        appIcon.setImageResource(R.drawable.ic_icon);
        appName.setText(getString(R.string.app_name));
        appInfo.setText("Добро пожаловать в официальное приложение \"OnTheWay\"! " +
                "С его помощью вы сможете сократить трату времени на перевозку товаров и предоставить это нам. " +
                "Приложение разработано для упрощения вашей жизни. " +
                "Автор: Sa1meq " +
                "Благодарим за использование нашего сервиса!");

        AlertDialog dialog = builder.create();

        closeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
    

    public void showRatingDialog(RouteOrder order) {
        if (order != null) {
            courierRepository.getCourierById(order.getCourierId()).thenAccept(courier -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserProfile.this);
                View view = getLayoutInflater().inflate(R.layout.dialog_rate_courier, null);

                TextView workerNameTextView = view.findViewById(R.id.workerNameTextView);
                RatingBar ratingBar = view.findViewById(R.id.ratingBar);
                Button submitRatingButton = view.findViewById(R.id.submitRatingButton);

                workerNameTextView.setText(courier.getFirstName());

                builder.setView(view);
                builder.setTitle("Оцените курьера");

                AlertDialog dialog = builder.create();

                submitRatingButton.setOnClickListener(v -> {
                    float rating = ratingBar.getRating();
                    if (rating > 0) {
                        courierRepository.updateCourierRating(courier.getId(), rating)
                                .thenRun(() -> {
                                    routeOrderRepository.setOrderRated(order.getOrderId())
                                            .thenRun(() -> {
                                                dialog.dismiss();
                                            });
                                });
                    } else {
                        Toast.makeText(UserProfile.this, "Пожалуйста, поставьте оценку", Toast.LENGTH_SHORT).show();
                    }
                });

                dialog.show();
            });
        }
    }
}