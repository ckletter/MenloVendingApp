package com.example.menlovending;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.external.callable.Callback;
import com.stripe.stripeterminal.external.callable.Cancelable;
import com.stripe.stripeterminal.external.models.DiscoveryConfiguration;
import com.stripe.stripeterminal.external.models.TerminalException;

import com.stripe.stripeterminal.external.callable.DiscoveryListener;
import com.stripe.stripeterminal.external.models.Reader;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DiscoverReadersActivity extends AppCompatActivity implements DiscoveryListener {
    // ...

    Cancelable discoverCancelable = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        discoverReadersAction();
    }

    // Action for a "Discover Readers" button
    public void discoverReadersAction() {
        int timeout = 0;
        boolean isSimulated = false;

        DiscoveryConfiguration.UsbDiscoveryConfiguration config = new DiscoveryConfiguration.UsbDiscoveryConfiguration(timeout, isSimulated);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        discoverCancelable = Terminal.getInstance().discoverReaders(
                config,
                this,
                new Callback() {

                    @Override
                    public void onSuccess() {
                        // Placeholder for handling successful operation
                    }

                    @Override
                    public void onFailure(@NotNull TerminalException e) {
                        // Placeholder for handling exception
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

        // Pass only the serial number or any other necessary information
        Intent intent = new Intent(DiscoverReadersActivity.this, DollarAmountActivity.class);
        intent.putExtra("reader_serial", selectedReader.getSerialNumber());
        startActivity(intent);
    }

    // ...
}