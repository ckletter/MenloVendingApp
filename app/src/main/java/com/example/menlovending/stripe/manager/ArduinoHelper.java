package com.example.menlovending.stripe.manager;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ArduinoHelper {
    private static final String TAG = "ArduinoHelper";
    private static final int ARDUINO_PORT = 80;  // Change if your Arduino uses a different port
    private static final String ARDUINO_IP = "192.168.1.100";  // Replace with your Arduino's IP
    private boolean connectionEstablished = false;
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public boolean isConnectionEstablished() {
        return connectionEstablished;
    }

    // Establish Wi-Fi connection to Arduino using Executors
    public void findAndConnectDevice() {
        executorService.execute(() -> {
            try {
                socket = new Socket(InetAddress.getByName(ARDUINO_IP), ARDUINO_PORT);
                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();
                connectionEstablished = true;
                Log.d(TAG, "Connected to Arduino via Wi-Fi");
            } catch (IOException e) {
                Log.e(TAG, "Error connecting to Arduino: " + e.getMessage());
            }
        });
    }

    // Send data to Arduino over Wi-Fi
    public void writeData(int code) {
        if (!connectionEstablished || socket == null) {
            Log.e(TAG, "Not connected to device");
            return;
        }

        executorService.execute(() -> {
            try {
//                String message = code + "\n";
                String message = "DISPENSE\n";  // Modify as needed
                outputStream.write(message.getBytes());
                Log.d(TAG, "Data sent to Arduino: " + message);
            } catch (IOException e) {
                Log.e(TAG, "Error writing data to Arduino: " + e.getMessage());
            }
        });
    }

    // Close connection to Arduino
    public void closeConnection() {
        executorService.execute(() -> {
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                    connectionEstablished = false;
                    Log.d(TAG, "Connection closed.");
                } catch (IOException e) {
                    Log.e(TAG, "Error closing connection: " + e.getMessage());
                }
            }
        });
    }
}



//package com.example.menlovending.stripe.manager;
//
//import android.app.PendingIntent;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.hardware.usb.UsbDevice;
//import android.hardware.usb.UsbDeviceConnection;
//import android.hardware.usb.UsbManager;
//import android.util.Log;
//
//import androidx.core.content.ContextCompat;
//
//import com.example.menlovending.stripe.permissions.UsbPermissionReceiver;
//import com.hoho.android.usbserial.driver.UsbSerialDriver;
//import com.hoho.android.usbserial.driver.UsbSerialPort;
//import com.hoho.android.usbserial.driver.UsbSerialProber;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.List;
//
//public class ArduinoHelper {
//    private static final String TAG = "ArduinoHelper";
//    private static final int ARDUINO_PRODUCT_ID = 4098;
//    private static final int ARDUINO_VENDOR_ID = 9025;
//    private static final int BAUD_RATE = 115200;
//    private String deviceName;
//    private final Context context;
//    private final UsbManager usbManager;
//    private UsbSerialPort serialPort;
//    private UsbDevice device;
//    private boolean connectionEstablished = false;
//
//    public ArduinoHelper(Context context, String deviceName) {
//        this.context = context;
//        this.deviceName = deviceName;
//        this.usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
//        Log.d(TAG, "Looking for managers...");
//        // Find all available drivers from attached devices
//        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);
//        for (UsbSerialDriver driver : availableDrivers) {
//            UsbDevice device = driver.getDevice();
//            Log.d(TAG, "Found device: " + device.getDeviceName() +
//                    " VendorID: " + device.getVendorId() +
//                    " ProductID: " + device.getProductId() + " Device name: " + device.getDeviceName());
//        }
//        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
//        for (UsbDevice device : deviceList.values()) {
//            Log.d(TAG, "Detected USB Device: " + device.getDeviceName() +
//                    " Vendor ID: " + device.getVendorId() +
//                    " Product ID: " + device.getProductId());
//        }
//
//    }
//
//    public boolean isConnectionEstablished() {
//        return connectionEstablished;
//    }
//
//    public void findAndConnectDevice() {
//        // Find all available drivers from attached devices
//        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);
//
//        // Reset device to null at the start
//        this.device = null;
//
//        // Log and check for our specific device
//        for (UsbSerialDriver driver : availableDrivers) {
//            UsbDevice device = driver.getDevice();
//
//            if (device.getVendorId() == ARDUINO_VENDOR_ID &&
//                    device.getProductId() == ARDUINO_PRODUCT_ID && deviceName.equals(device.getDeviceName())) {
//                this.device = device;
//                break;  // Exit the loop once we find our device
//            }
//        }
//
//        // Check if we found the device
//        if (this.device == null) {
//            Log.e(TAG, "Arduino device not found");
//            return;
//        }
//        // Request USB permission
////        PendingIntent permissionIntent = PendingIntent.getBroadcast(
////                context, 0,
////                new Intent(UsbPermissionReceiver.ACTION_USB_PERMISSION),
////                PendingIntent.FLAG_IMMUTABLE  // Use FLAG_MUTABLE for Android 12+
////        );
////        usbManager.requestPermission(this.device, permissionIntent);
//    }
//
//    private boolean connectToDevice(UsbDevice usbDevice) {
//        try {
//            UsbDeviceConnection connection = usbManager.openDevice(usbDevice);
//            if (connection == null) {
//                Log.e(TAG, "Failed to open connection");
//                return false;
//            }
//
//            // Find all available drivers
//            List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);
//            UsbSerialDriver driver = null;
//
//            // Find the driver for our device
//            for (UsbSerialDriver availableDriver : availableDrivers) {
//                if (availableDriver.getDevice().equals(usbDevice)) {
//                    driver = availableDriver;
//                    break;
//                }
//            }
//
//            if (driver == null) {
//                Log.e(TAG, "Driver not found for device");
//                return false;
//            }
//
//            // Most devices have just one port (port 0)
//            serialPort = driver.getPorts().get(0);
//            serialPort.open(connection);
//            serialPort.setParameters(BAUD_RATE, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
//
//            connectionEstablished = true;
//            Log.d(TAG, "Connected to Arduino via USB");
//            return true;
//
//        } catch (IOException e) {
//            Log.e(TAG, "Error connecting to device: " + e.getMessage());
//            return false;
//        }
//    }
//
//
//    public void writeData(int code) {
//        if (!connectionEstablished || serialPort == null) {
//            Log.e(TAG, "Not connected to device");
//            return;
//        }
//        try {
//            Log.d(TAG, "Writing data to Arduino");
////            serialPort.write((code + "\n").getBytes(), 1000);
//            serialPort.write("DISPENSE\n".getBytes(), 1000);
//        } catch (IOException e) {
//            Log.e(TAG, "Error writing data: " + e.getMessage());
//        }
//    }
//
//    public void closeConnection() {
//        if (serialPort != null) {
//            try {
//                serialPort.close();
//                connectionEstablished = false;
//            } catch (IOException e) {
//                Log.e(TAG, "Error closing connection: " + e.getMessage());
//            }
//        }
//    }
//}