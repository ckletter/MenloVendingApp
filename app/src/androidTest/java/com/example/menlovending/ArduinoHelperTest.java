package com.example.menlovending;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)  // Ensures the test runs on the Android device/emulator
@SmallTest
public class ArduinoHelperTest {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice arduinoDevice;

    @Before
    public void setUp() {
        // Initialize BluetoothAdapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Test
    public void testBluetoothConnection() {
        assertNotNull("Bluetooth Adapter should not be null", bluetoothAdapter);
        assertTrue("Bluetooth should be enabled", bluetoothAdapter.isEnabled());

        // Discovering paired devices
        for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
            if (device.getName().equals("MenloVending")) { // Replace with your actual Arduino's Bluetooth name
                arduinoDevice = device;
                break;
            }
        }

        assertNotNull("Arduino Bluetooth device should be found", arduinoDevice);
    }

    @Test
    public void testBluetoothCommunication() {
        // Add logic here to test sending/receiving data via Bluetooth with your Arduino
        assertNotNull("Arduino Bluetooth device should be found", arduinoDevice);

        // You can write further tests based on the communication you're expecting
    }
}
