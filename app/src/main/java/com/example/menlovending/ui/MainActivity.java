package com.example.menlovending.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import android.view.Gravity;
import android.view.View;
import android.widget.Button;

import com.example.menlovending.R;
import com.example.menlovending.stripe.manager.MenloVendingManager;
import com.example.menlovending.stripe.manager.MenloVendingState;

public class MainActivity extends AppCompatActivity {
    public static final int ITEM_COUNT = 16;
    private TextView displayTextView;
    private StringBuilder enteredCode = new StringBuilder();
    private static final int CONNECTION_CHECK_INTERVAL = 3000;
    private Handler connectionCheckHandler = new Handler();
    private Runnable connectionCheckRunnable;
    private double[] prices = new double[45];
    int[] validItemNums = {11, 12, 13, 14, 21, 22, 23, 24, 31, 32, 33, 34, 41, 42, 43, 44};
    boolean isValidItem = false;
    // Track if we've already shown disconnect notifications
    private static boolean arduino1DisconnectNotified = false;
    private static boolean arduino2DisconnectNotified = false;
    private static boolean readerDisconnectNotified = false;

    private boolean activityActive = true; // Track if this activity is active

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        prices[11] = 2.00;
        prices[12] = 2.00;
        prices[13] = 2.00;
        prices[14] = 3.00;
        prices[21] = 0.50;
        prices[22] = 0.50;
        prices[23] = 0.50;
        prices[24] = 2.00;
        prices[31] = 0.50;
        prices[32] = 0.50;
        prices[33] = 0.50;
        prices[34] = 0.50;
        prices[41] = 4.00;
        prices[42] = 4.00;
        prices[43] = 0.50;
        prices[44] = 0.50;
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


        startConnectionMonitoring();
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
            // Did not enter a code
            if (enteredCode.toString().isEmpty()) {
                displayTextView.setText("Please enter an Item Number");
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
            // Get the entered code (dollar amount)
            int itemNum = Integer.parseInt(enteredCode.toString());
            double dollarAmount = 0.0;
            // Check if the entered item number is in the list of valid numbers
            for (int validNum : validItemNums) {
                if (itemNum == validNum) {
                    isValidItem = true;
                    dollarAmount = prices[validNum]; // You'll need to adjust your prices array accordingly
                    break;
                }
            }

            if (!isValidItem) {
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
            // Pass user selection as an extra
            intent.putExtra("code", itemNum);
            // Start the new activity
            startActivity(intent);
        }
    }
    private void updateDisplay() {
        displayTextView.setText(enteredCode.toString());
    }

    private void startConnectionMonitoring() {
        connectionCheckRunnable = new Runnable() {
            @Override
            public void run() {
                checkArduinoConnection();
                checkReaderConnection();
                connectionCheckHandler.postDelayed(this, CONNECTION_CHECK_INTERVAL);
            }
        };

        // Start the initial check
        connectionCheckHandler.post(connectionCheckRunnable);
    }

    private void checkArduinoConnection() {
        // Arduino 1 connection check
        boolean isArduino1Connected = MenloVendingManager.getInstance().getArduinoHelper().isConnectionEstablished();

        // Only notify if Arduino 1 is disconnected AND we haven't already notified
        if (!isArduino1Connected && !arduino1DisconnectNotified) {
            // Stop the connection check handler
            connectionCheckHandler.removeCallbacks(connectionCheckRunnable);
            // Mark this activity as inactive
            activityActive = false;
            // Set the flag to indicate we've notified about this disconnect
            arduino1DisconnectNotified = true;

            Intent intent = new Intent(MainActivity.this, ArduinoDisconnectActivity.class);
            Log.d("MainActivity", "Preparing to launch ArduinoDisconnectActivity for Arduino 1");
            intent.putExtra("arduino", 1);
            try {
                startActivity(intent);
                Log.d("MainActivity", "Started ArduinoDisconnectActivity successfully");
            } catch (Exception e) {
                Log.e("MainActivity", "Failed to start ArduinoDisconnectActivity", e);
            }
            return; // Return early to prevent checking Arduino 2 in the same cycle
        }
        // Reset notification flag if Arduino 1 is connected again
        else if (isArduino1Connected) {
            arduino1DisconnectNotified = false;
        }

        // Arduino 2 connection check - only proceed if we didn't already launch a disconnect activity
        boolean isArduino2Connected = MenloVendingManager.getInstance().getArduinoHelper2().isConnectionEstablished();
        if (!isArduino2Connected && !arduino2DisconnectNotified) {
            // Stop the connection check handler
            connectionCheckHandler.removeCallbacks(connectionCheckRunnable);
            // Mark this activity as inactive
            activityActive = false;
            // Set the flag to indicate we've notified about this disconnect
            arduino2DisconnectNotified = true;

            Intent intent = new Intent(MainActivity.this, ArduinoDisconnectActivity.class);
            Log.d("MainActivity", "Preparing to launch ArduinoDisconnectActivity for Arduino 2");
            intent.putExtra("arduino", 2);
            try {
                startActivity(intent);
                Log.d("MainActivity", "Started ArduinoDisconnectActivity successfully");
            } catch (Exception e) {
                Log.e("MainActivity", "Failed to start ArduinoDisconnectActivity", e);
            }
        }
        // Reset notification flag if Arduino 2 is connected again
        else if (isArduino2Connected) {
            arduino2DisconnectNotified = false;
        }
    }

    private void checkReaderConnection() {
        MenloVendingState currentState = MenloVendingManager.getInstance().getMenloVendingState();

        boolean isReaderDisconnected = (currentState.getStatus() == MenloVendingState.MenloVendingStatus.ERROR ||
                currentState.getStatus() == MenloVendingState.MenloVendingStatus.FATAL ||
                currentState.getStatus() == MenloVendingState.MenloVendingStatus.INITIALIZING);

        // Only notify if reader is disconnected AND we haven't already notified
        if (isReaderDisconnected && !readerDisconnectNotified) {
            // Stop the connection check handler
            connectionCheckHandler.removeCallbacks(connectionCheckRunnable);
            // Mark this activity as inactive
            activityActive = false;
            // Set the flag to indicate we've notified about this disconnect
            readerDisconnectNotified = true;

            // Launch the disconnection activity
            Intent intent = new Intent(MainActivity.this, ReaderDisconnectActivity.class);
            Log.d("MainActivity", "Launching ReaderDisconnectActivity");
            startActivity(intent);
        }
        // Reset notification flag if reader is connected again
        else if (!isReaderDisconnected) {
            readerDisconnectNotified = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reset the activity state
        activityActive = true;

        // Add a delay before restarting the connection checks
        // This prevents immediate reconnection attempt after returning from disconnect activity
        connectionCheckHandler.removeCallbacks(connectionCheckRunnable);
        connectionCheckHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Only start connection monitoring if we're still active
                if (activityActive) {
                    connectionCheckHandler.post(connectionCheckRunnable);
                }
            }
        }, 3000); // 3-second delay
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Temporarily pause checks when activity is not visible
        connectionCheckHandler.removeCallbacks(connectionCheckRunnable);
    }
    @Override
    protected void onDestroy() {
        // Make sure to remove callbacks to prevent memory leaks
        activityActive = false;
        connectionCheckHandler.removeCallbacks(connectionCheckRunnable);
        super.onDestroy();
    }
    public static void resetDisconnectFlags() {
        arduino1DisconnectNotified = false;
        arduino2DisconnectNotified = false;
        readerDisconnectNotified = false;
    }

}
