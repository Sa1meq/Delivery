package com.example.delivery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.delivery.adapter.SupportChatAdapter;
import com.example.delivery.model.SupportChat;
import com.example.delivery.model.SupportMessage;
import com.example.delivery.repository.SupportChatRepository;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Query;


public class SupportChatActivity extends AppCompatActivity {

    private SupportChatAdapter adapter;
    private SupportChatRepository chatRepo;
    private String chatId;
    private String userId;
    private boolean isChatClosed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_chat);

        chatId = getIntent().getStringExtra("CHAT_ID");
        userId = FirebaseAuth.getInstance().getUid();
        chatRepo = new SupportChatRepository();

        setupRecyclerView();
        setupSendButton();
        setupChatListener();

        ImageView backImageView = findViewById(R.id.backImageView);
        backImageView.setOnClickListener(v -> {
            Intent intent = new Intent(SupportChatActivity.this, SupportChatListActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void setupRecyclerView() {
        Query query = chatRepo.getMessagesQuery(chatId);
        FirestoreRecyclerOptions<SupportMessage> options = new FirestoreRecyclerOptions.Builder<SupportMessage>()
                .setQuery(query, SupportMessage.class)
                .build();

        adapter = new SupportChatAdapter(options, userId);

        RecyclerView recyclerView = findViewById(R.id.recycler_message_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.addOnLayoutChangeListener((v, l, t, r, b, oldL, oldT, oldR, oldB) -> {
            if (b < oldB) recyclerView.smoothScrollToPosition(adapter.getItemCount());
        });
    }

    private void setupSendButton() {
        EditText input = findViewById(R.id.edit_message);
        ImageButton sendBtn = findViewById(R.id.button_send);

        sendBtn.setOnClickListener(v -> {
            String text = input.getText().toString().trim();
            if (!text.isEmpty() && !isChatClosed) {
                sendMessage(text);
                input.setText("");
            }
        });
    }

    private void sendMessage(String text) {
        chatRepo.sendMessage(chatId, text, userId, false)
                .addOnFailureListener(e -> showError("Ошибка отправки: " + e.getMessage()));
    }

    private void setupChatListener() {
        chatRepo.getChatRef(chatId).addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                showError("Ошибка обновления чата");
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                SupportChat chat = snapshot.toObject(SupportChat.class);
                isChatClosed = "closed".equals(chat.getStatus());
                updateUI();
            }
        });
    }

    private void updateUI() {
        ImageButton sendBtn = findViewById(R.id.button_send);
        EditText input = findViewById(R.id.edit_message);

        sendBtn.setEnabled(!isChatClosed);
        input.setEnabled(!isChatClosed);

        if (isChatClosed) {
            input.setHint("Чат закрыт");
            showError("Чат закрыт для сообщений");
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}