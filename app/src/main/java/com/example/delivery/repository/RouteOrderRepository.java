package com.example.delivery.repository;


import android.util.Log;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.example.delivery.model.RouteOrder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;

public class RouteOrderRepository {
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public CompletableFuture<Void> saveRouteOrder(RouteOrder routeOrder) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        firestore.collection("routeOrders")
                .document(routeOrder.orderId)
                .set(routeOrder)
                .addOnSuccessListener(aVoid -> future.complete(null))
                .addOnFailureListener(e -> future.completeExceptionally(e));
        return future;
    }

    public CompletableFuture<RouteOrder> getRouteOrderById(String orderId) {
        CompletableFuture<RouteOrder> future = new CompletableFuture<>();
        firestore.collection("routeOrders").document(orderId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        RouteOrder routeOrder = task.getResult().toObject(RouteOrder.class);
                        future.complete(routeOrder);
                    } else {
                        future.complete(null);
                    }
                });
        return future;
    }




    public CompletableFuture<Void> updateCourierForOrder(String orderId, String courierId) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        DocumentReference docRef = firestore.collection("routeOrders").document(orderId);
        docRef.update("courierId", courierId, "isAccepted", true)
                .addOnSuccessListener(aVoid -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    public CompletableFuture<Void> completeOrder(String orderId) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        DocumentReference docRef = firestore.collection("routeOrders").document(orderId);
        docRef.update("isCompleted", true, "isAccepted", false, "isActive", false)
                .addOnSuccessListener(aVoid -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }



    public CompletableFuture<List<RouteOrder>> getAllPendingRouteOrdersForCourier(String courierId) {
        CompletableFuture<List<RouteOrder>> future = new CompletableFuture<>();
        Query query = firestore.collection("routeOrders")
                .whereEqualTo("isAccepted", false)
                .whereEqualTo("courierId", "unassigned");

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<RouteOrder> routeOrders = queryDocumentSnapshots.toObjects(RouteOrder.class);
            Log.d("Firestore", "Получено заказов: " + routeOrders.size());
            future.complete(routeOrders);
        }).addOnFailureListener(future::completeExceptionally);
        return future;
    }


    public CompletableFuture<List<RouteOrder>> getOrdersByUserId(String userId) {
        CompletableFuture<List<RouteOrder>> future = new CompletableFuture<>();
        Query query = firestore.collection("routeOrders")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isCompleted", true);
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<RouteOrder> orders = queryDocumentSnapshots.toObjects(RouteOrder.class);
            future.complete(orders);
        }).addOnFailureListener(future::completeExceptionally);
        return future;
    }

    public CompletableFuture<List<RouteOrder>> getActiveOrdersByUserId(String userId) {
        CompletableFuture<List<RouteOrder>> future = new CompletableFuture<>();
        Query query = firestore.collection("routeOrders")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .whereEqualTo("isCompleted", false);
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<RouteOrder> orders = queryDocumentSnapshots.toObjects(RouteOrder.class);
            future.complete(orders);
        }).addOnFailureListener(future::completeExceptionally);
        return future;
    }

}