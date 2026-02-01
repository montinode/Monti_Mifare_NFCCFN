<!-- 
  MONTIAI.COM - Monti-Network-State Asset Integration
  
  Author: MONTIAI.COM (MONTINODE Development Team)
  Copyright: (c) 2025 JOHN CHARLES MONTI - All Rights Reserved
  License: GPL-3.0 with MONTIAI Network Requirements
  
  This documentation respects the principles of life, liberty, and property.
  For more information: JOHNCHARLESMONTI.COM
-->

# Wireless Security Module Documentation

## Overview

The Wireless Security Module enhances Monti_Mifare_NFCCFN with advanced security capabilities for deriving, managing, and encoding encryption keys from multiple sources including Bluetooth GATT communications and telephony data.

## Architecture

### Core Components

#### 1. **Models** (`de.syss.MifareClassicTool.security.models`)

- **DerivedKey**: Represents a derived encryption key with metadata
  - Supports AES-128, AES-256, DES, 3DES, and MIFARE Classic keys
  - Thread-safe with automatic memory clearing
  - Timestamped for audit trails

- **KeyMetadata**: Metadata associated with derived keys
  - Source tracking (GATT, Telephony, Hybrid)
  - Algorithm type information
  - Version support for key rotation
  - Extensible additional information map

- **SecurityContext**: Security context for operations
  - Operation logging with timestamps
  - Context locking mechanism
  - Age tracking for context expiration

#### 2. **Utilities** (`de.syss.MifareClassicTool.security.utils`)

- **CryptoUtils**: Cryptographic operations
  - AES-128/256 encryption/decryption
  - DES encryption/decryption
  - SHA-256, SHA-1, MD5 hashing
  - Secure random generation
  - Key generation helpers

- **KeyDerivationUtils**: Key derivation functions
  - Multi-factor key derivation
  - Key stretching (PBKDF2-like)
  - Context-aware derivation
  - MIFARE key derivation

#### 3. **Key Exchange & Derivation**

- **GattKeyExchange**: Bluetooth GATT key interception
  - Monitors characteristic read/write operations
  - Pattern detection for key exchange
  - Thread-safe with listener callbacks
  - Support for multiple concurrent monitors

- **TelephonyKeyDeriver**: Phone-based key derivation
  - IMEI-based key derivation
  - SIM card information extraction
  - Phone number utilization
  - Signal strength metrics
  - Multi-factor combination support

#### 4. **Management & Encoding**

- **WirelessSecurityManager**: Unified key management
  - Thread-safe key storage
  - Encrypt/decrypt operations
  - Key versioning and rotation
  - Comprehensive audit logging
  - Singleton pattern for global access

- **MontiKeyEncoder**: Key encoding for network transmission
  - Squared Unicode representation
  - Network-safe transmission format
  - Checksum verification
  - Batch encoding support

## Usage Examples

### Example 1: GATT Key Interception

```java
// Initialize GATT key exchange monitor
GattKeyExchange gattMonitor = new GattKeyExchange(context);

// Add listener for key events
gattMonitor.addListener(new GattKeyExchange.KeyExchangeListener() {
    @Override
    public void onKeyIntercepted(DerivedKey key) {
        Log.i(TAG, "Key intercepted: " + key.getKeyId());
        // Store the key
        WirelessSecurityManager.getInstance(context).storeKey(key);
    }
    
    @Override
    public void onKeyExchangeDetected(UUID characteristicUuid, byte[] data) {
        Log.d(TAG, "Key exchange pattern detected");
    }
    
    @Override
    public void onError(String error) {
        Log.e(TAG, "GATT error: " + error);
    }
});

// Start monitoring a connected GATT device
gattMonitor.startMonitoring(bluetoothGatt);

// Process characteristic reads/writes
gattMonitor.onCharacteristicRead(characteristic);
gattMonitor.onCharacteristicWrite(characteristic);

// Stop monitoring when done
gattMonitor.stopMonitoring();
gattMonitor.cleanup();
```

### Example 2: Telephony Key Derivation

```java
// Initialize telephony key deriver
TelephonyKeyDeriver telephony = new TelephonyKeyDeriver(context);

// Derive key from IMEI (requires READ_PHONE_STATE permission)
DerivedKey imeiKey = null;
try {
    imeiKey = telephony.deriveKeyFromIMEI(16); // 16 bytes = 128-bit AES
    
    // Use the key...
    if (imeiKey != null) {
        WirelessSecurityManager.getInstance(context).storeKey(imeiKey);
    }
} finally {
    // Always clear sensitive key data
    if (imeiKey != null) {
        imeiKey.clear();
    }
}

// Derive key from SIM card
DerivedKey simKey = telephony.deriveKeyFromSIM(32); // 32 bytes = 256-bit AES

// Derive multi-factor key combining all available sources
DerivedKey multiKey = telephony.deriveMultiFactorKey(
    true,  // use IMEI
    true,  // use SIM
    true,  // use phone number
    true,  // use signal strength
    16     // key length in bytes
);

// Inject random component for additional security
DerivedKey enhancedKey = telephony.injectRandomComponent(multiKey, 32); // 32 bits of randomness

// Store the key
WirelessSecurityManager.getInstance(context).storeKey(enhancedKey);
```

### Example 3: Key Management

```java
// Get the global security manager instance
WirelessSecurityManager securityManager = WirelessSecurityManager.getInstance(context);

// Store a key
boolean stored = securityManager.storeKey(derivedKey);

// Retrieve a key
DerivedKey retrievedKey = securityManager.retrieveKey("KEY_ID_HERE");

// Encrypt data with a stored key
byte[] plaintext = "Secret message".getBytes();
byte[] encrypted = securityManager.encrypt("KEY_ID_HERE", plaintext);

// Decrypt data
byte[] decrypted = securityManager.decrypt("KEY_ID_HERE", encrypted);

// Rotate a key (create new version)
DerivedKey newVersion = securityManager.rotateKey("OLD_KEY_ID");

// Get audit log
List<WirelessSecurityManager.SecurityOperation> auditLog = securityManager.getAuditLog();
String logReport = securityManager.exportAuditLog();

// Clear all keys when done
securityManager.clearAllKeys();
```

### Example 4: Key Encoding

```java
// Initialize encoder
MontiKeyEncoder encoder = new MontiKeyEncoder();

// Encode a derived key
MontiKeyEncoder.EncodedKey encoded = encoder.encodeKey(derivedKey);

// Create network-safe transmission format
String transmission = encoder.createTransmissionFormat(encoded);

// Or use compact format
String compact = encoder.createCompactFormat(encoded);

// Decode back
byte[] decoded = encoder.decodeFromSquaredUnicode(encoded.encodedData);

// Verify checksum
boolean valid = encoder.verifyChecksum(encoded, originalKeyData);

// Batch encode multiple keys
List<DerivedKey> keys = Arrays.asList(key1, key2, key3);
List<MontiKeyEncoder.EncodedKey> encodedKeys = encoder.encodeBatch(keys);
```

### Example 5: Complete Integration

```java
public class SecurityIntegration {
    private Context context;
    private WirelessSecurityManager securityManager;
    private GattKeyExchange gattMonitor;
    private TelephonyKeyDeriver telephonyDeriver;
    private MontiKeyEncoder encoder;
    
    public void initialize(Context context) {
        this.context = context;
        this.securityManager = WirelessSecurityManager.getInstance(context);
        this.gattMonitor = new GattKeyExchange(context);
        this.telephonyDeriver = new TelephonyKeyDeriver(context);
        this.encoder = new MontiKeyEncoder();
        
        setupGattListener();
    }
    
    private void setupGattListener() {
        gattMonitor.addListener(new GattKeyExchange.KeyExchangeListener() {
            @Override
            public void onKeyIntercepted(DerivedKey key) {
                // Store intercepted key
                securityManager.storeKey(key);
                
                // Encode for transmission
                MontiKeyEncoder.EncodedKey encoded = encoder.encodeKey(key);
                
                // Log the operation
                Log.i("Security", "Key intercepted and stored: " + key.getKeyId());
            }
            
            @Override
            public void onKeyExchangeDetected(UUID uuid, byte[] data) {
                // Analyze the data
                DerivedKey mifareKey = gattMonitor.deriveMifareKey(data);
                if (mifareKey != null) {
                    securityManager.storeKey(mifareKey);
                }
            }
            
            @Override
            public void onError(String error) {
                Log.e("Security", error);
            }
        });
    }
    
    public DerivedKey generateHybridKey() {
        // Derive telephony-based key
        DerivedKey telKey = telephonyDeriver.deriveMultiFactorKey(
            true, true, true, false, 16);
        
        // Inject random component
        if (telKey != null) {
            telKey = telephonyDeriver.injectRandomComponent(telKey, 32);
            securityManager.storeKey(telKey);
        }
        
        return telKey;
    }
    
    public void cleanup() {
        gattMonitor.cleanup();
        securityManager.clearAllKeys();
    }
}
```

## Required Permissions

Add these permissions to `AndroidManifest.xml`:

```xml
<!-- Bluetooth GATT -->
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

<!-- Telephony -->
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.READ_PHONE_NUMBERS" />

<!-- Network -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- Location (for signal strength) -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

## Security Considerations

### Critical Security Warnings

⚠️ **This module is designed for security research and analysis purposes. The following security considerations must be understood:**

1. **Telephony-Derived Keys Have Low Entropy**: Keys derived from IMEI, SIM, phone number, or signal strength are predictable and not suitable for production encryption. An attacker with access to this information can reconstruct your keys. Use Android Keystore for production key generation.

2. **DES is Cryptographically Broken**: DES encryption was deprecated in 1999 and can be broken in hours with modern hardware. It is included only for legacy system compatibility and analysis. **Never use DES for new implementations.** Use AES-256 instead.

3. **No Authenticated Encryption**: The current AES implementation uses CBC mode without HMAC authentication, making it vulnerable to padding oracle attacks and ciphertext manipulation. For production use, implement AES-GCM or add HMAC-SHA256 authentication.

4. **Memory Clearing is Best-Effort**: The JIT compiler may optimize away memory clearing operations. The `clear()` method provides best-effort security but cannot guarantee memory is wiped on all platforms.

5. **GATT Key Detection May Have False Positives**: The system assumes any GATT data of certain lengths (6, 16, 24, 32 bytes) is an encryption key. This may intercept non-key data. Validate intercepted keys before use.

6. **Android 12+ Bluetooth Permissions**: Apps must target API 31+ and request BLUETOOTH_CONNECT at runtime on Android 12+ devices. The legacy BLUETOOTH and BLUETOOTH_ADMIN permissions are restricted starting from Android 12.

### General Security Guidelines

1. **Key Storage**: Keys are stored in memory only. Consider using Android Keystore for persistent storage.

2. **Memory Clearing**: Sensitive data must be explicitly cleared by calling the `clear()` method on `DerivedKey` objects when done. Always clear keys in finally blocks or use try-with-resources patterns.

3. **Permission Handling**: Always check and request permissions at runtime for Android 6.0+.

4. **Audit Logging**: All key operations are logged. Review audit logs regularly.

5. **Key Rotation**: Implement regular key rotation using `WirelessSecurityManager.rotateKey()`.

6. **Thread Safety**: All components are thread-safe and can be used from multiple threads.

## Algorithm Support

- **AES-128**: 128-bit AES encryption
- **AES-256**: 256-bit AES encryption
- **DES**: 64-bit keys with 56-bit effective strength (8 parity bits)
- **3DES**: Triple DES encryption
- **MIFARE Classic**: 48-bit MIFARE keys

## Best Practices

1. Always explicitly clear sensitive data after use by calling `derivedKey.clear()`
2. Use try-finally blocks to ensure keys are cleared even on exceptions
3. Use multi-factor derivation when possible
4. Implement key rotation policies
5. Monitor audit logs for suspicious activity
6. Request permissions dynamically
7. Handle permission denials gracefully
8. Store keys securely using Android Keystore API when available
9. Use HTTPS for network transmission of encoded keys
10. Implement rate limiting for key derivation operations
11. Test thoroughly on multiple Android versions

## Troubleshooting

### Issue: Permission Denied Errors

**Solution**: Ensure all required permissions are declared in `AndroidManifest.xml` and requested at runtime.

```java
if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
    != PackageManager.PERMISSION_GRANTED) {
    ActivityCompat.requestPermissions(activity,
        new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE);
}
```

### Issue: Keys Not Persisting

**Solution**: The current implementation stores keys in memory only. Implement persistent storage using Android Keystore:

```java
// Store key in Android Keystore
KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
keyStore.load(null);
// Add key storage logic
```

### Issue: GATT Connection Failures

**Solution**: Ensure Bluetooth is enabled and device is properly paired:

```java
BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
if (!adapter.isEnabled()) {
    // Request to enable Bluetooth
}
```

## Performance Notes

- Key derivation operations are computationally intensive
- Use background threads for key derivation
- Limit the number of concurrent GATT monitors
- Clear unused keys regularly to free memory
- Consider caching frequently used keys

## Future Enhancements

1. Android Keystore integration for persistent key storage
2. Hardware security module (HSM) support
3. Key exchange protocol implementation
4. Biometric authentication integration
5. Cloud key management service integration
6. Advanced pattern recognition for GATT key exchange
7. Machine learning-based key prediction
8. Quantum-resistant cryptography support

## License

This module is part of Monti_Mifare_NFCCFN and is licensed under the GNU General Public License v3.0.

## Support

For issues, questions, or contributions, please refer to the main repository documentation.
