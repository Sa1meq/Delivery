package com.example.delivery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class BottomSheetFragment extends BottomSheetDialogFragment {
    private RecyclerView suggestionsRecyclerView;
    private AddressSuggestionAdapter suggestionAdapter;
    private List<String> suggestions = new ArrayList<>();

    public interface OnSuggestionClickListener {
        void onSuggestionClick(String suggestion);
    }

    private OnSuggestionClickListener listener;

    public BottomSheetFragment(OnSuggestionClickListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_address_bottom_sheet, container, false);
        suggestionsRecyclerView = view.findViewById(R.id.suggestionsRecyclerView);
        suggestionAdapter = new AddressSuggestionAdapter(suggestions, suggestion -> {
            listener.onSuggestionClick(suggestion);
            dismiss();
        });
        suggestionsRecyclerView.setAdapter(suggestionAdapter);
        return view;
    }

    public void updateSuggestions(List<String> newSuggestions) {
        suggestions.clear();
        suggestions.addAll(newSuggestions);
        suggestionAdapter.notifyDataSetChanged();
    }
}