package com.example.delivery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.delivery.model.Courier;

import java.util.List;

public class CourierAdapter extends RecyclerView.Adapter<CourierAdapter.CourierViewHolder> {

    private List<Courier> couriers;
    private OnVerifyListener onVerifyListener;
    private OnRejectListener onRejectListener;
    private OnItemClickListener onItemClickListener;

    public interface OnVerifyListener {
        void onVerify(Courier courier);
    }

    public interface OnRejectListener {
        void onReject(Courier courier);
    }

    public interface OnItemClickListener {
        void onItemClick(Courier courier);
    }

    public CourierAdapter(List<Courier> couriers, OnVerifyListener onVerifyListener, OnRejectListener onRejectListener, OnItemClickListener onItemClickListener) {
        this.couriers = couriers;
        this.onVerifyListener = onVerifyListener;
        this.onRejectListener = onRejectListener;
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public CourierViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_courier_verification, parent, false);
        return new CourierViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CourierViewHolder holder, int position) {
        Courier courier = couriers.get(position);
        holder.firstNameTextView.setText(courier.getFirstName());
        holder.surNameTextView.setText(courier.getSurName());
        holder.phoneTextView.setText(courier.getPhone());
        holder.textViewCourierType.setText(courier.getTypeOfCourier());


        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(courier));

        holder.verifyButton.setOnClickListener(v -> onVerifyListener.onVerify(courier));
        holder.rejectButton.setOnClickListener(v -> onRejectListener.onReject(courier));
    }

    @Override
    public int getItemCount() {
        return couriers.size();
    }

    public static class CourierViewHolder extends RecyclerView.ViewHolder {
        TextView firstNameTextView, surNameTextView, phoneTextView, textViewCourierType;
        Button verifyButton, rejectButton;

        public CourierViewHolder(View itemView) {
            super(itemView);
            textViewCourierType = itemView.findViewById(R.id.textViewCourierType);
            firstNameTextView = itemView.findViewById(R.id.textViewCourierName);
            surNameTextView = itemView.findViewById(R.id.textViewCourierSurName);
            phoneTextView = itemView.findViewById(R.id.textViewCourierPhone);
            verifyButton = itemView.findViewById(R.id.buttonApproveCourier);
            rejectButton = itemView.findViewById(R.id.buttonRejectCourier);
        }
    }
}
