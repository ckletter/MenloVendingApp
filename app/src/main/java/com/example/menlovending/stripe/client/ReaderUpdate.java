package com.example.menlovending.stripe.client;

import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.external.callable.Cancelable;
import com.stripe.stripeterminal.external.callable.MobileReaderListener;
import com.stripe.stripeterminal.external.models.DisconnectReason;
import com.stripe.stripeterminal.external.models.Reader;
import com.stripe.stripeterminal.external.models.ReaderSoftwareUpdate;
import com.stripe.stripeterminal.external.models.TerminalException;

private class ReaderUpdate {
    private final boolean isUpdating;
    private final float progress;

    public ReaderUpdate(boolean isUpdating, float progress) {
        this.isUpdating = isUpdating;
        this.progress = progress;
    }

    public boolean isUpdating() {
        return isUpdating;
    }

    public float getProgress() {
        return progress;
    }
}