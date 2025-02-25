package com.example.delivery;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.delivery.adapter.ChatAdapter;
import com.example.delivery.model.SupportChat;
import com.example.delivery.repository.SupportChatRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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

        chatRepository = new SupportChatRepository();
        loadingDialog = new LoadingDialog(this);

        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        chatAdapter = new ChatAdapter(this, chatList, new ChatAdapter.ChatClickListener() {
            @Override
            public void onChatClick(SupportChat chat) {
                if (chat.getStatus().equals("closed")){
                    return;
                }
                Intent intent = new Intent(SupportChatListActivity.this, SupportChatActivity.class);
                intent.putExtra("CHAT_ID", chat.getId());
                startActivity(intent);
                finish();
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
        ImageView backImageView = findViewById(R.id.backImageView);
        backImageView.setOnClickListener(v -> {
            Intent intent = new Intent(SupportChatListActivity.this, SupportActivity.class);
            startActivity(intent);
            finish();
        });
    }

    public void onClickAddChat() {
        Intent intent = new Intent(SupportChatListActivity.this, CreateSupportChatActivity.class);
        intent.putExtra("isComplaintFlow", false);
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

        Query query = chatRepository.getChatsQuery(userId);
        query.get().addOnCompleteListener(task -> {
            loadingDialog.dismiss();

            if (task.isSuccessful()) {
                chatList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    SupportChat chat = document.toObject(SupportChat.class);
                    chatList.add(chat);
                }
                chatAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Ошибка загрузки чатов", Toast.LENGTH_SHORT).show();
                Log.e("SupportChatList", "Ошибка загрузки чатов", task.getException());
            }
        });
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
                        Log.e("SupportChatList", "Ошибка удаления чата", task.getException());
                    }
                });
    }
}

