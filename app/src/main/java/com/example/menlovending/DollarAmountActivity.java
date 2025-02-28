package com.example.menlovending;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
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

public class DollarAmountActivity extends AppCompatActivity implements MobileReaderListener {
    Cancelable discoverCancelable = null;
    private double dollarAmount;
    Reader selectedReader;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dollar_amount);
        // Retrieve the extra from the intent
        double dollarAmount = getIntent().getDoubleExtra("DOLLAR_AMOUNT", 0.0);

        // Display the dollarAmount
        TextView amountTextView = findViewById(R.id.dollar_amount_text_view);
        amountTextView.setText(String.format("$%.2f", dollarAmount));

        // Handle the Back button
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish()); // Ends current activity and returns to MainActivity

        try {
            StripeTerminalApplication.processPayment();
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void onRequestReaderInput(ReaderInputOptions options) {
        // Placeholder for updating your app's checkout UI
//        Toast.makeText(getActivity(), options.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestReaderDisplayMessage(ReaderDisplayMessage message) {
//        Toast.makeText(getActivity(), message.toString(), Toast.LENGTH_SHORT).show();
    }
}

