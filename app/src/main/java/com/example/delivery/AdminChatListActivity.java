package com.example.delivery;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.delivery.R;
import com.example.delivery.adapter.AdminChatAdapter;
import com.example.delivery.model.SupportChat;
import com.example.delivery.repository.SupportChatRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class AdminChatListActivity extends AppCompatActivity {
    private RecyclerView adminChatRecyclerView;
    private SupportChatRepository chatRepository;
    private AdminChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_chat_list);

        adminChatRecyclerView = findViewById(R.id.adminChatRecyclerView);
        adminChatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        chatRepository = new SupportChatRepository(FirebaseFirestore.getInstance());

        chatRepository.getOpenChats().thenAccept(chats -> {
            chatAdapter = new AdminChatAdapter(chats, chat -> {
                // Открыть выбранный чат
                openChat(chat);
            });
            adminChatRecyclerView.setAdapter(chatAdapter);
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    private void openChat(SupportChat chat) {
        Intent intent = new Intent(this, AdminChatActivity.class);
        intent.putExtra("chatId", chat.getChatId());
        startActivity(intent);
    }

}
