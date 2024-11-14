package com.example.delivery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.delivery.model.RouteOrder;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<RouteOrder> orders;

    public OrderAdapter(List<RouteOrder> orders) {
        this.orders = orders;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_history_item, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        RouteOrder order = orders.get(position);
        holder.startAddress.setText("Откуда: " + order.getStartAddress());
        holder.endAddress.setText("Куда: " + order.getEndAddress());
        holder.estimatedCost.setText("Стоимость: " + order.getEstimatedCost());
        holder.orderDescription.setText("Описание: " + order.getOrderDescription());
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView startAddress, endAddress, estimatedCost, orderDescription;

        public OrderViewHolder(View itemView) {
            super(itemView);
            startAddress = itemView.findViewById(R.id.startAddress);
            endAddress = itemView.findViewById(R.id.endAddress);
            estimatedCost = itemView.findViewById(R.id.estimatedCost);
            orderDescription = itemView.findViewById(R.id.orderDescription);
        }
    }
}
