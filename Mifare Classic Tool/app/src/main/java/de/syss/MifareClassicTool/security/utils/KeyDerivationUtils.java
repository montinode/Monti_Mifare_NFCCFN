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

package de.syss.MifareClassicTool.security.utils;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Utilities for deriving encryption keys from various sources.
 * @author Monti Security Team
 */
public class KeyDerivationUtils {
    private static final String LOG_TAG = KeyDerivationUtils.class.getSimpleName();
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Derive a key from multiple input factors.
     * @param factors Array of input factors
     * @param keyLength Desired key length in bytes
     * @return Derived key
     */
    public static byte[] deriveKeyFromFactors(byte[][] factors, int keyLength) {
        if (factors == null || factors.length == 0) {
            Log.e(LOG_TAG, "No factors provided for key derivation");
            return null;
        }

        // Concatenate all factors
        int totalLength = 0;
        for (byte[] factor : factors) {
            if (factor != null) {
                totalLength += factor.length;
            }
        }

        byte[] combined = new byte[totalLength];
        int offset = 0;
        for (byte[] factor : factors) {
            if (factor != null) {
                System.arraycopy(factor, 0, combined, offset, factor.length);
                offset += factor.length;
            }
        }

        // Hash the combined factors
        byte[] hash = CryptoUtils.sha256(combined);
        
        // Clear sensitive data
        Arrays.fill(combined, (byte) 0);

        // Derive key of desired length
        return deriveKeyOfLength(hash, keyLength);
    }

    /**
     * Derive a key of specific length from a hash.
     * @param hash Input hash
     * @param keyLength Desired key length in bytes
     * @return Derived key
     */
    public static byte[] deriveKeyOfLength(byte[] hash, int keyLength) {
        if (hash == null || keyLength <= 0) {
            return null;
        }

        if (hash.length >= keyLength) {
            // Truncate to desired length
            return Arrays.copyOf(hash, keyLength);
        } else {
            // Extend by re-hashing
            byte[] extendedKey = new byte[keyLength];
            int offset = 0;
            byte[] currentHash = hash;

            while (offset < keyLength) {
                int copyLength = Math.min(currentHash.length, keyLength - offset);
                System.arraycopy(currentHash, 0, extendedKey, offset, copyLength);
                offset += copyLength;

                if (offset < keyLength) {
                    currentHash = CryptoUtils.sha256(currentHash);
                }
            }

            return extendedKey;
        }
    }

    /**
     * Derive key from string data (phone number, IMEI, etc.).
     * @param data String data
     * @param keyLength Desired key length in bytes
     * @return Derived key
     */
    public static byte[] deriveKeyFromString(String data, int keyLength) {
        if (data == null || data.isEmpty()) {
            Log.e(LOG_TAG, "Empty string provided for key derivation");
            return null;
        }

        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        byte[] hash = CryptoUtils.sha256(dataBytes);
        
        // Clear sensitive data
        Arrays.fill(dataBytes, (byte) 0);

        return deriveKeyOfLength(hash, keyLength);
    }

    /**
     * Derive key from integer value (signal strength, etc.).
     * @param value Integer value
     * @param keyLength Desired key length in bytes
     * @return Derived key
     */
    public static byte[] deriveKeyFromInt(int value, int keyLength) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(value);
        byte[] hash = CryptoUtils.sha256(buffer.array());
        return deriveKeyOfLength(hash, keyLength);
    }

    /**
     * Inject random component into derived key.
     * @param key Original key
     * @param randomBits Number of random bits to inject
     * @return Key with random component
     */
    public static byte[] injectRandomComponent(byte[] key, int randomBits) {
        if (key == null || randomBits <= 0) {
            return key;
        }

        int randomBytes = (randomBits + 7) / 8;
        byte[] random = new byte[randomBytes];
        secureRandom.nextBytes(random);

        // XOR random data with key
        byte[] result = Arrays.copyOf(key, key.length);
        for (int i = 0; i < Math.min(randomBytes, key.length); i++) {
            result[i] ^= random[i];
        }

        // Clear sensitive data
        Arrays.fill(random, (byte) 0);

        return result;
    }

    /**
     * Combine multiple keys using XOR.
     * @param keys Array of keys to combine
     * @return Combined key
     */
    public static byte[] combineKeys(byte[]... keys) {
        if (keys == null || keys.length == 0) {
            return null;
        }

        // Find the longest key
        int maxLength = 0;
        for (byte[] key : keys) {
            if (key != null && key.length > maxLength) {
                maxLength = key.length;
            }
        }

        byte[] combined = new byte[maxLength];
        for (byte[] key : keys) {
            if (key != null) {
                for (int i = 0; i < key.length; i++) {
                    combined[i] ^= key[i];
                }
            }
        }

        return combined;
    }

    /**
     * Derive MIFARE Classic key from data.
     * @param data Input data
     * @return 6-byte MIFARE key
     */
    public static byte[] deriveMifareKey(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }

        byte[] hash = CryptoUtils.sha256(data);
        // MIFARE Classic keys are 6 bytes (48 bits)
        return Arrays.copyOf(hash, 6);
    }

    /**
     * Apply PBKDF2-like key stretching.
     * @param input Input key material
     * @param iterations Number of iterations
     * @param keyLength Desired key length in bytes
     * @return Stretched key
     */
    public static byte[] stretchKey(byte[] input, int iterations, int keyLength) {
        if (input == null || iterations <= 0 || keyLength <= 0) {
            return null;
        }

        byte[] stretched = input;
        for (int i = 0; i < iterations; i++) {
            stretched = CryptoUtils.sha256(stretched);
        }

        return deriveKeyOfLength(stretched, keyLength);
    }

    /**
     * Create a key derivation context string.
     * @param purpose Purpose of the key
     * @param version Version number
     * @return Context string
     */
    public static String createContext(String purpose, int version) {
        return purpose + "_v" + version;
    }

    /**
     * Derive key with context.
     * @param data Input data
     * @param context Context string
     * @param keyLength Desired key length in bytes
     * @return Derived key
     */
    public static byte[] deriveKeyWithContext(byte[] data, String context, int keyLength) {
        if (data == null || context == null) {
            return null;
        }

        byte[] contextBytes = context.getBytes(StandardCharsets.UTF_8);
        byte[][] factors = {data, contextBytes};
        
        return deriveKeyFromFactors(factors, keyLength);
    }
}
