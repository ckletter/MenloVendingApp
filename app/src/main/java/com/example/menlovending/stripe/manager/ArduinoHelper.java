package com.example.menlovending.stripe.manager;

import static jssc.SerialPort.BAUDRATE_115200;
import static jssc.SerialPort.DATABITS_8;
import static jssc.SerialPort.PARITY_NONE;
import static jssc.SerialPort.STOPBITS_1;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class ArduinoHelper {
    SerialPort port;
    public ArduinoHelper() throws SerialPortException {
        String[] portNames = SerialPortList.getPortNames();
        for (String portName : portNames) {
            System.out.println("Available port: " + portName);
        }
        port = new SerialPort("/dev/tty.usbmodem4827E2E6471C2");
        port.openPort();
        port.setParams(BAUDRATE_115200,  DATABITS_8, STOPBITS_1, PARITY_NONE);
    }
    public void writeData() throws SerialPortException {
        port.writeBytes("DISPENSE".getBytes());
        port.closePort();
    }
}




//package com.example.menlovending;
//
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothSocket;
//import android.util.Log;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.Set;
//import java.util.UUID;
//
//public class ArduinoHelper {
//    private static final String TAG = "ArduinoHelper";
//    private static final String DEVICE_NAME = "MenloVending"; // Change to your Arduino's Bluetooth name
//    private static final UUID UUID_SERIAL = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
//
//    private BluetoothAdapter bluetoothAdapter;
//    private BluetoothSocket bluetoothSocket;
//    private OutputStream outputStream;
//    private InputStream inputStream;
//
//    public ArduinoHelper() {
//        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//    }
//
//    public boolean connectToArduino() {
//        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
//            Log.e(TAG, "Bluetooth is not available or not enabled.");
//            return false;
//        }
//
//        BluetoothDevice device = findArduinoDevice();
//        if (device == null) {
//            Log.e(TAG, "Arduino Bluetooth device not found.");
//            return false;
//        }
//
//        try {
//            bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID_SERIAL);
//            bluetoothSocket.connect();
//            outputStream = bluetoothSocket.getOutputStream();
//            inputStream = bluetoothSocket.getInputStream();
//            Log.d(TAG, "Connected to Arduino!");
//            return true;
//        } catch (IOException e) {
//            Log.e(TAG, "Connection failed: " + e.getMessage());
//            return false;
//        }
//    }
//
//    private BluetoothDevice findArduinoDevice() {
//        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
//        for (BluetoothDevice device : pairedDevices) {
//            if (device.getName().equals(DEVICE_NAME)) {
//                return device;
//            }
//        }
//        return null;
//    }
//
//    public void sendData(String data) {
//        try {
//            if (outputStream != null) {
//                outputStream.write(data.getBytes());
//                Log.d(TAG, "Sent: " + data);
//            }
//        } catch (IOException e) {
//            Log.e(TAG, "Error sending data: " + e.getMessage());
//        }
//    }
//
//    public String receiveData() {
//        try {
//            if (inputStream != null) {
//                byte[] buffer = new byte[1024];
//                int bytes = inputStream.read(buffer);
//                String received = new String(buffer, 0, bytes);
//                Log.d(TAG, "Received: " + received);
//                return received;
//            }
//        } catch (IOException e) {
//            Log.e(TAG, "Error receiving data: " + e.getMessage());
//        }
//        return null;
//    }
//
//    public void closeConnection() {
//        try {
//            if (bluetoothSocket != null) bluetoothSocket.close();
//            Log.d(TAG, "Bluetooth connection closed.");
//        } catch (IOException e) {
//            Log.e(TAG, "Error closing connection: " + e.getMessage());
//        }
//    }
//}
