package com.example.menlovending.stripe.client;

import android.app.Application;

import androidx.annotation.NonNull;

import com.example.menlovending.stripe.manager.MenloVendingManager;
import com.example.menlovending.stripe.server.StripeServer;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.TerminalApplicationDelegate;
import com.stripe.stripeterminal.external.callable.Cancelable;
import com.stripe.stripeterminal.external.callable.PaymentIntentCallback;
import com.stripe.stripeterminal.external.models.PaymentIntent;
import com.stripe.stripeterminal.external.models.PaymentIntentParameters;
import com.stripe.stripeterminal.external.models.TerminalException;

import org.jetbrains.annotations.NotNull;

public class StripeTerminalApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MenloVendingManager.getInstance().initialize(this);
        TerminalApplicationDelegate.onCreate(this);
    }
    public static void processPayment() throws StripeException {
        StripeServer server = StripeServer.getInstance();

        PaymentIntentParameters params = new PaymentIntentParameters.Builder()
                .setAmount(50L)
                .setCurrency("usd")
                .build();
        Terminal.getInstance().createPaymentIntent(
                params,
                new PaymentIntentCallback() {

                    @Override
                    public void onSuccess(@NotNull PaymentIntent paymentIntent) {
                        Terminal.getInstance().collectPaymentMethod(paymentIntent, new PaymentIntentCallback() {
                            @Override
                            public void onSuccess(@NotNull PaymentIntent paymentIntent) {
                                Terminal.getInstance().confirmPaymentIntent(paymentIntent, new PaymentIntentCallback() {
                                    @Override
                                    public void onSuccess(@NotNull PaymentIntent paymentIntent) {
                                        String id = paymentIntent.getId();
                                        try {
                                            server.capturePaymentIntent(id);
                                        } catch (StripeException e) {
                                            MenloVendingManager.getInstance().fatalStatus("Failed to capture payment", "Unknown Error");
                                        }
                                    }

                                    @Override
                                    public void onFailure(@NotNull TerminalException exception) {
                                        MenloVendingManager.getInstance().fatalStatus("Failed to confirm payment", "Unknown Error");
                                    }
                                });
                            }

                            @Override
                            public void onFailure(@NotNull TerminalException exception) {
                                MenloVendingManager.getInstance().fatalStatus("Failed to collect payment", "Unknown Error");
                            }
                        });
                    }
                    @Override
                    public void onFailure(@NonNull TerminalException e) {
                        MenloVendingManager.getInstance().fatalStatus("Failed to create payment intent", "Unknown Error");
                    }
                }
        );
    }
}