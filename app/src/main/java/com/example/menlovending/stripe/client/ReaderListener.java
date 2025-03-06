package com.example.menlovending.stripe.client;

import static android.app.PendingIntent.getActivity;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.menlovending.stripe.manager.ContextHolder;
import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.external.callable.Cancelable;
import com.stripe.stripeterminal.external.callable.MobileReaderListener;
import com.stripe.stripeterminal.external.models.DisconnectReason;
import com.stripe.stripeterminal.external.models.Reader;
import com.stripe.stripeterminal.external.models.ReaderDisplayMessage;
import com.stripe.stripeterminal.external.models.ReaderInputOptions;
import com.stripe.stripeterminal.external.models.ReaderSoftwareUpdate;
import com.stripe.stripeterminal.external.models.TerminalException;

import java.util.function.Consumer;


public class ReaderListener implements MobileReaderListener {
    private final Runnable onReconnectStarted;
    private final Runnable onReconnectSucceeded;
    private final Runnable onReconnectFailed;
    private final ReaderListenerCallback callback;
    private final java.util.function.Consumer<ReaderUpdate> onReaderUpdate;


    public ReaderListener(
            Runnable onReconnectStarted,
            Runnable onReconnectSucceeded,
            Runnable onReconnectFailed,
            Consumer<ReaderUpdate> onReaderUpdate, ReaderListenerCallback readerListenerCallback) {
        this.onReconnectStarted = onReconnectStarted;
        this.onReconnectSucceeded = onReconnectSucceeded;
        this.onReconnectFailed = onReconnectFailed;
        this.onReaderUpdate = onReaderUpdate;
        this.callback = readerListenerCallback;
    }

    @Override
    public void onReaderReconnectStarted(Reader reader, Cancelable cancelReconnect, DisconnectReason reason) {
        onReconnectStarted.run();
    }

    @Override
    public void onReaderReconnectSucceeded(Reader reader) {
        onReconnectSucceeded.run();
    }

    @Override
    public void onReaderReconnectFailed(Reader reader) {
        onReconnectFailed.run();
    }

    @Override
    public void onStartInstallingUpdate(ReaderSoftwareUpdate update, Cancelable cancelable) {
        onReaderUpdate.accept(new ReaderUpdate(true, 0f));
    }

    @Override
    public void onReportReaderSoftwareUpdateProgress(float progress) {
        onReaderUpdate.accept(new ReaderUpdate(true, progress));
    }

    @Override
    public void onFinishInstallingUpdate(ReaderSoftwareUpdate update, TerminalException e) {
        onReaderUpdate.accept(new ReaderUpdate(false, 100f));
    }

    @Override
    public void onReportAvailableUpdate(ReaderSoftwareUpdate update) {
        Terminal.getInstance().installAvailableUpdate();
    }
    @Override
    public void onRequestReaderInput(ReaderInputOptions options) {
        // Placeholder for updating your app's checkout UI
        Log.d("ReaderListener", "onRequestReaderInput: " + options.toString());
        // Use callback to display message
        callback.showToastMessage(options.toString());
    }

    @Override
    public void onRequestReaderDisplayMessage(ReaderDisplayMessage message) {
        Log.d("ReaderListener", "onRequestReaderDisplayMessage: " + message.toString());
        // Use callback to display message
        callback.showToastMessage(message.toString());
    }
}
