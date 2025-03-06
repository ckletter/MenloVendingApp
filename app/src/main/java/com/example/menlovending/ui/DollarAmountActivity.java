package com.example.menlovending.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.menlovending.R;
import com.example.menlovending.stripe.client.StripeTerminalApplication;
import com.example.menlovending.stripe.manager.MenloVendingManager;
import com.stripe.exception.StripeException;
import com.stripe.stripeterminal.external.callable.Cancelable;

import com.stripe.stripeterminal.external.models.Reader;

public class DollarAmountActivity extends AppCompatActivity {
    Cancelable discoverCancelable = null;
    private double dollarAmount;
    Reader selectedReader;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dollar_amount);
        // Retrieve the extra from the intent
        dollarAmount = getIntent().getDoubleExtra("DOLLAR_AMOUNT", 0.0);
        // Display the dollarAmount
        TextView amountTextView = findViewById(R.id.dollar_amount_text_view);
        amountTextView.setText(String.format("$%.2f", dollarAmount));

        // Display tap to pay image
        ImageView imageView = findViewById(R.id.tap_to_pay_image);
        imageView.setImageResource(R.drawable.tap_to_pay);

        // Handle the Back button
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(view -> {
            StripeTerminalApplication.cancelPaymentIntent();
            finish(); // Ends current activity and returns to MainActivity
        });


        try {
            StripeTerminalApplication.processPayment(dollarAmount);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }
}

