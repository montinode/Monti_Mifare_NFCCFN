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

import java.util.HashMap;
import java.util.Map;

/**
 * Metadata associated with a derived key.
 * @author JOHNCHARLESMONTI
 */
public class KeyMetadata {
    public enum KeySource {
        GATT,
        TELEPHONY,
        HYBRID,
        MANUAL,
        GENERATED
    }

    public enum AlgorithmType {
        AES_128,
        AES_256,
        DES,
        TRIPLE_DES,
        MIFARE_CLASSIC
    }

    private final KeySource source;
    private final AlgorithmType algorithm;
    private final String description;
    private final Map<String, String> additionalInfo;
    private final int version;

    /**
     * Create key metadata.
     * @param source Source of the key
     * @param algorithm Algorithm type
     * @param description Human-readable description
     * @param version Version number for key rotation
     */
    public KeyMetadata(KeySource source, AlgorithmType algorithm, String description, int version) {
        this.source = source;
        this.algorithm = algorithm;
        this.description = description;
        this.version = version;
        this.additionalInfo = new HashMap<>();
    }

    /**
     * Get the key source.
     * @return Key source type
     */
    public KeySource getSource() {
        return source;
    }

    /**
     * Get the algorithm type.
     * @return Algorithm type
     */
    public AlgorithmType getAlgorithm() {
        return algorithm;
    }

    /**
     * Get the description.
     * @return Description string
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the version number.
     * @return Version number
     */
    public int getVersion() {
        return version;
    }

    /**
     * Add additional information.
     * @param key Info key
     * @param value Info value
     */
    public void addInfo(String key, String value) {
        additionalInfo.put(key, value);
    }

    /**
     * Get additional information.
     * @param key Info key
     * @return Info value or null
     */
    public String getInfo(String key) {
        return additionalInfo.get(key);
    }

    /**
     * Get all additional info.
     * @return Map of additional info
     */
    public Map<String, String> getAllInfo() {
        return new HashMap<>(additionalInfo);
    }

    @Override
    public String toString() {
        return "KeyMetadata{" +
                "source=" + source +
                ", algorithm=" + algorithm +
                ", description='" + description + '\'' +
                ", version=" + version +
                ", additionalInfo=" + additionalInfo +
                '}';
    }
}
