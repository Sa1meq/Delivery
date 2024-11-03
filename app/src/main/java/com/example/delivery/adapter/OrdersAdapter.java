package com.example.delivery.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.delivery.R;
import com.example.delivery.model.RouteOrder;
import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

    private List<RouteOrder> routeOrders;

    public OrdersAdapter(List<RouteOrder> routeOrders) {
        this.routeOrders = routeOrders;
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
        holder.distanceTextView.setText("Distance: " + routeOrder.totalDistance + " km");
        holder.timeTextView.setText("Time: " + routeOrder.travelTime / 60 + " min");
        holder.costTextView.setText("Cost: $" + calculateCost(routeOrder));
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

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdTextView = itemView.findViewById(R.id.order_id);
            distanceTextView = itemView.findViewById(R.id.order_distance);
            timeTextView = itemView.findViewById(R.id.order_time);
            costTextView = itemView.findViewById(R.id.order_cost);
        }
    }

    private double calculateCost(RouteOrder order) {
        return order.totalDistance * 1.5;
    }
}
