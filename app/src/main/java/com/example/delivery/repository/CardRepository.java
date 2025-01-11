package com.example.delivery.repository;

import android.util.Log;

import com.example.delivery.model.Card;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CardRepository {

    public final CollectionReference cardsCollection;
    private final FirebaseFirestore firestore;

    public CardRepository() {
        firestore = FirebaseFirestore.getInstance();
        cardsCollection = firestore.collection("cards");
    }

    public CompletableFuture<Card> addCard(Card card) {
        CompletableFuture<Card> future = new CompletableFuture<>();
        cardsCollection.document(card.getId())
                .set(card)
                .addOnSuccessListener(aVoid -> {
                    Log.d("CardRepository", "Карта добавлена: " + card.getCardNumber());
                    future.complete(card);
                })
                .addOnFailureListener(e -> future.completeExceptionally(new RuntimeException("Ошибка добавления карты: " + e.getMessage())));
        return future;
    }

    public CompletableFuture<Card> getCardById(String cardId) {
        CompletableFuture<Card> future = new CompletableFuture<>();
        cardsCollection.document(cardId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Card card = task.getResult().toObject(Card.class);
                        future.complete(card);
                    } else {
                        future.completeExceptionally(new Exception("Ошибка получения карты"));
                    }
                });
        return future;
    }



    public CompletableFuture<Void> updateMainCard(String cardId, String userId) {
        return CompletableFuture.supplyAsync(() -> {
            Task<QuerySnapshot> task = cardsCollection.whereEqualTo("cardUserID", userId).get();
            try {
                QuerySnapshot snapshot = Tasks.await(task);
                if (snapshot != null) {
                    for (QueryDocumentSnapshot document : snapshot) {
                        String currentCardId = document.getId();
                        boolean isMain = currentCardId.equals(cardId);
                        Task<Void> updateTask = cardsCollection.document(currentCardId)
                                .update("main", isMain);
                        Tasks.await(updateTask);
                    }
                } else {
                    throw new RuntimeException("Карты пользователя не найдены");
                }
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException("Ошибка при обновлении основной карты: " + e.getMessage());
            }
            return null;
        });
    }


    public CompletableFuture<Void> deleteCard(String cardId) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        cardsCollection.document(cardId).delete()
                .addOnFailureListener(e -> future.completeExceptionally(new RuntimeException("Ошибка удаления карты: " + e.getMessage())))
                .addOnSuccessListener(aVoid -> future.complete(null));
        return future;
    }
}
