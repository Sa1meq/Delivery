package com.example.delivery.repository;


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

    // Получение заказа маршрута по ID
    public CompletableFuture<RouteOrder> getRouteOrderById(String orderId) {
        CompletableFuture<RouteOrder> future = new CompletableFuture<>();
        DocumentReference docRef = firestore.collection("routeOrders").document(orderId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            RouteOrder routeOrder = documentSnapshot.toObject(RouteOrder.class);
            future.complete(routeOrder);
        }).addOnFailureListener(future::completeExceptionally);
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

    // Получение всех не принятых маршрутов для курьера
    public CompletableFuture<List<RouteOrder>> getAllPendingRouteOrdersForCourier(String courierId) {
        CompletableFuture<List<RouteOrder>> future = new CompletableFuture<>();
        Query query = firestore.collection("routeOrders")
                .whereEqualTo("isAccepted", false)
                .whereEqualTo("courierId", courierId); // Получаем только заказы для данного курьера

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<RouteOrder> routeOrders = queryDocumentSnapshots.toObjects(RouteOrder.class);
            future.complete(routeOrders);
        }).addOnFailureListener(future::completeExceptionally);
        return future;
    }
}