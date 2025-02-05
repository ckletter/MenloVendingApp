package com.example.menlovending.stripe.client;

import android.app.Application;

import com.example.menlovending.stripe.server.StripeServer;
import com.stripe.exception.StripeException;
import com.stripe.stripeterminal.TerminalApplicationDelegate;
import com.stripe.stripeterminal.external.callable.Cancelable;

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
        server.capturePaymentIntent(id);

    }
}