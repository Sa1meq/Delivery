package com.example.delivery.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.delivery.R;
import com.example.delivery.model.Courier;
import com.example.delivery.model.User;

import java.util.List;

public class UserCourierAdapter extends RecyclerView.Adapter<UserCourierAdapter.ViewHolder> {

    private final List<User> users;
    private final List<Courier> couriers;
    private final OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClick(User user, Courier courier, int requestCode);
    }

    public UserCourierAdapter(List<User> users, List<Courier> couriers, OnItemClickListener clickListener) {
        this.users = users;
        this.couriers = couriers;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_courier, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (users != null) {
            User user = users.get(position);
            holder.nameTextView.setText(user.getName());
            holder.detailTextView.setText(user.getEmail());
            holder.itemView.setOnClickListener(v -> clickListener.onItemClick(user, null, 1001)); // requestCode для пользователей
        } else if (couriers != null) {
            Courier courier = couriers.get(position);
            holder.nameTextView.setText(courier.getFirstName() + " " + courier.getSurName());
            holder.detailTextView.setText(courier.getPhone());
            holder.itemView.setOnClickListener(v -> clickListener.onItemClick(null, courier, 1002)); // requestCode для курьеров
        }
    }

    @Override
    public int getItemCount() {
        return users != null ? users.size() : couriers.size();
    }

    public void updateUsers(List<User> newUsers) {
        users.clear();
        users.addAll(newUsers);
        notifyDataSetChanged();
    }

    public void updateCouriers(List<Courier> newCouriers) {
        couriers.clear();
        couriers.addAll(newCouriers);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, detailTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            detailTextView = itemView.findViewById(R.id.detailTextView);
        }
    }
}