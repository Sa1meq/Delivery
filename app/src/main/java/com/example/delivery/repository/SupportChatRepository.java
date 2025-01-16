package com.example.delivery.repository;

import android.util.Log;

import com.example.delivery.model.SupportChat;
import com.example.delivery.model.SupportMessage;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.util.Consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SupportChatRepository {
    private final FirebaseFirestore firestore;
    private final CollectionReference chatsCollection;

    public SupportChatRepository(FirebaseFirestore firestore) {
        this.firestore = firestore;
        this.chatsCollection = firestore.collection("supportChats");
    }

    public interface Callback<T> {
        void onComplete(T result);
    }


    public CompletableFuture<List<SupportChat>> getUserChats(String userId) {
        CompletableFuture<List<SupportChat>> future = new CompletableFuture<>();
        chatsCollection.whereEqualTo("userId", userId).get()
                .addOnSuccessListener(querySnapshot -> {
                    List<SupportChat> chats = querySnapshot.toObjects(SupportChat.class);
                    future.complete(chats);
                })
                .addOnFailureListener(e -> future.completeExceptionally(new RuntimeException("Ошибка получения чатов: " + e.getMessage())));
        return future;
    }

    // Получить чат по chatId
    public CompletableFuture<SupportChat> getChatById(String chatId) {
        CompletableFuture<SupportChat> future = new CompletableFuture<>();
        chatsCollection.document(chatId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    SupportChat chat = documentSnapshot.toObject(SupportChat.class);
                    future.complete(chat);
                })
                .addOnFailureListener(e -> future.completeExceptionally(new RuntimeException("Ошибка получения чата: " + e.getMessage())));
        return future;
    }

    public CompletableFuture<List<SupportMessage>> loadOldMessages(String chatId) {
        CompletableFuture<List<SupportMessage>> future = new CompletableFuture<>();
        firestore.collection("supportChats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<SupportMessage> messages = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        messages.add(doc.toObject(SupportMessage.class));
                    }
                    future.complete(messages);
                })
                .addOnFailureListener(e -> future.completeExceptionally(e));
        return future;
    }

    public ListenerRegistration getChatMessagesListener(String chatId, Callback<List<SupportMessage>> callback) {
        return firestore.collection("supportChats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp")
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        e.printStackTrace();
                        return;
                    }
                    if (querySnapshot != null) {
                        List<SupportMessage> newMessages = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            newMessages.add(doc.toObject(SupportMessage.class));
                        }
                        callback.onComplete(newMessages);
                    }
                });
    }



    public CompletableFuture<Void> closeChat(String chatId) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        DocumentReference chatDoc = chatsCollection.document(chatId);

        // Обновляем статус чата на "closed"
        chatDoc.update("status", "closed")
                .addOnSuccessListener(aVoid -> future.complete(null))
                .addOnFailureListener(e -> future.completeExceptionally(new RuntimeException("Ошибка закрытия чата: " + e.getMessage())));

        return future;
    }


    public CompletableFuture<SupportChat> createNewChat(SupportChat newChat) {
        CompletableFuture<SupportChat> future = new CompletableFuture<>();

        // Генерируем новый chatId
        String chatId = chatsCollection.document().getId();
        newChat.setChatId(chatId);

        // Добавляем новый чат с явным chatId
        chatsCollection.document(chatId)
                .set(newChat)
                .addOnSuccessListener(aVoid -> {
                    Log.d("SupportChatRepository", "Chat added with ID: " + chatId);
                    future.complete(newChat);
                })
                .addOnFailureListener(e -> {
                    Log.e("SupportChatRepository", "Error adding chat: " + e.getMessage());
                    future.completeExceptionally(new RuntimeException("Ошибка добавления чата: " + e.getMessage()));
                });

        return future;
    }

    public CompletableFuture<Void> addMessageToChat(String chatId, SupportMessage message) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        DocumentReference chatDoc = chatsCollection.document(chatId);
        chatDoc.update("messages", FieldValue.arrayUnion(message))
                .addOnSuccessListener(aVoid -> future.complete(null))
                .addOnFailureListener(e -> future.completeExceptionally(new RuntimeException("Ошибка добавления сообщения: " + e.getMessage())));
        return future;
    }

    public CompletableFuture<List<SupportChat>> getOpenChats() {
        CompletableFuture<List<SupportChat>> future = new CompletableFuture<>();
        chatsCollection.whereEqualTo("status", "open").get()
                .addOnSuccessListener(querySnapshot -> {
                    List<SupportChat> chats = querySnapshot.toObjects(SupportChat.class);
                    future.complete(chats);
                })
                .addOnFailureListener(e -> future.completeExceptionally(new RuntimeException("Ошибка получения открытых чатов: " + e.getMessage())));
        return future;
    }


    // Удалить чат (пример дополнительного метода)
    public CompletableFuture<Void> deleteChat(String chatId) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        chatsCollection.document(chatId).delete()
                .addOnSuccessListener(aVoid -> future.complete(null))
                .addOnFailureListener(e -> future.completeExceptionally(new RuntimeException("Ошибка удаления чата: " + e.getMessage())));
        return future;
    }
}
