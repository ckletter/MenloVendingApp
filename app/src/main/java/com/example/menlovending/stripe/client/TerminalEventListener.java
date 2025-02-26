package com.example.menlovending.stripe.client;

import androidx.annotation.NonNull;

import com.example.menlovending.stripe.manager.MenloVendingManager;
import com.stripe.stripeterminal.external.callable.TerminalListener;
import com.stripe.stripeterminal.external.models.ConnectionStatus;
import com.stripe.stripeterminal.external.models.PaymentStatus;

public class TerminalEventListener implements TerminalListener {
    @Override
    public void onConnectionStatusChange(@NonNull ConnectionStatus connectionStatus) {
        MenloVendingManager.getInstance().onConnectionStatusChange(connectionStatus);
    }

    @Override
    public void onPaymentStatusChange(@NonNull PaymentStatus paymentStatus) {
        MenloVendingManager.getInstance().onPaymentStatusChange(paymentStatus);
    }
}
