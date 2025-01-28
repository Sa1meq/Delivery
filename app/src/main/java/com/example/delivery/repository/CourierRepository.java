package com.example.delivery.repository;

import android.util.Log;

import com.example.delivery.model.Courier;
import com.example.delivery.model.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CourierRepository {
    public final CollectionReference courierCollection;
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

    public CompletableFuture<Boolean> updateCourierRating(String courierId, float newRating) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        courierCollection.document(courierId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Courier courier = task.getResult().toObject(Courier.class);
                if (courier != null) {
                    int currentRatingCount = courier.getRatingCount();
                    float currentRating = courier.getRating();

                    float updatedRating = ((currentRating * currentRatingCount) + newRating) / (currentRatingCount + 1);
                    courier.setRating(updatedRating);
                    courier.setRatingCount(currentRatingCount + 1);


                    courierCollection.document(courierId).set(courier)
                            .addOnSuccessListener(aVoid -> future.complete(true))
                            .addOnFailureListener(future::completeExceptionally);
                } else {
                    future.complete(false);
                }
            } else {
                future.complete(false);
            }
        }).addOnFailureListener(future::completeExceptionally);

        return future;
    }

    public CompletableFuture<List<Courier>> getPendingCouriers() {
        CompletableFuture<List<Courier>> future = new CompletableFuture<>();
        courierCollection.whereEqualTo("isVerified", false).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            List<Courier> couriers = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Courier courier = document.toObject(Courier.class);
                                couriers.add(courier);
                            }
                            future.complete(couriers);
                        } else {
                            future.complete(new ArrayList<>());
                        }
                    } else {
                        future.completeExceptionally(new Exception("Error fetching couriers"));
                    }
                });
        return future;
    }


    public CompletableFuture<Boolean> updateCourierVerification(String courierId, boolean isVerified) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        courierCollection.document(courierId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Courier courier = task.getResult().toObject(Courier.class);
                        if (courier != null) {
                            courier.setVerified(isVerified);
                            courierCollection.document(courierId).set(courier)
                                    .addOnSuccessListener(aVoid -> future.complete(true))
                                    .addOnFailureListener(e -> future.complete(false));
                        } else {
                            future.complete(false);
                        }
                    } else {
                        future.complete(false);
                    }
                });
        return future;
    }

    public CompletableFuture<Boolean> blockCourier(String courierId, long blockDuration) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        courierCollection.document(courierId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Courier courier = task.getResult().toObject(Courier.class);
                if (courier != null) {
                    long currentTime = System.currentTimeMillis();
                    courier.setBlockedUntil(currentTime + blockDuration);
                    courier.setStatus("blocked");

                    courierCollection.document(courierId).set(courier)
                            .addOnSuccessListener(aVoid -> future.complete(true))
                            .addOnFailureListener(future::completeExceptionally);
                } else {
                    future.complete(false);
                }
            } else {
                future.complete(false);
            }
        }).addOnFailureListener(future::completeExceptionally);
        return future;
    }

    public CompletableFuture<Boolean> unblockCourier(String courierId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        courierCollection.document(courierId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Courier courier = task.getResult().toObject(Courier.class);
                if (courier != null) {
                    long currentTime = System.currentTimeMillis();
                    if (courier.getBlockedUntil() > currentTime) {
                        courier.setStatus("active");
                        courier.setBlockedUntil(0);

                        courierCollection.document(courierId).set(courier)
                                .addOnSuccessListener(aVoid -> future.complete(true))
                                .addOnFailureListener(future::completeExceptionally);
                    } else {
                        future.complete(false);
                    }
                } else {
                    future.complete(false); // Если курьер не найден
                }
            } else {
                future.complete(false); // Ошибка при получении данных
            }
        }).addOnFailureListener(future::completeExceptionally);

        return future;
    }


    public CompletableFuture<Boolean> updateCourierBonusPoints(String courierId, int bonusPoints) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        courierCollection.document(courierId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Courier courier = task.getResult().toObject(Courier.class);
                if (courier != null) {
                    courier.setBonusPoints(courier.getBonusPoints() + bonusPoints);
                    courierCollection.document(courierId).set(courier)
                            .addOnSuccessListener(aVoid -> future.complete(true))
                            .addOnFailureListener(future::completeExceptionally);
                } else {
                    future.complete(false);
                }
            } else {
                future.complete(false);
            }
        }).addOnFailureListener(future::completeExceptionally);
        return future;
    }

    public CompletableFuture<Boolean> updateCourierEarnings(String courierId, double earnings) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        courierCollection.document(courierId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Courier courier = task.getResult().toObject(Courier.class);
                if (courier != null) {
                    courier.setBalance(courier.getBalance() + earnings);
                    courierCollection.document(courierId).set(courier)
                            .addOnSuccessListener(aVoid -> future.complete(true))
                            .addOnFailureListener(future::completeExceptionally);
                } else {
                    future.complete(false);
                }
            } else {
                future.complete(false);
            }
        }).addOnFailureListener(future::completeExceptionally);
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