package com.example.delivery.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.delivery.R;
import com.example.delivery.model.SupportChat;

import java.util.List;

public class SupportChatAdapter extends RecyclerView.Adapter<SupportChatAdapter.ChatViewHolder> {

    private List<SupportChat> chats;
    private OnChatClickListener onChatClickListener;

    public SupportChatAdapter(List<SupportChat> chats) {
        this.chats = chats;
    }

    public void setChats(List<SupportChat> chats) {
        this.chats = chats;
        notifyDataSetChanged();
    }

    public void setOnChatClickListener(OnChatClickListener listener) {
        this.onChatClickListener = listener;
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {
        SupportChat chat = chats.get(position);
        holder.chatName.setText("Чат с " + chat.getUserId());
        holder.chatStatus.setText(chat.getStatus());
        holder.chatCard.setOnClickListener(v -> {
            if (onChatClickListener != null) {
                onChatClickListener.onChatClick(chat);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public interface OnChatClickListener {
        void onChatClick(SupportChat chat);
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        public TextView chatName;
        public TextView chatStatus;
        public CardView chatCard;

        public ChatViewHolder(View itemView) {
            super(itemView);
            chatName = itemView.findViewById(R.id.chatName);
            chatStatus = itemView.findViewById(R.id.chatStatus);
            chatCard = itemView.findViewById(R.id.cardViewChat);
        }
    }
}

