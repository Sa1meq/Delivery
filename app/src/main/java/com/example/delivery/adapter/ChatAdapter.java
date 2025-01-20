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
import com.example.delivery.model.SupportChat;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final Context context;
    private final List<SupportChat> chatList;
    private final ChatClickListener chatClickListener;

    public interface ChatClickListener {
        void onChatClick(SupportChat chat);
        void onDeleteClick(SupportChat chat);
    }

    public ChatAdapter(Context context, List<SupportChat> chatList, ChatClickListener chatClickListener) {
        this.context = context;
        this.chatList = chatList;
        this.chatClickListener = chatClickListener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        SupportChat chat = chatList.get(position);

        holder.chatTopic.setText(chat.getTopic());
        holder.chatRequestType.setText(chat.getRequestType());
        holder.chatStatus.setImageResource(chat.getStatus().equals("open")
                ? R.drawable.ic_chat_open : R.drawable.ic_chat_closed);

        holder.cardView.setOnClickListener(v -> chatClickListener.onChatClick(chat));
        holder.deleteButton.setOnClickListener(v -> chatClickListener.onDeleteClick(chat));
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView chatTopic, chatRequestType;
        ImageView chatStatus, deleteButton;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_chat);
            chatTopic = itemView.findViewById(R.id.chat_topic);
            chatRequestType = itemView.findViewById(R.id.chat_request_type);
            chatStatus = itemView.findViewById(R.id.chat_status);
            deleteButton = itemView.findViewById(R.id.chat_delete_button);
        }
    }
}
