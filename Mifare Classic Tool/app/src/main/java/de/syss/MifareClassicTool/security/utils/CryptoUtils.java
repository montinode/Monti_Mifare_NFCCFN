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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Cryptographic utility functions for key operations.
 * Provides AES, DES, and hash functions.
 * @author Monti Security Team
 */
public class CryptoUtils {
    private static final String LOG_TAG = CryptoUtils.class.getSimpleName();
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate a random AES key.
     * @param keySize Key size in bits (128 or 256)
     * @return Random key bytes
     */
    public static byte[] generateAESKey(int keySize) {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(keySize, secureRandom);
            SecretKey secretKey = keyGen.generateKey();
            return secretKey.getEncoded();
        } catch (NoSuchAlgorithmException e) {
            Log.e(LOG_TAG, "Error generating AES key", e);
            return null;
        }
    }

    /**
     * Generate a random DES key (64-bit key with 56-bit effective strength).
     * DES uses 64-bit keys where 8 bits are parity bits, resulting in 56 bits of effective key strength.
     * 
     * ⚠️ SECURITY WARNING: DES is cryptographically broken and deprecated since 1999.
     * It can be broken in hours with modern hardware. Use AES-256 instead.
     * This method is provided only for legacy system compatibility and security research.
     * 
     * @return Random DES key bytes (8 bytes)
     * @deprecated Use AES encryption instead
     */
    @Deprecated
    public static byte[] generateDESKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("DES");
            keyGen.init(56, secureRandom);
            SecretKey secretKey = keyGen.generateKey();
            return secretKey.getEncoded();
        } catch (NoSuchAlgorithmException e) {
            Log.e(LOG_TAG, "Error generating DES key", e);
            return null;
        }
    }

    /**
     * Encrypt data using AES-128/256.
     * @param data Data to encrypt
     * @param key Encryption key (16 or 32 bytes)
     * @param iv Initialization vector (16 bytes)
     * @return Encrypted data or null on error
     */
    public static byte[] encryptAES(byte[] data, byte[] key, byte[] iv) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            return cipher.doFinal(data);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error encrypting with AES", e);
            return null;
        }
    }

    /**
     * Decrypt data using AES-128/256.
     * @param data Data to decrypt
     * @param key Decryption key (16 or 32 bytes)
     * @param iv Initialization vector (16 bytes)
     * @return Decrypted data or null on error
     */
    public static byte[] decryptAES(byte[] data, byte[] key, byte[] iv) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            return cipher.doFinal(data);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error decrypting with AES", e);
            return null;
        }
    }

    /**
     * Encrypt data using DES with CBC mode.
     * 
     * ⚠️ SECURITY WARNING: DES is cryptographically broken and deprecated since 1999.
     * Use AES encryption instead. This method is provided only for legacy compatibility.
     * 
     * @param data Data to encrypt
     * @param key DES key (8 bytes)
     * @param iv Initialization vector (8 bytes)
     * @return Encrypted data or null on error
     * @deprecated Use AES encryption instead
     */
    @Deprecated
    public static byte[] encryptDES(byte[] data, byte[] key, byte[] iv) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, "DES");
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            return cipher.doFinal(data);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error encrypting with DES", e);
            return null;
        }
    }

    /**
     * Decrypt data using DES with CBC mode.
     * 
     * ⚠️ SECURITY WARNING: DES is cryptographically broken and deprecated since 1999.
     * Use AES encryption instead. This method is provided only for legacy compatibility.
     * 
     * @param data Data to decrypt
     * @param key DES key (8 bytes)
     * @param iv Initialization vector (8 bytes)
     * @return Decrypted data or null on error
     * @deprecated Use AES encryption instead
     */
    @Deprecated
    public static byte[] decryptDES(byte[] data, byte[] key, byte[] iv) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, "DES");
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            return cipher.doFinal(data);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error decrypting with DES", e);
            return null;
        }
    }

    /**
     * Generate a random IV for DES (8 bytes).
     * @return Random IV
     */
    public static byte[] generateDESIV() {
        return randomBytes(8);
    }

    /**
     * Calculate SHA-256 hash.
     * @param data Data to hash
     * @return Hash bytes or null on error
     */
    public static byte[] sha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(data);
        } catch (NoSuchAlgorithmException e) {
            Log.e(LOG_TAG, "Error calculating SHA-256", e);
            return null;
        }
    }

    /**
     * Calculate SHA-1 hash.
     * @param data Data to hash
     * @return Hash bytes or null on error
     */
    public static byte[] sha1(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            return digest.digest(data);
        } catch (NoSuchAlgorithmException e) {
            Log.e(LOG_TAG, "Error calculating SHA-1", e);
            return null;
        }
    }

    /**
     * Calculate MD5 hash.
     * @param data Data to hash
     * @return Hash bytes or null on error
     */
    public static byte[] md5(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            return digest.digest(data);
        } catch (NoSuchAlgorithmException e) {
            Log.e(LOG_TAG, "Error calculating MD5", e);
            return null;
        }
    }

    /**
     * Generate random bytes.
     * @param length Number of bytes to generate
     * @return Random bytes
     */
    public static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return bytes;
    }

    /**
     * Generate a random IV for AES (16 bytes).
     * @return Random IV
     */
    public static byte[] generateAESIV() {
        return randomBytes(16);
    }

    /**
     * XOR two byte arrays.
     * @param a First array
     * @param b Second array
     * @return XOR result (length of shorter array)
     */
    public static byte[] xor(byte[] a, byte[] b) {
        int length = Math.min(a.length, b.length);
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            result[i] = (byte) (a[i] ^ b[i]);
        }
        return result;
    }

    /**
     * Clear sensitive data from memory.
     * @param data Array to clear
     */
    public static void clearSensitiveData(byte[] data) {
        if (data != null) {
            Arrays.fill(data, (byte) 0);
        }
    }

    /**
     * Convert bytes to hex string.
     * @param bytes Bytes to convert
     * @return Hex string
     */
    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) return "";
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    /**
     * Convert hex string to bytes.
     * @param hex Hex string
     * @return Byte array or null if invalid
     */
    public static byte[] hexToBytes(String hex) {
        if (hex == null || hex.length() % 2 != 0) {
            return null;
        }
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }
}
