package com.example.menlovending.stripe.manager;

public class MenloVendingState {

    public enum MenloVendingStatus {
        INITIALIZING,
        READY,
        WARNING,
        ERROR,
        FATAL
    }

    private final MenloVendingStatus status;
    private final String statusMessage;
    private final String statusDetails;

    public MenloVendingState(MenloVendingStatus status, String statusMessage, String statusDetails) {
        this.status = status;
        this.statusMessage = statusMessage;
        this.statusDetails = statusDetails;
    }

    public MenloVendingStatus getStatus() {
        return status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public String getStatusDetails() {
        return statusDetails;
    }

    @Override
    public String toString() {
        return "MenloVendingState{" +
                "status=" + status +
                ", statusMessage='" + statusMessage + '\'' +
                ", statusDetails='" + statusDetails + '\'' +
                '}';
    }
}