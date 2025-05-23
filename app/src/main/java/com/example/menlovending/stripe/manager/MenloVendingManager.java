package com.example.menlovending.stripe.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.menlovending.stripe.client.ReaderListener;
import com.example.menlovending.stripe.client.ReaderListenerCallback;
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

import jssc.SerialPortException;

public class MenloVendingManager implements DiscoveryListener {
    private static final MenloVendingManager INSTANCE = new MenloVendingManager();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private ConnectionStatus connectionStatus = ConnectionStatus.NOT_CONNECTED;
    private PaymentStatus paymentStatus = PaymentStatus.NOT_READY;
    private Cancelable discoverCancelable;
    private MenloVendingState menloVendingState;
    private static final String ARDUINO_1_IP = "172.16.0.22"; // Static IP for Arduino 1
    private static final String ARDUINO_2_IP = "172.16.0.23"; // Static IP for Arduino 2

    private ArduinoHelper arduinoHelper;
    private ArduinoHelper arduinoHelper2;

    private MenloVendingManager() {}

    public static MenloVendingManager getInstance() {
        return INSTANCE;
    }

    public MenloVendingState getMenloVendingState() {
        return menloVendingState;
    }
    public ArduinoHelper getArduinoHelper() {
        return arduinoHelper; // Provide instance via getter
    }
    public ArduinoHelper getArduinoHelper2() {
        return arduinoHelper2; // Provide instance via getter
    }

    public void initialize(Context context) {
        executorService.execute(() -> {
            menloVendingState = new MenloVendingState(MenloVendingState.MenloVendingStatus.INITIALIZING, "Initializing...", "");
            connectionStatus = ConnectionStatus.NOT_CONNECTED;
            paymentStatus = PaymentStatus.NOT_READY;
            if (context != null) {
                arduinoHelper = new ArduinoHelper(ARDUINO_1_IP);
                arduinoHelper2 = new ArduinoHelper(ARDUINO_2_IP);
                arduinoHelper.findAndConnectDevice();
                arduinoHelper2.findAndConnectDevice();
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
            // Disconnect from any existing reader first before discovering new readers
            disconnectReader(() -> {
                // Only proceed with reader discovery after disconnection completes
                discoverReaders();
            });
        });
    }
    public void initializeReader(Context context) {
        // Disconnect from any existing reader first before discovering new readers
        disconnectReader(() -> {
            // Only proceed with reader discovery after disconnection completes
            discoverReaders();
        });
    }
    public void onConnectionStatusChange(ConnectionStatus status) {
        System.out.println("Connection status changed to: " + status);
        this.connectionStatus = status;
        updateStatus();
    }

    public void onPaymentStatusChange(PaymentStatus status) {
        System.out.println("Payment status changed to: " + status);
        this.paymentStatus = status;
        updateStatus();
    }

    @SuppressLint("MissingPermission")
    private void discoverReaders() {
        DiscoveryConfiguration.BluetoothDiscoveryConfiguration config =
                new DiscoveryConfiguration.BluetoothDiscoveryConfiguration(0, false);

        Terminal.getInstance().discoverReaders(config, this, new Callback() {
            @Override
            public void onSuccess() {}

            @Override
            public void onFailure(TerminalException e) {
                System.out.println("Failed to discover readers: " + e.getMessage());
            }
        });
    }

    @Override
    public void onUpdateDiscoveredReaders(List<Reader> readers) {
        if (!readers.isEmpty()) {
            ReaderManager.getInstance().setReaders(readers);
            Log.d("Manager", "Discovered " + readers.size() + " readers");
            ReaderManager.getInstance().listReaders();
            Reader bbpos = ReaderManager.getInstance().getReaderBySerial("CHB20Z118001480");
            if (bbpos != null) {
                ReaderManager.getInstance().setLastConnectedReader(bbpos);
                ConnectionConfiguration.BluetoothConnectionConfiguration connectionConfig =
                        new ConnectionConfiguration.BluetoothConnectionConfiguration(
                                BuildConfig.STRIPE_LOCATION,
                                true,
                                new ReaderListener(
                                        this::onReconnectStarted,
                                        this::onReconnectSucceeded,
                                        this::onReconnectFailed,
                                        this::onSoftwareUpdate,
                                        new ReaderListenerCallback() {
                                            @Override
                                            public void showToastMessage(String message) {
                                                if (ContextHolder.getContext() != null) {
                                                    android.os.Handler mainHandler = new android.os.Handler(ContextHolder.getContext().getMainLooper());
                                                    mainHandler.post(() ->
                                                            Toast.makeText(ContextHolder.getContext(), message, Toast.LENGTH_SHORT).show()
                                                    );
                                                }
                                            }
                                        }
                                )
                        );
                Terminal.getInstance().connectReader(
                        bbpos,
                        connectionConfig,
                        new ReaderCallback() {
                            @Override
                            public void onSuccess(Reader reader) {
                                Log.d("Manager", "Connected to reader");
                            }

                            @Override
                            public void onFailure(TerminalException e) {
                                fatalStatus("Failed to connect to reader", e.getMessage());
                            }
                        }
                );
            }
        }
    }
    public void reconnectReader() {
        Reader reader = ReaderManager.getInstance().getLastConnectedReader();
        if (reader != null) {
            ConnectionConfiguration.BluetoothConnectionConfiguration connectionConfig =
                    new ConnectionConfiguration.BluetoothConnectionConfiguration(
                            BuildConfig.STRIPE_LOCATION,
                            true,
                            new ReaderListener(
                                    this::onReconnectStarted,
                                    this::onReconnectSucceeded,
                                    this::onReconnectFailed,
                                    this::onSoftwareUpdate,
                                    new ReaderListenerCallback() {
                                        @Override
                                        public void showToastMessage(String message) {
                                            if (ContextHolder.getContext() != null) {
                                                android.os.Handler mainHandler = new android.os.Handler(ContextHolder.getContext().getMainLooper());
                                                mainHandler.post(() ->
                                                        Toast.makeText(ContextHolder.getContext(), message, Toast.LENGTH_SHORT).show()
                                                );
                                            }
                                        }
                                    }
                            )
                    );

            Terminal.getInstance().connectReader(
                    reader,
                    connectionConfig,
                    new ReaderCallback() {
                        @Override
                        public void onSuccess(Reader connectedReader) {
                            Log.d("Reader", "Reconnected to reader successfully");
                        }

                        @Override
                        public void onFailure(TerminalException e) {
                            Log.e("Reader", "Failed to reconnect to reader", e);
                        }
                    }
            );
        }
    }

    private void onReconnectStarted() {
        System.out.println("Reconnecting to Stripe Terminal...");
        menloVendingState = new MenloVendingState(MenloVendingState.MenloVendingStatus.INITIALIZING, "Reconnecting to Stripe Terminal...", "");
    }

    private void onReconnectSucceeded() {
        Log.d("Manager", "Reconnected to Stripe Terminal");
        menloVendingState = new MenloVendingState(MenloVendingState.MenloVendingStatus.READY, "Reconnected to Stripe Terminal", "");
    }

    private void onReconnectFailed() {
        Log.d("Manager", "Failed to reconnect to Stripe Terminal");
        fatalStatus("Failed to reconnect to Stripe Terminal", "Unknown Error");
    }

    private void onSoftwareUpdate(ReaderUpdate updateProgress) {
        if (updateProgress.isUpdating()) {
            menloVendingState = new MenloVendingState(MenloVendingState.MenloVendingStatus.INITIALIZING, "Updating Stripe Terminal (" + Math.round(updateProgress.getProgress() * 100) + "%)", "Progress: " + Math.round(updateProgress.getProgress() * 100) + "%");
        } else {
            System.out.println("Updated Stripe Terminal");
            menloVendingState = new MenloVendingState(MenloVendingState.MenloVendingStatus.READY, "Updated Stripe Terminal", "");
        }
    }
    // Global Status Update
    private void updateStatus() {
        if (connectionStatus == ConnectionStatus.NOT_CONNECTED) {
            menloVendingState = new MenloVendingState(MenloVendingState.MenloVendingStatus.ERROR,
                    "Disconnected from Stripe Terminal", "");
        }
        else if (connectionStatus == ConnectionStatus.CONNECTING) {
            menloVendingState = new MenloVendingState(MenloVendingState.MenloVendingStatus.INITIALIZING,
                    "Connecting to Stripe Terminal...", "");
        }
        else if (connectionStatus == ConnectionStatus.CONNECTED) {
            menloVendingState = new MenloVendingState(MenloVendingState.MenloVendingStatus.READY,
                    "Connected to Stripe Terminal", "");

        }
        else if (connectionStatus == ConnectionStatus.DISCOVERING) {
            menloVendingState = new MenloVendingState(MenloVendingState.MenloVendingStatus.INITIALIZING,
                    "Searching for Stripe Terminal...", "");

        }
        if (paymentStatus == PaymentStatus.NOT_READY) {
            menloVendingState = new MenloVendingState(MenloVendingState.MenloVendingStatus.ERROR,
                    "Disconnected from Stripe Terminal", "");
        }
        else if (paymentStatus == PaymentStatus.READY) {
            menloVendingState = new MenloVendingState(MenloVendingState.MenloVendingStatus.READY,
                    "Connected to Stripe Terminal", "");

        }
    }
    public void fatalStatus(String message, String details) {
        menloVendingState = new MenloVendingState(MenloVendingState.MenloVendingStatus.FATAL, message, details);
    }

    /**
     * Disconnects from the currently connected reader
     * @param onDisconnectComplete Optional callback to run after disconnection completes
     */
    public void disconnectReader(Runnable onDisconnectComplete) {
        if (!Terminal.isInitialized()) {
            Log.d("VendingManager", "Terminal not initialized, no need to disconnect");
            if (onDisconnectComplete != null) {
                onDisconnectComplete.run();
            }
            return;
        }

        ConnectionStatus status = Terminal.getInstance().getConnectionStatus();
        if (status == ConnectionStatus.CONNECTED) {
            Log.d("VendingManager", "Disconnecting from reader...");
            menloVendingState = new MenloVendingState(
                    MenloVendingState.MenloVendingStatus.INITIALIZING,
                    "Disconnecting from reader...",
                    ""
            );

            Terminal.getInstance().disconnectReader(new Callback() {
                @Override
                public void onSuccess() {
                    Log.d("VendingManager", "Reader disconnected successfully");
                    connectionStatus = ConnectionStatus.NOT_CONNECTED;
                    updateStatus();
                    if (onDisconnectComplete != null) {
                        onDisconnectComplete.run();
                    }
                }

                @Override
                public void onFailure(TerminalException e) {
                    Log.e("VendingManager", "Failed to disconnect reader: " + e.getMessage());
                    // Continue anyway since we want to try to discover readers
                    if (onDisconnectComplete != null) {
                        onDisconnectComplete.run();
                    }
                }
            });
        } else {
            Log.d("VendingManager", "Reader not connected (status: " + status + "), no need to disconnect");
            if (onDisconnectComplete != null) {
                onDisconnectComplete.run();
            }
        }
    }

    /**
     * Disconnects from the currently connected reader (no callback)
     */
    public void disconnectReader() {
        disconnectReader(null);
    }
}