package com.example.menlovending;

import android.app.Application;

import com.stripe.stripeterminal.TerminalApplicationDelegate;

class StripeTerminalApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        TerminalApplicationDelegate.onCreate(this);
    }
}