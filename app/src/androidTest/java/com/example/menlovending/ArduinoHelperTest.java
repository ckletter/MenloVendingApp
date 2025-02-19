import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

public class ArduinoHelperTest {

    private BluetoothAdapter mockBluetoothAdapter;
    private BluetoothDevice mockBluetoothDevice;

    @Before
    public void setUp() {
        // Mock BluetoothAdapter and BluetoothDevice
        mockBluetoothAdapter = Mockito.mock(BluetoothAdapter.class);
        mockBluetoothDevice = Mockito.mock(BluetoothDevice.class);

        // Simulate Bluetooth behavior
        when(mockBluetoothAdapter.isEnabled()).thenReturn(true);
        when(mockBluetoothAdapter.getBondedDevices()).thenReturn(Collections.singleton(mockBluetoothDevice));
    }

    @Test
    public void testBluetoothConnection() {
        assertNotNull("Bluetooth Adapter should not be null", mockBluetoothAdapter);
        assertTrue("Bluetooth should be enabled", mockBluetoothAdapter.isEnabled());

        // Simulate getting bonded devices
        for (BluetoothDevice device : mockBluetoothAdapter.getBondedDevices()) {
            assertNotNull("Bluetooth device should not be null", device);
        }
    }
}
