package com.example.menlovending.stripe.server;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.terminal.ConnectionToken;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.terminal.ConnectionTokenCreateParams;

public class StripeServer {

    // Stripe API key (DO NOT COMMIT THIS, REPLACE WITH ENVIRONMENT VARIABLE)
    private static final String STRIPE_API_KEY = "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";

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
        return intent.getId();
    }

    public String capturePaymentIntent(String paymentIntentId) throws StripeException {
        PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
        PaymentIntent capturedIntent = intent.capture();
        return capturedIntent.getId();
    }

    // Constructor
    private StripeServer() {
        Stripe.apiKey = STRIPE_API_KEY;
    }
}
