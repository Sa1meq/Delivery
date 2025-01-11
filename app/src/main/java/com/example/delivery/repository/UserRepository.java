package com.example.delivery.repository;

import com.example.delivery.model.Card;
import com.example.delivery.model.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.concurrent.CompletableFuture;

public class UserRepository {
    public final CollectionReference usersCollection;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public UserRepository(FirebaseFirestore db, FirebaseAuth auth) {
        this.db = db;
        this.auth = auth;
        this.usersCollection = db.collection("users");
    }

    public CompletableFuture<User> addUser(String name, String email, String password, String balance) {
        CompletableFuture<User> future = new CompletableFuture<>();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            boolean isAdmin = email.equals("sa1mejpn@gmail.com");
                            User user = new User(userId, name, email, password, "0", isAdmin, "");
                            usersCollection.document(userId).set(user)
                                    .addOnSuccessListener(aVoid -> future.complete(user))
                                    .addOnFailureListener(future::completeExceptionally);
                        } else {
                            future.completeExceptionally(new Exception("User registration failed."));
                        }
                    } else {
                        future.completeExceptionally(task.getException());
                    }
                });
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
                        User user = null;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            user = document.toObject(User.class);
                        }
                        future.complete(user);
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

    public CompletableFuture<Boolean> updateUser(String id, User user) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        usersCollection.document(id).set(user)
                .addOnSuccessListener(aVoid -> future.complete(true))
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

}
