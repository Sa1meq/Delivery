package com.example.delivery.repository;

import com.example.delivery.model.CourierLocation;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.concurrent.CompletableFuture;

public class CourierLocationRepository {

    private final FirebaseFirestore db;

    public CourierLocationRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public CompletableFuture<CourierLocation> getCourierLocationByOrderId(String orderId) {
        CompletableFuture<CourierLocation> future = new CompletableFuture<>();

        db.collection("courierLocations")
                .whereEqualTo("orderId", orderId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            CourierLocation location = doc.toObject(CourierLocation.class);
                            future.complete(location);
                            return;
                        }
                    }
                    future.complete(null);
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }
}
