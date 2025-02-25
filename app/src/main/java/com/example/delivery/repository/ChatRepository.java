package com.example.delivery.repository;

import com.example.delivery.model.ChatMessage;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatRepository {
    private final FirebaseFirestore firestore;
    private final CollectionReference chatsCollection;

    public ChatRepository() {
        firestore = FirebaseFirestore.getInstance();
        chatsCollection = firestore.collection("chats");
    }

    public Task<DocumentReference> sendMessage(String orderId, String text) {
        String senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> message = new HashMap<>();
        message.put("orderId", orderId);
        message.put("senderId", senderId);
        message.put("text", text);
        message.put("timestamp", new Timestamp(new Date()));

        return chatsCollection.document(orderId)
                .collection("messages")
                .add(message);
    }

    // Получение сообщений в реальном времени
    public Query getMessagesQuery(String orderId) {
        return chatsCollection.document(orderId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING);
    }

    // Проверка существования чата
    public Task<QuerySnapshot> checkChatExists(String orderId) {
        return chatsCollection.document(orderId)
                .collection("messages")
                .limit(1)
                .get();
    }
}