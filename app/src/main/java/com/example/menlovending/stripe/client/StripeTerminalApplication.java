package com.example.menlovending.stripe.client;

import static java.lang.reflect.Array.get;

import android.app.Application;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import com.example.menlovending.stripe.permissions.PermissionService;
import com.stripe.model.PaymentIntent;

import com.google.gson.Gson;
import static spark.Spark.post;
import com.example.menlovending.stripe.manager.MenloVendingManager;
import com.example.menlovending.stripe.server.StripeServer;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.reporting.ReportRunCreateParams;
import com.stripe.stripeterminal.Terminal;
import com.stripe.model.PaymentIntent;
import com.stripe.stripeterminal.TerminalApplicationDelegate;
import com.stripe.stripeterminal.external.callable.Cancelable;
import com.stripe.stripeterminal.external.callable.PaymentIntentCallback;
import com.stripe.stripeterminal.external.models.CaptureMethod;
import com.stripe.stripeterminal.external.models.CollectConfiguration;
import com.stripe.stripeterminal.external.models.PaymentIntentParameters;
import com.stripe.stripeterminal.external.models.RefundParameters;
import com.stripe.stripeterminal.external.models.TerminalException;

import org.jetbrains.annotations.NotNull;

public class StripeTerminalApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MenloVendingManager.getInstance().initialize(this);
        TerminalApplicationDelegate.onCreate(this);
    }

    public static void processPayment() throws StripeException {
        StripeServer server = StripeServer.getInstance();

//        PaymentIntentCreateParams params = new PaymentIntentCreateParams.Builder()
//                .setAmount(50L)
//                .addPaymentMethodType("card_present")
//                .setCurrency("usd")
//                .setCaptureMethod(PaymentIntentCreateParams.CaptureMethod.MANUAL)
//                .build();
//        Gson gson = new Gson();
//
//        get("/create_payment_intent", (request, response) -> {
//            PaymentIntent intent = PaymentIntent.create(params);
//
//            Map<String, String> map = new HashMap();
//            map.put("client_secret", intent.getClientSecret());
//            return map;
//        }, gson::toJson);

        PaymentIntentParameters params = new PaymentIntentParameters.Builder()
                .setAmount(50L)
                .setCurrency("usd")
                .setCaptureMethod(CaptureMethod.Manual)
                .build();

        Terminal.getInstance().createPaymentIntent(
                params,
                new PaymentIntentCallback() {

                    @Override
                    public void onSuccess(@NonNull com.stripe.stripeterminal.external.models.PaymentIntent paymentIntent) {
                        Terminal.getInstance().collectPaymentMethod(paymentIntent, new PaymentIntentCallback() {
                            @Override
                            public void onSuccess(@NonNull com.stripe.stripeterminal.external.models.PaymentIntent paymentIntent) {
                                Terminal.getInstance().confirmPaymentIntent(paymentIntent, new PaymentIntentCallback() {
                                    @Override
                                    public void onSuccess(@NonNull com.stripe.stripeterminal.external.models.PaymentIntent paymentIntent) {
                                        String id = paymentIntent.getId();
                                        try {
                                            server.capturePaymentIntent(id);
                                        } catch (StripeException e) {
                                            MenloVendingManager.getInstance().fatalStatus("Failed to capture payment", "Unknown Error");
                                        }
                                    }

                                    @Override
                                    public void onFailure(@NonNull TerminalException e) {
                                        MenloVendingManager.getInstance().fatalStatus("Failed to confirm payment", "Unknown Error");
                                    }
                                });
                            }

                            @Override
                            public void onFailure(@NonNull TerminalException e) {
                                MenloVendingManager.getInstance().fatalStatus("Failed to collect payment", "Unknown Error");
                            }
                        });
                    }

                    @Override
                    public void onFailure(@NonNull TerminalException e) {
                        MenloVendingManager.getInstance().fatalStatus("Failed to create payment intent", "Unknown Error");
                    }
                }
        );

//        Terminal.getInstance().createPaymentIntent(
//                params,
//                new PaymentIntentCallback() {
//
//                    @Override
//                    public void onSuccess(@NonNull com.stripe.stripeterminal.external.models.PaymentIntent paymentIntent) {
//                        Terminal.getInstance().collectPaymentMethod(paymentIntent, new PaymentIntentCallback() {
//                            @Override
//                            public void onSuccess(@NonNull com.stripe.stripeterminal.external.models.PaymentIntent paymentIntent) {
//                                Terminal.getInstance().confirmPaymentIntent(paymentIntent, new PaymentIntentCallback() {
//                                    @Override
//                                    public void onSuccess(@NonNull com.stripe.stripeterminal.external.models.PaymentIntent paymentIntent) {
//                                        String id = paymentIntent.getId();
//                                        try {
//                                            server.capturePaymentIntent(id);
//                                        } catch (StripeException e) {
//                                            MenloVendingManager.getInstance().fatalStatus("Failed to capture payment", "Unknown Error");
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onFailure(@NonNull TerminalException e) {
//                                        MenloVendingManager.getInstance().fatalStatus("Failed to confirm payment", "Unknown Error");
//                                    }
//                                });
//                            }
//
//                            @Override
//                            public void onFailure(@NonNull TerminalException e) {
//                                MenloVendingManager.getInstance().fatalStatus("Failed to collect payment", "Unknown Error");
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onFailure(@NonNull TerminalException e) {
//                        MenloVendingManager.getInstance().fatalStatus("Failed to create payment intent", "Unknown Error");
//                    }
//                }
//        );
    }
}