package com.example.delivery;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.delivery.repository.SupportChatRepository;
import com.example.delivery.auth.Authentication;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class CreateSupportChatActivity extends AppCompatActivity {

    private SupportChatRepository chatRepository;
    private EditText topicEditText;
    private EditText requestTypeEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_support_chat);

        chatRepository = new SupportChatRepository(FirebaseFirestore.getInstance());
        topicEditText = findViewById(R.id.edit_topic);
        requestTypeEditText = findViewById(R.id.edit_request_type);
    }

    public void onClickCreateChat(View view) {
        String topic = topicEditText.getText().toString().trim();
        String requestType = requestTypeEditText.getText().toString().trim();

        if (topic.isEmpty() || requestType.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        chatRepository.addChat(topic, requestType, FirebaseAuth.getInstance().getUid());
        Toast.makeText(this, "Чат успешно создан", Toast.LENGTH_SHORT).show();
        finish();
    }
}
