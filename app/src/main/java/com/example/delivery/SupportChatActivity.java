package com.example.delivery;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.delivery.R;
import com.example.delivery.adapter.MessageAdapter;
import com.example.delivery.model.SupportChat;
import com.example.delivery.model.SupportMessage;
import com.example.delivery.repository.SupportChatRepository;
import com.example.delivery.repository.SupportMessageRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SupportChatActivity extends AppCompatActivity {

    private RecyclerView messageListRecycler;
    private EditText editMessage;
    private ImageButton sendButton;
    private MessageAdapter messageAdapter;

    private List<SupportMessage> messages = new ArrayList<>();
    private SupportMessageRepository messageRepository;
    private SupportChatRepository chatRepository;
    private SupportChat chat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_chat);

        String chatId = getIntent().getStringExtra("CHAT_ID");
        Log.d("SupportChatActivity", "Chat ID: " + chatId);
        if (chatId == null || chatId.isEmpty()) {
            Toast.makeText(this, "Ошибка загрузки чата", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        messageRepository = new SupportMessageRepository(FirebaseFirestore.getInstance());
        chatRepository = new SupportChatRepository(FirebaseFirestore.getInstance());

        CompletableFuture<SupportChat> chatFuture = chatRepository.getChatById(chatId);
        chatFuture.thenAccept(loadedChat -> {
            chat = loadedChat;
            runOnUiThread(this::loadMessages);
        }).exceptionally(throwable -> {
            runOnUiThread(() -> {
                Toast.makeText(this, "Ошибка загрузки чата", Toast.LENGTH_SHORT).show();
                finish();
            });
            return null;
        });



        messageListRecycler = findViewById(R.id.recycler_message_list);
        editMessage = findViewById(R.id.edit_message);
        sendButton = findViewById(R.id.button_send);

        messageAdapter = new MessageAdapter(this, messages);
        messageListRecycler.setLayoutManager(new LinearLayoutManager(this));
        messageListRecycler.setAdapter(messageAdapter);

        sendButton.setOnClickListener(view -> sendMessage());
    }


    private void loadMessages() {
        if (chat == null) {
            Toast.makeText(this, "Ошибка загрузки чата", Toast.LENGTH_SHORT).show();
            return;
        }
        CompletableFuture<List<SupportMessage>> future = messageRepository.getMessagesByChatId(chat.getId());
        future.thenAccept(loadedMessages -> {
            runOnUiThread(() -> {
                messages.clear();
                messages.addAll(loadedMessages);
                messageAdapter.notifyDataSetChanged();
                scrollToBottom();
            });
        }).exceptionally(throwable -> {
            runOnUiThread(() -> {
                Toast.makeText(this, "Ошибка загрузки сообщений: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                Log.d("SupportChatActivity.this", "Ошибка загрузки сообщений: " + throwable.getMessage());
            });
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
        SupportMessage newMessage = messageRepository.addMessage(content, chat.getUserId(), chat.getId(), false);

        messages.add(newMessage);
        messageAdapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
    }

    private void scrollToBottom() {
        messageListRecycler.post(() -> messageListRecycler.scrollToPosition(messages.size() - 1));
    }

}
