/*
 * Copyright 2013 Gerhard Klostermeier
 * Copyright 2026 JOHN CHARLES MONTI
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.syss.MifareClassicTool.security;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import de.syss.MifareClassicTool.security.models.DerivedKey;
import de.syss.MifareClassicTool.security.models.KeyMetadata;
import de.syss.MifareClassicTool.security.models.SecurityContext;
import de.syss.MifareClassicTool.security.utils.CryptoUtils;
import de.syss.MifareClassicTool.security.utils.KeyDerivationUtils;

/**
 * Intercepts and derives encryption keys from Bluetooth GATT communications.
 * Monitors GATT characteristic read/write for key exchange patterns.
 * Thread-safe with callback support.
 * @author Monti Security Team
 */
public class GattKeyExchange {
    private static final String LOG_TAG = GattKeyExchange.class.getSimpleName();
    
    // Common GATT service UUIDs for key exchange
    private static final UUID GENERIC_ACCESS_SERVICE = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
    private static final UUID GENERIC_ATTRIBUTE_SERVICE = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb");
    
    private final Context context;
    private final SecurityContext securityContext;
    private final ConcurrentHashMap<String, byte[]> interceptedData;
    private final List<KeyExchangeListener> listeners;
    private BluetoothGatt connectedGatt;
    private volatile boolean monitoring;

    /**
     * Listener interface for key exchange events.
     */
    public interface KeyExchangeListener {
        /**
         * Called when a potential key is intercepted.
         * @param key The derived key
         */
        void onKeyIntercepted(DerivedKey key);

        /**
         * Called when key exchange pattern is detected.
         * @param characteristicUuid UUID of the characteristic
         * @param data Raw data from characteristic
         */
        void onKeyExchangeDetected(UUID characteristicUuid, byte[] data);

        /**
         * Called on error.
         * @param error Error message
         */
        void onError(String error);
    }

    /**
     * Create a new GATT key exchange monitor.
     * @param context Android context
     */
    public GattKeyExchange(Context context) {
        this.context = context;
        this.securityContext = new SecurityContext("GATT_" + System.currentTimeMillis());
        this.interceptedData = new ConcurrentHashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
        this.monitoring = false;
    }

    /**
     * Add a listener for key exchange events.
     * @param listener Listener to add
     */
    public void addListener(KeyExchangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a listener.
     * @param listener Listener to remove
     */
    public void removeListener(KeyExchangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Start monitoring GATT communications.
     * @param gatt Connected GATT instance
     */
    public synchronized void startMonitoring(BluetoothGatt gatt) {
        if (gatt == null) {
            notifyError("Cannot monitor null GATT connection");
            return;
        }

        this.connectedGatt = gatt;
        this.monitoring = true;
        securityContext.logOperation("Started GATT monitoring");
        Log.i(LOG_TAG, "Started monitoring GATT communications");
    }

    /**
     * Stop monitoring GATT communications.
     */
    public synchronized void stopMonitoring() {
        this.monitoring = false;
        this.connectedGatt = null;
        securityContext.logOperation("Stopped GATT monitoring");
        Log.i(LOG_TAG, "Stopped monitoring GATT communications");
    }

    /**
     * Check if currently monitoring.
     * @return true if monitoring
     */
    public boolean isMonitoring() {
        return monitoring;
    }

    /**
     * Process GATT characteristic read data.
     * @param characteristic The characteristic that was read
     */
    public void onCharacteristicRead(BluetoothGattCharacteristic characteristic) {
        if (!monitoring || characteristic == null) {
            return;
        }

        byte[] value = characteristic.getValue();
        if (value == null || value.length == 0) {
            return;
        }

        UUID uuid = characteristic.getUuid();
        String key = uuid.toString();

        securityContext.logOperation("Characteristic read: " + key + " (" + value.length + " bytes)");
        Log.d(LOG_TAG, "Read characteristic: " + key);

        // Store intercepted data
        interceptedData.put(key, Arrays.copyOf(value, value.length));

        // Analyze for key patterns
        analyzeForKeyPattern(uuid, value);
    }

    /**
     * Process GATT characteristic write data.
     * @param characteristic The characteristic that was written
     */
    public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic) {
        if (!monitoring || characteristic == null) {
            return;
        }

        byte[] value = characteristic.getValue();
        if (value == null || value.length == 0) {
            return;
        }

        UUID uuid = characteristic.getUuid();
        String key = uuid.toString();

        securityContext.logOperation("Characteristic write: " + key + " (" + value.length + " bytes)");
        Log.d(LOG_TAG, "Write characteristic: " + key);

        // Store intercepted data
        interceptedData.put(key + "_write", Arrays.copyOf(value, value.length));

        // Analyze for key patterns
        analyzeForKeyPattern(uuid, value);
    }

    /**
     * Analyze data for key exchange patterns.
     * @param uuid Characteristic UUID
     * @param data Raw data
     */
    /**
     * Analyze data for key exchange patterns.
     * 
     * Note: This simple length-based heuristic may produce false positives,
     * intercepting non-key data that happens to be 6, 16, 24, or 32 bytes long.
     * Applications should validate intercepted keys before use.
     * 
     * @param uuid Characteristic UUID
     * @param data Raw data
     */
    private void analyzeForKeyPattern(UUID uuid, byte[] data) {
        // Notify listeners of potential key exchange
        for (KeyExchangeListener listener : listeners) {
            listener.onKeyExchangeDetected(uuid, data);
        }

        // Check for common key lengths (128-bit, 192-bit, 256-bit AES, 48-bit MIFARE)
        // WARNING: This may intercept non-key data of the same length
        if (data.length == 6 || data.length == 16 || data.length == 24 || data.length == 32) {
            deriveKeyFromGattData(uuid, data);
        }
    }

    /**
     * Derive encryption key from GATT data.
     * @param uuid Characteristic UUID
     * @param data Raw data
     */
    private void deriveKeyFromGattData(UUID uuid, byte[] data) {
        try {
            // Determine key type based on length
            KeyMetadata.AlgorithmType algorithm;
            if (data.length == 6) {
                algorithm = KeyMetadata.AlgorithmType.MIFARE_CLASSIC;
            } else if (data.length == 8) {
                algorithm = KeyMetadata.AlgorithmType.DES;
            } else if (data.length == 16) {
                algorithm = KeyMetadata.AlgorithmType.AES_128;
            } else if (data.length == 32) {
                algorithm = KeyMetadata.AlgorithmType.AES_256;
            } else {
                algorithm = KeyMetadata.AlgorithmType.AES_128;
            }

            // Create metadata
            KeyMetadata metadata = new KeyMetadata(
                KeyMetadata.KeySource.GATT,
                algorithm,
                "Key derived from GATT characteristic " + uuid.toString(),
                1
            );
            metadata.addInfo("uuid", uuid.toString());
            metadata.addInfo("timestamp", String.valueOf(System.currentTimeMillis()));

            // Create derived key
            String keyId = "GATT_" + uuid.toString().substring(0, 8) + "_" + System.currentTimeMillis();
            DerivedKey derivedKey = new DerivedKey(data, keyId, metadata);

            securityContext.logOperation("Derived key from GATT: " + keyId);
            Log.i(LOG_TAG, "Derived key from GATT: " + keyId);

            // Notify listeners
            for (KeyExchangeListener listener : listeners) {
                listener.onKeyIntercepted(derivedKey);
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error deriving key from GATT data", e);
            notifyError("Error deriving key: " + e.getMessage());
        }
    }

    /**
     * Derive AES key from GATT data.
     * @param data Raw GATT data
     * @param keySize Desired key size in bits (128 or 256)
     * @return Derived key or null
     */
    public DerivedKey deriveAESKey(byte[] data, int keySize) {
        if (data == null || data.length == 0) {
            Log.e(LOG_TAG, "No data provided for AES key derivation");
            return null;
        }

        int keyBytes = keySize / 8;
        byte[] keyData = KeyDerivationUtils.deriveKeyOfLength(data, keyBytes);
        
        if (keyData == null) {
            return null;
        }

        KeyMetadata metadata = new KeyMetadata(
            KeyMetadata.KeySource.GATT,
            keySize == 256 ? KeyMetadata.AlgorithmType.AES_256 : KeyMetadata.AlgorithmType.AES_128,
            "AES-" + keySize + " key derived from GATT data",
            1
        );

        String keyId = "GATT_AES" + keySize + "_" + System.currentTimeMillis();
        securityContext.logOperation("Derived AES-" + keySize + " key: " + keyId);

        return new DerivedKey(keyData, keyId, metadata);
    }

    /**
     * Derive MIFARE Classic key from GATT data.
     * @param data Raw GATT data
     * @return Derived 6-byte MIFARE key or null
     */
    public DerivedKey deriveMifareKey(byte[] data) {
        if (data == null || data.length == 0) {
            Log.e(LOG_TAG, "No data provided for MIFARE key derivation");
            return null;
        }

        byte[] keyData = KeyDerivationUtils.deriveMifareKey(data);
        
        if (keyData == null) {
            return null;
        }

        KeyMetadata metadata = new KeyMetadata(
            KeyMetadata.KeySource.GATT,
            KeyMetadata.AlgorithmType.MIFARE_CLASSIC,
            "MIFARE Classic key derived from GATT data",
            1
        );

        String keyId = "GATT_MIFARE_" + System.currentTimeMillis();
        securityContext.logOperation("Derived MIFARE key: " + keyId);

        return new DerivedKey(keyData, keyId, metadata);
    }

    /**
     * Get all intercepted data.
     * @return Map of characteristic UUID to data
     */
    public ConcurrentHashMap<String, byte[]> getInterceptedData() {
        return new ConcurrentHashMap<>(interceptedData);
    }

    /**
     * Clear all intercepted data.
     */
    public void clearInterceptedData() {
        for (byte[] data : interceptedData.values()) {
            Arrays.fill(data, (byte) 0);
        }
        interceptedData.clear();
        securityContext.logOperation("Cleared intercepted data");
    }

    /**
     * Get the security context.
     * @return Security context
     */
    public SecurityContext getSecurityContext() {
        return securityContext;
    }

    /**
     * Notify all listeners of an error.
     * @param error Error message
     */
    private void notifyError(String error) {
        Log.e(LOG_TAG, error);
        for (KeyExchangeListener listener : listeners) {
            listener.onError(error);
        }
    }

    /**
     * Clean up resources.
     */
    public void cleanup() {
        stopMonitoring();
        clearInterceptedData();
        listeners.clear();
        Log.i(LOG_TAG, "GattKeyExchange cleaned up");
    }
}
