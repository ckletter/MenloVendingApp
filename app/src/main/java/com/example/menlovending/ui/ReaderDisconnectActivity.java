package com.example.menlovending.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.menlovending.R;
import com.example.menlovending.stripe.manager.MenloVendingManager;
import com.example.menlovending.stripe.manager.MenloVendingState;

public class ReaderDisconnectActivity extends AppCompatActivity {
    private TextView statusTextView;
    private Button retryButton;
    private ProgressBar progressBar;
    private Handler handler;
    private boolean isMonitoring = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader_disconnection);

        // Initialize views
        statusTextView = findViewById(R.id.status_text_view);
        retryButton = findViewById(R.id.retry_button);
        progressBar = findViewById(R.id.progress_bar);

        // Initialize handler for UI updates
        handler = new Handler(Looper.getMainLooper());

        // Set initial status message and UI state
        updateStatusUI("Stripe Terminal Reader Not Connected", false);

        // Setup retry button
        retryButton.setOnClickListener(v -> attemptReconnection());

        // Start monitoring connection status
        startConnectionStatusMonitoring();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume monitoring if it was stopped
        if (!isMonitoring) {
            startConnectionStatusMonitoring();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up resources
        isMonitoring = false;
        handler.removeCallbacksAndMessages(null);
    }

    private void startConnectionStatusMonitoring() {
        isMonitoring = true;

        new Thread(() -> {
            while (isMonitoring) {
                MenloVendingState currentState = MenloVendingManager.getInstance().getMenloVendingState();

                if (currentState.getStatus() == MenloVendingState.MenloVendingStatus.READY) {
                    handler.post(() -> {
                        // Navigate back to main activity when connection is established
                        Intent intent = new Intent(ReaderDisconnectActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    });
                    break;
                }

                try {
                    Thread.sleep(1500); // Check every 1.5 seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    isMonitoring = false;
                }
            }
        }).start();
    }

    private void attemptReconnection() {
        // Update UI to show connection attempt in progress
        updateStatusUI("Attempting to reconnect...", true);

        // Attempt to reinitialize and discover readers
        new Thread(() -> {
            try {
                MenloVendingManager.getInstance().initializeReader(getApplicationContext());

                // Allow time for initialization before enabling retry
                Thread.sleep(3000);

                // Check connection status after initialization
                MenloVendingState currentState = MenloVendingManager.getInstance().getMenloVendingState();

                if (currentState.getStatus() == MenloVendingState.MenloVendingStatus.READY) {
                    handler.post(() -> {
                        // Navigate back to main activity if connection successful
                        Intent intent = new Intent(ReaderDisconnectActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    // Update UI if connection failed
                    handler.post(() -> updateStatusUI("Connection failed. Please try again.", false));
                }
            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() -> updateStatusUI("Error: " + e.getMessage(), false));
            }
        }).start();
    }

    private void updateStatusUI(String message, boolean isConnecting) {
        statusTextView.setText(message);
        progressBar.setVisibility(isConnecting ? View.VISIBLE : View.GONE);
        retryButton.setEnabled(!isConnecting);
    }
}