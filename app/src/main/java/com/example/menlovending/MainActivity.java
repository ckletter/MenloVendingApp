package com.example.menlovending;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.Manifest;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.terminal.ConnectionToken;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.terminal.ConnectionTokenCreateParams;
import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.external.callable.Cancelable;
import com.stripe.stripeterminal.external.callable.PaymentIntentCallback;
import com.stripe.stripeterminal.external.callable.TerminalListener;
import com.stripe.stripeterminal.external.models.ConnectionStatus;
import com.stripe.stripeterminal.external.models.PaymentIntent;
import com.stripe.stripeterminal.external.models.PaymentIntentParameters;
import com.stripe.stripeterminal.external.models.PaymentStatus;
import com.stripe.stripeterminal.external.models.TerminalException;
import com.stripe.stripeterminal.log.LogLevel;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {
    public static final int ITEM_COUNT = 16;
    private TextView displayTextView;
    private StringBuilder enteredCode = new StringBuilder();
    private double[] prices = new double[ITEM_COUNT + 1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        prices[1] = 3.50;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        displayTextView = findViewById(R.id.display_text_view);
        GridLayout keypadGrid = findViewById(R.id.keypad_grid);

        for (int i = 1; i <= 9; i++) {
            Button button = new Button(this);
            button.setText(String.valueOf(i));
            button.setTextSize(24);
            button.setGravity(Gravity.CENTER);
            button.setOnClickListener(new NumberClickListener(i));
            keypadGrid.addView(button);
        }

        // Add a "Clear" button
        Button clearButton = new Button(this);
        clearButton.setText("CLEAR");
        clearButton.setTextSize(18);
        clearButton.setGravity(Gravity.CENTER);
        clearButton.setOnClickListener(v -> {
            enteredCode.setLength(0);
            updateDisplay();
        });
        keypadGrid.addView(clearButton);

        // Add a zero button
        Button button = new Button(this);
        button.setText(String.valueOf(0));
        button.setTextSize(24);
        button.setGravity(Gravity.CENTER);
        button.setOnClickListener(new NumberClickListener(0));
        keypadGrid.addView(button);

        // Add an "Enter" button
        Button enterButton = new Button(this);
        enterButton.setText("ENTER");
        enterButton.setTextSize(18);
        enterButton.setGravity(Gravity.CENTER);
        enterButton.setOnClickListener(new EnterClickListener());
        keypadGrid.addView(enterButton);

    }
    private class NumberClickListener implements View.OnClickListener {
        private final int number;

        NumberClickListener(int number) {
            this.number = number;
        }

        @Override
        public void onClick(View v) {
            enteredCode.append(number);
            updateDisplay();
        }
    }
    private class EnterClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // Get the entered code (dollar amount)
            int itemNum = Integer.parseInt(enteredCode.toString());
            double dollarAmount;
            if (itemNum > 0 && itemNum < ITEM_COUNT + 1) {
                dollarAmount = prices[itemNum];
            }
            else {
                displayTextView.setText("Invalid Item Number");
                return;
            }
            // Create an Intent to start the DiscoverReadersActivity
            Intent intent = new Intent(MainActivity.this, DollarAmountActivity.class);
            // Pass the dollar amount as an extra
            intent.putExtra("DOLLAR_AMOUNT", dollarAmount);

            // Start the new activity
            startActivity(intent);
        }
    }
    private void updateDisplay() {
        displayTextView.setText(enteredCode.toString());
    }
}
