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

import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.external.callable.TerminalListener;
import com.stripe.stripeterminal.external.models.ConnectionStatus;
import com.stripe.stripeterminal.external.models.PaymentStatus;
import com.stripe.stripeterminal.external.models.TerminalException;
import com.stripe.stripeterminal.log.LogLevel;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_LOCATION = 1;
    private TextView displayTextView;
    private StringBuilder enteredCode = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

//    @Override
//    public void onRequestPermissionsResult(
//            int requestCode,
//            @NotNull String[] permissions,
//            @NotNull int[] grantResults
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (requestCode == REQUEST_CODE_LOCATION && grantResults.length > 0 &&
//                grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//            throw new RuntimeException("Location services are required in order to connect to a reader.");
//        }
//    }
//
//    private void createTerminalListener() throws TerminalException {
//        // Make sure ACCESS_FINE_LOCATION is initialized in terminal app
//        if (ContextCompat.checkSelfPermission(getActivity(),
//                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            String[] permissions = {
//                    android.Manifest.permission.ACCESS_FINE_LOCATION
//            };
//            ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_CODE_LOCATION);
//        }
//        // Create your listener object. Override any methods that you want to be notified about
//        TerminalListener listener = new TerminalListener() {
//            @Override
//            public void onConnectionStatusChange(ConnectionStatus status) {
//                System.out.printf("onConnectionStatusChange: %s\n", status);
//            }
//
//            @Override
//            public void onPaymentStatusChange(PaymentStatus status) {
//                System.out.printf("onPaymentStatusChange: %s\n ", status);
//            }
//        };
//
//        // Choose the level of messages that should be logged to your console
//        LogLevel logLevel = LogLevel.VERBOSE;
//
//        // Create your token provider.
//        CustomConnectionTokenProvider tokenProvider = new CustomConnectionTokenProvider();
//
//        // Pass in the current application context, your desired logging level, your token provider, and the listener you created
//        if (!Terminal.isInitialized()) {
//            Terminal.initTerminal(getApplicationContext(), logLevel, tokenProvider, listener);
//        }
//
//        // Since the Terminal is a singleton, you can call getInstance whenever you need it
//        Terminal.getInstance();
//    }

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
            String dollarAmount = "3.50";

            // Create an Intent to start the DollarAmountActivity
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
