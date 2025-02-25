package com.example.delivery;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.delivery.adapter.ChatUserCourierAdapter;
import com.example.delivery.model.ChatMessage;
import com.example.delivery.repository.ChatRepository;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Query;

public class ChatActivity extends AppCompatActivity {
    private ChatUserCourierAdapter adapter;
    private ChatRepository chatRepository;
    private String orderId;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        orderId = getIntent().getStringExtra("orderId");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        chatRepository = new ChatRepository();

        setupRecyclerView();
        setupSendButton();
    }

    private void setupRecyclerView() {
        Query query = chatRepository.getMessagesQuery(orderId);
        FirestoreRecyclerOptions<ChatMessage> options = new FirestoreRecyclerOptions.Builder<ChatMessage>()
                .setQuery(query, ChatMessage.class)
                .build();

        adapter = new ChatUserCourierAdapter(options, currentUserId);

        RecyclerView recyclerView = findViewById(R.id.chat_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
    }

    private void setupSendButton() {
        EditText inputMessage = findViewById(R.id.input_message);
        findViewById(R.id.send_button).setOnClickListener(v -> {
            String messageText = inputMessage.getText().toString().trim();
            if (!messageText.isEmpty()) {
                chatRepository.sendMessage(orderId, messageText)
                        .addOnSuccessListener(documentReference -> inputMessage.setText(""))
                        .addOnFailureListener(e -> Toast.makeText(
                                ChatActivity.this,
                                "Ошибка отправки: " + e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}