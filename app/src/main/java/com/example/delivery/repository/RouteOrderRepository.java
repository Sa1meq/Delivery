package com.example.delivery.repository;


import android.util.Log;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import com.example.delivery.model.Courier;
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

    public CompletableFuture<Void> updateCourierBalance(String courierId, double amountToAdd) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        DocumentReference courierRef = firestore.collection("couriers").document(courierId);
        courierRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Courier courier = snapshot.toObject(Courier.class);
                if (courier != null) {
                    try {
                        double currentBalance = Double.parseDouble(courier.getBalance());
                        double newBalance = currentBalance + amountToAdd;

                        courier.setBalance(String.format(Locale.US, "%.2f", newBalance));

                        courierRef.set(courier)
                                .addOnSuccessListener(aVoid -> future.complete(null))
                                .addOnFailureListener(future::completeExceptionally);
                    } catch (NumberFormatException e) {
                        future.completeExceptionally(new Exception("Invalid balance format for courier ID: " + courierId, e));
                    }
                } else {
                    future.completeExceptionally(new Exception("Courier not found"));
                }
            } else {
                future.completeExceptionally(new Exception("Courier document does not exist"));
            }
        }).addOnFailureListener(future::completeExceptionally);

        return future;
    }


    public CompletableFuture<Void> completeOrder(String orderId) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        DocumentReference docRef = firestore.collection("routeOrders").document(orderId);

        docRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                RouteOrder routeOrder = snapshot.toObject(RouteOrder.class);
                if (routeOrder != null && routeOrder.getCourierId() != null) {
                    double estimatedCost = routeOrder.getEstimatedCost();
                    String courierId = routeOrder.getCourierId();
                    docRef.update("isCompleted", true, "isAccepted", false, "isActive", false)
                            .addOnSuccessListener(aVoid -> {
                                updateCourierBalance(courierId, estimatedCost)
                                        .thenRun(() -> future.complete(null))
                                        .exceptionally(e -> {
                                            future.completeExceptionally(e);
                                            return null;
                                        });
                            })
                            .addOnFailureListener(future::completeExceptionally);
                } else {
                    future.completeExceptionally(new Exception("Order or courier ID not found"));
                }
            } else {
                future.completeExceptionally(new Exception("Order document does not exist"));
            }
        }).addOnFailureListener(future::completeExceptionally);

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

    public CompletableFuture<Void> setOrderRated(String orderId) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        DocumentReference docRef = firestore.collection("routeOrders").document(orderId);

        docRef.update("isRated", true)
                .addOnSuccessListener(aVoid -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    public CompletableFuture<List<RouteOrder>> getOrdersToRate(String userId) {
        CompletableFuture<List<RouteOrder>> future = new CompletableFuture<>();
        Query query = firestore.collection("routeOrders")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isCompleted", true)
                .whereEqualTo("isRated", false);
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<RouteOrder> ordersToRate = queryDocumentSnapshots.toObjects(RouteOrder.class);
            future.complete(ordersToRate);
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

    public CompletableFuture <List<RouteOrder>> cancelOrder(String orderId){
       CompletableFuture<List<RouteOrder>> future = new CompletableFuture<>();
       Query query = firestore.collection("routeOrders")
               .whereEqualTo("orderId", orderId)
               .whereEqualTo("isActive", false);

        return future;
    }

}