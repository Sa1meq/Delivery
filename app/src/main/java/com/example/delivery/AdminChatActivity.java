package com.example.delivery;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.delivery.adapter.MessageAdapter;
import com.example.delivery.model.SupportChat;
import com.example.delivery.model.SupportMessage;
import com.example.delivery.repository.SupportChatRepository;
import com.example.delivery.repository.SupportMessageRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AdminChatActivity extends AppCompatActivity {

    private RecyclerView messageListRecycler;
    private EditText editMessage;
    private ImageButton sendButton;
    private Button closeChatButton;
    private MessageAdapter messageAdapter;

    private List<SupportMessage> messages = new ArrayList<>();
    private SupportMessageRepository messageRepository;
    private SupportChatRepository chatRepository;
    private SupportChat chat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_chat);

        String chatId = getIntent().getStringExtra("CHAT_ID");
        if (chatId == null || chatId.isEmpty()) {
            Toast.makeText(this, "Ошибка загрузки чата", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        messageRepository = new SupportMessageRepository(FirebaseFirestore.getInstance());
        chatRepository = new SupportChatRepository(FirebaseFirestore.getInstance());

        messageListRecycler = findViewById(R.id.recycler_message_list);
        editMessage = findViewById(R.id.edit_message);
        sendButton = findViewById(R.id.button_send);
        closeChatButton = findViewById(R.id.button_close_chat);

        messageAdapter = new MessageAdapter(this, messages);
        messageListRecycler.setLayoutManager(new LinearLayoutManager(this));
        messageListRecycler.setAdapter(messageAdapter);

        loadChat(chatId);
        sendButton.setOnClickListener(view -> sendMessage());
        closeChatButton.setOnClickListener(view -> closeChat());
    }

    private void loadChat(String chatId) {
        chatRepository.getChatById(chatId).thenAccept(loadedChat -> {
            chat = loadedChat;
            loadMessages();
        }).exceptionally(throwable -> {
            runOnUiThread(() -> {
                Toast.makeText(this, "Ошибка загрузки чата", Toast.LENGTH_SHORT).show();
                finish();
            });
            return null;
        });
    }

    private void loadMessages() {
        CompletableFuture<List<SupportMessage>> future = messageRepository.getMessagesByChatId(chat.getId());
        future.thenAccept(loadedMessages -> {
            runOnUiThread(() -> {
                messages.clear();
                messages.addAll(loadedMessages);
                messageAdapter.notifyDataSetChanged();
                scrollToBottom();
            });
        }).exceptionally(throwable -> {
            runOnUiThread(() -> Toast.makeText(this, "Ошибка загрузки сообщений", Toast.LENGTH_SHORT).show());
            return null;
        });
    }

    private void sendMessage() {
        String content = editMessage.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "Введите сообщение", Toast.LENGTH_SHORT).show();
            return;
        }

        editMessage.setText("");
        SupportMessage newMessage = messageRepository.addMessage(content, "admin", chat.getId(), true);
        messages.add(newMessage);
        messageAdapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
    }

    private void closeChat() {
        chatRepository.closeChat(chat.getId());
        Toast.makeText(this, "Чат закрыт", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void scrollToBottom() {
        messageListRecycler.post(() -> messageListRecycler.scrollToPosition(messages.size() - 1));
    }
}
