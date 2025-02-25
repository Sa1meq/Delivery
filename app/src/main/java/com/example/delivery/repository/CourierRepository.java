package com.example.delivery.repository;


import android.net.Uri;
import android.util.Log;

import com.example.delivery.model.Courier;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CourierRepository {
    public final CollectionReference courierCollection;
    private final FirebaseFirestore db;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public CourierRepository(FirebaseFirestore db) {
        this.db = db;
        this.courierCollection = db.collection("couriers");
        scheduleDailyReset();
    }

    private static CourierRepository instance;

    public static synchronized CourierRepository getInstance() {
        if (instance == null) {
            instance = new CourierRepository(FirebaseFirestore.getInstance());
        }
        return instance;
    }

    private void scheduleDailyReset() {
        // Рассчитываем время до следующего полуночи
        long now = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long delay = calendar.getTimeInMillis() - now;

        scheduler.scheduleAtFixedRate(this::resetDailyCounters,
                delay,
                TimeUnit.DAYS.toMillis(1),
                TimeUnit.MILLISECONDS);
    }

    private void resetDailyCounters() {
        courierCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    document.getReference().update("dailyCompletedOrders", 0);
                }
            }
        });
    }


    public CompletableFuture<Boolean> addCourier(Courier courier, String userId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        courier.id = userId;
        courierCollection.document(userId).set(courier)
                .addOnSuccessListener(aVoid -> future.complete(true))
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

    public CompletableFuture<Boolean> updateCourierAvatar(String courierId, String avatarUrl) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        courierCollection.document(courierId).update("avatarUrl", avatarUrl)
                .addOnSuccessListener(aVoid -> future.complete(true))
                .addOnFailureListener(future::completeExceptionally);
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

    public CompletableFuture<Boolean> blockCourier(String courierId, long blockDurationMillis) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        courierCollection.document(courierId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Courier courier = task.getResult().toObject(Courier.class);
                if (courier != null) {
                    long currentTime = System.currentTimeMillis();
                    long blockEndTime = currentTime + blockDurationMillis;

                    courier.setBlockedUntil(blockEndTime);
                    courier.setStatus("blocked");

                    courierCollection.document(courierId).set(courier)
                            .addOnSuccessListener(aVoid -> {
                                future.complete(true);
                                scheduleUnblock(courierId, blockEndTime);
                            })
                            .addOnFailureListener(e -> {
                                future.completeExceptionally(e);
                            });
                } else {
                    future.complete(false);
                }
            } else {
                future.complete(false);
            }
        });

        return future;
    }

    private void scheduleUnblock(String courierId, long blockEndTime) {
        long delay = blockEndTime - System.currentTimeMillis();
        if (delay > 0) {
            scheduler.schedule(() -> unblockCourier(courierId), delay, TimeUnit.MILLISECONDS);
        }
    }

    public CompletableFuture<Boolean> unblockCourier(String courierId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        courierCollection.document(courierId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Courier courier = task.getResult().toObject(Courier.class);
                if (courier != null) {
                    courier.setStatus("active"); // Устанавливаем статус "active"
                    courier.setBlockedUntil(0); // Сбрасываем время блокировки

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

    public CompletableFuture<Boolean> updateCourierEnterCode(String courierId, String enterCode) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        courierCollection.document(courierId).update("enterCode", enterCode)
                .addOnSuccessListener(aVoid -> future.complete(true))
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }


    public CompletableFuture<Courier> getCourierByEnterCode(String enterCode) {
        CompletableFuture<Courier> future = new CompletableFuture<>();
        courierCollection.whereEqualTo("enterCode", enterCode).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Courier courier = document.toObject(Courier.class);
                            future.complete(courier);
                            break;
                        }
                    } else {
                        future.complete(null);
                    }
                }).addOnFailureListener(future::completeExceptionally);
        return future;
    }


    public CompletableFuture<Void> updateCourierStats(String courierId, int dailyOrders, int totalOrders, int bonus) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        db.runTransaction(transaction -> {
                    DocumentReference docRef = courierCollection.document(courierId);
                    Courier courier = transaction.get(docRef).toObject(Courier.class);

                    if (courier != null) {
                        courier.setDailyCompletedOrders(dailyOrders);
                        courier.setTotalCompletedOrders(totalOrders);
                        courier.setBonusPoints(courier.getBonusPoints() + bonus);
                        transaction.set(docRef, courier);
                    }
                    return null;
                }).addOnSuccessListener(aVoid -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);

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
                                .addOnFailureListener(e -> {
                                    Log.e("DeleteCourier", "Ошибка удаления аватара", e);
                                    future.complete(true);
                                });
                    } else {
                        future.complete(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DeleteCourier", "Ошибка удаления курьера", e);
                    future.complete(false);
                });

        return future;
    }

    public void shutdown() {
        scheduler.shutdown();
    }

    public CompletableFuture<Boolean> updateCourierTariff(String courierId, double multiplier, long endTime, int bonusDeduction) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        FirebaseFirestore.getInstance().runTransaction(transaction -> {
                    DocumentReference docRef = courierCollection.document(courierId);
                    Courier courier = transaction.get(docRef).toObject(Courier.class);

                    if (courier != null) {
                        courier.setTariffMultiplier(multiplier);
                        courier.setTariffEndTime(endTime);
                        courier.setBonusPoints(courier.getBonusPoints() - bonusDeduction);
                        transaction.set(docRef, courier);
                    }
                    return null;
                }).addOnSuccessListener(aVoid -> future.complete(true))
                .addOnFailureListener(e -> future.complete(false));

        return future;
    }

    public CompletableFuture<Boolean> checkAndResetTariff(String courierId) {
        return getCourierById(courierId).thenCompose(courier -> {
            if (courier != null && courier.getTariffEndTime() < System.currentTimeMillis()) {
                return updateCourierTariff(courierId, 1.0, 0, 0);
            }
            return CompletableFuture.completedFuture(false);
        });
    }

    public CompletableFuture<Boolean> isPhoneExists(String phone) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        courierCollection.whereEqualTo("phone", phone).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        future.complete(!task.getResult().isEmpty());
                    } else {
                        future.completeExceptionally(task.getException());
                    }
                });
        return future;
    }

    public CompletableFuture<Boolean> isEmailExists(String email) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        courierCollection.whereEqualTo("email", email).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        future.complete(!task.getResult().isEmpty());
                    } else {
                        future.completeExceptionally(task.getException());
                    }
                });
        return future;
    }

}