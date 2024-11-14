package com.example.delivery.repository;

import com.example.delivery.model.Courier;
import com.example.delivery.model.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.concurrent.CompletableFuture;

public class CourierRepository {
    private final CollectionReference courierCollection;
    private final FirebaseFirestore db;

    public CourierRepository(FirebaseFirestore db) {
        this.db = db;
        this.courierCollection = db.collection("couriers");
    }

    public CompletableFuture<Courier> addCourier(Courier courier, String userId) {
        CompletableFuture<Courier> future = new CompletableFuture<>();
        courier.id = userId;
        courierCollection.document(userId).set(courier)
                .addOnSuccessListener(aVoid -> future.complete(courier))
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }




    public CompletableFuture<Courier> getCourierById(String id) {
        CompletableFuture<Courier> future = new CompletableFuture<>();
        courierCollection.document(id).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Courier user = task.getResult().toObject(Courier.class);
                        future.complete(user);
                    } else {
                        future.complete(null);
                    }
                });
        return future;
    }

    public CompletableFuture<Courier> getCourierByPhone(String phone) {
        CompletableFuture<Courier> future = new CompletableFuture<>();
        courierCollection.whereEqualTo("phone", phone).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Courier courier = document.toObject(Courier.class);
                            future.complete(courier);
                        }
                    } else {
                        future.complete(null);
                    }
                });
        return future;
    }

    public CompletableFuture<String> getCourierTypeByUid(String courierUid) {
        CompletableFuture<String> future = new CompletableFuture<>();
        courierCollection.document(courierUid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Courier courier = task.getResult().toObject(Courier.class);
                        if (courier != null) {
                            future.complete(courier.getTypeOfCourier());
                        } else {
                            future.completeExceptionally(new Exception("Courier not found"));
                        }
                    } else {
                        future.completeExceptionally(new Exception("Failed to get courier type"));
                    }
                });
        return future;
    }


    public CompletableFuture<Boolean> deleteCourierById(String id, FirebaseStorage firebaseStorage) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        courierCollection.document(id).delete()
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