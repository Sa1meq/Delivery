package com.example.delivery.repository;

import com.example.delivery.model.SupportMessage;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.android.gms.tasks.Task;

public class SupportMessageRepository {
    private final FirebaseFirestore firestore;
    private final CollectionReference messagesCollection;

    public SupportMessageRepository(FirebaseFirestore firestore) {
        this.firestore = firestore;
        this.messagesCollection = firestore.collection("supportMessages");
    }

    public Task<Void> sendMessage(String chatId, SupportMessage message) {
        DocumentReference chatDoc = firestore.collection("supportChats").document(chatId);
        return chatDoc.update("messages", FieldValue.arrayUnion(message)).continueWith(task -> {
            if (task.isSuccessful()) {
                return null;
            } else {
                throw task.getException();
            }
        });
    }
}
