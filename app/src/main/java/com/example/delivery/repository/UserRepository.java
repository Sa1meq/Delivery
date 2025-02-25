package com.example.delivery.repository;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.delivery.model.Card;
import com.example.delivery.model.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

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

    public CompletableFuture<Boolean> deleteUserById(String userId, Cloudinary cloudinary) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        usersCollection.document(userId).delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        getUserById(userId).thenAccept(user -> {
                            if (user != null && user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                                try {
                                    String publicId = extractPublicIdFromUrl(user.getAvatarUrl());
                                    cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).exceptionally(e -> {
                            e.printStackTrace();
                            future.complete(false);
                            return null;
                        });
                    } else {
                        future.complete(false);
                    }
                });

        return future;
    }

    public CompletableFuture<Boolean> makeUserAdmin(String id) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        getUserById(id).thenAccept(user -> {
            if (user != null) {
                user.setAdmin(true);
                updateUser(id, user)
                        .thenAccept(future::complete)
                        .exceptionally(throwable -> {
                            future.completeExceptionally(throwable);
                            return null;
                        });
            } else {
                future.complete(false);
            }
        });
        return future;
    }

    private String extractPublicIdFromUrl(String url) {
        // Пример URL: https://res.cloudinary.com/demo/image/upload/v1234567890/sample.jpg
        String[] parts = url.split("/");
        String filename = parts[parts.length - 1];
        return filename.substring(0, filename.lastIndexOf("."));
    }


    public CompletableFuture<Boolean> updateUser(String id, User user) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        usersCollection.document(id).set(user)
                .addOnSuccessListener(aVoid -> future.complete(true))
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    public CompletableFuture<Boolean> updateUserPassword(String email, String newPassword) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        usersCollection.whereEqualTo("email", email).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            document.getReference().update("password", newPassword)
                                    .addOnSuccessListener(aVoid -> future.complete(true))
                                    .addOnFailureListener(e -> future.complete(false));
                            break;
                        }
                    } else {
                        future.complete(false);
                    }
                });
        return future;
    }
}
