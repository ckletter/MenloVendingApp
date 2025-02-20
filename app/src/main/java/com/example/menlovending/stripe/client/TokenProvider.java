package com.example.menlovending.stripe.client;
import android.util.Log;

import com.example.menlovending.stripe.server.StripeServer;


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
            Log.d("TokenProvider", "Connection token fetched: " + token);
            callback.onSuccess(token);
        } catch (StripeException e) {
            e.printStackTrace(); // Log the full stack trace
            callback.onFailure(new ConnectionTokenException(
                    e.getMessage() == null ? "Creating connection token failed" : e.getMessage(),
                    e.getCause()
            ));
        } catch (Exception e) {
            e.printStackTrace(); // Catch unexpected errors
            callback.onFailure(new ConnectionTokenException("Unexpected error: " + e.getMessage(), e));
        }
    }

}
