package com.example.menlovending.stripe.client;

import android.app.Application;

import com.example.menlovending.stripe.server.StripeServer;
import com.stripe.exception.StripeException;
import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.TerminalApplicationDelegate;
import com.stripe.stripeterminal.external.callable.Cancelable;
import com.stripe.stripeterminal.external.callable.PaymentIntentCallback;
import com.stripe.stripeterminal.external.models.PaymentIntent;
import com.stripe.stripeterminal.external.models.TerminalException;

import org.jetbrains.annotations.NotNull;

public class StripeTerminalApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        TerminalApplicationDelegate.onCreate(this);
    }
    public void connectToStripe() throws StripeException {
        StripeServer server = StripeServer.getInstance();
//        TokenProvider tokenProvider = new TokenProvider();
//        tokenProvider.fetchConnectionToken();
        String id = server.createPaymentIntent(1L);
        Terminal.getInstance().retrievePaymentIntent(server.getConnectionToken(), new PaymentIntentCallback() {
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
                                    throw new RuntimeException(e);
                                }
                            }

                            @Override
                            public void onFailure(@NotNull TerminalException exception) {
                                // Placeholder for handling exception
                            }
                        });
                    }

                    @Override
                    public void onFailure(@NotNull TerminalException exception) {
                        // Placeholder for handling exception
                    }
                });
            }

            @Override
            public void onFailure(@NotNull TerminalException exception) {
                // Placeholder for handling exception
            }
        });

    }
}