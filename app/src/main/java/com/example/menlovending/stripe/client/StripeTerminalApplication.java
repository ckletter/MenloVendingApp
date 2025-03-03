package com.example.menlovending.stripe.client;

import static java.lang.reflect.Array.get;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import com.example.menlovending.ContextHolder;
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

import jssc.SerialPortException;

public class StripeTerminalApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MenloVendingManager.getInstance().initialize(this);
        TerminalApplicationDelegate.onCreate(this);
    }

    public static void processPayment() throws StripeException {
        StripeServer server = StripeServer.getInstance();

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
                                            // Make sure we are on the main thread before showing the Toast
                                            if (Looper.myLooper() == Looper.getMainLooper()) {
                                                // If we're already on the main thread, show the Toast
                                                Toast.makeText(ContextHolder.getContext(), "Payment success!", Toast.LENGTH_SHORT).show();
                                            } else {
                                                // Otherwise, post to the main thread to show the Toast
                                                new Handler(Looper.getMainLooper()).post(() -> {
                                                    Toast.makeText(ContextHolder.getContext(), "Payment success!", Toast.LENGTH_SHORT).show();
                                                });
                                            }
                                            MenloVendingManager.getInstance().arduinoSignal();
                                        } catch (StripeException e) {
                                            MenloVendingManager.getInstance().fatalStatus("Failed to capture payment", "Unknown Error");
                                        } catch (SerialPortException e) {
                                            throw new RuntimeException(e);
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
    }
}