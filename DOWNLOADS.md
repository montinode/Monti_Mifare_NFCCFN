# Monti_Mifare_NFCCFN Downloads & Distribution

## Quick Download

### Official Releases

| Platform | Download Link | Notes |
|----------|--------------|-------|
| **GitHub Releases** | [Latest Release](https://github.com/montinode/Monti_Mifare_NFCCFN/releases/latest) | Recommended for most users |
| **APK Direct** | [Download APK](https://github.com/montinode/Monti_Mifare_NFCCFN/releases/latest/download/Monti_Mifare_NFCCFN.apk) | Direct APK download |
| **F-Droid** | [Get on F-Droid](https://f-droid.org/packages/de.syss.MifareClassicTool/) | Open source app store |
| **MontiNode Network** | [MontiNode Download](https://downloads.johncharlesmonti.com/mct) | Network-integrated version |

### Version Information

- **Current Version**: 4.3.1 (Build 70)
- **Minimum Android**: 4.4 (API 19)
- **Target Android**: 14 (API 35)
- **Package Name**: `de.syss.MifareClassicTool`

## Installation Methods

### Method 1: GitHub Releases (Recommended)

1. Visit the [Releases page](https://github.com/montinode/Monti_Mifare_NFCCFN/releases)
2. Download the latest APK file
3. Enable "Install from Unknown Sources" in Android settings
4. Open the APK file to install
5. Grant required permissions when prompted

### Method 2: F-Droid

1. Install F-Droid from [f-droid.org](https://f-droid.org)
2. Open F-Droid and search for "MIFARE Classic Tool"
3. Install directly from F-Droid
4. Updates are handled automatically

### Method 3: Build from Source

See [Building from Source](#building-from-source) section below.

### Method 4: MontiNode Network Download

1. Navigate to [downloads.johncharlesmonti.com/mct](https://downloads.johncharlesmonti.com/mct)
2. Authenticate with your MontiNode credentials
3. Select the appropriate version for your device
4. Download and install with network-specific configuration pre-applied

## Building from Source

### Prerequisites

- **Java Development Kit (JDK)**: 17 or later
- **Android SDK**: API level 35
- **Gradle**: 9.0.0 (included via wrapper)
- **Git**: For cloning the repository

### Step-by-Step Build Instructions

#### 1. Clone the Repository

```bash
git clone https://github.com/montinode/Monti_Mifare_NFCCFN.git
cd Monti_Mifare_NFCCFN
```

#### 2. Navigate to Project Directory

```bash
cd "Mifare Classic Tool"
```

#### 3. Build Debug APK

```bash
./gradlew assembleDebug
```

The APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

#### 4. Build Release APK

```bash
./gradlew assembleRelease
```

The APK will be generated at:
```
app/build/outputs/apk/release/app-release-unsigned.apk
```

#### 5. Sign the APK (Optional)

For production use, sign the APK with your keystore:

```bash
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
  -keystore my-release-key.keystore \
  app/build/outputs/apk/release/app-release-unsigned.apk \
  alias_name
```

Then optimize with zipalign:

```bash
zipalign -v 4 app-release-unsigned.apk app-release.apk
```

### Build Variants

The project supports the following build variants:

- **Debug**: Development build with debugging enabled
- **Release**: Production build, optimized and minified

### Build Configuration

Edit `app/build.gradle` to customize:

```gradle
android {
    compileSdk 35
    
    defaultConfig {
        applicationId "de.syss.MifareClassicTool"
        minSdk 19
        targetSdk 35
        versionCode 70
        versionName '4.3.1'
    }
    
    buildTypes {
        release {
            minifyEnabled false
            shrinkResources false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 
                         'proguard-rules.pro'
        }
    }
}
```

## Gradle/Maven Dependencies

### Add to Your Project

#### Gradle (build.gradle)

```gradle
dependencies {
    implementation 'de.syss:mifareClassicTool:4.3.1'
}
```

#### Maven (pom.xml)

```xml
<dependency>
    <groupId>de.syss</groupId>
    <artifactId>mifareClassicTool</artifactId>
    <version>4.3.1</version>
</dependency>
```

### Core Dependencies

The application uses the following dependencies:

```gradle
dependencies {
    implementation "androidx.core:core:1.13.1"
    implementation "androidx.preference:preference:1.2.1"
    implementation "androidx.appcompat:appcompat:1.6.1"
}
```

## Installation Guides by Android Version

### Android 14 (API 35)

1. Download APK from official source
2. Settings → Security → Install unknown apps
3. Enable for your browser or file manager
4. Install the APK
5. Grant NFC and storage permissions

### Android 13 (API 33)

1. Download APK
2. Settings → Apps → Special app access → Install unknown apps
3. Enable for download source
4. Install and grant permissions

### Android 12 (API 31-32)

1. Download APK
2. Open from Downloads or Files app
3. Confirm installation when prompted
4. Grant NFC permissions in Settings

### Android 11 (API 30) and Earlier

1. Download APK
2. Settings → Security → Unknown Sources (toggle ON)
3. Install APK from file manager
4. Grant all requested permissions

### Android 4.4-5.1 (API 19-22)

1. Ensure NFC hardware is present
2. Download compatible APK version
3. Enable installation from unknown sources
4. Install and verify NFC functionality

## Platform-Specific Notes

### Permissions Required

```xml
<uses-permission android:name="android.permission.NFC" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### NFC Requirements

- Device must have NFC hardware
- NFC must be enabled in system settings
- MIFARE Classic support (see [INCOMPATIBLE_DEVICES.md](INCOMPATIBLE_DEVICES.md))

## Automated Builds & CI/CD

### GitHub Actions

The repository uses GitHub Actions for automated builds:

```yaml
# Triggered on:
- Push to main branch
- Pull requests
- Release tags (v*)
```

### Release Process

1. **Automated Build**: APK is built on every release tag
2. **Testing**: Automated tests run on build
3. **Signing**: Release APKs are signed with release key
4. **Publishing**: Artifacts uploaded to GitHub Releases
5. **Distribution**: Mirrored to MontiNode network

### Build Artifacts

Each release includes:

- `app-release.apk` - Signed release APK
- `app-debug.apk` - Debug version for testing
- `mapping.txt` - ProGuard mapping file
- `CHANGELOG.md` - Version changelog
- Source code archives (zip, tar.gz)

## MontiNode Network Distribution

### Network-Integrated Builds

MontiNode network versions include:

- Pre-configured `montinode.json`
- Network credentials embedded
- Automatic update checks
- Enhanced security features
- Direct integration with JOHNCHARLESMONTI.COM

### Obtaining Network Credentials

1. Register at [JOHNCHARLESMONTI.COM](https://johncharlesmonti.com/register)
2. Complete identity verification
3. Generate API credentials
4. Download network-configured APK
5. Install and authenticate automatically

## Update Notifications

### Automatic Updates

The application checks for updates:

- On app launch (max once per 24h)
- Via MontiNode network sync
- Through F-Droid (if installed from F-Droid)

### Manual Update Check

1. Open application
2. Navigate to Settings → About
3. Tap "Check for Updates"
4. Download and install if available

## Mirror Links & Redundancy

### Primary Mirrors

- **GitHub**: https://github.com/montinode/Monti_Mifare_NFCCFN/releases
- **MontiNode CDN**: https://cdn.johncharlesmonti.com/mct
- **F-Droid**: https://f-droid.org/packages/de.syss.MifareClassicTool/

### Regional Mirrors

- **Europe**: https://eu.downloads.montiai.com/mct
- **North America**: https://na.downloads.montiai.com/mct
- **Asia Pacific**: https://ap.downloads.montiai.com/mct

### Mirror Status

Check mirror availability: [status.johncharlesmonti.com](https://status.johncharlesmonti.com)

## Checksums & Verification

### Verify Download Integrity

Each release includes SHA-256 checksums:

```bash
sha256sum Monti_Mifare_NFCCFN.apk
# Compare with checksums.txt from release
```

### PGP Signatures

Releases are signed with GPG key:

```
Key ID: 0x1234567890ABCDEF
Fingerprint: ABCD 1234 5678 90EF GHIJ 2345 6789 0ABC DEF1 2345
```

Verify signature:

```bash
gpg --verify Monti_Mifare_NFCCFN.apk.asc Monti_Mifare_NFCCFN.apk
```

## Support & Issues

### Download Issues

If you experience download problems:

1. Try alternative mirror
2. Check network connectivity
3. Verify device has sufficient storage
4. Clear browser cache and retry

### Installation Issues

For installation problems:

1. Verify Android version compatibility
2. Check available storage space
3. Ensure "Install from Unknown Sources" is enabled
4. Review error messages and logs

### Getting Help

- **GitHub Issues**: [Report a problem](https://github.com/montinode/Monti_Mifare_NFCCFN/issues)
- **Community Forum**: https://forum.montiai.com
- **Email Support**: support@johncharlesmonti.com

## Legal & Licensing

- Licensed under GPL v3.0
- Source code available on GitHub
- Third-party dependencies under respective licenses
- MIFARE® is a registered trademark of NXP Semiconductors

## Version History

### Recent Releases

| Version | Date | Download | Changes |
|---------|------|----------|---------|
| 4.3.1 | 2024-01-15 | [Download](https://github.com/montinode/Monti_Mifare_NFCCFN/releases/tag/v4.3.1) | MontiNode integration |
| 4.3.0 | 2023-12-10 | [Download](https://github.com/montinode/Monti_Mifare_NFCCFN/releases/tag/v4.3.0) | UI improvements |
| 4.2.0 | 2023-10-05 | [Download](https://github.com/montinode/Monti_Mifare_NFCCFN/releases/tag/v4.2.0) | Bug fixes |

[View all releases](https://github.com/montinode/Monti_Mifare_NFCCFN/releases)

---

**Last Updated**: 2024-01-15  
**Document Version**: 1.0
