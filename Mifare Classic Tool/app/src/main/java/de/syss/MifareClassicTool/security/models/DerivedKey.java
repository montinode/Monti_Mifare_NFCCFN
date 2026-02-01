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

package de.syss.MifareClassicTool.security.models;

import java.util.Arrays;

/**
 * Represents a derived encryption key with metadata.
 * Keys can be derived from GATT, telephony, or other sources.
 * @author Monti Security Team
 */
public class DerivedKey {
    private final byte[] keyData;
    private final String keyId;
    private final KeyMetadata metadata;
    private final long timestamp;

    /**
     * Create a new derived key.
     * @param keyData The raw key bytes
     * @param keyId Unique identifier for this key
     * @param metadata Additional metadata about the key
     */
    public DerivedKey(byte[] keyData, String keyId, KeyMetadata metadata) {
        if (keyData == null || keyData.length == 0) {
            throw new IllegalArgumentException("Key data cannot be null or empty");
        }
        if (keyId == null || keyId.isEmpty()) {
            throw new IllegalArgumentException("Key ID cannot be null or empty");
        }
        
        this.keyData = Arrays.copyOf(keyData, keyData.length);
        this.keyId = keyId;
        this.metadata = metadata;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Get the key data (returns a copy for security).
     * @return Copy of the key bytes
     */
    public byte[] getKeyData() {
        return Arrays.copyOf(keyData, keyData.length);
    }

    /**
     * Get the key ID.
     * @return The unique key identifier
     */
    public String getKeyId() {
        return keyId;
    }

    /**
     * Get the key metadata.
     * @return The metadata object
     */
    public KeyMetadata getMetadata() {
        return metadata;
    }

    /**
     * Get the timestamp when this key was created.
     * @return Timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Get the key length in bits.
     * @return Key length
     */
    public int getKeyLengthBits() {
        return keyData.length * 8;
    }

    /**
     * Clear the key data from memory.
     * Call this explicitly when done with the key.
     * 
     * Note: This provides best-effort memory clearing. The JIT compiler may optimize
     * away the clearing operation. For maximum security, consider using additional
     * techniques or relying on Android Keystore for key storage.
     */
    public void clear() {
        Arrays.fill(keyData, (byte) 0);
    }

    @Override
    public String toString() {
        return "DerivedKey{" +
                "keyId='" + keyId + '\'' +
                ", length=" + getKeyLengthBits() + " bits" +
                ", timestamp=" + timestamp +
                ", metadata=" + metadata +
                '}';
    }
}
