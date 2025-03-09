package com.example.menlovending.stripe.manager;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.List;

public class ArduinoHelper {
    private static final String TAG = "ArduinoHelper";
    // In your ArduinoHelper class
    private static final int ARDUINO_VENDOR_ID = 9025; // Replace with your actual VID
    private static final int ARDUINO_PRODUCT_ID = 4098; // Replace with your actual PID
    private static final int BAUD_RATE = 115200;
//    private static final String ACTION_USB_PERMISSION = "com.example.menlovending.USB_PERMISSION";
//    private final BroadcastReceiver usbPermissionReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (ACTION_USB_PERMISSION.equals(action)) {
//                synchronized (this) {
//                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
//                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
//                        if (device != null) {
//                            // Permission granted, connect to device
//                            connectToDevice(device);
//                            connectionEstablished = true;
//                        }
//                    } else {
//                        Log.d(TAG, "Permission denied for device " + device);
//                    }
//                }
//            }
//        }
//    };

    private final Context context;
    private final UsbManager usbManager;
    private UsbSerialPort serialPort;
    private UsbDevice device;
    private boolean connectionEstablished = false;

    public ArduinoHelper(Context context) {
        this.context = context;
        this.usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        Log.d(TAG, "Looking for managers...");
        // Find all available drivers from attached devices
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);
        for (UsbSerialDriver driver : availableDrivers) {
            UsbDevice device = driver.getDevice();
            Log.d(TAG, "Found device: " + device.getDeviceName() +
                    " VendorID: " + device.getVendorId() +
                    " ProductID: " + device.getProductId());
        }
    }

    public boolean isConnectionEstablished() {
        return connectionEstablished;
    }

    public boolean findAndConnectDevice() {
        // Find all available drivers from attached devices
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);

        // Reset device to null at the start
        this.device = null;

        // Log and check for our specific device
        for (UsbSerialDriver driver : availableDrivers) {
            UsbDevice device = driver.getDevice();

            if (device.getVendorId() == ARDUINO_VENDOR_ID &&
                    device.getProductId() == ARDUINO_PRODUCT_ID) {
                this.device = device;
                break;  // Exit the loop once we find our device
            }
        }

        // Check if we found the device
        if (this.device == null) {
            Log.e(TAG, "Arduino device not found");
            return false;
        }

        // In your findAndConnectDevice method, replace the "No permission" section:
        if (usbManager.hasPermission(device)) {
            return connectToDevice(device);
        } else {
            Log.d(TAG, "Permission already granted in XML");
            return false;
        }
    }

    private boolean connectToDevice(UsbDevice usbDevice) {
        try {
            UsbDeviceConnection connection = usbManager.openDevice(usbDevice);
            if (connection == null) {
                Log.e(TAG, "Failed to open connection");
                return false;
            }

            // Find all available drivers
            List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);
            UsbSerialDriver driver = null;

            // Find the driver for our device
            for (UsbSerialDriver availableDriver : availableDrivers) {
                if (availableDriver.getDevice().equals(usbDevice)) {
                    driver = availableDriver;
                    break;
                }
            }

            if (driver == null) {
                Log.e(TAG, "Driver not found for device");
                return false;
            }

            // Most devices have just one port (port 0)
            serialPort = driver.getPorts().get(0);
            serialPort.open(connection);
            serialPort.setParameters(BAUD_RATE, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

            connectionEstablished = true;
            Log.d(TAG, "Connected to Arduino via USB");
            return true;

        } catch (IOException e) {
            Log.e(TAG, "Error connecting to device: " + e.getMessage());
            return false;
        }
    }


    public void writeData() {
        if (!connectionEstablished || serialPort == null) {
            Log.e(TAG, "Not connected to device");
            return;
        }

        try {
            Log.d(TAG, "Writing data to Arduino");
            serialPort.write("DISPENSE\n".getBytes(), 1000);
        } catch (IOException e) {
            Log.e(TAG, "Error writing data: " + e.getMessage());
        }
    }

    public void closeConnection() {
        if (serialPort != null) {
            try {
                serialPort.close();
                connectionEstablished = false;
            } catch (IOException e) {
                Log.e(TAG, "Error closing connection: " + e.getMessage());
            }
        }
    }
}