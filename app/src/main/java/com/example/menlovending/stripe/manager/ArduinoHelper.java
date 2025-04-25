package com.example.menlovending.stripe.manager;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private static final int TIMEOUT_IN_MS = 3000;
    private final String ARDUINO_IP;
    private boolean connectionEstablished = false;
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private int[] arduinoTwoCodeMap;

    public ArduinoHelper(String ARDUINO_IP) {
        this.ARDUINO_IP = ARDUINO_IP;
        this.arduinoTwoCodeMap = new int[17];
        this.arduinoTwoCodeMap[16] = 1;
        this.arduinoTwoCodeMap[15] = 2;
        this.arduinoTwoCodeMap[14] = 3;
        this.arduinoTwoCodeMap[13] = 4;
        this.arduinoTwoCodeMap[12] = 5;
        this.arduinoTwoCodeMap[11] = 6;
        this.arduinoTwoCodeMap[10] = 7;
        this.arduinoTwoCodeMap[9] = 8;
    }

    public boolean isConnectionEstablished() {
        return connectionEstablished;
    }

    // Establish Wi-Fi connection to Arduino using Executors
    public void findAndConnectDevice() {
        executorService.execute(() -> {
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(ARDUINO_IP, ARDUINO_PORT), TIMEOUT_IN_MS);
                if (socket.isConnected()) {
                    outputStream = socket.getOutputStream();
                    inputStream = socket.getInputStream();
                    connectionEstablished = true;
                    Log.d(TAG, "Connected to Arduino at " + ARDUINO_IP);
                } else {
                    Log.e(TAG, "Failed to connect to Arduino, socket not connected");
                }
            } catch (IOException e) {
                Log.e(TAG, "Error connecting to Arduino: " + e.getMessage());
            }
        });
    }

    // Send data to Arduino over Wi-Fi
    public void writeData(int code) {
        if (!connectionEstablished || socket == null) {
            Log.e(TAG, "Not connected to device, cannot write to device");
            return;
        }

        executorService.execute(() -> {
            try {
                String message;
                // Alter the code if necessary to the proper pin if code betweeen 9-16
                if (code <= 8) {
                    message = code + "\n";
                }
                else {
                    message = arduinoTwoCodeMap[code] + "\n";
                }
                outputStream.write(message.getBytes());
                outputStream.flush();  // Ensure it's sent immediately
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