package com.example.delivery.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.delivery.R;
import com.example.delivery.model.SupportMessage;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class SupportChatAdapter extends FirestoreRecyclerAdapter<SupportMessage, SupportChatAdapter.MessageViewHolder> {

    private static final int TYPE_SENT = 0;
    private static final int TYPE_RECEIVED = 1;
    private final String currentUserId;

    public SupportChatAdapter(@NonNull FirestoreRecyclerOptions<SupportMessage> options, String userId) {
        super(options);
        this.currentUserId = userId;
    }

    @Override
    public int getItemViewType(int position) {
        SupportMessage message = getItem(position);
        return message.getUserId().equals(currentUserId) ? TYPE_SENT : TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes = viewType == TYPE_SENT ?
                R.layout.item_message_sent : R.layout.item_message_received;

        View view = LayoutInflater.from(parent.getContext())
                .inflate(layoutRes, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull MessageViewHolder holder, int position, @NonNull SupportMessage message) {
        holder.bind(message);
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageText;
        private final TextView timeText;

        MessageViewHolder(View view) {
            super(view);
            messageText = view.findViewById(R.id.message_text);
            timeText = view.findViewById(R.id.message_time);
        }

        void bind(SupportMessage message) {
            messageText.setText(message.getText());
            timeText.setText(new SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(message.getTimestamp()));
        }
    }
}