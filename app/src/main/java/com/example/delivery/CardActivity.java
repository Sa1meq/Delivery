package com.example.delivery;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.delivery.adapter.CardAdapter;
import com.example.delivery.model.Card;
import com.example.delivery.repository.CardRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CardActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private String currentUserId;

    private List<Card> cardList;
    private CardAdapter cardAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        firestore = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();
        ImageView backImageView = findViewById(R.id.backImageView);

        RecyclerView cardRecyclerView = findViewById(R.id.cardRecyclerView);
        Button addCardButton = findViewById(R.id.addCardButton);

        cardList = new ArrayList<>();
        cardAdapter = new CardAdapter(this, cardList);

        cardRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cardRecyclerView.setAdapter(cardAdapter);

        addCardButton.setOnClickListener(v -> showAddCardDialog());
        loadCardsFromFirestore();

        backImageView.setOnClickListener(v -> {
            Intent intent = new Intent(CardActivity.this, UserProfile.class);
            startActivity(intent);
            finish();
        });

    }


    private void loadCardsFromFirestore() {
        firestore.collection("cards")
                .whereEqualTo("cardUserID", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cardList.clear();
                    cardList.addAll(queryDocumentSnapshots.toObjects(Card.class));
                    cardAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("CardActivity", "Ошибка загрузки карт", e));
    }

    private void showAddCardDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_card, null);
        builder.setView(dialogView);

        EditText cardNumberEditText = dialogView.findViewById(R.id.cardNumberEditText);
        EditText expiryDateEditText = dialogView.findViewById(R.id.expiryDateEditText);
        EditText cvcEditText = dialogView.findViewById(R.id.cvcEditText);
        ImageView cardLogoImageView = dialogView.findViewById(R.id.cardLogoImageView);
        Button saveCardButton = dialogView.findViewById(R.id.saveCardButton);

        AlertDialog dialog = builder.create();

        // Форматирование номера карты
        cardNumberEditText.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isFormatting) {
                    isFormatting = true;

                    String formatted = s.toString().replaceAll("\\s", "");
                    StringBuilder formattedBuilder = new StringBuilder();

                    for (int i = 0; i < formatted.length(); i++) {
                        formattedBuilder.append(formatted.charAt(i));
                        if ((i + 1) % 4 == 0 && i + 1 < formatted.length()) {
                            formattedBuilder.append(" ");
                        }
                    }
                    cardNumberEditText.setText(formattedBuilder.toString());
                    cardNumberEditText.setSelection(formattedBuilder.length());
                    updateCardLogo(formattedBuilder.toString(), cardLogoImageView);

                    isFormatting = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        expiryDateEditText.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isFormatting) {
                    isFormatting = true;

                    String formatted = s.toString().replaceAll("/", "");
                    if (formatted.length() > 2) {
                        formatted = formatted.substring(0, 2) + "/" + formatted.substring(2);
                    }
                    expiryDateEditText.setText(formatted);
                    expiryDateEditText.setSelection(formatted.length());

                    isFormatting = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        saveCardButton.setOnClickListener(v -> {
            String cardNumber = cardNumberEditText.getText().toString().trim();
            String expiryDate = expiryDateEditText.getText().toString().trim();
            String cvc = cvcEditText.getText().toString().trim();

            if (!isValidCardNumber(cardNumber)) {
                cardNumberEditText.setError("Введите корректный номер карты в формате '1234 5678 1234 5678'");
                return;
            }

            if (!isValidExpiryDate(expiryDate)) {
                expiryDateEditText.setError("Введите корректную дату в формате 'MM/YY'");
                return;
            }

            if (cvc.isEmpty() || cvc.length() != 3) {
                cvcEditText.setError("Введите корректный CVC (3 цифры)");
                return;
            }

            Card newCard = new Card(
                    firestore.collection("cards").document().getId(),
                    cardNumber,
                    expiryDate,
                    Integer.parseInt(cvc),
                    currentUserId,
                    false
            );

            saveCardToFirestore(newCard);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateCardLogo(String cardNumber, ImageView cardLogoImageView) {
        if (cardNumber.startsWith("5")) {
            cardLogoImageView.setImageResource(R.drawable.mastercard_logo);
        } else if (cardNumber.startsWith("4")) {
            cardLogoImageView.setImageResource(R.drawable.visa_logo);
        } else if (cardNumber.startsWith("9112")) {
            cardLogoImageView.setImageResource(R.drawable.belcart_logo);
        } else if (cardNumber.startsWith("2200") || cardNumber.startsWith("2201") || cardNumber.startsWith("2202")) {
            cardLogoImageView.setImageResource(R.drawable.mir_logo);
        } else {
            cardLogoImageView.setImageResource(0);
        }
    }

    private boolean isValidCardNumber(String cardNumber) {
        return cardNumber.matches("\\d{4}[-\\s]\\d{4}[-\\s]\\d{4}[-\\s]\\d{4}");
    }



    private boolean isValidExpiryDate(String expiryDate) {
        if (!expiryDate.matches("\\d{2}/\\d{2}")) return false;

        String[] parts = expiryDate.split("/");
        int month = Integer.parseInt(parts[0]);
        int year = Integer.parseInt(parts[1]) + 2000;

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;

        return (year > currentYear || (year == currentYear && month >= currentMonth)) && month >= 1 && month <= 12;
    }

    private void saveCardToFirestore(Card card) {
        CardRepository cardRepository = new CardRepository();
        cardRepository.addCard(card)
                .thenRun(() -> {
                    cardList.add(card);
                    cardAdapter.notifyDataSetChanged();
                })
                .exceptionally(throwable -> {
                    Log.e("CardActivity", "Ошибка сохранения карты", throwable);
                    return null;
                });
    }

}
