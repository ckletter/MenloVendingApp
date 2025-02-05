package com.example.menlovending.stripe.client;

import android.app.Application;
import com.stripe.stripeterminal.TerminalApplicationDelegate;

public class StripeTerminalApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        TerminalApplicationDelegate.onCreate(this);
    }
}