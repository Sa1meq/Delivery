package com.example.delivery.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.delivery.R;
import com.example.delivery.model.SupportMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SupportMessageAdapter extends RecyclerView.Adapter<SupportMessageAdapter.MessageViewHolder> {
    private final List<SupportMessage> messages;
    private final String currentUserId;

    public SupportMessageAdapter(List<SupportMessage> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(viewType == 0 ? R.layout.item_message_user : R.layout.item_message_admin, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        SupportMessage message = messages.get(position);

        // Устанавливаем текст сообщения
        holder.messageTextView.setText(message.getMessageContent());

        // Форматируем и устанавливаем время сообщения
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date(message.getTimestamp()));
        holder.timestampTextView.setText(time);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        // Определяем, сообщение от текущего пользователя или админа
        return messages.get(position).getSenderId().equals(currentUserId) ? 0 : 1;
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView, timestampTextView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
        }
    }
    public void addMessages(List<SupportMessage> newMessages) {
        int startPosition = messages.size();
        messages.addAll(newMessages);
        notifyItemRangeInserted(startPosition, newMessages.size());
    }

}
