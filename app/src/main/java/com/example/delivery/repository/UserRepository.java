package com.example.delivery.repository;

import com.example.delivery.model.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.concurrent.CompletableFuture;

public class UserRepository {
    private final CollectionReference usersCollection;
    private final FirebaseFirestore db;

    public UserRepository(FirebaseFirestore db) {
        this.db = db;
        this.usersCollection = db.collection("users");
    }

    public CompletableFuture<User> addUser(String name, String email, String password) {
        CompletableFuture<User> future = new CompletableFuture<>();
        String userId = usersCollection.document().getId();
        User user = new User(userId, name, email, password);

        usersCollection.document(userId).set(user)
                .addOnSuccessListener(aVoid -> future.complete(user))
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    public CompletableFuture<User> getUserById(String id) {
        CompletableFuture<User> future = new CompletableFuture<>();
        usersCollection.document(id).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        User user = task.getResult().toObject(User.class);
                        future.complete(user);
                    } else {
                        future.complete(null);
                    }
                });
        return future;
    }

    public CompletableFuture<User> getUserByEmail(String email) {
        CompletableFuture<User> future = new CompletableFuture<>();
        usersCollection.whereEqualTo("email", email).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User user = document.toObject(User.class);
                            future.complete(user);
                        }
                    } else {
                        future.complete(null);
                    }
                });
        return future;
    }


    public CompletableFuture<Boolean> deleteUserById(String id, FirebaseStorage firebaseStorage) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        usersCollection.document(id).delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        StorageReference avatarRef = firebaseStorage.getReference().child("avatars/" + id + ".jpg");
                        avatarRef.delete()
                                .addOnCompleteListener(task1 -> future.complete(true))
                                .addOnFailureListener(e -> future.complete(false));
                    } else {
                        future.complete(false);
                    }
                });
        return future;
    }
}