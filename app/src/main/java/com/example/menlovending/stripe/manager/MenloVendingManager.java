package com.example.menlovending.stripe.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.menlovending.stripe.client.ReaderListener;
import com.example.menlovending.stripe.client.ReaderUpdate;
import com.example.menlovending.stripe.client.TerminalEventListener;
import com.example.menlovending.stripe.client.TokenProvider;
import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.external.callable.Callback;
import com.stripe.stripeterminal.external.callable.Cancelable;
import com.stripe.stripeterminal.external.callable.DiscoveryListener;
import com.stripe.stripeterminal.external.callable.ReaderCallback;
import com.stripe.stripeterminal.external.models.ConnectionConfiguration;
import com.stripe.stripeterminal.external.models.ConnectionStatus;
import com.stripe.stripeterminal.external.models.DiscoveryConfiguration;
import com.stripe.stripeterminal.external.models.PaymentStatus;
import com.stripe.stripeterminal.external.models.Reader;
import com.stripe.stripeterminal.external.models.TerminalException;
import com.stripe.stripeterminal.log.LogLevel;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.menlovending.BuildConfig;

public class MenloVendingManager implements DiscoveryListener {
    private static final MenloVendingManager INSTANCE = new MenloVendingManager();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private ConnectionStatus connectionStatus = ConnectionStatus.NOT_CONNECTED;
    private PaymentStatus paymentStatus = PaymentStatus.NOT_READY;
    private Cancelable discoverCancelable;

    private MenloVendingManager() {}

    public static MenloVendingManager getInstance() {
        return INSTANCE;
    }

    public void initialize(Context context) {
        executorService.execute(() -> {
//            MenloVendingState.MenloVendingState(MenloVendingState.MenloVendingStatus.INITIALIZING, "Initializing...", "");
            connectionStatus = ConnectionStatus.NOT_CONNECTED;
            paymentStatus = PaymentStatus.NOT_READY;
            if (context != null) {
                try {
                    TerminalEventListener listener = new TerminalEventListener();
                    TokenProvider tokenProvider = new TokenProvider();
                    if (!Terminal.isInitialized()) {
                        Terminal.initTerminal(context, LogLevel.VERBOSE, tokenProvider, listener);
                    }
                } catch (Exception e) {
                    System.out.println("Failed to initialize Stripe SDK: " + e.getMessage());
                    return;
                }
            }
            discoverReaders();
        });
    }

    public void onConnectionStatusChange(ConnectionStatus status) {
        System.out.println("Connection status changed to: " + status);
        this.connectionStatus = status;
    }

    public void onPaymentStatusChange(PaymentStatus status) {
        System.out.println("Payment status changed to: " + status);
        this.paymentStatus = status;
    }

    @SuppressLint("MissingPermission")
    private void discoverReaders() {
        DiscoveryConfiguration.UsbDiscoveryConfiguration config =
                new DiscoveryConfiguration.UsbDiscoveryConfiguration(10, true);

        Terminal.getInstance().discoverReaders(config, this, new Callback() {
            @Override
            public void onSuccess() {}

            @Override
            public void onFailure(TerminalException e) {
                System.out.println("Failed to discover readers: " + e.getMessage());
//                retryInitialization();
            }
        });
    }

    @Override
    public void onUpdateDiscoveredReaders(List<Reader> readers) {
        if (!readers.isEmpty()) {
            ConnectionConfiguration.UsbConnectionConfiguration connectionConfig =
                    new ConnectionConfiguration.UsbConnectionConfiguration(
                            BuildConfig.STRIPE_LOCATION,
                            true,
                            new ReaderListener(
                                    this::onReconnectStarted,
                                    this::onReconnectSucceeded,
                                    this::onReconnectFailed,
                                    this::onSoftwareUpdate
                            )
                    );
            Terminal.getInstance().connectReader(
                    readers.get(0),
                    connectionConfig,
                    new ReaderCallback() {
                        @Override
                        public void onSuccess(Reader reader) {
                            System.out.println("Connected to reader");
                        }

                        @Override
                        public void onFailure(TerminalException e) {
                            System.out.println("Failed to connect to reader: " + e.getMessage());
//                            retryInitialization();
                        }
                    }
            );
        }
    }

    private void onReconnectStarted() {
        System.out.println("Reconnecting to Stripe Terminal...");
    }

    private void onReconnectSucceeded() {
        System.out.println("Reconnected to Stripe Terminal");
    }

    private void onReconnectFailed() {
        System.out.println("Failed to reconnect to Stripe Terminal");
//        retryInitialization();
    }

    private void onSoftwareUpdate(ReaderUpdate updateProgress) {
        if (updateProgress.isUpdating()) {
            System.out.println("Updating Stripe Terminal (" + Math.round(updateProgress.getProgress() * 100) + "%)");
        } else {
            System.out.println("Updated Stripe Terminal");
        }
    }
//
//    private void retryInitialization() {
//        new Handler(Looper.getMainLooper()).postDelayed(this::initialize, 15000);
//    }
}