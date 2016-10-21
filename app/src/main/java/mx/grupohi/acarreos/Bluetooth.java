package mx.grupohi.acarreos;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * Created by Usuario on 19/10/2016.
 */


public class Bluetooth {

    public static final String TAG = "BLUETOOTH";

    public static void startBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
    }

    public static void stopBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
        }
    }

    public static void unpairMac(String macToRemove) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        try {
            Class<?> btDeviceInstance = Class.forName(BluetoothDevice.class.getCanonicalName());
            Method removeBondMethod = btDeviceInstance.getMethod("removeBond");
            boolean cleared = false;
            for (BluetoothDevice bluetoothDevice : bondedDevices) {
                String mac = bluetoothDevice.getAddress();
                if (mac.equals(macToRemove)) {
                    removeBondMethod.invoke(bluetoothDevice);
                    Log.i(TAG, "Cleared Pairing");
                    cleared = true;
                    break;
                }
            }

            if (!cleared) {
                Log.i(TAG, "Not Paired");
            }
        } catch (Throwable th) {
            Log.e(TAG, "Error pairing", th);
        }
    }
}
