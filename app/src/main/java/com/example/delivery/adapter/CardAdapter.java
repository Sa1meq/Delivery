package com.example.delivery.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.delivery.R;
import com.example.delivery.model.Card;
import com.example.delivery.repository.CardRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private final Context context;
    private final List<Card> cardList;
    private final CardRepository cardRepository;

    public CardAdapter(Context context, List<Card> cardList) {
        this.context = context;
        this.cardList = cardList;
        this.cardRepository = new CardRepository();
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_view_item, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Card card = cardList.get(position);
        holder.cardNumberTextView.setText(card.getCardNumber());
        holder.expiryDateTextView.setText(card.getCardExpiryDate());


        if (card.getCardNumber().startsWith("5")) {
            holder.cardLogoImageView.setImageResource(R.drawable.mastercard_logo);
        } else if (card.getCardNumber().startsWith("4")) {
            holder.cardLogoImageView.setImageResource(R.drawable.visa_logo);
        } else if (card.getCardNumber().startsWith("9112")) {
            holder.cardLogoImageView.setImageResource(R.drawable.belcart_logo);
        } else if (card.getCardNumber().startsWith("2200") ||
                card.getCardNumber().startsWith("2201") ||
                card.getCardNumber().startsWith("2202")) {
            holder.cardLogoImageView.setImageResource(R.drawable.mir_logo);
        } else {
            holder.cardLogoImageView.setImageResource(0);
        }

        if (card.isMain()) {
            holder.itemView.setBackgroundResource(R.drawable.main_card_background);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.normal_card_background);
        }

        // Обработчик нажатий на карту
        holder.itemView.setOnClickListener(v -> showSetAsMainDialog(card, position));

        holder.deleteCardImageView.setOnClickListener(v -> showDeleteCardDialog(card, position));
    }



    private void showSetAsMainDialog(Card card, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Сделать эту карту основной?")
                .setMessage("Вы действительно хотите сделать эту карту основной?")
                .setPositiveButton("Да", (dialog, which) -> setMainCard(card, position))
                .setNegativeButton("Нет", null)
                .show();
    }

    private void setMainCard(Card card, int position) {

        for (Card c : cardList) {
            c.setMain(false);
        }

        card.setMain(true);

        cardList.set(position, card);
        notifyDataSetChanged();

        // Обновляем изменения в Firestore
        cardRepository.updateMainCard(card.getId(), FirebaseAuth.getInstance().getCurrentUser().getUid())
                .thenRun(() -> {
                    Toast.makeText(context, "Карта установлена как основная", Toast.LENGTH_SHORT).show();
                })
                .exceptionally(throwable -> {
                    // Обработка ошибок
                    Toast.makeText(context, "Ошибка установки основной карты: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                    return null;
                });
    }

    private void showDeleteCardDialog(Card card, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Удалить карту?")
                .setMessage("Вы действительно хотите удалить эту карту?")
                .setPositiveButton("Да", (dialog, which) -> deleteCard(card, position))
                .setNegativeButton("Нет", null)
                .show();
    }

    private void deleteCard(Card card, int position) {
        cardRepository.deleteCard(card.getId())
                .thenRun(() -> {
                    Toast.makeText(context, "Карта удалена", Toast.LENGTH_SHORT).show();
                    cardList.remove(position);
                    notifyItemRemoved(position);
                })
                .exceptionally(throwable -> {
                    Toast.makeText(context, "Ошибка удаления карты: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                    return null;
                });
    }



    @Override
    public int getItemCount() {
        return cardList.size();
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        private final TextView cardNumberTextView;
        private final TextView expiryDateTextView;
        private final ImageView cardLogoImageView;
        private final ImageView deleteCardImageView;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardNumberTextView = itemView.findViewById(R.id.cardNumberTextView);
            expiryDateTextView = itemView.findViewById(R.id.cardExpiryDateTextView);
            cardLogoImageView = itemView.findViewById(R.id.cardOperatorImageView);
            deleteCardImageView = itemView.findViewById(R.id.deleteCardImageView);
        }
    }
}
