package com.example.menlovending.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.menlovending.R;
import com.example.menlovending.stripe.manager.MenloVendingManager;
import com.example.menlovending.stripe.manager.MenloVendingState;
import com.stripe.stripeterminal.external.models.ConnectionStatus;

public class ReaderDisconnectActivity extends AppCompatActivity {
    private TextView statusTextView;
    private Button retryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader_disconnection);

        statusTextView = findViewById(R.id.status_text_view);
//        retryButton = findViewById(R.id.retry_button);

        // Set initial status message
        statusTextView.setText("Stripe Terminal Reader Not Connected");

        // Setup retry button
//        retryButton.setOnClickListener(v -> checkReaderConnection());

        // Start monitoring connection status
        startConnectionStatusMonitoring();
    }

    private void startConnectionStatusMonitoring() {
        // This is a simplified approach. In a real-world scenario,
        // you might want to use a more robust method like LiveData or a custom Observer
        new Thread(() -> {
            while (true) {
                MenloVendingState currentState = MenloVendingManager.getInstance().getMenloVendingState();

                if (currentState.getStatus() == MenloVendingState.MenloVendingStatus.READY) {
                    runOnUiThread(() -> {
                        // Navigate back to main activity when connection is established
                        Intent intent = new Intent(ReaderDisconnectActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    });
                    break;
                }

                try {
                    Thread.sleep(2000); // Check every 2 seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void checkReaderConnection() {
        // Attempt to reinitialize and discover readers
        MenloVendingManager.getInstance().initialize(getApplicationContext());

        // Update UI to show retry status
        statusTextView.setText("Attempting to reconnect...");
        retryButton.setEnabled(false);
    }
}