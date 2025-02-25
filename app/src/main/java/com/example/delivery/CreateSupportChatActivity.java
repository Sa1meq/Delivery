package com.example.delivery;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.delivery.repository.SupportChatRepository;
import com.example.delivery.auth.Authentication;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class CreateSupportChatActivity extends AppCompatActivity {

    private SupportChatRepository chatRepository;
    private EditText topicEditText;
    private Spinner requestTypeSpinner;
    private boolean isComplaintFlow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_support_chat);

        chatRepository = new SupportChatRepository();
        topicEditText = findViewById(R.id.edit_topic);
        requestTypeSpinner = findViewById(R.id.request_type_spinner);

        // Проверяем, откуда пришли
        isComplaintFlow = getIntent().getBooleanExtra("isComplaintFlow", false);

        setupRequestTypeSpinner();
    }

    private void setupRequestTypeSpinner() {
        ArrayAdapter<CharSequence> adapter;
        if (isComplaintFlow) {
            // Если это жалоба, устанавливаем фиксированное значение
            adapter = ArrayAdapter.createFromResource(this,
                    R.array.complaint_request_types, android.R.layout.simple_spinner_item);
        } else {
            // Если это обычное создание чата, показываем все варианты
            adapter = ArrayAdapter.createFromResource(this,
                    R.array.all_request_types, android.R.layout.simple_spinner_item);
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        requestTypeSpinner.setAdapter(adapter);

        if (isComplaintFlow) {
            // Устанавливаем "Жалоба на обслуживание" по умолчанию
            requestTypeSpinner.setSelection(0);
            requestTypeSpinner.setEnabled(false); // Блокируем выбор
        }
    }

    public void onClickCreateChat(View view) {
        String topic = topicEditText.getText().toString().trim();
        String requestType = requestTypeSpinner.getSelectedItem().toString();

        if (topic.isEmpty()) {
            Toast.makeText(this, "Введите тему обращения", Toast.LENGTH_SHORT).show();
            return;
        }

        chatRepository.createChat(
                FirebaseAuth.getInstance().getUid(),
                topic,
                requestType
        ).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Чат успешно создан", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Ошибка создания чата", Toast.LENGTH_SHORT).show();
            }
        });
    }
}