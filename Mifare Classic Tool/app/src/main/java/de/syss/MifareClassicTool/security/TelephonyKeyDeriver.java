/**
 * MONTIAI.COM - Monti-Network-State Asset Integration
 * 
 * @author MONTIAI.COM (MONTINODE Development Team)
 * @copyright 2025 JOHN CHARLES MONTI - All Rights Reserved
 * @license GPL-3.0 with MONTIAI Network Requirements
 * 
 * This software respects the principles of:
 * - Life: No malicious code; research & education focused
 * - Liberty: Open source; freedom to study & modify  
 * - Property: JOHN CHARLES MONTI copyright protected
 * - MONTIAI Network: Integration & authorization protocols
 * 
 * For more information: JOHNCHARLESMONTI.COM
 */

/*
 * Copyright 2013 Gerhard Klostermeier
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

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.telephony.CellInfo;
import android.telephony.CellSignalStrength;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import de.syss.MifareClassicTool.security.models.DerivedKey;
import de.syss.MifareClassicTool.security.models.KeyMetadata;
import de.syss.MifareClassicTool.security.models.SecurityContext;
import de.syss.MifareClassicTool.security.utils.CryptoUtils;
import de.syss.MifareClassicTool.security.utils.KeyDerivationUtils;

/**
 * Generates encryption keys from cellular/phone data.
 * Extracts key material from phone state, SIM, and signal strength.
 * Supports multi-factor key combination with secure random injection.
 * 
 * ⚠️ SECURITY WARNING: Telephony-derived keys have low entropy and are predictable.
 * Keys derived from IMEI, SIM, phone number, or signal strength can be reconstructed
 * by attackers with access to this information. Signal strength is highly variable
 * and unsuitable for cryptographic key derivation.
 * 
 * These methods are intended for:
 * - Security research and analysis
 * - Legacy system compatibility
 * - Educational purposes
 * 
 * For production encryption, use Android Keystore to generate and store cryptographic keys.
 * 
 * @author Monti Security Team
 */
public class TelephonyKeyDeriver {
    private static final String LOG_TAG = TelephonyKeyDeriver.class.getSimpleName();
    
    private final Context context;
    private final TelephonyManager telephonyManager;
    private final SecurityContext securityContext;

    /**
     * Create a new telephony key deriver.
     * @param context Android context
     */
    public TelephonyKeyDeriver(Context context) {
        this.context = context;
        this.telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        this.securityContext = new SecurityContext("TELEPHONY_" + System.currentTimeMillis());
    }

    /**
     * Derive key from phone IMEI.
     * Note: Requires READ_PHONE_STATE permission.
     * @param keyLength Desired key length in bytes
     * @return Derived key or null if permission denied
     */
    @SuppressLint("MissingPermission")
    public DerivedKey deriveKeyFromIMEI(int keyLength) {
        if (!checkPermission(Manifest.permission.READ_PHONE_STATE)) {
            Log.e(LOG_TAG, "READ_PHONE_STATE permission not granted");
            return null;
        }

        try {
            String imei = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                imei = telephonyManager.getImei();
            } else {
                imei = telephonyManager.getDeviceId();
            }

            if (imei == null || imei.isEmpty()) {
                Log.e(LOG_TAG, "IMEI not available");
                return null;
            }

            byte[] keyData = KeyDerivationUtils.deriveKeyFromString(imei, keyLength);
            
            KeyMetadata metadata = new KeyMetadata(
                KeyMetadata.KeySource.TELEPHONY,
                KeyMetadata.AlgorithmType.AES_128,
                "Key derived from device IMEI",
                1
            );
            metadata.addInfo("source", "IMEI");

            String keyId = "TELEPHONY_IMEI_" + System.currentTimeMillis();
            securityContext.logOperation("Derived key from IMEI: " + keyId);

            return new DerivedKey(keyData, keyId, metadata);

        } catch (SecurityException e) {
            Log.e(LOG_TAG, "Permission denied accessing IMEI", e);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error deriving key from IMEI", e);
            return null;
        }
    }

    /**
     * Derive key from SIM serial number (ICCID).
     * Note: Requires READ_PHONE_STATE permission.
     * @param keyLength Desired key length in bytes
     * @return Derived key or null if permission denied
     */
    @SuppressLint("MissingPermission")
    public DerivedKey deriveKeyFromSIM(int keyLength) {
        if (!checkPermission(Manifest.permission.READ_PHONE_STATE)) {
            Log.e(LOG_TAG, "READ_PHONE_STATE permission not granted");
            return null;
        }

        try {
            String simSerial = telephonyManager.getSimSerialNumber();
            
            if (simSerial == null || simSerial.isEmpty()) {
                Log.e(LOG_TAG, "SIM serial number not available");
                return null;
            }

            byte[] keyData = KeyDerivationUtils.deriveKeyFromString(simSerial, keyLength);
            
            KeyMetadata metadata = new KeyMetadata(
                KeyMetadata.KeySource.TELEPHONY,
                KeyMetadata.AlgorithmType.AES_128,
                "Key derived from SIM serial number",
                1
            );
            metadata.addInfo("source", "SIM_ICCID");

            String keyId = "TELEPHONY_SIM_" + System.currentTimeMillis();
            securityContext.logOperation("Derived key from SIM: " + keyId);

            return new DerivedKey(keyData, keyId, metadata);

        } catch (SecurityException e) {
            Log.e(LOG_TAG, "Permission denied accessing SIM", e);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error deriving key from SIM", e);
            return null;
        }
    }

    /**
     * Derive key from phone number.
     * Note: Requires READ_PHONE_NUMBERS permission (API 26+) or READ_PHONE_STATE.
     * @param keyLength Desired key length in bytes
     * @return Derived key or null if permission denied
     */
    @SuppressLint("MissingPermission")
    public DerivedKey deriveKeyFromPhoneNumber(int keyLength) {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O 
            ? Manifest.permission.READ_PHONE_NUMBERS 
            : Manifest.permission.READ_PHONE_STATE;
            
        if (!checkPermission(permission)) {
            Log.e(LOG_TAG, "Phone number permission not granted");
            return null;
        }

        try {
            String phoneNumber = telephonyManager.getLine1Number();
            
            if (phoneNumber == null || phoneNumber.isEmpty()) {
                Log.e(LOG_TAG, "Phone number not available");
                return null;
            }

            byte[] keyData = KeyDerivationUtils.deriveKeyFromString(phoneNumber, keyLength);
            
            KeyMetadata metadata = new KeyMetadata(
                KeyMetadata.KeySource.TELEPHONY,
                KeyMetadata.AlgorithmType.AES_128,
                "Key derived from phone number",
                1
            );
            metadata.addInfo("source", "PHONE_NUMBER");

            String keyId = "TELEPHONY_PHONE_" + System.currentTimeMillis();
            securityContext.logOperation("Derived key from phone number: " + keyId);

            return new DerivedKey(keyData, keyId, metadata);

        } catch (SecurityException e) {
            Log.e(LOG_TAG, "Permission denied accessing phone number", e);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error deriving key from phone number", e);
            return null;
        }
    }

    /**
     * Derive key from cellular signal strength.
     * Note: Requires ACCESS_FINE_LOCATION permission.
     * @param keyLength Desired key length in bytes
     * @return Derived key or null
     */
    @SuppressLint("MissingPermission")
    public DerivedKey deriveKeyFromSignalStrength(int keyLength) {
        if (!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Log.e(LOG_TAG, "ACCESS_FINE_LOCATION permission not granted");
            return null;
        }

        try {
            List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
            
            if (cellInfoList == null || cellInfoList.isEmpty()) {
                Log.e(LOG_TAG, "No cell info available");
                return null;
            }

            // Collect signal strength from all cells
            List<Integer> signalStrengths = new ArrayList<>();
            for (CellInfo cellInfo : cellInfoList) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    CellSignalStrength signalStrength = cellInfo.getCellSignalStrength();
                    signalStrengths.add(signalStrength.getDbm());
                }
            }

            if (signalStrengths.isEmpty()) {
                Log.e(LOG_TAG, "No signal strength data available");
                return null;
            }

            // Combine signal strengths into key material
            ByteBuffer buffer = ByteBuffer.allocate(signalStrengths.size() * 4);
            for (Integer strength : signalStrengths) {
                buffer.putInt(strength);
            }

            byte[] hash = CryptoUtils.sha256(buffer.array());
            byte[] keyData = KeyDerivationUtils.deriveKeyOfLength(hash, keyLength);
            
            KeyMetadata metadata = new KeyMetadata(
                KeyMetadata.KeySource.TELEPHONY,
                KeyMetadata.AlgorithmType.AES_128,
                "Key derived from cellular signal strength",
                1
            );
            metadata.addInfo("source", "SIGNAL_STRENGTH");
            metadata.addInfo("cell_count", String.valueOf(signalStrengths.size()));

            String keyId = "TELEPHONY_SIGNAL_" + System.currentTimeMillis();
            securityContext.logOperation("Derived key from signal strength: " + keyId);

            return new DerivedKey(keyData, keyId, metadata);

        } catch (SecurityException e) {
            Log.e(LOG_TAG, "Permission denied accessing signal strength", e);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error deriving key from signal strength", e);
            return null;
        }
    }

    /**
     * Derive key from multiple telephony factors.
     * @param useIMEI Include IMEI in key derivation
     * @param useSIM Include SIM in key derivation
     * @param usePhone Include phone number in key derivation
     * @param useSignal Include signal strength in key derivation
     * @param keyLength Desired key length in bytes
     * @return Multi-factor derived key or null
     */
    public DerivedKey deriveMultiFactorKey(boolean useIMEI, boolean useSIM, 
                                          boolean usePhone, boolean useSignal, 
                                          int keyLength) {
        List<byte[]> factors = new ArrayList<>();

        if (useIMEI) {
            DerivedKey imeiKey = deriveKeyFromIMEI(keyLength);
            if (imeiKey != null) {
                factors.add(imeiKey.getKeyData());
                imeiKey.clear();
            }
        }

        if (useSIM) {
            DerivedKey simKey = deriveKeyFromSIM(keyLength);
            if (simKey != null) {
                factors.add(simKey.getKeyData());
                simKey.clear();
            }
        }

        if (usePhone) {
            DerivedKey phoneKey = deriveKeyFromPhoneNumber(keyLength);
            if (phoneKey != null) {
                factors.add(phoneKey.getKeyData());
                phoneKey.clear();
            }
        }

        if (useSignal) {
            DerivedKey signalKey = deriveKeyFromSignalStrength(keyLength);
            if (signalKey != null) {
                factors.add(signalKey.getKeyData());
                signalKey.clear();
            }
        }

        if (factors.isEmpty()) {
            Log.e(LOG_TAG, "No factors available for multi-factor key derivation");
            return null;
        }

        byte[][] factorArray = factors.toArray(new byte[0][]);
        byte[] keyData = KeyDerivationUtils.deriveKeyFromFactors(factorArray, keyLength);
        
        // Clear factor data
        for (byte[] factor : factors) {
            CryptoUtils.clearSensitiveData(factor);
        }

        KeyMetadata metadata = new KeyMetadata(
            KeyMetadata.KeySource.TELEPHONY,
            KeyMetadata.AlgorithmType.AES_128,
            "Multi-factor key derived from telephony data",
            1
        );
        metadata.addInfo("factor_count", String.valueOf(factors.size()));

        String keyId = "TELEPHONY_MULTI_" + System.currentTimeMillis();
        securityContext.logOperation("Derived multi-factor key: " + keyId);

        return new DerivedKey(keyData, keyId, metadata);
    }

    /**
     * Derive key with random component injection.
     * @param baseKey Base key to enhance
     * @param randomBits Number of random bits to inject
     * @return Enhanced key
     */
    public DerivedKey injectRandomComponent(DerivedKey baseKey, int randomBits) {
        if (baseKey == null) {
            Log.e(LOG_TAG, "Cannot inject random component into null key");
            return null;
        }

        byte[] enhancedData = KeyDerivationUtils.injectRandomComponent(
            baseKey.getKeyData(), randomBits);

        KeyMetadata metadata = new KeyMetadata(
            KeyMetadata.KeySource.HYBRID,
            baseKey.getMetadata().getAlgorithm(),
            "Enhanced with " + randomBits + " bits of random data",
            baseKey.getMetadata().getVersion() + 1
        );

        String keyId = baseKey.getKeyId() + "_RANDOM";
        securityContext.logOperation("Injected random component: " + keyId);

        return new DerivedKey(enhancedData, keyId, metadata);
    }

    /**
     * Get the security context.
     * @return Security context
     */
    public SecurityContext getSecurityContext() {
        return securityContext;
    }

    /**
     * Check if a permission is granted.
     * @param permission Permission to check
     * @return true if granted
     */
    private boolean checkPermission(String permission) {
        return ActivityCompat.checkSelfPermission(context, permission) 
            == PackageManager.PERMISSION_GRANTED;
    }
}
