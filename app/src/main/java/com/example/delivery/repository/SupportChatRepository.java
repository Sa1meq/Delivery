package com.example.delivery.repository;

import com.example.delivery.model.SupportChat;
import com.example.delivery.model.SupportMessage;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SupportChatRepository {
    private final CollectionReference chatCollection;

    public SupportChatRepository(FirebaseFirestore db) {
        this.chatCollection = db.collection("supportChats");
    }

    public SupportChat addChat(String topic, String requestType, String userId) {
        String chatId = chatCollection.document().getId();
        SupportChat chat = new SupportChat(chatId, topic, requestType, userId, com.google.firebase.Timestamp.now(), 0, "open");
        chatCollection.document(chatId).set(chat);
        return chat;
    }

    public CompletableFuture<List<SupportChat>> getAllChats() {
        CompletableFuture<List<SupportChat>> future = new CompletableFuture<>();
        List<SupportChat> chatList = new ArrayList<>();

        chatCollection.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            chatList.add(document.toObject(SupportChat.class));
                        }
                        future.complete(chatList);
                    }
                });

        return future;
    }


    public CompletableFuture<List<SupportChat>> getAllChatsByUserId(String userId) {
        CompletableFuture<List<SupportChat>> future = new CompletableFuture<>();
        List<SupportChat> chatList = new ArrayList<>();

        chatCollection.whereEqualTo("userId", userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            chatList.add(document.toObject(SupportChat.class));
                        }
                        future.complete(chatList);
                    }
                });

        return future;
    }

    public void deleteChatById(String chatId) {
        chatCollection.document(chatId).delete();
    }

    public CompletableFuture<SupportChat> getChatById(String chatId) {
        CompletableFuture<SupportChat> future = new CompletableFuture<>();

        chatCollection.document(chatId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        SupportChat chat = documentSnapshot.toObject(SupportChat.class);
                        future.complete(chat);
                    } else {
                        future.completeExceptionally(new Exception("Chat not found"));
                    }
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }


    public void updateChat(SupportChat chat) {
        chatCollection.document(chat.getId()).set(chat);
    }

    public void closeChat(String chatId) {
        chatCollection.document(chatId).update("status", "closed");
    }
}