package com.example.menlovending.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.menlovending.R;
import com.example.menlovending.stripe.client.StripeTerminalApplication;
import com.stripe.exception.StripeException;
import com.stripe.stripeterminal.external.callable.Cancelable;
import com.stripe.stripeterminal.external.models.Reader;

public class DollarAmountActivity extends AppCompatActivity {
    Cancelable discoverCancelable = null;
    private double dollarAmount;
    private boolean isProcessingCancellation = false;
    Reader selectedReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dollar_amount);

        dollarAmount = getIntent().getDoubleExtra("DOLLAR_AMOUNT", 0.0);
        int code = getIntent().getIntExtra("code", 1);

        TextView amountTextView = findViewById(R.id.dollar_amount_text_view);
        amountTextView.setText(String.format("$%.2f", dollarAmount));

        ImageView downArrow = findViewById(R.id.down_arrow);
        TextView instructionText = findViewById(R.id.instruction_text);

        // Initially hide the arrow and instruction text
        downArrow.setVisibility(View.GONE);
        instructionText.setVisibility(View.GONE);

        Button confirmButton = findViewById(R.id.confirm_button);
        Button cancelButton = findViewById(R.id.cancel_button);
        Button fullWidthCancelButton = findViewById(R.id.full_width_cancel_button);

        // Initially hide the full-width cancel button
        fullWidthCancelButton.setVisibility(View.GONE);

        confirmButton.setOnClickListener(view -> {
            // Show tap instructions and arrow
            downArrow.setVisibility(View.VISIBLE);
            instructionText.setVisibility(View.VISIBLE);

            // Hide the confirm button and regular cancel button
            confirmButton.setVisibility(View.GONE);
            cancelButton.setVisibility(View.GONE);

            // Show the full-width cancel button
            fullWidthCancelButton.setVisibility(View.VISIBLE);

            try {
                StripeTerminalApplication.processPayment(dollarAmount, code);
            } catch (StripeException e) {
                Toast.makeText(this, "Payment failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                // Revert UI changes on failure
                downArrow.setVisibility(View.GONE);
                instructionText.setVisibility(View.GONE);
                confirmButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
                fullWidthCancelButton.setVisibility(View.GONE);
            }
        });

        // Set click listener for both cancel buttons
        View.OnClickListener cancelClickListener = view -> {
            if (isProcessingCancellation) return;
            isProcessingCancellation = true;

            // Disable all buttons to prevent multiple clicks
            confirmButton.setEnabled(false);
            cancelButton.setEnabled(false);
            fullWidthCancelButton.setEnabled(false);

            runOnUiThread(DollarAmountActivity.this::finish);
        };

        cancelButton.setOnClickListener(cancelClickListener);
        fullWidthCancelButton.setOnClickListener(cancelClickListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!isProcessingCancellation) {
            StripeTerminalApplication.safelyAbortPayment();
        }
    }
}