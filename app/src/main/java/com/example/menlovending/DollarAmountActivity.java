package com.example.menlovending;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.menlovending.BuildConfig;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.menlovending.stripe.client.StripeTerminalApplication;
import com.stripe.exception.StripeException;
import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.external.callable.Callback;
import com.stripe.stripeterminal.external.callable.Cancelable;

import com.stripe.stripeterminal.external.callable.MobileReaderListener;
import com.stripe.stripeterminal.external.callable.ReaderCallback;
import com.stripe.stripeterminal.external.models.ConnectionConfiguration;
import com.stripe.stripeterminal.external.models.DiscoveryConfiguration;
import com.stripe.stripeterminal.external.models.Reader;
import com.stripe.stripeterminal.external.models.ReaderDisplayMessage;
import com.stripe.stripeterminal.external.models.ReaderInputOptions;
import com.stripe.stripeterminal.external.models.TerminalException;

import org.jetbrains.annotations.NotNull;

import java.util.List;

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
        backButton.setOnClickListener(v -> finish()); // Ends current activity and returns to MainActivity


        try {
            StripeTerminalApplication.processPayment(dollarAmount);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }
}

