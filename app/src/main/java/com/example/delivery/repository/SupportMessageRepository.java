package com.example.delivery.repository;

import com.example.delivery.model.SupportMessage;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SupportMessageRepository {
    private final CollectionReference messageCollection;

    public SupportMessageRepository(FirebaseFirestore db) {
        this.messageCollection = db.collection("supportMessages");
    }

    public SupportMessage addMessage(String content, String senderId, String chatId, boolean isAdmin) {
        String messageId = messageCollection.document().getId();
        SupportMessage message = new SupportMessage(messageId, content, senderId, chatId, com.google.firebase.Timestamp.now(), isAdmin);
        messageCollection.document(messageId).set(message);
        return message;
    }

    public CompletableFuture<List<SupportMessage>> getMessagesByChatId(String chatId) {
        CompletableFuture<List<SupportMessage>> future = new CompletableFuture<>();
        List<SupportMessage> messageList = new ArrayList<>();

        messageCollection.whereEqualTo("chatId", chatId).orderBy("createTime").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            messageList.add(document.toObject(SupportMessage.class));
                        }
                        future.complete(messageList);
                    } else {
                        future.completeExceptionally(task.getException());
                    }
                });

        return future;
    }


    public void deleteMessagesByChatId(String chatId) {
        messageCollection.whereEqualTo("chatId", chatId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            document.getReference().delete();
                        }
                    }
                });
    }

    public void updateMessage(SupportMessage message) {
        messageCollection.document(message.getId()).set(message);
    }
}
