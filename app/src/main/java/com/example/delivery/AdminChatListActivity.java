package com.example.delivery;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.delivery.adapter.ChatAdapter;
import com.example.delivery.model.SupportChat;
import com.example.delivery.repository.SupportChatRepository;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminChatListActivity extends AppCompatActivity {

    private SupportChatRepository chatRepository;
    private ChatAdapter chatAdapter;
    private final List<SupportChat> chatList = new ArrayList<>();
    private RecyclerView chatRecyclerView;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_chat_list);

        chatRepository = new SupportChatRepository();
        loadingDialog = new LoadingDialog(this);

        chatRecyclerView = findViewById(R.id.recycler_chat_list);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

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

        chatRecyclerView.setAdapter(chatAdapter);

        // Загрузка чатов
        loadChats();
        ImageView backImageView = findViewById(R.id.backImageView);
        backImageView.setOnClickListener(v -> {
            Intent intent = new Intent(AdminChatListActivity.this, AdminPanel.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChats();
    }

    private void loadChats() {
        loadingDialog.setMessage("Загрузка чатов...");
        loadingDialog.show();

        // Запрос всех открытых чатов
        Query query = chatRepository.getAllOpenChatsQuery();
        query.get().addOnCompleteListener(task -> {
            loadingDialog.dismiss();

            if (task.isSuccessful()) {
                chatList.clear(); // Очищаем старый список
                for (QueryDocumentSnapshot document : task.getResult()) {
                    SupportChat chat = document.toObject(SupportChat.class);
                    chat.setId(document.getId()); // Устанавливаем ID чата
                    chatList.add(chat);
                }
                chatAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Ошибка загрузки чатов", Toast.LENGTH_SHORT).show();
                Log.e("AdminChatList", "Ошибка загрузки чатов", task.getException());
            }
        });
    }

    private void openChat(SupportChat chat) {
        if (chat != null) {
            Intent intent = new Intent(this, AdminChatActivity.class);
            intent.putExtra("CHAT_ID", chat.getId());
            startActivity(intent);
        } else {
            Toast.makeText(this, "Ошибка: чат не найден", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteChat(@NonNull SupportChat chat) {
        loadingDialog.setMessage("Удаление чата...");
        loadingDialog.show();

        chatRepository.deleteChatById(chat.getId())
                .addOnCompleteListener(task -> {
                    loadingDialog.dismiss();
                    if (task.isSuccessful()) {
                        chatList.remove(chat);
                        chatAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "Чат удален", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Ошибка удаления чата", Toast.LENGTH_SHORT).show();
                        Log.e("AdminChatList", "Ошибка удаления чата", task.getException());
                    }
                });
    }
}