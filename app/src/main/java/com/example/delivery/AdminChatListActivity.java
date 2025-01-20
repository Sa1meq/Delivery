package com.example.delivery;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.delivery.adapter.ChatAdapter;
import com.example.delivery.model.SupportChat;
import com.example.delivery.repository.SupportChatRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AdminChatListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<SupportChat> chatList = new ArrayList<>();
    private SupportChatRepository chatRepository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_chat_list);

        recyclerView = findViewById(R.id.recycler_chat_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        chatRepository = new SupportChatRepository(FirebaseFirestore.getInstance());
        chatAdapter = new ChatAdapter(this, chatList, new ChatAdapter.ChatClickListener() {
            @Override
            public void onChatClick(SupportChat chat) {
                openChat(chat);
            }

            @Override
            public void onDeleteClick(SupportChat chat) {
                deleteChat(chat);
            }
        });

        recyclerView.setAdapter(chatAdapter);

        loadChats();
    }

    private void loadChats() {
        CompletableFuture<List<SupportChat>> future = chatRepository.getAllChats();
        future.thenAccept(chats -> runOnUiThread(() -> {
            chatList.clear();
            chatList.addAll(chats);
            chatAdapter.notifyDataSetChanged();
        })).exceptionally(throwable -> {
            runOnUiThread(() -> {
                Toast.makeText(this, "Ошибка загрузки чатов: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("AdminChatListActivity", "Ошибка загрузки чатов", throwable);
            });
            return null;
        });
    }

    private void openChat(SupportChat chat) {
        Intent intent = new Intent(this, AdminChatActivity.class);
        intent.putExtra("CHAT_ID", chat.getId());
        startActivity(intent);
    }

    private void deleteChat(SupportChat chat) {
        chatRepository.deleteChatById(chat.getId());
        chatList.remove(chat);
        chatAdapter.notifyDataSetChanged();
        Toast.makeText(this, "Чат удален", Toast.LENGTH_SHORT).show();
    }
}
