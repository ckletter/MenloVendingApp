package com.example.menlovending.stripe.server;
import com.example.menlovending.BuildConfig;

import android.util.Log;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.terminal.ConnectionToken;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.terminal.ConnectionTokenCreateParams;

public class StripeServer {

    // Stripe API key
    private static final String STRIPE_API_KEY = BuildConfig.STRIPE_API_KEY;

    // Instance
    private static final StripeServer INSTANCE = new StripeServer();
    public static StripeServer getInstance() {
        return INSTANCE;
    }

    // Endpoints
    public String getConnectionToken() throws StripeException {
        ConnectionTokenCreateParams params = ConnectionTokenCreateParams.builder()
                .build();

        ConnectionToken connectionToken = ConnectionToken.create(params);
        return connectionToken.getSecret();
    }

    public String createPaymentIntent(Long amount) throws StripeException {
        PaymentIntentCreateParams createParams = PaymentIntentCreateParams.builder()
                .setCurrency("usd")
                .setAmount(amount)
                .setCaptureMethod(PaymentIntentCreateParams.CaptureMethod.MANUAL)
                .build();

        PaymentIntent intent = PaymentIntent.create(createParams);
        return intent.getClientSecret();
    }

    public String capturePaymentIntent(String paymentIntentId) throws StripeException {
        PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
        PaymentIntent capturedIntent = intent.capture();
        return capturedIntent.getId();
    }

    // Constructor
    private StripeServer() {
        if (STRIPE_API_KEY == null || STRIPE_API_KEY.isEmpty()) {
            Log.e("StripeServer", "api key not set");
            throw new IllegalStateException("Stripe API key is not set. Please set STRIPE_API_KEY environment variable.");
        }
        Stripe.apiKey = STRIPE_API_KEY;
        Log.d("StripeServer", "Using API Key: " + STRIPE_API_KEY);

    }
}
