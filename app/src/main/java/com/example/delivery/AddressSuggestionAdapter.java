package com.example.delivery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AddressSuggestionAdapter extends RecyclerView.Adapter<AddressSuggestionAdapter.ViewHolder> {
    private List<String> suggestions;
    private OnSuggestionClickListener listener;

    public AddressSuggestionAdapter(List<String> suggestions, OnSuggestionClickListener listener) {
        this.suggestions = suggestions != null ? suggestions : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String suggestion = suggestions.get(position);
        holder.textView.setText(suggestion);
        holder.itemView.setOnClickListener(v -> listener.onSuggestionClick(suggestion));
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    public void updateSuggestions(List<String> newSuggestions) {
        suggestions.clear();
        if (newSuggestions != null) {
            suggestions.addAll(newSuggestions);
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }

    public interface OnSuggestionClickListener {
        void onSuggestionClick(String suggestion);
    }
}
