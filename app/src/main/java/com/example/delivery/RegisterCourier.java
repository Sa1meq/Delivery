package com.example.delivery;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.delivery.model.Courier;
import com.example.delivery.repository.CourierRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.CompletableFuture;

public class RegisterCourier extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST_CODE = 1;

    private EditText editTextFirstName, editTextSurName, editTextMiddleName, editTextPhone,
            editTextPreviousJobs, editTextAdditionalInfo, editTextEmail;
    private CheckBox checkBoxExperience;
    private Button buttonUploadFile, buttonSubmit;
    private RadioGroup radioGroupCourierType;

    private Uri fileUri;
    private String userId = FirebaseAuth.getInstance().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_courier);

        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextSurName = findViewById(R.id.editTextSurName);
        editTextMiddleName = findViewById(R.id.editTextMiddleName);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextPreviousJobs = findViewById(R.id.editTextPreviousJobs);
        checkBoxExperience = findViewById(R.id.checkBoxExperience);
        editTextAdditionalInfo = findViewById(R.id.editTextAdditionalInfo);
        buttonUploadFile = findViewById(R.id.buttonUploadFile);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        editTextEmail = findViewById(R.id.editTextEmail);
        radioGroupCourierType = findViewById(R.id.radioGroupCourierType);

        buttonUploadFile.setOnClickListener(v -> openFilePicker());

        buttonSubmit.setOnClickListener(v -> submitQuestionnaire());

        ImageView backImageView = findViewById(R.id.backImageView);
        backImageView.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterCourier.this, UserProfile.class);
            startActivity(intent);
            finish();
        });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            fileUri = data.getData();
            if (fileUri != null) {
                String fileName = getFileName(fileUri);
                Toast.makeText(this, "Файл выбран: " + fileName, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void submitQuestionnaire() {
        // Проверка заполнения обязательных полей
        if (!validateFields()) {
            return; // Если поля не заполнены, прерываем выполнение
        }

        // Получаем данные из полей
        String firstName = editTextFirstName.getText().toString();
        String surName = editTextSurName.getText().toString();
        String middleName = editTextMiddleName.getText().toString();
        String phone = editTextPhone.getText().toString();
        String email = editTextEmail.getText().toString();
        String previousJobs = editTextPreviousJobs.getText().toString();
        String additionalInfo = editTextAdditionalInfo.getText().toString();

        // Проверка уникальности телефона и почты
        CourierRepository courierRepository = new CourierRepository(FirebaseFirestore.getInstance());

        CompletableFuture.allOf(
                courierRepository.isPhoneExists(phone).thenAccept(isPhoneExists -> {
                    if (isPhoneExists) {
                        runOnUiThread(() -> {
                            editTextPhone.setError("Этот номер телефона уже используется");
                            Toast.makeText(this, "Этот номер телефона уже используется", Toast.LENGTH_SHORT).show();
                        });
                        throw new RuntimeException("Phone already exists");
                    }
                }),
                courierRepository.isEmailExists(email).thenAccept(isEmailExists -> {
                    if (isEmailExists) {
                        runOnUiThread(() -> {
                            editTextEmail.setError("Эта почта уже используется");
                            Toast.makeText(this, "Эта почта уже используется", Toast.LENGTH_SHORT).show();
                        });
                        throw new RuntimeException("Email already exists");
                    }
                })
        ).thenRun(() -> {
            StringBuilder drivingLicenseCategories = new StringBuilder();
            if (((CheckBox) findViewById(R.id.checkBoxA)).isChecked()) drivingLicenseCategories.append("A ");
            if (((CheckBox) findViewById(R.id.checkBoxA1)).isChecked()) drivingLicenseCategories.append("A1 ");
            if (((CheckBox) findViewById(R.id.checkBoxAM)).isChecked()) drivingLicenseCategories.append("AM ");
            if (((CheckBox) findViewById(R.id.checkBoxB)).isChecked()) drivingLicenseCategories.append("B ");
            if (((CheckBox) findViewById(R.id.checkBoxBE)).isChecked()) drivingLicenseCategories.append("BE ");
            if (((CheckBox) findViewById(R.id.checkBoxC)).isChecked()) drivingLicenseCategories.append("C ");
            if (((CheckBox) findViewById(R.id.checkBoxCE)).isChecked()) drivingLicenseCategories.append("CE ");
            if (((CheckBox) findViewById(R.id.checkBoxD)).isChecked()) drivingLicenseCategories.append("D ");
            if (((CheckBox) findViewById(R.id.checkBoxDE)).isChecked()) drivingLicenseCategories.append("DE ");

            boolean hasExperience = checkBoxExperience.isChecked();

            String courierType = "";
            int selectedCourierTypeId = radioGroupCourierType.getCheckedRadioButtonId();
            if (selectedCourierTypeId == R.id.radioButtonCar) {
                courierType = "Авто";
            } else if (selectedCourierTypeId == R.id.radioButtonTruck) {
                courierType = "Грузовой";
            }

            if (courierType.isEmpty()) {
                runOnUiThread(() -> Toast.makeText(this, "Ошибка определения типа курьера", Toast.LENGTH_SHORT).show());
                return;
            }

            Courier courier = new Courier();
            courier.setFirstName(firstName);
            courier.setSurName(surName);
            courier.setMiddleName(middleName);
            courier.setPhone(phone);
            courier.setEmail(email);
            courier.setBalance("0.0");
            courier.setPreviousJobs(previousJobs);
            courier.setDrivingLicenseCategories(drivingLicenseCategories.toString().trim());
            courier.setHasExperience(hasExperience);
            courier.setTypeOfCourier(courierType);
            courier.setStatus("active");
            courier.setAdditionalInfo(additionalInfo);

            if (fileUri != null) {
                uploadFileToCloudinary(fileUri, userId)
                        .thenAccept(fileUrl -> {
                            courier.setAttachedFiles(fileUrl);
                            saveCourierProfile(courier, userId);
                        })
                        .exceptionally(ex -> {
                            runOnUiThread(() -> Toast.makeText(this, "Ошибка при загрузке файла: " + ex.getMessage(), Toast.LENGTH_SHORT).show());
                            return null;
                        });
            } else {
                saveCourierProfile(courier, userId);
            }
        }).exceptionally(ex -> {
            runOnUiThread(() -> Toast.makeText(this, "Ошибка: " + ex.getMessage(), Toast.LENGTH_SHORT).show());
            return null;
        });
    }

    private void saveCourierProfile(Courier courier, String userId) {
        CourierRepository courierRepository = new CourierRepository(FirebaseFirestore.getInstance());
        courierRepository.addCourier(courier, userId)
                .thenAccept(success -> {
                    if (success) {
                        Toast.makeText(this, "Анкета успешно отправлена!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, UserProfile.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .exceptionally(ex -> {
                    Toast.makeText(this, "Ошибка: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                    return null;
                });
    }

    private boolean validateFields() {
        // Проверка имени
        if (editTextFirstName.getText().toString().trim().isEmpty()) {
            editTextFirstName.setError("Введите имя");
            return false;
        }

        // Проверка фамилии
        if (editTextSurName.getText().toString().trim().isEmpty()) {
            editTextSurName.setError("Введите фамилию");
            return false;
        }

        String phone = editTextPhone.getText().toString().trim();
        if (phone.isEmpty()) {
            editTextPhone.setError("Введите номер телефона");
            return false;
        } else if (!isValidPhone(phone)) {
            editTextPhone.setError("Номер телефона должен быть в формате +375 (XX) XXX XX XX");
            return false;
        }

        // Проверка электронной почты
        String email = editTextEmail.getText().toString().trim();
        if (email.isEmpty()) {
            editTextEmail.setError("Введите электронную почту");
            return false;
        } else if (!isValidEmail(email)) {
            editTextEmail.setError("Введите корректный адрес электронной почты");
            return false;
        }

        // Если все поля заполнены корректно
        return true;
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        String phonePattern = "^\\+375\\d{2}\\d{3}\\d{2}\\d{2}$";
        return phone.matches(phonePattern);
    }

    private CompletableFuture<String> uploadFileToCloudinary(Uri fileUri, String userId) {
        CompletableFuture<String> future = new CompletableFuture<>();
        CloudinaryUploader cloudinaryUploader = new CloudinaryUploader(this);
        cloudinaryUploader.uploadImage(fileUri, userId, new CloudinaryUploader.UploadCallback() {
            @Override
            public void onUploadComplete(String imageUrl) {
                if (imageUrl != null) {
                    future.complete(imageUrl);
                } else {
                    future.completeExceptionally(new RuntimeException("Ошибка при загрузке файла в Cloudinary."));
                }
            }
        });
        return future;
    }
}