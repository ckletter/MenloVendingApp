package com.example.menlovending.stripe.client;

import com.example.menlovending.stripe.server.StripeServer;
import com.stripe.exception.StripeException;
import com.stripe.stripeterminal.external.callable.ConnectionTokenCallback;
import com.stripe.stripeterminal.external.callable.ConnectionTokenProvider;
import com.stripe.stripeterminal.external.models.ConnectionTokenException;

public class TokenProvider implements ConnectionTokenProvider {

    @Override
    public void fetchConnectionToken(ConnectionTokenCallback callback) {
        try {
            String token = StripeServer.getInstance().getConnectionToken();
            callback.onSuccess(token);
        } catch (StripeException e) {
            callback.onFailure(new ConnectionTokenException(e.getMessage() == null ? "Creating connection token failed" : e.getMessage(), e.getCause()));
        }
    }
}
