package com.example.delivery.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.delivery.R;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.ViewHolder> {

    private final Context context;

    // Конструктор адаптера принимает контекст
    public OnboardingAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Инфлейтинг (создание) макета для каждого элемента списка
        View view = LayoutInflater.from(context).inflate(R.layout.item_onboarding, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Привязка данных к ViewHolder в зависимости от позиции
        switch (position) {
            case 0:
                holder.bind(R.drawable.image1, "Добро пожаловать!", "Откройте для себя удобное приложение для доставки. Легкость и надежность в одном месте.");
                break;
            case 1:
                holder.bind(R.drawable.image2, "Легкие заказы", "Создавайте заказы в пару кликов и выбирайте удобный тариф для вас.");
                break;
            case 2:
                holder.bind(R.drawable.image3, "Отслеживание", "Следите за статусом доставки и местоположением курьера в реальном времени.");
                break;
            case 3:
                holder.bind(R.drawable.image4, "Доставка", "Получайте товары быстро и безопасно. Мы заботимся о качестве.");
                break;
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageView;
        private final TextView titleTextView;
        private final TextView descriptionTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            titleTextView = itemView.findViewById(R.id.title);
            descriptionTextView = itemView.findViewById(R.id.description);
        }

        // Метод для привязки данных к Views
        public void bind(int imageRes, String title, String description) {
            imageView.setImageResource(imageRes);
            titleTextView.setText(title);
            descriptionTextView.setText(description);
        }
    }
}
