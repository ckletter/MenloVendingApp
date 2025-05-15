package com.example.menlovending.stripe.client;

import static com.example.menlovending.stripe.manager.MenloVendingManager.getInstance;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.menlovending.BuildConfig;
import com.example.menlovending.stripe.manager.ArduinoHelper;
import com.example.menlovending.stripe.manager.ContextHolder;
import com.example.menlovending.stripe.manager.ReaderManager;
import com.example.menlovending.ui.PaymentSuccessActivity;

import static spark.Spark.post;
import com.example.menlovending.stripe.manager.MenloVendingManager;
import com.stripe.exception.StripeException;
import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.TerminalApplicationDelegate;
import com.stripe.stripeterminal.external.callable.Callback;
import com.stripe.stripeterminal.external.callable.Cancelable;
import com.stripe.stripeterminal.external.callable.PaymentIntentCallback;
import com.stripe.stripeterminal.external.callable.ReaderCallback;
import com.stripe.stripeterminal.external.models.CaptureMethod;
import com.stripe.stripeterminal.external.models.ConnectionConfiguration;
import com.stripe.stripeterminal.external.models.PaymentIntent;
import com.stripe.stripeterminal.external.models.PaymentIntentParameters;
import com.stripe.stripeterminal.external.models.Reader;
import com.stripe.stripeterminal.external.models.TerminalException;

import jssc.SerialPortException;

public class StripeTerminalApplication extends Application {
    private static com.stripe.stripeterminal.external.models.PaymentIntent currentPaymentIntent;
    private static Cancelable collectPaymentMethodCancelable;
    private static int currentItemCode = -1;
    private static double currentAmount = 0.0;
    private static boolean isProcessingPayment = false;
    private static boolean paymentCompleted = false;

    @Override
    public void onCreate() {
        super.onCreate();
        getInstance().initialize(this);
        ContextHolder.setContext(getApplicationContext());

        TerminalApplicationDelegate.onCreate(this);
    }

    public interface PaymentCancellationCallback {
        void onCancellationComplete();
        void onCancellationFailed(Exception e);
    }


    public static void cancelPaymentIntent(final PaymentCancellationCallback callback) {
        if (currentPaymentIntent != null) {
            final int canceledItemCode = currentItemCode;
            Log.d("PaymentIntent", "Canceling payment for item: " + canceledItemCode);

            // Cancel collecting payment method if in progress
            if (collectPaymentMethodCancelable != null) {
                collectPaymentMethodCancelable.cancel(new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d("PaymentIntent", "Payment method collection canceled for item: " + canceledItemCode);

                        // Store the currently connected reader to potentially reconnect if needed
                        final Reader connectedReader = Terminal.getInstance().getConnectedReader();

                        if (connectedReader != null) {
                            // The reader is connected, proceed with canceling the payment intent
                            cancelActivePaymentIntent(callback, connectedReader);
                        } else {
                            Log.d("PaymentIntent", "Reader is not connected, skipping cancelPaymentIntent call");
                            resetPaymentStateOnly();
                            if (callback != null) {
                                callback.onCancellationComplete();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull TerminalException e) {
                        Log.e("PaymentIntent", "Failed to cancel payment method collection for item: " + canceledItemCode, e);

                        // Store the currently connected reader
                        final Reader connectedReader = Terminal.getInstance().getConnectedReader();

                        if (connectedReader != null) {
                            // The reader is connected, proceed with canceling the payment intent
                            cancelActivePaymentIntent(callback, connectedReader);
                        } else {
                            resetPaymentStateOnly();
                            if (callback != null) {
                                callback.onCancellationFailed(e);
                            }
                        }
                    }
                });
            } else {
                // No active collection to cancel, proceed to cancel payment intent
                final Reader connectedReader = Terminal.getInstance().getConnectedReader();

                if (connectedReader != null) {
                    cancelActivePaymentIntent(callback, connectedReader);
                } else {
                    Log.w("PaymentIntent", "Reader is already disconnected, skipping cancelPaymentIntent call");
                    resetPaymentStateOnly();
                    if (callback != null) {
                        callback.onCancellationComplete();
                    }
                }
            }
        } else {
            // No active payment intent to cancel
            Log.d("PaymentIntent", "No active payment intent to cancel");
            resetPaymentStateOnly();
            if (callback != null) {
                callback.onCancellationComplete();
            }
        }
    }

    /**
     * New method to reset only payment-related state variables
     * This separates payment state from reader connection state
     */
    private static void resetPaymentStateOnly() {
        Log.d("PaymentIntent", "Resetting ONLY payment state variables. Previous item: " + currentItemCode);
        currentPaymentIntent = null;
        collectPaymentMethodCancelable = null;
        currentItemCode = -1;
        isProcessingPayment = false;
        paymentCompleted = false;
        currentAmount = 0.0;
    }

    /**
     * Modified version of cancelActivePaymentIntent that preserves reader connection
     */
    private static void cancelActivePaymentIntent(final PaymentCancellationCallback callback, final Reader connectedReader) {
        Log.d("PaymentIntent", "Canceling active payment intent: " +
                (currentPaymentIntent != null ? currentPaymentIntent.getId() : "null") +
                " for item: " + currentItemCode);

        // Store payment intent ID for logging clarity
        final String paymentIntentId = currentPaymentIntent.getId();

        Terminal.getInstance().cancelPaymentIntent(currentPaymentIntent, new PaymentIntentCallback() {
            @Override
            public void onSuccess(PaymentIntent paymentIntent) {
                Log.d("PaymentIntent", "Successfully canceled payment intent: " + paymentIntentId);

                // Clear payment state variables
                resetPaymentStateOnly();

                // Check if reader is still connected after cancellation
                if (Terminal.getInstance().getConnectedReader() == null && connectedReader != null) {
                    Log.w("PaymentIntent", "Reader disconnected during cancellation, attempting to reconnect");
                    // If implementing reconnection logic, it would go here
                    // For now, just log the issue
                }

                if (callback != null) {
                    callback.onCancellationComplete();
                }
            }

            @Override
            public void onFailure(@NonNull TerminalException e) {
                Log.e("PaymentIntent", "Failed to cancel payment intent: " + paymentIntentId, e);

                // Clear payment state variables even on failure
                resetPaymentStateOnly();

                // Check if reader is still connected after cancellation
                if (Terminal.getInstance().getConnectedReader() == null && connectedReader != null) {
                    Log.w("PaymentIntent", "Reader disconnected during failed cancellation, may need reconnection");
                    // If implementing reconnection logic, it would go here
                }

                if (callback != null) {
                    callback.onCancellationFailed(e);
                }
            }
        });
    }

    /**
     * Modified safelyAbortPayment method that monitors reader connection state
     */
    public static void safelyAbortPayment() {
        if (currentPaymentIntent != null) {
            Log.d("PaymentIntent", "Safely aborting payment for item: " + currentItemCode);

            // Store the connected reader state before cancellation
            final Reader connectedReader = Terminal.getInstance().getConnectedReader();

            cancelPaymentIntent(new PaymentCancellationCallback() {
                @Override
                public void onCancellationComplete() {
                    Log.d("PaymentIntent", "Payment safely aborted");

                    // Check if reader is still connected after abort
                    if (Terminal.getInstance().getConnectedReader() == null && connectedReader != null) {
                        Log.w("PaymentIntent", "Reader disconnected during payment abort");
                        // Consider implementing reconnection logic here if needed
                    }
                }

                @Override
                public void onCancellationFailed(Exception e) {
                    Log.e("PaymentIntent", "Failed to abort payment safely", e);

                    // Check if reader is still connected after failed abort
                    if (Terminal.getInstance().getConnectedReader() == null && connectedReader != null) {
                        Log.w("PaymentIntent", "Reader disconnected during failed payment abort");
                        // Consider implementing reconnection logic here if needed
                    }
                }
            });
        }
    }

    public static void processPayment(double amount, int code) throws StripeException {
        currentItemCode = code;
        currentAmount = amount;
        Log.d("PaymentIntent", "Processing payment for item: " + code + " with amount: $" + amount);
        // Convert from dollars to proper currency
        long amountInCents = (long) (amount * 100);

        PaymentIntentParameters params = new PaymentIntentParameters.Builder()
                .setAmount(amountInCents)
                .setCurrency("usd")
                .setCaptureMethod(CaptureMethod.Automatic)
                .build();

        // Create Payment Intent
        createPaymentIntent(params, code);
    }

    private static void createPaymentIntent(PaymentIntentParameters params, int code) {
        Terminal.getInstance().createPaymentIntent(params, new PaymentIntentCallback() {
            @Override
            public void onSuccess(@NonNull com.stripe.stripeterminal.external.models.PaymentIntent paymentIntent) {
                currentPaymentIntent = paymentIntent;
                collectPaymentMethod(paymentIntent, code);
            }

            @Override
            public void onFailure(@NonNull TerminalException e) {
                getInstance().fatalStatus("Failed to create payment intent", "Unknown Error");
            }
        });
    }

    private static void collectPaymentMethod(com.stripe.stripeterminal.external.models.PaymentIntent paymentIntent, int code) {
        collectPaymentMethodCancelable = Terminal.getInstance().collectPaymentMethod(
                paymentIntent,
                new PaymentIntentCallback() {
                    @Override
                    public void onSuccess(@NonNull com.stripe.stripeterminal.external.models.PaymentIntent updatedPaymentIntent) {
                        confirmPaymentIntent(updatedPaymentIntent, code);
                    }

                    @Override
                    public void onFailure(@NonNull TerminalException e) {
                        getInstance().fatalStatus("Failed to collect payment", "Unknown Error");
                    }
                }
        );
    }

    private static void confirmPaymentIntent(com.stripe.stripeterminal.external.models.PaymentIntent paymentIntent, int code) {
        Terminal.getInstance().confirmPaymentIntent(
                paymentIntent,
                new PaymentIntentCallback() {
                    @Override
                    public void onSuccess(@NonNull com.stripe.stripeterminal.external.models.PaymentIntent confirmedPaymentIntent) {
                        String id = confirmedPaymentIntent.getId();
                        double amount = (double) confirmedPaymentIntent.getAmount() / 100;
                        signalToArduino(code);
                        navigateToPaymentSuccess(amount);

                    }

                    @Override
                    public void onFailure(@NonNull TerminalException e) {
                        getInstance().fatalStatus("Failed to confirm payment", "Unknown Error");
                    }
                }
        );
    }


    private static void navigateToPaymentSuccess(double amount) {
        Intent intent = new Intent(ContextHolder.getContext(), PaymentSuccessActivity.class);
        intent.putExtra("PAYMENT_AMOUNT", amount);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        ContextHolder.getContext().startActivity(intent);
    }

    private static void signalToArduino(int code) {
        if (code <= 24) {
            Log.d("Arduino", code + " Signal sent to Arduino 1");
            getInstance().getArduinoHelper().writeData(code);
        }
        else {
            Log.d("Arduino", code + " Signal sent to Arduino 2");
            getInstance().getArduinoHelper2().writeData(code);
        }

    }
    @Override
    public void onTerminate() {
        super.onTerminate();
        getInstance().getArduinoHelper().closeConnection();  // Close the socket when the app terminates
        getInstance().getArduinoHelper2().closeConnection();
        Log.d("TerminalApplication", "App is terminating. Closing connection.");
    }
}