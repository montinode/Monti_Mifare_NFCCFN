# MONTI-NFC // CLASSIC TOOL
### PHYSICAL LAYER INTERCEPTION & REPLICATION SUITE

![MontiNode Status](https://img.shields.io/badge/SYSTEM-ONLINE-brightgreen?style=for-the-badge)
![Protocol](https://img.shields.io/badge/PROTOCOL-MIFARE_CLASSIC-blue?style=for-the-badge)
![License](https://img.shields.io/badge/LICENSE-GPLv3-red?style=for-the-badge)

<a href="https://play.google.com/store/apps/details?id=de.syss.MifareClassicTool"><img src="metadata/common/assets/google-play-badge.png" alt="Download from Play Store" height="80"></a>
<a href="https://f-droid.org/packages/de.syss.MifareClassicTool/"><img src="metadata/common/assets/fdroid-badge.png" alt="Download from F-Droid" height="80"></a>
<a href="https://www.icaria.de/mct/releases/"><img src="metadata/common/assets/direct-apk-download-badge.png" alt="Direct APK Download" height="80"></a>

**ACCESS THE PHYSICAL SUBSTRATE.**
This Android terminal allows for the reading, writing, analysis, and cloning of MIFARE Classic RFID assets. It serves as the bridge between the **MontiNode Network** and physical access tokens.

---

## ⚿ SYSTEM CAPABILITIES

*   **Asset Extraction:** Read data from MIFARE Classic tags (Sectors & Blocks).
*   **Payload Injection:** Write data to tags block-wise.
*   **Token Replication:** Clone tags (Dump-to-Tag) or Clone UIDs (requires "Magic" Gen2/CUID cards).
*   **Dictionary Attack Vector:**
    *   Utilizes a `keys` file (Dictionary) to attempt authentication against all sectors.
    *   *Note: This is not a brute-force cracker. It is a key-mapping tool.*
*   **Sector Analysis:** Decode & Encode Value Blocks and Access Conditions.
*   **Data Visualization:**
    *   Highlighted Hexadecimal Output.
    *   7-Bit US-ASCII decoding.
*   **Diff Tool:** Compare two dumps to identify changes in the data structure.
*   **Hardware Bridge:** Supports external NFC readers (e.g., ACR 122U).

---

## ⚠ OPERATIONAL INTELLIGENCE (READ BEFORE USE)

### 1. NO BRUTE FORCE CAPABILITY
This tool **CANNOT** crack unknown keys via brute force. The protocol is too slow. To interact with a secured tag, you must already possess the keys.
*   *Recommended Extraction Vector:* Use **Proxmark3** or **LibNFC** hardware to recover keys, then import them into MONTI-NFC.

### 2. HARDWARE COMPATIBILITY
*   **Device Support:** Not all Android chipsets support MIFARE Classic. [Check the Incompatible Device List](https://github.com/ikarus23/MifareClassicTool/blob/master/INCOMPATIBLE_DEVICES.md).
*   **Magic Tags:** To write to Block 0 (Manufacturer Block/UID), you need **Gen2 (CUID)** tags.
    *   *Gen1 (UID)* tags require a special wake-up command sequence not supported by standard Android APIs.

### 3. DATA PERSISTENCE
Uninstalling this application will **PURGE** all saved dumps and key files. Ensure you export your `state-data` to the MontiNode server or local storage before removal.

---

## ⌬ INITIATION SEQUENCE

### PHASE 1: KEY ACQUISITION
You cannot read a sector without the correct Key A or Key B.
1.  Use the included `std.keys` (Standard Default Keys) for low-security targets.
2.  For high-value targets, crack the keys using external hardware.
3.  Import your keys into the **Edit/Add Key File** menu.

### PHASE 2: MAPPING
1.  Select **"Read Tag"**.
2.  Choose your Key Dictionary.
3.  Map the keys to the sectors. The app will attempt to authenticate every sector against every key in your list.

### PHASE 3: REPLICATION
1.  Once a dump is secured, use **"Write Tag"**.
2.  Select the dump and the target tag.
3.  **WARNING:** Ensure Access Bits (Trailer Block) are correct. Writing bad Access Bits will permanently lock the sector (Brick the tag).

---

## ⁂ RESOURCES & LINKS

*   **Original Source:** [MifareClassicTool by ikarus23](https://github.com/ikarus23/MifareClassicTool)
*   **Proxmark Forum:** [Thread ID 1535](http://www.proxmark.org/forum/viewtopic.php?id=1535)
*   **Magic Card Notes:** [RfidResearchGroup Docs](https://github.com/RfidResearchGroup/proxmark3/blob/master/doc/magic_cards_notes.md)

---

## ⚖ LEGAL & LICENSE

**ORIGINAL AUTHOR:** Gerhard Klostermeier (2012-2023)
**LICENSE:** GNU General Public License v3.0 (GPLv3)

*This software is for educational and diagnostic purposes. JOHNCHARLESMONTI and MONTINODE assume no liability for the misuse of this tool within the Tektronic Grid.*

**[JOHNCHARLESMONTI.COM](http://johncharlesmonti.com)**
