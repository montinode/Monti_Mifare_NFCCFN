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

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import de.syss.MifareClassicTool.security.models.DerivedKey;
import de.syss.MifareClassicTool.security.models.KeyMetadata;
import de.syss.MifareClassicTool.security.models.SecurityContext;
import de.syss.MifareClassicTool.security.utils.CryptoUtils;

/**
 * Unified security key management system.
 * Stores and retrieves derived keys securely with support for multiple
 * encryption algorithms and key versioning.
 * Thread-safe with comprehensive audit logging.
 * 
 * ⚠️ SECURITY WARNING: The current encryption implementation uses AES-CBC without
 * message authentication (HMAC). This makes it vulnerable to padding oracle attacks
 * and ciphertext manipulation. For production use:
 * - Implement AES-GCM for authenticated encryption
 * - Or add HMAC-SHA256 authentication over IV and ciphertext
 * - Or use Android Keystore's encryption capabilities
 * 
 * This implementation is suitable for:
 * - Security research and analysis
 * - Educational purposes
 * - Development and testing
 * 
 * @author JOHNCHARLESMONTI
 */
public class WirelessSecurityManager {
    private static final String LOG_TAG = WirelessSecurityManager.class.getSimpleName();
    
    private final Context context;
    private final SecurityContext securityContext;
    private final ConcurrentHashMap<String, DerivedKey> keyStore;
    private final List<SecurityOperation> auditLog;
    private static WirelessSecurityManager instance;

    /**
     * Security operation for audit logging.
     */
    public static class SecurityOperation {
        public final long timestamp;
        public final String operation;
        public final String keyId;
        public final boolean success;
        public final String details;

        public SecurityOperation(String operation, String keyId, boolean success, String details) {
            this.timestamp = System.currentTimeMillis();
            this.operation = operation;
            this.keyId = keyId;
            this.success = success;
            this.details = details;
        }

        @Override
        public String toString() {
            return timestamp + " [" + (success ? "SUCCESS" : "FAILURE") + "] " 
                + operation + " - " + keyId + ": " + details;
        }
    }

    /**
     * Get singleton instance.
     * @param context Android context
     * @return WirelessSecurityManager instance
     */
    public static synchronized WirelessSecurityManager getInstance(Context context) {
        if (instance == null) {
            instance = new WirelessSecurityManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Private constructor for singleton.
     * @param context Android context
     */
    private WirelessSecurityManager(Context context) {
        this.context = context;
        this.securityContext = new SecurityContext("SECURITY_MANAGER_" + System.currentTimeMillis());
        this.keyStore = new ConcurrentHashMap<>();
        this.auditLog = new ArrayList<>();
        
        logOperation("INIT", "SYSTEM", true, "WirelessSecurityManager initialized");
    }

    /**
     * Store a derived key securely.
     * @param key Key to store
     * @return true if stored successfully
     */
    public synchronized boolean storeKey(DerivedKey key) {
        if (key == null) {
            logOperation("STORE", "NULL", false, "Attempted to store null key");
            return false;
        }

        try {
            String keyId = key.getKeyId();
            keyStore.put(keyId, key);
            
            securityContext.logOperation("Stored key: " + keyId);
            logOperation("STORE", keyId, true, 
                "Key stored - " + key.getKeyLengthBits() + " bits");
            
            Log.i(LOG_TAG, "Stored key: " + keyId);
            return true;
            
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error storing key", e);
            logOperation("STORE", key.getKeyId(), false, "Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieve a stored key.
     * @param keyId Key identifier
     * @return Retrieved key or null if not found
     */
    public synchronized DerivedKey retrieveKey(String keyId) {
        if (keyId == null || keyId.isEmpty()) {
            logOperation("RETRIEVE", "NULL", false, "Invalid key ID");
            return null;
        }

        DerivedKey key = keyStore.get(keyId);
        
        if (key != null) {
            securityContext.logOperation("Retrieved key: " + keyId);
            logOperation("RETRIEVE", keyId, true, "Key retrieved");
            Log.d(LOG_TAG, "Retrieved key: " + keyId);
        } else {
            logOperation("RETRIEVE", keyId, false, "Key not found");
            Log.w(LOG_TAG, "Key not found: " + keyId);
        }

        return key;
    }

    /**
     * Delete a stored key.
     * @param keyId Key identifier
     * @return true if deleted successfully
     */
    public synchronized boolean deleteKey(String keyId) {
        if (keyId == null || keyId.isEmpty()) {
            logOperation("DELETE", "NULL", false, "Invalid key ID");
            return false;
        }

        DerivedKey key = keyStore.remove(keyId);
        
        if (key != null) {
            key.clear();
            securityContext.logOperation("Deleted key: " + keyId);
            logOperation("DELETE", keyId, true, "Key deleted and cleared");
            Log.i(LOG_TAG, "Deleted key: " + keyId);
            return true;
        } else {
            logOperation("DELETE", keyId, false, "Key not found");
            return false;
        }
    }

    /**
     * Check if a key exists.
     * @param keyId Key identifier
     * @return true if key exists
     */
    public boolean hasKey(String keyId) {
        return keyStore.containsKey(keyId);
    }

    /**
     * Get all stored key IDs.
     * @return List of key IDs
     */
    public synchronized List<String> getAllKeyIds() {
        return new ArrayList<>(keyStore.keySet());
    }

    /**
     * Get count of stored keys.
     * @return Number of keys in store
     */
    public int getKeyCount() {
        return keyStore.size();
    }

    /**
     * Encrypt data using a stored key.
     * @param keyId Key identifier
     * @param data Data to encrypt
     * @return Encrypted data or null on error
     */
    public synchronized byte[] encrypt(String keyId, byte[] data) {
        DerivedKey key = retrieveKey(keyId);
        
        if (key == null) {
            logOperation("ENCRYPT", keyId, false, "Key not found");
            return null;
        }

        try {
            byte[] result = null;
            KeyMetadata.AlgorithmType algorithm = key.getMetadata().getAlgorithm();

            switch (algorithm) {
                case AES_128:
                case AES_256:
                    byte[] aesIv = CryptoUtils.generateAESIV();
                    byte[] encrypted = CryptoUtils.encryptAES(data, key.getKeyData(), aesIv);
                    
                    // Prepend IV to encrypted data
                    if (encrypted != null) {
                        result = new byte[aesIv.length + encrypted.length];
                        System.arraycopy(aesIv, 0, result, 0, aesIv.length);
                        System.arraycopy(encrypted, 0, result, aesIv.length, encrypted.length);
                    }
                    break;
                    
                case DES:
                case TRIPLE_DES:
                    byte[] desIv = CryptoUtils.generateDESIV();
                    byte[] desEncrypted = CryptoUtils.encryptDES(data, key.getKeyData(), desIv);
                    
                    // Prepend IV to encrypted data
                    if (desEncrypted != null) {
                        result = new byte[desIv.length + desEncrypted.length];
                        System.arraycopy(desIv, 0, result, 0, desIv.length);
                        System.arraycopy(desEncrypted, 0, result, desIv.length, desEncrypted.length);
                    }
                    break;
                    
                default:
                    Log.e(LOG_TAG, "Unsupported algorithm: " + algorithm);
                    logOperation("ENCRYPT", keyId, false, "Unsupported algorithm");
                    return null;
            }

            if (result != null) {
                logOperation("ENCRYPT", keyId, true, "Data encrypted - " + result.length + " bytes");
                Log.d(LOG_TAG, "Encrypted data with key: " + keyId);
            } else {
                logOperation("ENCRYPT", keyId, false, "Encryption failed");
            }

            return result;
            
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error encrypting data", e);
            logOperation("ENCRYPT", keyId, false, "Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Decrypt data using a stored key.
     * @param keyId Key identifier
     * @param data Data to decrypt (includes IV for AES)
     * @return Decrypted data or null on error
     */
    public synchronized byte[] decrypt(String keyId, byte[] data) {
        DerivedKey key = retrieveKey(keyId);
        
        if (key == null) {
            logOperation("DECRYPT", keyId, false, "Key not found");
            return null;
        }

        try {
            byte[] result = null;
            KeyMetadata.AlgorithmType algorithm = key.getMetadata().getAlgorithm();

            switch (algorithm) {
                case AES_128:
                case AES_256:
                    // Extract IV from beginning of data
                    if (data.length < 16) {
                        Log.e(LOG_TAG, "Data too short to contain IV");
                        logOperation("DECRYPT", keyId, false, "Invalid data length");
                        return null;
                    }
                    
                    byte[] aesIv = Arrays.copyOfRange(data, 0, 16);
                    byte[] encryptedData = Arrays.copyOfRange(data, 16, data.length);
                    result = CryptoUtils.decryptAES(encryptedData, key.getKeyData(), aesIv);
                    break;
                    
                case DES:
                case TRIPLE_DES:
                    // Extract IV from beginning of data (DES uses 8-byte IV)
                    if (data.length < 8) {
                        Log.e(LOG_TAG, "Data too short to contain DES IV");
                        logOperation("DECRYPT", keyId, false, "Invalid data length");
                        return null;
                    }
                    
                    byte[] desIv = Arrays.copyOfRange(data, 0, 8);
                    byte[] desEncryptedData = Arrays.copyOfRange(data, 8, data.length);
                    result = CryptoUtils.decryptDES(desEncryptedData, key.getKeyData(), desIv);
                    break;
                    
                default:
                    Log.e(LOG_TAG, "Unsupported algorithm: " + algorithm);
                    logOperation("DECRYPT", keyId, false, "Unsupported algorithm");
                    return null;
            }

            if (result != null) {
                logOperation("DECRYPT", keyId, true, "Data decrypted - " + result.length + " bytes");
                Log.d(LOG_TAG, "Decrypted data with key: " + keyId);
            } else {
                logOperation("DECRYPT", keyId, false, "Decryption failed");
            }

            return result;
            
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error decrypting data", e);
            logOperation("DECRYPT", keyId, false, "Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Rotate a key (create new version).
     * @param oldKeyId Old key identifier
     * @return New key or null on error
     */
    public synchronized DerivedKey rotateKey(String oldKeyId) {
        DerivedKey oldKey = retrieveKey(oldKeyId);
        
        if (oldKey == null) {
            logOperation("ROTATE", oldKeyId, false, "Key not found");
            return null;
        }

        try {
            // Generate new key data
            int keyLength = oldKey.getKeyData().length;
            byte[] newKeyData = CryptoUtils.generateAESKey(keyLength * 8);
            
            if (newKeyData == null) {
                logOperation("ROTATE", oldKeyId, false, "Failed to generate new key");
                return null;
            }

            // Create new metadata with incremented version
            KeyMetadata oldMetadata = oldKey.getMetadata();
            KeyMetadata newMetadata = new KeyMetadata(
                oldMetadata.getSource(),
                oldMetadata.getAlgorithm(),
                "Rotated from " + oldKeyId,
                oldMetadata.getVersion() + 1
            );

            String newKeyId = oldKeyId + "_v" + newMetadata.getVersion();
            DerivedKey newKey = new DerivedKey(newKeyData, newKeyId, newMetadata);

            // Store new key
            storeKey(newKey);
            
            securityContext.logOperation("Rotated key: " + oldKeyId + " -> " + newKeyId);
            logOperation("ROTATE", oldKeyId, true, "New key: " + newKeyId);
            
            return newKey;
            
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error rotating key", e);
            logOperation("ROTATE", oldKeyId, false, "Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Clear all stored keys.
     */
    public synchronized void clearAllKeys() {
        for (DerivedKey key : keyStore.values()) {
            key.clear();
        }
        keyStore.clear();
        
        securityContext.logOperation("Cleared all keys");
        logOperation("CLEAR_ALL", "SYSTEM", true, "All keys cleared");
        Log.i(LOG_TAG, "Cleared all keys");
    }

    /**
     * Get the audit log.
     * @return List of security operations
     */
    public synchronized List<SecurityOperation> getAuditLog() {
        return new ArrayList<>(auditLog);
    }

    /**
     * Clear the audit log.
     */
    public synchronized void clearAuditLog() {
        auditLog.clear();
        logOperation("CLEAR_LOG", "SYSTEM", true, "Audit log cleared");
    }

    /**
     * Get the security context.
     * @return Security context
     */
    public SecurityContext getSecurityContext() {
        return securityContext;
    }

    /**
     * Log a security operation.
     * @param operation Operation type
     * @param keyId Key identifier
     * @param success Success status
     * @param details Additional details
     */
    private synchronized void logOperation(String operation, String keyId, 
                                          boolean success, String details) {
        SecurityOperation op = new SecurityOperation(operation, keyId, success, details);
        auditLog.add(op);
        
        // Keep log size manageable (last 1000 operations)
        if (auditLog.size() > 1000) {
            auditLog.remove(0);
        }
    }

    /**
     * Export audit log as string.
     * @return Formatted audit log
     */
    public synchronized String exportAuditLog() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Wireless Security Manager Audit Log ===\n");
        sb.append("Total operations: ").append(auditLog.size()).append("\n\n");
        
        for (SecurityOperation op : auditLog) {
            sb.append(op.toString()).append("\n");
        }
        
        return sb.toString();
    }
}
