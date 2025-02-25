package com.example.delivery.repository;

import com.example.delivery.model.SupportChat;
import com.example.delivery.model.SupportMessage;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;

public class SupportChatRepository {
    private final CollectionReference chatsRef;
    private final CollectionReference messagesRef;

    public SupportChatRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        chatsRef = db.collection("support_chats");
        messagesRef = db.collection("support_messages");
    }

    public Query getChatsQuery(String userId) {
        return chatsRef.whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING);
    }

    public Query getAllOpenChatsQuery() {
        return chatsRef.whereEqualTo("status", "open")
                .orderBy("createdAt", Query.Direction.DESCENDING);
    }

    public Task<Void> deleteChatById(String chatId) {
        return chatsRef.document(chatId).delete();
    }

    public Task<Void> createChat(String userId, String topic, String requsetType) {
        String chatId = chatsRef.document().getId();
        SupportChat chat = new SupportChat(chatId, userId, topic, requsetType, "open", new Date());
        return chatsRef.document(chatId).set(chat);
    }

    public DocumentReference getChatRef(String chatId) {
        return chatsRef.document(chatId);
    }

    public Query getMessagesQuery(String chatId) {
        return messagesRef.whereEqualTo("chatId", chatId)
                .orderBy("timestamp", Query.Direction.ASCENDING);
    }

    public Task<DocumentReference> sendMessage(String chatId, String text, String userId, boolean isSupport) {
        SupportMessage message = new SupportMessage(text, userId, chatId, new Date(), isSupport);
        return messagesRef.add(message);
    }

    public ListenerRegistration listenForChats(EventListener<QuerySnapshot> listener) {
        return chatsRef.addSnapshotListener(listener);
    }

    public Task<Void> closeChat(String chatId) {
        return chatsRef.document(chatId).update("status", "closed");
    }
}
