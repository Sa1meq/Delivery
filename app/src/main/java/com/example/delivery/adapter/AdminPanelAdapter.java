package com.example.delivery.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.delivery.R;
import com.example.delivery.model.AdminCardItem;

import java.util.List;

public class AdminPanelAdapter extends RecyclerView.Adapter<AdminPanelAdapter.AdminViewHolder> {

    private final Context context;
    private final List<AdminCardItem> itemList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public AdminPanelAdapter(Context context, List<AdminCardItem> itemList, OnItemClickListener listener) {
        this.context = context;
        this.itemList = itemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_card, parent, false);
        return new AdminViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminViewHolder holder, int position) {
        AdminCardItem item = itemList.get(position);
        holder.imageView.setImageResource(item.getImageResId());
        holder.titleTextView.setText(item.getTitle());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(position));
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class AdminViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        CardView cardView;

        public AdminViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.adminCard);
            imageView = itemView.findViewById(R.id.cardImage);
            titleTextView = itemView.findViewById(R.id.cardTitle);
        }
    }
}
