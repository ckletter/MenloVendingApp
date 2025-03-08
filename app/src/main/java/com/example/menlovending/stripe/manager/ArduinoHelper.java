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
    private static final String ACTION_USB_PERMISSION = "com.example.menlovending.USB_PERMISSION";
    private final BroadcastReceiver usbPermissionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            // Permission granted, connect to device
                            connectToDevice(device);
                            connectionEstablished = true;
                        }
                    } else {
                        Log.d(TAG, "Permission denied for device " + device);
                    }
                }
            }
        }
    };

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

//package com.example.menlovending.stripe.manager;
//
//import static jssc.SerialPort.BAUDRATE_115200;
//import static jssc.SerialPort.DATABITS_8;
//import static jssc.SerialPort.PARITY_NONE;
//import static jssc.SerialPort.STOPBITS_1;
//
//import jssc.SerialPort;
//import jssc.SerialPortException;
//import jssc.SerialPortList;
//
//public class ArduinoHelper {
//    SerialPort port;
//    public ArduinoHelper() throws SerialPortException {
//        String[] portNames = SerialPortList.getPortNames();
//        for (String portName : portNames) {
//            System.out.println("Available port: " + portName);
//        }
//        port = new SerialPort("/dev/tty.usbmodem4827E2E6471C2");
//        port.openPort();
//        port.setParams(BAUDRATE_115200,  DATABITS_8, STOPBITS_1, PARITY_NONE);
//    }
//    public void writeData() throws SerialPortException {
//        port.writeBytes("DISPENSE\n".getBytes());
//        port.closePort();
//    }
//}
//
//
//
//
////package com.example.menlovending;
////
////import android.bluetooth.BluetoothAdapter;
////import android.bluetooth.BluetoothDevice;
////import android.bluetooth.BluetoothSocket;
////import android.util.Log;
////
////import java.io.IOException;
////import java.io.InputStream;
////import java.io.OutputStream;
////import java.util.Set;
////import java.util.UUID;
////
////public class ArduinoHelper {
////    private static final String TAG = "ArduinoHelper";
////    private static final String DEVICE_NAME = "MenloVending"; // Change to your Arduino's Bluetooth name
////    private static final UUID UUID_SERIAL = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
////
////    private BluetoothAdapter bluetoothAdapter;
////    private BluetoothSocket bluetoothSocket;
////    private OutputStream outputStream;
////    private InputStream inputStream;
////
////    public ArduinoHelper() {
////        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
////    }
////
////    public boolean connectToArduino() {
////        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
////            Log.e(TAG, "Bluetooth is not available or not enabled.");
////            return false;
////        }
////
////        BluetoothDevice device = findArduinoDevice();
////        if (device == null) {
////            Log.e(TAG, "Arduino Bluetooth device not found.");
////            return false;
////        }
////
////        try {
////            bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID_SERIAL);
////            bluetoothSocket.connect();
////            outputStream = bluetoothSocket.getOutputStream();
////            inputStream = bluetoothSocket.getInputStream();
////            Log.d(TAG, "Connected to Arduino!");
////            return true;
////        } catch (IOException e) {
////            Log.e(TAG, "Connection failed: " + e.getMessage());
////            return false;
////        }
////    }
////
////    private BluetoothDevice findArduinoDevice() {
////        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
////        for (BluetoothDevice device : pairedDevices) {
////            if (device.getName().equals(DEVICE_NAME)) {
////                return device;
////            }
////        }
////        return null;
////    }
////
////    public void sendData(String data) {
////        try {
////            if (outputStream != null) {
////                outputStream.write(data.getBytes());
////                Log.d(TAG, "Sent: " + data);
////            }
////        } catch (IOException e) {
////            Log.e(TAG, "Error sending data: " + e.getMessage());
////        }
////    }
////
////    public String receiveData() {
////        try {
////            if (inputStream != null) {
////                byte[] buffer = new byte[1024];
////                int bytes = inputStream.read(buffer);
////                String received = new String(buffer, 0, bytes);
////                Log.d(TAG, "Received: " + received);
////                return received;
////            }
////        } catch (IOException e) {
////            Log.e(TAG, "Error receiving data: " + e.getMessage());
////        }
////        return null;
////    }
////
////    public void closeConnection() {
////        try {
////            if (bluetoothSocket != null) bluetoothSocket.close();
////            Log.d(TAG, "Bluetooth connection closed.");
////        } catch (IOException e) {
////            Log.e(TAG, "Error closing connection: " + e.getMessage());
////        }
////    }
////}
