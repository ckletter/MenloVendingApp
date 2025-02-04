package com.example.menlovending;

import android.app.Application;

import com.stripe.stripeterminal.TerminalApplicationDelegate;
import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.external.callable.TerminalListener;
import com.stripe.stripeterminal.external.models.ConnectionStatus;
import com.stripe.stripeterminal.external.models.PaymentStatus;
import com.stripe.stripeterminal.external.models.TerminalException;
import com.stripe.stripeterminal.log.LogLevel;

class StripeTerminalApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        TerminalApplicationDelegate.onCreate(this);
    }
    public void connectToStripe() {
            TerminalListener listener = new TerminalListener() {
            @Override
            public void onConnectionStatusChange(ConnectionStatus status) {
                System.out.printf("onConnectionStatusChange: %s\n", status);
            }

            @Override
            public void onPaymentStatusChange(PaymentStatus status) {
                System.out.printf("onPaymentStatusChange: %s\n ", status);
            }
        };

            // Choose the level of messages that should be logged to your console
            LogLevel logLevel = LogLevel.VERBOSE;
            // Create your token provider.
            CustomConnectionTokenProvider tokenProvider = new CustomConnectionTokenProvider();


            // Pass in the current application context, your desired logging level, your token provider, and the listener you created
            if(!Terminal.isInitialized())

            {
                try {
                    Terminal.initTerminal(getApplicationContext(), logLevel, tokenProvider, listener);
                } catch (TerminalException e) {
                    throw new RuntimeException(e);
                }
            }
    }
}