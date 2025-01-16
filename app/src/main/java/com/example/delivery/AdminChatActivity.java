package com.example.delivery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.delivery.R;
import com.example.delivery.adapter.SupportMessageAdapter;
import com.example.delivery.model.SupportMessage;
import com.example.delivery.repository.SupportChatRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class AdminChatActivity extends AppCompatActivity {
    private RecyclerView chatMessagesRecyclerView;
    private EditText messageEditText;
    private Button sendMessageButton;
    private Button closeChatButton;
    private SupportChatRepository chatRepository;
    private List<SupportMessage> messages = new ArrayList<>();
    private SupportMessageAdapter adapter;
    private String chatId;
    private ListenerRegistration chatListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_chat);

        chatMessagesRecyclerView = findViewById(R.id.chatMessagesRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        closeChatButton = findViewById(R.id.closeChatButton);

        // Устанавливаем менеджер для RecyclerView
        chatMessagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SupportMessageAdapter(messages, "admin");
        chatMessagesRecyclerView.setAdapter(adapter);

        chatRepository = new SupportChatRepository(FirebaseFirestore.getInstance());

        // Получаем chatId из Intent
        chatId = getIntent().getStringExtra("chatId");

        if (chatId == null || chatId.isEmpty()) {
            finish();
            return;
        }

        // Загрузка старых сообщений
        chatRepository.loadOldMessages(chatId).thenAccept(oldMessages -> {
            runOnUiThread(() -> {
                messages.clear();
                messages.addAll(oldMessages);
                adapter.notifyDataSetChanged();
                chatMessagesRecyclerView.scrollToPosition(messages.size() - 1);
            });
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });

        // Подписка на новые сообщения
        chatListener = chatRepository.getChatMessagesListener(chatId, newMessages -> {
            runOnUiThread(() -> {
                if (newMessages.isEmpty()) {
                    Toast.makeText(this, "Нет сообщений", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Загружено " + newMessages.size() + " сообщений", Toast.LENGTH_SHORT).show();
                    messages.addAll(newMessages);
                    adapter.notifyDataSetChanged();
                    chatMessagesRecyclerView.scrollToPosition(messages.size() - 1);
                }
            });
        });

        // Обработчик отправки сообщения
        sendMessageButton.setOnClickListener(v -> {
            String messageContent = messageEditText.getText().toString().trim();
            if (!messageContent.isEmpty()) {
                SupportMessage message = new SupportMessage("admin", messageContent, System.currentTimeMillis());
                chatRepository.addMessageToChat(chatId, message).thenRun(() -> {
                    runOnUiThread(() -> messageEditText.setText(""));  // Очистка поля ввода
                }).exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
            }
        });

        closeChatButton.setOnClickListener(v -> {
            chatRepository.closeChat(chatId).thenRun(() -> {
                Toast.makeText(AdminChatActivity.this, "Чат закрыт", Toast.LENGTH_SHORT).show();
                finish();
            }).exceptionally(e -> {
                e.printStackTrace();
                return null;
            });
        });

        ImageView backImageView = findViewById(R.id.backImageView);
        backImageView.setOnClickListener(v -> {
            Intent intent = new Intent(AdminChatActivity.this, AdminChatListActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatListener != null) {
            chatListener.remove();
        }
    }
}
