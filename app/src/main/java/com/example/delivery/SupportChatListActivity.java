package com.example.delivery;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.delivery.adapter.ChatAdapter;
import com.example.delivery.model.SupportChat;
import com.example.delivery.repository.SupportChatRepository;
import com.example.delivery.LoadingDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SupportChatListActivity extends AppCompatActivity {

    private SupportChatRepository chatRepository;
    private ChatAdapter chatAdapter;
    private final List<SupportChat> chatList = new ArrayList<>();
    private RecyclerView chatRecyclerView;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_chat_list);

        // Инициализация репозитория и компонентов UI
        chatRepository = new SupportChatRepository(FirebaseFirestore.getInstance());
        loadingDialog = new LoadingDialog(this);

        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        chatAdapter = new ChatAdapter(this, chatList, new ChatAdapter.ChatClickListener() {
            @Override
            public void onChatClick(SupportChat chat) {
                Intent intent = new Intent(SupportChatListActivity.this, SupportChatActivity.class);
                intent.putExtra("CHAT_ID", chat.getId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(SupportChat chat) {
                deleteChat(chat);
            }
        });

        chatRecyclerView.setAdapter(chatAdapter);

        FloatingActionButton addChatButton = findViewById(R.id.add_chat_button);
        addChatButton.setOnClickListener(view -> onClickAddChat());

        loadChats();
    }

    public void onClickAddChat() {
        Intent intent = new Intent(SupportChatListActivity.this, CreateSupportChatActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChats();
    }

    private void loadChats() {
        loadingDialog.setMessage("Загрузка чатов...");
        loadingDialog.show();

        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show();
            loadingDialog.dismiss();
            return;
        }

        chatRepository.getAllChatsByUserId(userId)
                .thenAccept(chats -> {
                    loadingDialog.dismiss();
                    chatList.clear();
                    chatList.addAll(chats);
                    chatAdapter.notifyDataSetChanged();
                })
                .exceptionally(throwable -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Ошибка загрузки чатов", Toast.LENGTH_SHORT).show();
                    Log.e("SupportChatList", "Ошибка загрузки чатов", throwable);
                    return null;
                });
    }

    private void deleteChat(@NonNull SupportChat chat) {
        loadingDialog.setMessage("Удаление чата...");
        loadingDialog.show();

        chatRepository.deleteChatById(chat.getId()); // Изменено на deleteChatById
        CompletableFuture.runAsync(() -> {
            // Успешное удаление
            runOnUiThread(() -> {
                loadingDialog.dismiss();
                chatList.remove(chat);
                chatAdapter.notifyDataSetChanged();
                Toast.makeText(this, "Чат удален", Toast.LENGTH_SHORT).show();
            });
        }).exceptionally(throwable -> {
            // Обработка ошибок
            runOnUiThread(() -> {
                loadingDialog.dismiss();
                Toast.makeText(this, "Ошибка удаления чата", Toast.LENGTH_SHORT).show();
                Log.e("SupportChatList", "Ошибка удаления чата", throwable);
            });
            return null;
        });
    }

}
