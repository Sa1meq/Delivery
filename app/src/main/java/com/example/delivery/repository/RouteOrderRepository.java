package com.example.delivery.repository;


import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.example.delivery.model.Courier;
import com.example.delivery.model.RouteOrder;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
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
                                // Обновляем баланс и бонусы
                                updateCourierBalance(courierId, estimatedCost)
                                        .thenCompose(aVoid2 -> updateCourierStatsAndBonus(courierId))
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


    public CompletableFuture<List<RouteOrder>> getOrdersByCourierId(String courierId) {
        CompletableFuture<List<RouteOrder>> future = new CompletableFuture<>();
        Query queryCancelled = firestore.collection("routeOrders")
                .whereEqualTo("courierId", courierId)
                .whereEqualTo("cancelled", true);

        Query queryCompleted = firestore.collection("routeOrders")
                .whereEqualTo("courierId", courierId)
                .whereEqualTo("isCompleted", true);

        Task<List<RouteOrder>> taskCancelled = queryCancelled.get().continueWith(task -> {
            if (task.isSuccessful()) {
                return task.getResult().toObjects(RouteOrder.class);
            } else {
                throw task.getException();
            }
        });

        Task<List<RouteOrder>> taskCompleted = queryCompleted.get().continueWith(task -> {
            if (task.isSuccessful()) {
                return task.getResult().toObjects(RouteOrder.class);
            } else {
                throw task.getException();
            }
        });

        Tasks.whenAllSuccess(taskCancelled, taskCompleted).addOnSuccessListener(results -> {
            List<RouteOrder> orders = new ArrayList<>();

            if (results.get(0) != null) {
                orders.addAll((List<RouteOrder>) results.get(0));
            }

            if (results.get(1) != null) {
                orders.addAll((List<RouteOrder>) results.get(1));
            }

            List<RouteOrder> uniqueOrders = orders.stream()
                    .distinct()
                    .collect(Collectors.toList());

            future.complete(uniqueOrders);
        }).addOnFailureListener(future::completeExceptionally);

        return future;
    }


    private CompletableFuture<Void> updateCourierStatsAndBonus(String courierId) {
        return CompletableFuture.supplyAsync(() -> {
            CourierRepository courierRepo = new CourierRepository(FirebaseFirestore.getInstance());

            // Получаем текущие данные курьера
            Courier courier = courierRepo.getCourierById(courierId).join();

            if (courier != null) {
                // Обновляем счетчики
                int daily = courier.getDailyCompletedOrders() + 1;
                int total = courier.getTotalCompletedOrders() + 1;

                // Рассчитываем бонусы
                int bonus = daily >= 5 ? 15 : 10;

                // Обновляем данные
                courier.setDailyCompletedOrders(daily);
                courier.setTotalCompletedOrders(total);
                courier.setBonusPoints(courier.getBonusPoints() + bonus);

                courierRepo.updateCourierStats(courierId, daily, total, bonus).join();
            }
            return null;
        });
    }

    public CompletableFuture<RouteOrder> getActiveOrderForCourier(String courierId) {
        CompletableFuture<RouteOrder> future = new CompletableFuture<>();

        firestore.collection("routeOrders")
                .whereEqualTo("courierId", courierId)
                .whereEqualTo("isActive", true)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        RouteOrder order = query.getDocuments().get(0).toObject(RouteOrder.class);
                        future.complete(order);
                    } else {
                        future.complete(null);
                    }
                });

        return future;
    }


    public CompletableFuture<List<RouteOrder>> getAllPendingRouteOrdersForCourier(String courierId) {
        CompletableFuture<List<RouteOrder>> future = new CompletableFuture<>();
        Query query = firestore.collection("routeOrders")
                .whereEqualTo("isAccepted", false)
                .whereEqualTo("courierId", "unassigned")
                .whereEqualTo("cancelled", false);
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
        Query queryCancelled = firestore.collection("routeOrders")
                .whereEqualTo("userId", userId)
                .whereEqualTo("cancelled", true);

        Query queryCompleted = firestore.collection("routeOrders")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isCompleted", true);

        Task<List<RouteOrder>> taskCancelled = queryCancelled.get().continueWith(task -> {
            if (task.isSuccessful()) {
                return task.getResult().toObjects(RouteOrder.class);
            } else {
                throw task.getException();
            }
        });

        Task<List<RouteOrder>> taskCompleted = queryCompleted.get().continueWith(task -> {
            if (task.isSuccessful()) {
                return task.getResult().toObjects(RouteOrder.class);
            } else {
                throw task.getException();
            }
        });

        Tasks.whenAllSuccess(taskCancelled, taskCompleted).addOnSuccessListener(results -> {
            List<RouteOrder> orders = new ArrayList<>();

            if (results.get(0) != null) {
                orders.addAll((List<RouteOrder>) results.get(0));
            }

            if (results.get(1) != null) {
                orders.addAll((List<RouteOrder>) results.get(1));
            }

            List<RouteOrder> uniqueOrders = orders.stream()
                    .distinct()
                    .collect(Collectors.toList());

            future.complete(uniqueOrders);
        }).addOnFailureListener(future::completeExceptionally);

        return future;
    }

    public CompletableFuture<List<RouteOrder>> getActiveOrdersByUserId(String userId) {
        CompletableFuture<List<RouteOrder>> future = new CompletableFuture<>();
        Query query = firestore.collection("routeOrders")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .whereEqualTo("cancelled", false)
                .whereEqualTo("isCompleted", false);
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<RouteOrder> orders = queryDocumentSnapshots.toObjects(RouteOrder.class);
            future.complete(orders);
        }).addOnFailureListener(future::completeExceptionally);

        return future;
    }

    public CompletableFuture<Void> cancelOrder(String orderId) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        DocumentReference docRef = firestore.collection("routeOrders").document(orderId);

        docRef.update("isActive", false, "cancelled", true, "isAccepted", false)
                .addOnSuccessListener(aVoid -> {
                    future.complete(null);
                })
                .addOnFailureListener(e -> {
                    future.completeExceptionally(e);
                });

        return future;
    }

}