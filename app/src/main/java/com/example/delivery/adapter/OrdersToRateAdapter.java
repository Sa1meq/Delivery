package com.example.delivery.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.delivery.R;
import com.example.delivery.model.RouteOrder;

import java.util.List;

public class OrdersToRateAdapter extends RecyclerView.Adapter<OrdersToRateAdapter.ViewHolder> {

    private final List<RouteOrder> orders;
    private final OnOrderRateListener onOrderRateListener;

    public OrdersToRateAdapter(List<RouteOrder> orders, OnOrderRateListener listener) {
        this.orders = orders;
        this.onOrderRateListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_to_rate, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RouteOrder order = orders.get(position);
        holder.orderIdTextView.setText("Заказ #" + order.getOrderId());
        holder.rateButton.setOnClickListener(v -> onOrderRateListener.onRateOrder(order));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdTextView;
        Button rateButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdTextView = itemView.findViewById(R.id.orderIdTextView);
            rateButton = itemView.findViewById(R.id.rateButton);
        }
    }

    public interface OnOrderRateListener {
        void onRateOrder(RouteOrder order);
    }
}
