package com.example.delivery;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.delivery.adapter.SupportMessageAdapter;
import com.example.delivery.model.SupportMessage;
import com.example.delivery.repository.SupportChatRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SupportChatActivity extends AppCompatActivity {

    private RecyclerView messageRecyclerView;
    private EditText messageEditText;
    private ImageButton sendMessageButton;
    private SupportMessageAdapter messageAdapter;
    private List<SupportMessage> messages;
    private String chatId;
    private String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_chat);

        chatId = getIntent().getStringExtra("chatId");
        currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        messages = new ArrayList<>();
        messageAdapter = new SupportMessageAdapter(messages, currentUser);

        messageRecyclerView = findViewById(R.id.messageRecyclerView);
        messageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageRecyclerView.setAdapter(messageAdapter);

        messageEditText = findViewById(R.id.messageEditText);
        sendMessageButton = findViewById(R.id.sendMessageButton);

        SupportChatRepository chatRepository = new SupportChatRepository(FirebaseFirestore.getInstance());

        chatRepository.getChatById(chatId).thenAccept(chat -> {
            runOnUiThread(() -> {
                messages.addAll(chat.getMessages());
                messageAdapter.notifyDataSetChanged();
                messageRecyclerView.scrollToPosition(messages.size() - 1);
            });
        });

        sendMessageButton.setOnClickListener(v -> {
            String messageText = messageEditText.getText().toString().trim();
            if (!messageText.isEmpty()) {
                SupportMessage newMessage = new SupportMessage(currentUser, messageText, System.currentTimeMillis());
                chatRepository.addMessageToChat(chatId, newMessage).thenRun(() -> {
                    runOnUiThread(() -> {
                        messages.add(newMessage);
                        messageAdapter.notifyItemInserted(messages.size() - 1);
                        messageRecyclerView.scrollToPosition(messages.size() - 1);
                        messageEditText.setText("");
                    });
                });
            }
        });
    }
}
