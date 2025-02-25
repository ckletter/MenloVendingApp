package com.example.menlovending;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.connection.ConnectionTokenProvider;
import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.external.models.ConnectionTokenException;
import com.stripe.stripeterminal.external.models.TerminalException;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.menlovending.stripe.client.TerminalEventListener;
import com.example.menlovending.stripe.client.TokenProvider;
import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.external.callable.Callback;
import com.stripe.stripeterminal.external.callable.Cancelable;
import com.stripe.stripeterminal.external.models.DiscoveryConfiguration;
import com.stripe.stripeterminal.external.models.TerminalException;

import com.stripe.stripeterminal.external.callable.DiscoveryListener;
import com.stripe.stripeterminal.external.models.Reader;
import com.stripe.stripeterminal.log.LogLevel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DiscoverReadersActivity extends AppCompatActivity implements DiscoveryListener {
    // ...
    private double dollarAmount;
    Cancelable discoverCancelable = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("DiscoverReadersActivity", "onCreate");
        discoverReadersAction();
        // Retrieve the extra from the intent
        dollarAmount = getIntent().getDoubleExtra("DOLLAR_AMOUNT", 0.0);
    }

    // Action for a "Discover Readers" button
    public void discoverReadersAction() {
        // Initialize Stripe Terminal SDK
        if (!Terminal.isInitialized()) {
            try {
                Terminal.initTerminal(
                        getApplicationContext(),
                        LogLevel.VERBOSE,
                        new TokenProvider(),
                        new TerminalEventListener()
                );
                Log.d("MenloVendingApplication", "Stripe Terminal initialized successfully.");
            } catch (TerminalException e) {
                Log.e("MenloVendingApplication", "Error initializing Stripe Terminal: " + e.getMessage());
            }
        }
        int timeout = 0;
        boolean isSimulated = false;

        DiscoveryConfiguration.BluetoothDiscoveryConfiguration config = new DiscoveryConfiguration.BluetoothDiscoveryConfiguration(timeout, isSimulated);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("DiscoverReadersActivity", "Permission not granted");
            return;
        }
        Log.d("DiscoverReadersActivity", "Discovering reader.");
        discoverCancelable = Terminal.getInstance().discoverReaders(
                config,
                this,
                new Callback() {

                    @Override
                    public void onSuccess() {
                        Log.d("DiscoverReadersActivity", "Discovered!");
                    }

                    @Override
                    public void onFailure(@NotNull TerminalException e) {
                        Log.e("DiscoverReadersActivity", "Not discovered");
                    }
                }
        );
    }

    @Override
    public void onUpdateDiscoveredReaders(@NotNull List<Reader> readers) {
        if (readers.isEmpty()) {
            return;
        }

        // Select the first reader for now (or prompt the user to select one)
        Reader selectedReader = readers.get(0);
        ReaderManager.getInstance().setReaders(new ArrayList<>(readers));

        // Pass only the serial number or any other necessary information
        Intent intent = new Intent(DiscoverReadersActivity.this, DollarAmountActivity.class);
        intent.putExtra("reader_serial", selectedReader.getSerialNumber());
        // Pass the dollar amount as an extra
        intent.putExtra("DOLLAR_AMOUNT", dollarAmount);
        startActivity(intent);
    }
}