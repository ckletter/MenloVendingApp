package com.example.menlovending.stripe.client;

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
import com.stripe.stripeterminal.external.models.PaymentIntentParameters;
import com.stripe.stripeterminal.external.models.Reader;
import com.stripe.stripeterminal.external.models.TerminalException;

import jssc.SerialPortException;

public class StripeTerminalApplication extends Application {
    private static com.stripe.stripeterminal.external.models.PaymentIntent currentPaymentIntent;
    private static Cancelable collectPaymentMethodCancelable;

    @Override
    public void onCreate() {
        super.onCreate();
        MenloVendingManager.getInstance().initialize(this);
        ContextHolder.setContext(getApplicationContext());
        TerminalApplicationDelegate.onCreate(this);
    }

    public static void processPayment(double amount) throws StripeException {
        // Convert from dollars to proper currency
        long amountInCents = (long) (amount * 100);

        PaymentIntentParameters params = new PaymentIntentParameters.Builder()
                .setAmount(amountInCents)
                .setCurrency("usd")
                .setCaptureMethod(CaptureMethod.Manual)
                .build();

        // Create Payment Intent
        createPaymentIntent(params);
    }

    private static void createPaymentIntent(PaymentIntentParameters params) {
        Terminal.getInstance().createPaymentIntent(params, new PaymentIntentCallback() {
            @Override
            public void onSuccess(@NonNull com.stripe.stripeterminal.external.models.PaymentIntent paymentIntent) {
                currentPaymentIntent = paymentIntent;
                collectPaymentMethod(paymentIntent);
            }

            @Override
            public void onFailure(@NonNull TerminalException e) {
                MenloVendingManager.getInstance().fatalStatus("Failed to create payment intent", "Unknown Error");
            }
        });
    }

    private static void collectPaymentMethod(com.stripe.stripeterminal.external.models.PaymentIntent paymentIntent) {
        collectPaymentMethodCancelable = Terminal.getInstance().collectPaymentMethod(
                paymentIntent,
                new PaymentIntentCallback() {
                    @Override
                    public void onSuccess(@NonNull com.stripe.stripeterminal.external.models.PaymentIntent updatedPaymentIntent) {
                        confirmPaymentIntent(updatedPaymentIntent);
                    }

                    @Override
                    public void onFailure(@NonNull TerminalException e) {
                        MenloVendingManager.getInstance().fatalStatus("Failed to collect payment", "Unknown Error");
                    }
                }
        );
    }

    private static void confirmPaymentIntent(com.stripe.stripeterminal.external.models.PaymentIntent paymentIntent) {
        Terminal.getInstance().confirmPaymentIntent(
                paymentIntent,
                new PaymentIntentCallback() {
                    @Override
                    public void onSuccess(@NonNull com.stripe.stripeterminal.external.models.PaymentIntent confirmedPaymentIntent) {
                        String id = confirmedPaymentIntent.getId();
                        double amount = (double) confirmedPaymentIntent.getAmount() / 100;
                        try {
                            signalToArduino();
                        } catch (SerialPortException e) {
                            Log.e("Arduino", "Failed to send signal to Arduino", e);
                            throw new RuntimeException(e);
                        }
                        navigateToPaymentSuccess(amount);

                    }

                    @Override
                    public void onFailure(@NonNull TerminalException e) {
                        MenloVendingManager.getInstance().fatalStatus("Failed to confirm payment", "Unknown Error");
                    }
                }
        );
    }

    public static void cancelPaymentIntent() {
        if (currentPaymentIntent != null) {
            // Cancel collecting payment method if in progress
            if (collectPaymentMethodCancelable != null) {
                collectPaymentMethodCancelable.cancel(new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d("PaymentIntent", "Payment method collection canceled");
                        // Reconnect to the reader if it was disconnected
                        if (Terminal.getInstance().getConnectedReader() == null) {
                            Log.d("PaymentIntent", "Reader is not connected. Reconnecting...");
                            MenloVendingManager.getInstance().reconnectReader();
                        } else {
                            Log.d("PaymentIntent", "Reader is still connected.");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull TerminalException e) {
                        Log.e("PaymentIntent", "Failed to cancel payment method collection", e);
                        // Reconnect to the reader to avoid session loss
                        if (Terminal.getInstance().getConnectedReader() == null) {
                            MenloVendingManager.getInstance().reconnectReader();
                        }
                    }
                });
            }
            // Only cancel payment intent if the reader session is still active
            if (Terminal.getInstance().getConnectedReader() != null) {
                // Cancel the payment intent
                Terminal.getInstance().cancelPaymentIntent(
                        currentPaymentIntent,
                        new PaymentIntentCallback() {
                            @Override
                            public void onSuccess(@NonNull com.stripe.stripeterminal.external.models.PaymentIntent paymentIntent) {
                                Log.d("PaymentIntent", "Payment intent successfully canceled");
                                currentPaymentIntent = null;
                                collectPaymentMethodCancelable = null;
                            }

                            @Override
                            public void onFailure(@NonNull TerminalException e) {
                                Log.e("PaymentIntent", "Failed to cancel payment intent", e);
                            }
                        }
                );
            } else {
                Log.w("PaymentIntent", "Reader is already disconnected, skipping cancelPaymentIntent.");
            }
        }
    }

    private static void navigateToPaymentSuccess(double amount) {
        Intent intent = new Intent(ContextHolder.getContext(), PaymentSuccessActivity.class);
        intent.putExtra("PAYMENT_AMOUNT", amount);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        ContextHolder.getContext().startActivity(intent);
    }
    private static void signalToArduino() throws SerialPortException {
        Log.d("Arduino", "Signal sent to Arduino");
        ArduinoHelper ah = new ArduinoHelper();
        ah.writeData();
    }
}