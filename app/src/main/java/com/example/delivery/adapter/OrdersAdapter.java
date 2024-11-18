package com.example.delivery.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.delivery.CourierAcceptedOrder;
import com.example.delivery.R;
import com.example.delivery.model.RouteOrder;
import com.example.delivery.repository.CourierRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {
    private List<RouteOrder> routeOrders;
    private OnOrderClickListener onOrderClickListener;
    private Context context;

    public OrdersAdapter(List<RouteOrder> routeOrders, OnOrderClickListener onOrderClickListener, Context context) {
        this.routeOrders = routeOrders;
        this.onOrderClickListener = onOrderClickListener;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        RouteOrder routeOrder = routeOrders.get(position);

        holder.orderIdTextView.setText(routeOrder.orderId);
        holder.startAddressTextView.setText("Откуда: " + routeOrder.getStartAddress());
        holder.endAddressTextView.setText("Куда: " + routeOrder.getEndAddress());
        holder.descriptionTextView.setText("Примечание: " + routeOrder.getOrderDescription());
        double distanceInKm = routeOrder.totalDistance / 1000.0;
        holder.distanceTextView.setText("Расстояние: " + String.format("%.1f", distanceInKm) + " км");
        holder.timeTextView.setText("Время: " + routeOrder.travelTime / 60 + " минут");

        String courierId = FirebaseAuth.getInstance().getUid();
        new CourierRepository(FirebaseFirestore.getInstance())
                .getCourierTypeByUid(courierId)
                .thenAccept(courierType -> {
                    double cost = calculateCost(courierType, distanceInKm);
                    holder.costTextView.setText("Оплата: " + String.format("%.2f", cost) + " BYN");
                })
                .exceptionally(e -> {
                    holder.costTextView.setText("Оплата: недоступно");
                    return null;
                });
        holder.itemView.setOnClickListener(v -> onOrderClickListener.onOrderClick(routeOrder));
    }


    @Override
    public int getItemCount() {
        return routeOrders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdTextView;
        TextView distanceTextView;
        TextView timeTextView;
        TextView costTextView;
        TextView startAddressTextView;
        TextView endAddressTextView;
        TextView descriptionTextView;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdTextView = itemView.findViewById(R.id.order_id);
            distanceTextView = itemView.findViewById(R.id.order_distance);
            timeTextView = itemView.findViewById(R.id.order_time);
            costTextView = itemView.findViewById(R.id.order_cost);
            startAddressTextView = itemView.findViewById(R.id.start_address);
            endAddressTextView = itemView.findViewById(R.id.end_address);
            descriptionTextView = itemView.findViewById(R.id.order_description);
        }
    }

    private double calculateCost(String courierType, double distanceInKm) {
        double baseCost = 0.0;
        double costPerKm;

        if ("Пеший".equals(courierType)) {
            costPerKm = 1.3;
        } else if ("Авто".equals(courierType)) {
            costPerKm = 1.5;
            baseCost += 3.0;
        } else if ("Грузовой".equals(courierType)) {
            costPerKm = 2;
            baseCost += 5.0;
        } else {
            costPerKm = 1.0;
        }
        double cost = baseCost + (costPerKm * distanceInKm);
        return cost;
    }




    public interface OnOrderClickListener {
        void onOrderClick(RouteOrder routeOrder);
    }
}

