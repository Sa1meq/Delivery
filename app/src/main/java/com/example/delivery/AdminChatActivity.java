package com.example.delivery;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.delivery.R;
import com.example.delivery.adapter.SupportMessageAdapter;
import com.example.delivery.model.SupportMessage;
import com.example.delivery.repository.SupportChatRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AdminChatActivity extends AppCompatActivity {
    private RecyclerView chatMessagesRecyclerView;
    private EditText messageEditText;
    private Button sendMessageButton;
    private SupportChatRepository chatRepository;
    private List<SupportMessage> messages = new ArrayList<>();
    private String chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_chat);

        chatMessagesRecyclerView = findViewById(R.id.chatMessagesRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendMessageButton = findViewById(R.id.sendMessageButton);

        chatMessagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        chatRepository = new SupportChatRepository(FirebaseFirestore.getInstance());

        chatId = getIntent().getStringExtra("chatId");

        if (chatId == null || chatId.isEmpty()) {
            finish();
            return;
        }

        chatRepository.getChatById(chatId).thenAccept(chat -> {
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
    }
}
