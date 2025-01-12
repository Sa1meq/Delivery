package com.example.delivery;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.delivery.R;
import com.example.delivery.adapter.SupportMessageAdapter;
import com.example.delivery.model.SupportMessage;
import com.example.delivery.model.SupportChat;
import com.example.delivery.repository.SupportChatRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AdminChatActivity extends AppCompatActivity {
    private RecyclerView chatMessagesRecyclerView;
    private EditText messageEditText;
    private Button sendMessageButton;
    private Button closeChatButton;
    private SupportChatRepository chatRepository;
    private List<SupportMessage> messages = new ArrayList<>();
    private String chatId;
    private SupportChat currentChat;

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

        // Инициализация репозитория чатов
        chatRepository = new SupportChatRepository(FirebaseFirestore.getInstance());

        // Получаем chatId из Intent
        chatId = getIntent().getStringExtra("chatId");

        if (chatId == null || chatId.isEmpty()) {
            finish();
            return;
        }

        // Загружаем чат по chatId
        chatRepository.getChatById(chatId).thenAccept(chat -> {
            currentChat = chat;
            messages.clear();
            messages.addAll(chat.getMessages());
            runOnUiThread(() -> {
                SupportMessageAdapter adapter = new SupportMessageAdapter(messages, "admin");
                chatMessagesRecyclerView.setAdapter(adapter);
            });
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });

        // Обработчик отправки сообщения
        sendMessageButton.setOnClickListener(v -> {
            String messageContent = messageEditText.getText().toString().trim();
            if (!messageContent.isEmpty()) {
                SupportMessage message = new SupportMessage("admin", messageContent, System.currentTimeMillis());
                chatRepository.addMessageToChat(chatId, message).thenRun(() -> {
                    messages.add(message);
                    runOnUiThread(() -> {
                        chatMessagesRecyclerView.getAdapter().notifyItemInserted(messages.size() - 1);
                        chatMessagesRecyclerView.scrollToPosition(messages.size() - 1);
                    });
                    messageEditText.setText("");
                }).exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
            }
        });

        // Обработчик закрытия чата
        closeChatButton.setOnClickListener(v -> {
            if (currentChat != null) {
                currentChat.setStatus("closed");
                chatRepository.closeChat(chatId).thenRun(() -> {
                    Toast.makeText(AdminChatActivity.this, "Чат закрыт", Toast.LENGTH_SHORT).show();
                    finish();
                }).exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
            }
        });
    }
}
