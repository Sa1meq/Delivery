package com.example.delivery;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.delivery.R;

public class LoadingDialog extends Dialog {

    private final TextView loadingMessage;

    public LoadingDialog(@NonNull Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_loading_chat);
        setCancelable(false);
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        loadingMessage = findViewById(R.id.loading_message);
        progressBar.setIndeterminate(true);
    }

    public void setMessage(String message) {
        loadingMessage.setText(message);
    }
}
