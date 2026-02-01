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

import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.syss.MifareClassicTool.security.models.DerivedKey;
import de.syss.MifareClassicTool.security.models.SecurityContext;
import de.syss.MifareClassicTool.security.utils.CryptoUtils;

/**
 * Encodes derived keys into Monti-Network-State assets.
 * Converts encryption keys to squared Unicode representation and
 * generates secure key identifiers for network-safe transmission.
 * @author JOHNCHARLESMONTI
 */
public class MontiKeyEncoder {
    private static final String LOG_TAG = MontiKeyEncoder.class.getSimpleName();
    
    private final SecurityContext securityContext;
    
    // Unicode block ranges for encoding
    private static final int UNICODE_BASE = 0x2800; // Braille Patterns block
    private static final int UNICODE_RANGE = 256;

    /**
     * Encoded key representation.
     */
    public static class EncodedKey {
        public final String keyId;
        public final String encodedData;
        public final String checksum;
        public final long timestamp;
        public final Map<String, String> metadata;

        public EncodedKey(String keyId, String encodedData, String checksum, 
                         long timestamp, Map<String, String> metadata) {
            this.keyId = keyId;
            this.encodedData = encodedData;
            this.checksum = checksum;
            this.timestamp = timestamp;
            this.metadata = metadata;
        }

        @Override
        public String toString() {
            return "EncodedKey{" +
                    "keyId='" + keyId + '\'' +
                    ", encodedLength=" + encodedData.length() +
                    ", checksum='" + checksum + '\'' +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }

    /**
     * Create a new Monti key encoder.
     */
    public MontiKeyEncoder() {
        this.securityContext = new SecurityContext("MONTI_ENCODER_" + System.currentTimeMillis());
    }

    /**
     * Encode a derived key into squared Unicode representation.
     * @param key Key to encode
     * @return Encoded key or null on error
     */
    public EncodedKey encodeKey(DerivedKey key) {
        if (key == null) {
            Log.e(LOG_TAG, "Cannot encode null key");
            return null;
        }

        try {
            byte[] keyData = key.getKeyData();
            String encoded = encodeToSquaredUnicode(keyData);
            
            // Generate checksum
            byte[] checksumBytes = CryptoUtils.sha256(keyData);
            String checksum = CryptoUtils.bytesToHex(checksumBytes).substring(0, 16);
            
            // Create metadata
            Map<String, String> metadata = new HashMap<>();
            metadata.put("algorithm", key.getMetadata().getAlgorithm().toString());
            metadata.put("source", key.getMetadata().getSource().toString());
            metadata.put("length", String.valueOf(key.getKeyLengthBits()));
            metadata.put("version", String.valueOf(key.getMetadata().getVersion()));

            EncodedKey encodedKey = new EncodedKey(
                key.getKeyId(),
                encoded,
                checksum,
                System.currentTimeMillis(),
                metadata
            );

            securityContext.logOperation("Encoded key: " + key.getKeyId());
            Log.i(LOG_TAG, "Encoded key: " + key.getKeyId());

            return encodedKey;

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error encoding key", e);
            return null;
        }
    }

    /**
     * Encode bytes to squared Unicode representation.
     * Each byte is converted to a Unicode character from a specific range.
     * @param data Bytes to encode
     * @return Encoded Unicode string
     */
    private String encodeToSquaredUnicode(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length);
        
        for (byte b : data) {
            int value = b & 0xFF;
            // Map byte value to Unicode character
            char unicodeChar = (char) (UNICODE_BASE + (value % UNICODE_RANGE));
            sb.append(unicodeChar);
        }
        
        return sb.toString();
    }

    /**
     * Decode squared Unicode representation back to bytes.
     * @param encoded Encoded string
     * @return Decoded bytes or null on error
     */
    public byte[] decodeFromSquaredUnicode(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return null;
        }

        try {
            byte[] result = new byte[encoded.length()];
            
            for (int i = 0; i < encoded.length(); i++) {
                char c = encoded.charAt(i);
                int value = c - UNICODE_BASE;
                
                if (value < 0 || value >= UNICODE_RANGE) {
                    // Fallback for out-of-range characters - handle negative modulo properly
                    value = ((value % UNICODE_RANGE) + UNICODE_RANGE) % UNICODE_RANGE;
                }
                
                result[i] = (byte) (value & 0xFF);
            }
            
            return result;

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error decoding Unicode", e);
            return null;
        }
    }

    /**
     * Generate a secure key identifier.
     * @param prefix Identifier prefix
     * @param data Key data
     * @return Secure identifier
     */
    public String generateSecureIdentifier(String prefix, byte[] data) {
        byte[] hash = CryptoUtils.sha256(data);
        String hashHex = CryptoUtils.bytesToHex(hash).substring(0, 12);
        
        return prefix + "_" + hashHex + "_" + System.currentTimeMillis();
    }

    /**
     * Create network-safe key transmission format.
     * @param encodedKey Encoded key
     * @return Network-safe format string
     */
    public String createTransmissionFormat(EncodedKey encodedKey) {
        if (encodedKey == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("MONTI-KEY:v1\n");
        sb.append("ID:").append(encodedKey.keyId).append("\n");
        sb.append("CHECKSUM:").append(encodedKey.checksum).append("\n");
        sb.append("TIMESTAMP:").append(encodedKey.timestamp).append("\n");
        
        // Add metadata
        for (Map.Entry<String, String> entry : encodedKey.metadata.entrySet()) {
            sb.append("META:").append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        
        sb.append("DATA:").append(encodedKey.encodedData).append("\n");
        sb.append("END-MONTI-KEY\n");

        return sb.toString();
    }

    /**
     * Parse network-safe transmission format.
     * @param transmission Transmission format string
     * @return Encoded key or null on error
     */
    public EncodedKey parseTransmissionFormat(String transmission) {
        if (transmission == null || !transmission.startsWith("MONTI-KEY:")) {
            return null;
        }

        try {
            String[] lines = transmission.split("\n");
            String keyId = null;
            String checksum = null;
            long timestamp = 0;
            String encodedData = null;
            Map<String, String> metadata = new HashMap<>();

            for (String line : lines) {
                if (line.startsWith("ID:")) {
                    keyId = line.substring(3);
                } else if (line.startsWith("CHECKSUM:")) {
                    checksum = line.substring(9);
                } else if (line.startsWith("TIMESTAMP:")) {
                    timestamp = Long.parseLong(line.substring(10));
                } else if (line.startsWith("META:")) {
                    String metaLine = line.substring(5);
                    String[] parts = metaLine.split("=", 2);
                    if (parts.length == 2) {
                        metadata.put(parts[0], parts[1]);
                    }
                } else if (line.startsWith("DATA:")) {
                    encodedData = line.substring(5);
                }
            }

            if (keyId == null || encodedData == null || checksum == null) {
                Log.e(LOG_TAG, "Invalid transmission format: missing required fields");
                return null;
            }

            return new EncodedKey(keyId, encodedData, checksum, timestamp, metadata);

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error parsing transmission format", e);
            return null;
        }
    }

    /**
     * Encode multiple keys in batch.
     * @param keys List of keys to encode
     * @return List of encoded keys
     */
    public List<EncodedKey> encodeBatch(List<DerivedKey> keys) {
        List<EncodedKey> encoded = new ArrayList<>();

        if (keys == null || keys.isEmpty()) {
            return encoded;
        }

        for (DerivedKey key : keys) {
            EncodedKey encodedKey = encodeKey(key);
            if (encodedKey != null) {
                encoded.add(encodedKey);
            }
        }

        securityContext.logOperation("Batch encoded " + encoded.size() + " keys");
        Log.i(LOG_TAG, "Batch encoded " + encoded.size() + " keys");

        return encoded;
    }

    /**
     * Verify encoded key checksum.
     * @param encodedKey Encoded key to verify
     * @param originalData Original key data
     * @return true if checksum matches
     */
    public boolean verifyChecksum(EncodedKey encodedKey, byte[] originalData) {
        if (encodedKey == null || originalData == null) {
            return false;
        }

        byte[] checksumBytes = CryptoUtils.sha256(originalData);
        String calculatedChecksum = CryptoUtils.bytesToHex(checksumBytes).substring(0, 16);

        return calculatedChecksum.equals(encodedKey.checksum);
    }

    /**
     * Generate key summary report.
     * @param encodedKey Encoded key
     * @return Summary string
     */
    public String generateKeySummary(EncodedKey encodedKey) {
        if (encodedKey == null) {
            return "No key data";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Key ID: ").append(encodedKey.keyId).append("\n");
        sb.append("Checksum: ").append(encodedKey.checksum).append("\n");
        sb.append("Timestamp: ").append(encodedKey.timestamp).append("\n");
        sb.append("Encoded Length: ").append(encodedKey.encodedData.length()).append(" characters\n");
        sb.append("Metadata:\n");
        
        for (Map.Entry<String, String> entry : encodedKey.metadata.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        return sb.toString();
    }

    /**
     * Get the security context.
     * @return Security context
     */
    public SecurityContext getSecurityContext() {
        return securityContext;
    }

    /**
     * Convert encoded key to hex string for compatibility.
     * @param encodedKey Encoded key
     * @return Hex string representation
     */
    public String toHexString(EncodedKey encodedKey) {
        if (encodedKey == null) {
            return null;
        }

        byte[] decoded = decodeFromSquaredUnicode(encodedKey.encodedData);
        if (decoded == null) {
            return null;
        }

        return CryptoUtils.bytesToHex(decoded);
    }

    /**
     * Create a compact encoded format (for storage/transmission efficiency).
     * @param encodedKey Encoded key
     * @return Compact format string
     */
    public String createCompactFormat(EncodedKey encodedKey) {
        if (encodedKey == null) {
            return null;
        }

        return encodedKey.keyId + "|" + 
               encodedKey.checksum + "|" + 
               encodedKey.timestamp + "|" + 
               encodedKey.encodedData;
    }

    /**
     * Parse compact format.
     * @param compact Compact format string
     * @return Encoded key or null
     */
    public EncodedKey parseCompactFormat(String compact) {
        if (compact == null || compact.isEmpty()) {
            return null;
        }

        try {
            String[] parts = compact.split("\\|", 4);
            if (parts.length != 4) {
                return null;
            }

            String keyId = parts[0];
            String checksum = parts[1];
            long timestamp = Long.parseLong(parts[2]);
            String encodedData = parts[3];

            return new EncodedKey(keyId, encodedData, checksum, timestamp, new HashMap<>());

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error parsing compact format", e);
            return null;
        }
    }
}
