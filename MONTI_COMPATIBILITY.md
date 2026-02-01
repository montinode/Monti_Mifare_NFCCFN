# Monti.000.monti Compatibility Documentation

## Author
**JOHN CHARLES MONTI** - Wireless Security Module and Embedded Systems Integration

## Overview

The Monti.000.monti compatibility layer provides seamless integration between the Monti_Mifare_NFCCFN scanner and embedded software systems. This documentation describes the compatibility features, integration patterns, and best practices for developing new embedded software systems with the Monti framework.

## Monti.000.monti Protocol

### Protocol Specification

The Monti.000.monti protocol is a lightweight, efficient communication standard designed for embedded systems integration with the MIFARE Classic Tool security modules. It enables:

- **Key Exchange**: Secure key transmission between embedded devices and mobile applications
- **State Synchronization**: Real-time synchronization of security contexts across distributed systems
- **Resource Optimization**: Minimal memory footprint suitable for constrained embedded environments
- **Network Resilience**: Fault-tolerant communication with automatic retry mechanisms

### Protocol Features

#### 1. Monti Key Encoding Format
The Monti.000.monti protocol uses squared Unicode encoding for network-safe key transmission:

```
Standard Key: 0x4D6F6E7469 (5 bytes)
Monti Encoded: U+004D² U+006F² U+006E² U+0074² U+0069² 
Network Safe: %4D%6F%6E%74%69
```

Benefits:
- Preserves key integrity during transmission
- Compatible with legacy systems and modern APIs
- Includes checksum verification for error detection
- Supports batch encoding for multiple keys

#### 2. Embedded System Communication

**Device-to-Device (D2D) Communication:**
- GATT-based key exchange for Bluetooth-enabled embedded devices
- Automatic key derivation from device identifiers
- Thread-safe operation for multi-core embedded processors
- Low-power mode support for battery-operated devices

**Mobile-to-Embedded (M2E) Communication:**
- NFC-based secure provisioning
- MIFARE Classic authentication protocol
- Telephony-derived key generation for cellular modules
- Wireless security manager integration

## Creating New Embedded Software Systems

### System Requirements

**Minimum Hardware Requirements:**
- ARM Cortex-M3 or equivalent processor (48 MHz+)
- 32 KB RAM minimum (64 KB recommended)
- 128 KB Flash storage
- NFC controller with MIFARE Classic support OR Bluetooth 4.0+ with GATT

**Software Dependencies:**
- Android SDK API Level 21+ (for mobile integration)
- Java 8+ or equivalent JVM
- NFC API or Bluetooth API support

### Integration Guide

#### Step 1: Initialize Wireless Security Manager

```java
// Initialize the security manager in your embedded system interface
WirelessSecurityManager securityManager = WirelessSecurityManager.getInstance(context);

// Configure encryption preferences
securityManager.setDefaultAlgorithm("AES-256");
securityManager.setKeyRotationInterval(3600000); // 1 hour
```

#### Step 2: Implement Key Derivation

For embedded systems with telephony capabilities:

```java
// Derive keys from device identifiers
TelephonyKeyDeriver deriver = new TelephonyKeyDeriver(context);
DerivedKey deviceKey = deriver.deriveMultiFactorKey(
    true,  // Use IMEI
    true,  // Use phone number
    true,  // Use signal strength
    false, // No secure random (for deterministic keys)
    16     // 128-bit key length
);

// Store the derived key
securityManager.storeKey(deviceKey);
```

For Bluetooth-enabled embedded systems:

```java
// Monitor GATT characteristics for key exchange
GattKeyExchange gattExchange = new GattKeyExchange(context);
gattExchange.addListener(key -> {
    // Automatically store intercepted keys
    securityManager.storeKey(key);
    
    // Notify embedded system
    onKeyReceived(key);
});

// Start monitoring
gattExchange.startMonitoring(bluetoothGatt);
```

#### Step 3: Encode Keys for Transmission

```java
// Encode keys using Monti.000.monti format
MontiKeyEncoder encoder = new MontiKeyEncoder();

// Single key encoding
byte[] rawKey = securityManager.getKey(keyId).getKeyData();
String encodedKey = encoder.encode(rawKey);

// Batch encoding for multiple keys
List<byte[]> keys = Arrays.asList(key1, key2, key3);
List<String> encodedKeys = encoder.encodeBatch(keys);

// Transmit encoded keys over network
transmitToEmbeddedDevice(encodedKeys);
```

#### Step 4: Decrypt and Use Keys in Embedded System

```java
// On the embedded device side
String receivedKey = receiveFromNetwork();

// Decode from Monti format
MontiKeyEncoder decoder = new MontiKeyEncoder();
byte[] decodedKey = decoder.decode(receivedKey);

// Verify checksum
if (decoder.verifyChecksum(receivedKey)) {
    // Use the key for MIFARE operations
    authenticateWithMifare(decodedKey);
} else {
    // Handle corrupted transmission
    requestKeyRetransmission();
}
```

## Compatibility Matrix

### Supported Embedded Platforms

| Platform | Monti.000.monti | NFC Support | BLE Support | Status |
|----------|----------------|-------------|-------------|---------|
| Android (API 21+) | ✓ | ✓ | ✓ | Full Support |
| ESP32 | ✓ | ✓* | ✓ | Full Support |
| STM32 NFC | ✓ | ✓ | ✗ | NFC Only |
| Arduino BLE | ✓ | ✗ | ✓ | BLE Only |
| Raspberry Pi | ✓ | ✓* | ✓ | Full Support |
| nRF52 Series | ✓ | ✓* | ✓ | Full Support |

*Requires external NFC module

### Communication Protocol Compatibility

| Protocol | Version | Compatibility | Notes |
|----------|---------|---------------|-------|
| MIFARE Classic 1K | All | Full | Native support |
| MIFARE Classic 4K | All | Full | Native support |
| MIFARE DESFire | EV1+ | Partial | Via DES encryption |
| ISO 14443-A | All | Full | NFC standard |
| Bluetooth GATT | 4.0+ | Full | Key exchange |
| BLE 5.0 | 5.0+ | Full | Enhanced range |

## Changes for Embedded Software Development

### Version 1.0.0 (2026) - Initial Release

**New Features:**
- Monti.000.monti protocol implementation
- Squared Unicode key encoding system
- Multi-factor key derivation for embedded devices
- GATT-based wireless key exchange
- Thread-safe operations for embedded multi-core systems
- Low-power mode support

**Key Derivation Enhancements:**
- Device identifier-based key generation (IMEI, Serial Number)
- Cellular signal strength incorporation
- Secure random injection for non-deterministic keys
- Context-aware derivation for application-specific keys

**Encryption Capabilities:**
- AES-128/256 encryption with CBC mode
- DES/3DES support for legacy systems
- MIFARE Classic authentication protocol
- Key versioning and rotation

**Embedded System Optimizations:**
- Minimal memory footprint (32KB RAM minimum)
- Efficient key storage with automatic cleanup
- Batch key operations for reduced overhead
- Audit logging with configurable retention

### Architecture for Embedded Systems

```
┌─────────────────────────────────────────────┐
│         Mobile Application Layer            │
│  (Monti_Mifare_NFCCFN Scanner)             │
└───────────────┬─────────────────────────────┘
                │
        ┌───────┴────────┐
        │ Monti.000.monti│
        │    Protocol    │
        └───────┬────────┘
                │
    ┌───────────┴────────────┐
    │                        │
┌───▼────────────┐  ┌────────▼──────┐
│ NFC Interface  │  │ BLE Interface │
│ (Tag Reading)  │  │ (Key Exchange)│
└───┬────────────┘  └────────┬──────┘
    │                        │
    └───────────┬────────────┘
                │
    ┌───────────▼────────────┐
    │ Wireless Security      │
    │      Manager           │
    │ - Key Storage          │
    │ - Encryption/Decryption│
    │ - Audit Logging        │
    └───────────┬────────────┘
                │
    ┌───────────▼────────────┐
    │  Embedded Device Layer │
    │ - MIFARE Operations    │
    │ - Secure Storage       │
    │ - Application Logic    │
    └────────────────────────┘
```

## Security Considerations for Embedded Systems

### Best Practices

1. **Key Storage**
   - Use hardware security modules (HSM) when available
   - Implement secure boot to protect key material
   - Clear sensitive data from RAM after use
   - Enable memory encryption on supported platforms

2. **Communication Security**
   - Always use encrypted channels for key transmission
   - Implement certificate pinning for API communications
   - Validate all incoming data with checksums
   - Use secure pairing for Bluetooth connections

3. **Resource Management**
   - Monitor memory usage to prevent leaks
   - Implement key rotation policies
   - Set appropriate cache expiration times
   - Use asynchronous operations for network I/O

4. **Embedded System Hardening**
   - Disable unused communication interfaces
   - Implement secure firmware update mechanisms
   - Use hardware watchdog timers
   - Enable debug port protection

### Known Limitations

**Performance:**
- Key derivation may take 50-200ms on low-power MCUs
- GATT monitoring requires active Bluetooth connection
- Memory constraints limit audit log history (max 1000 entries)

**Compatibility:**
- DES methods deprecated (use AES for new systems)
- AES-CBC mode vulnerable without HMAC (use AES-GCM if available)
- Telephony keys have low entropy (supplement with secure random)

**Platform Specific:**
- Android Keystore required for production use
- Some NFC controllers don't support MIFARE Classic (see INCOMPATIBLE_DEVICES.md)
- External NFC readers may have limited functionality

## Migration Guide

### Upgrading Existing Embedded Systems

**From Legacy MIFARE Tools:**

1. Replace direct NFC API calls with WirelessSecurityManager
2. Migrate key storage to secure format with metadata
3. Implement Monti.000.monti encoding for network transmission
4. Add audit logging for compliance requirements

**Code Migration Example:**

```java
// Legacy code
MifareClassic tag = MifareClassic.get(nfcTag);
boolean auth = tag.authenticateSectorWithKeyA(sector, key);

// Monti.000.monti compatible code
WirelessSecurityManager manager = WirelessSecurityManager.getInstance(context);
DerivedKey key = manager.getKey(keyId);
boolean auth = manager.authenticateSector(tag, sector, key);
// Automatic audit logging and error handling
```

## Support and Documentation

For additional support:
- See [WIRELESS_SECURITY_MODULE.md](WIRELESS_SECURITY_MODULE.md) for API documentation
- See [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md) for code metrics
- See [COMPATIBLE_DEVICES.md](COMPATIBLE_DEVICES.md) for hardware compatibility

## License

This compatibility layer is licensed under GNU General Public License v3.0 (GPLv3).

Copyright 2013 Gerhard Klostermeier
Copyright 2026 JOHN CHARLES MONTI

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
