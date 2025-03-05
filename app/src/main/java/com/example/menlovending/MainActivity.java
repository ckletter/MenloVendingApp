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

import com.example.menlovending.stripe.permissions.PermissionService;
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

        prices[1] = 0.50;
        prices[2] = 1;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        displayTextView = findViewById(R.id.display_text_view);
        GridLayout keypadGrid = findViewById(R.id.keypad_grid);
        // Define button size parameters
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0; // Fill available space in the column
        params.height = 200; // Increase button height for better touch interaction
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); // Make columns evenly distributed
        params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); // Make rows evenly distributed
        params.setMargins(8, 8, 8, 8); // Add spacing between buttons


        for (int i = 1; i <= 9; i++) {
            Button button = new Button(this);
            button.setText(String.valueOf(i));
            button.setTextSize(32);
            button.setGravity(Gravity.CENTER);
            button.setPadding(16, 16, 16, 16);
            button.setLayoutParams(new GridLayout.LayoutParams(params)); // Apply layout params
            button.setOnClickListener(new NumberClickListener(i));
            keypadGrid.addView(button);
        }

        // Add a "Clear" button
        Button clearButton = new Button(this);
        clearButton.setText("CLEAR");
        clearButton.setTextSize(24);
        clearButton.setGravity(Gravity.CENTER);
        clearButton.setPadding(16, 16, 16, 16);
        clearButton.setLayoutParams(new GridLayout.LayoutParams(params));
        clearButton.setOnClickListener(v -> {
            enteredCode.setLength(0);
            updateDisplay();
        });
        keypadGrid.addView(clearButton);

        // Add a zero button
        Button zeroButton = new Button(this);
        zeroButton.setText(String.valueOf(0));
        zeroButton.setTextSize(32);
        zeroButton.setGravity(Gravity.CENTER);
        zeroButton.setPadding(16, 16, 16, 16);
        zeroButton.setLayoutParams(new GridLayout.LayoutParams(params));
        zeroButton.setOnClickListener(new NumberClickListener(0));
        keypadGrid.addView(zeroButton);

        // Add an "Enter" button
        Button enterButton = new Button(this);
        enterButton.setText("ENTER");
        enterButton.setTextSize(24);
        enterButton.setGravity(Gravity.CENTER);
        enterButton.setPadding(16, 16, 16, 16);
        enterButton.setLayoutParams(new GridLayout.LayoutParams(params));
        enterButton.setOnClickListener(new EnterClickListener());
        keypadGrid.addView(enterButton);

        // Check Permissions
//        PermissionService.checkPermissions(this);

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
                // Delay clearing the text for 2 seconds
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        enteredCode.setLength(0);  // Clear the entered code
                        updateDisplay();  // Update the display
                    }
                }, 2000); // 2000 milliseconds = 2 seconds
                return;
            }
            enteredCode.setLength(0);  // Clear the entered code
            updateDisplay();
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
