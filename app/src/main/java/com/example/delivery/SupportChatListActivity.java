package com.example.delivery;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.delivery.adapter.SupportChatAdapter;
import com.example.delivery.model.SupportChat;
import com.example.delivery.repository.SupportChatRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class SupportChatListActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private SupportChatAdapter chatAdapter;
    private SupportChatRepository chatRepository;
    private Button createNewChatButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_chat_list);

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        chatRepository = new SupportChatRepository(FirebaseFirestore.getInstance());

        chatAdapter = new SupportChatAdapter(new ArrayList<>());
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);
        createNewChatButton = findViewById(R.id.createNewChatButton);

        String userId = FirebaseAuth.getInstance().getUid();
        chatRepository.getUserChats(userId).thenAccept(chats -> {
            runOnUiThread(() -> {
                if (chats != null && !chats.isEmpty()) {
                    chatAdapter.setChats(chats);
                } else {
                    Toast.makeText(SupportChatListActivity.this, "No chats found. Create a new one.", Toast.LENGTH_SHORT).show();
                }
            });
        });


        chatAdapter.setOnChatClickListener(chat -> {
            Intent intent = new Intent(SupportChatListActivity.this, SupportChatActivity.class);
            intent.putExtra("chatId", chat.getChatId());
            startActivity(intent);
        });

        createNewChatButton.setOnClickListener(v -> createNewChat());


    }

    private void createNewChat() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }

        SupportChat newChat = new SupportChat();
        newChat.setUserId(userId);
        newChat.setAdminId(null);
        newChat.setStatus("open");
        newChat.setCreatedAt(System.currentTimeMillis());
        newChat.setMessages(new ArrayList<>());

        chatRepository.createNewChat(newChat)
                .thenRun(() -> {
                    String chatId = newChat.getChatId();
                    if (chatId != null) {
                        Log.d("SupportChatListActivity", "New chat created with ID: " + chatId);
                        Intent intent = new Intent(SupportChatListActivity.this, SupportChatActivity.class);
                        intent.putExtra("chatId", chatId);
                        startActivity(intent);
                    } else {
                        Toast.makeText(SupportChatListActivity.this, "Failed to get chat ID", Toast.LENGTH_SHORT).show();
                    }
                })
                .exceptionally(throwable -> {
                    Log.e("SupportChatListActivity", "Error creating chat: " + throwable.getMessage());
                    runOnUiThread(() -> {
                        Toast.makeText(SupportChatListActivity.this, "Failed to create chat: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    return null;
                });
    }


}


