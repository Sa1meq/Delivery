package com.example.delivery.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.delivery.R;
import com.example.delivery.model.SupportChat;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AdminChatAdapter extends RecyclerView.Adapter<AdminChatAdapter.ChatViewHolder> {

    private final List<SupportChat> chats;
    private final OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(SupportChat chat);
    }

    public AdminChatAdapter(List<SupportChat> chats, OnChatClickListener listener) {
        this.chats = chats;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        SupportChat chat = chats.get(position);
        holder.bind(chat);
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    class ChatViewHolder extends RecyclerView.ViewHolder {
        private final TextView chatTitle;
        private final TextView chatStatus;
        private final TextView chatDate;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            chatTitle = itemView.findViewById(R.id.chatTitleTextView);
            chatStatus = itemView.findViewById(R.id.chatStatusTextView);
            chatDate = itemView.findViewById(R.id.chatDateTextView);
        }

        public void bind(SupportChat chat) {
            chatTitle.setText("Чат с: " + chat.getUserId());
            chatStatus.setText("Статус: " + chat.getStatus());
            chatDate.setText(new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                    .format(chat.getCreatedAt()));

            itemView.setOnClickListener(v -> listener.onChatClick(chat));
        }
    }
}
