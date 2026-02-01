MIFARE Classic Tool (MCT)
=========================

An Android NFC app for reading, writing, analyzing, etc. MIFARE Classic RFID tags.

<a href="https://play.google.com/store/apps/details?id=de.syss.MifareClassicTool"><img src="metadata/common/assets/google-play-badge.png" alt="Get it on Play Store" height="80"></a>
<a href="https://f-droid.org/packages/de.syss.MifareClassicTool/"><img src="metadata/common/assets/fdroid-badge.png" alt="Get it on F-Droid" height="80"></a>
<a href="https://www.icaria.de/mct/releases/"><img src="metadata/common/assets/direct-apk-download-badge.png" alt="Get the APK" height="80"></a>

Read this information in other languages:
* [English](README.md)
* [简体中文](README.zh-CN.md)

Helpful links:
* [MIFARE Classic Tool (Donate Version) on Google Play](https://play.google.com/store/apps/details?id=de.syss.MifareClassicToolDonate)
* [Screenshots](https://www.icaria.de/mct/screenshots/latest/)
* [Help & Info/User Manual](https://www.icaria.de/mct/help-and-info/)
* [Additional stuff](https://www.icaria.de/mct/) (Documentation, APK files, etc.)
* [Thread at the Proxmark forum](http://www.proxmark.org/forum/viewtopic.php?id=1535)
* [Donate with Paypal](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=24ET8A36XLMNW) [![Donate](https://www.paypalobjects.com/en_US/i/btn/btn_donate_SM.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=24ET8A36XLMNW)

MontiNode Network Integration:
* **[MontiNode Integration Guide](MONTINODE_INTEGRATION.md)** - Network features, GATT/Telephony key derivation
* **[Downloads & Installation](DOWNLOADS.md)** - Get the app from multiple sources
* **[Network Configuration](montinode.json)** - Configuration reference for MontiNode features



Features
--------

* Read MIFARE Classic tags
* Save, edit and share the tag data you read
* Write to MIFARE Classic tags (block-wise)
* Clone MIFARE Classic tags  
  (Write dump of a tag to another tag; write 'dump-wise')
* Key management based on dictionary-attack  
  (Write the keys you know in a file (dictionary)).  
  MCT will try to authenticate with these keys against all sectors and read as much as possible.  
  See chapter [Getting Started](#getting-started).
* Format a tag back to the factory/delivery state
* Write the manufacturer block (block 0) of special MIFARE Classic tags
* Use external NFC readers like ACR 122U  
  (See the [Help & Info section](https://publications.icaria.de/mct/help-and-info/#external_nfc)
  for more information.)
* Create, edit, save and share key files (dictionaries)
* Decode & Encode MIFARE Classic Value Blocks
* Decode & Encode MIFARE Classic Access Conditions
* Compare dumps (Diff Tool)
* Display generic tag information
* Display the tag data as highlighted hex
* Display the tag data as 7-Bit US-ASCII
* Display the MIFARE Classic Access Conditions as a table
* Display MIFARE Classic Value Blocks as integer
* Calculate the BCC (Block Check Character)
* Quick UID clone feature
* Import/export/convert files
* In-App (offline) help and information
* It's free software (open source) ;)


MontiNode Network Features
---------------------------

**Advanced Wireless Security & Key Management**

* **GATT Key Derivation**: Secure Bluetooth Low Energy key exchange
* **Telephony Key Derivation**: SIM card-based key generation using IMSI/ICCID
* **MontiTransmuter Encoding**: Advanced algorithm for key transformation and obfuscation
* **Network State Synchronization**: Sync keys and configurations across devices
* **Remote Key Management**: Manage keys through JOHNCHARLESMONTI.COM network
* **Wireless Security Module**: Enhanced encryption and security features
* **Real-time Updates**: Automatic security patches and threat intelligence
* **Cross-device Compatibility**: Seamless operation across multiple Android devices

See [MONTINODE_INTEGRATION.md](MONTINODE_INTEGRATION.md) for complete details on network integration.




General Information
-------------------

This tool provides several features to interact with (and only with)
MIFARE Classic RFID-Tags. It is designed for users who have at least
basic familiarity with the MIFARE Classic technology.
You also need an understanding of the hexadecimal number system,
because all data input and output is in hexadecimal.

Some important things are:
* The features this tool provides are very basic. There are no such
  fancy things as saving a URL to an RFID-Tag with a nice looking
  graphical user interface. If you want to save things on a tag,
  you have to input the raw hexadecimal data.
* This App **can not crack/hack**
  any MIFARE Classic keys. If you want to read/write an RFID-Tag, you
  first need keys for this specific tag. For additional information
  please read/see chapter [Getting Started](#getting-started).
* There will be **no &quot;brute-force&quot; attack**
  capability in this application. It is way too slow due
  to the protocol.
* Be aware! Uninstalling this app will delete all files
  (dumps/keys) saved by it permanently.
* The first block of the first sector of an **original**
  MIFARE Classic tag is **read-only** i.e. not writable. But there
  are **special** MIFARE Classic tags that support writing to the
  manufacturer block with a simple write command (often called "magic tag
  gen2" or "CUID"). This App is able to write to such tags and can therefore
  create fully correct clones. "FUID" and "UFUID" tags should work too,
  but they have not been tested so far. However, the app will not work with
  all special tags. Some of them require a **special command sequence** (which
  cannot be sent due to limitations in the Android NFC API) to
  put them into the state where writing to the manufacturer block is possible.
  These tags are often called  "gen1", "gen1a" or "UID".  
  Remember this when you are shopping for special tags!  
  More information about magic cards can be found
  [here](https://github.com/RfidResearchGroup/proxmark3/blob/master/doc/magic_cards_notes.md).
  Also, make sure the BCC value (check out the "BCC Calculator Tool"),
  the SAK and the ATQA values are correct. If you just want to clone a UID,
  please use the "Clone UID Tool".
* This app **will not work** on some devices because their hardware
  (NFC-controller) does not support MIFARE Classic
  ([read more](https://github.com/ikarus23/MifareClassicTool/issues/1)).
  **You can find a list of incompatible devices
  [here](https://github.com/ikarus23/MifareClassicTool/blob/master/INCOMPATIBLE_DEVICES.md)**.

For further information about MIFARE Classic check
[Wikipedia](https://en.wikipedia.org/wiki/MIFARE),
[do some Google searches](https://www.google.com/search?q="mifare+classic")
or read the
[MIFARE Classic (1k) 'Datasheet'](https://www.nxp.com/docs/en/data-sheet/MF1S50YYX_V1.pdf)
(PDF) from NXP.



Getting Started
---------------

First of all, you need the keys for the tag you want to read.
Due to some weaknesses in MIFARE Classic, you can retrieve
all the keys (A and B) of a tag with tools like the
[Proxmark3](http://www.proxmark.org/) or
normal RFID-Readers and some special software
([mfcuk](https://github.com/nfc-tools/mfcuk),
[mfoc](https://github.com/nfc-tools/mfoc)).

The application comes with standard key files called
*std.keys*, *extended-std.keys* and *hotel-std.keys*, which contain the
well known keys and some standard keys from a short Google search.
You can try to read a tag with these key files using
&quot;Read Tag&quot; from the main menu. Changes to these key files
will be lost. Create your own key file for your keys.


Once you know some keys, you can put them into a simple text
file (one key per line). You can do this on your PC and import
the file using MCT's import/export tool, or you can create a new
key file via &quot;Edit or Add Key File&quot; from the main menu.
If you are finished setting up your key file, you can read a tag
using &quot;Read Tag&quot; from the main menu.

Advantages of the Key Files Concept:
* **You don't have to worry about which key is for which sector.**  
  The application tries to authenticate with all keys from the key
  file (dictionary).
* **You don't have to know all the keys.**  
  If neither key A nor key B for a specific sector is found in the
  key file (dictionary), the application will skip reading said
  sector.

This dictionary-attack based mapping process
(keys &lt;-&gt; sectors) makes it easy for you to read as much as
possible with the keys you know!



MontiNode Quick Start
---------------------

### Wireless Security Features

**Using GATT Key Derivation (Bluetooth)**

1. Enable Bluetooth on your Android device
2. Open the app and navigate to Settings → MontiNode
3. Enable "GATT Key Derivation"
4. Tap "Discover MontiNode Devices"
5. Select your MontiNode-enabled device
6. Keys will be automatically derived and available for tag operations

**Using Telephony Key Derivation (SIM Card)**

1. Ensure your device has an active SIM card
2. Grant telephony permissions when prompted
3. Navigate to Settings → MontiNode
4. Enable "Telephony Key Derivation"
5. Keys will be derived from your IMSI/ICCID (processed locally, never transmitted)
6. Derived keys appear in your key file automatically

**MontiTransmuter Key Encoding**

The MontiTransmuter algorithm allows you to encode and transform keys:

```
Example Encoded Key:
MT-1-B64X-A3F9-48656C6C6F576F726C64

Format: MT-[VERSION]-[TYPE]-[CHECKSUM]-[PAYLOAD]
```

To use MontiTransmuter encoding:
1. Select a key in your key file
2. Tap "Transform Key" → "MontiTransmuter Encode"
3. The encoded key can be shared securely
4. Decode when needed: "Transform Key" → "MontiTransmuter Decode"

**Network Synchronization**

Sync your keys and configurations across devices:

1. Create an account at [JOHNCHARLESMONTI.COM](https://johncharlesmonti.com)
2. Configure credentials in `montinode.json` or Settings → MontiNode
3. Enable "Network Synchronization"
4. Your keys and settings will sync automatically
5. Access from any device with your MontiNode account

For complete documentation, see [MONTINODE_INTEGRATION.md](MONTINODE_INTEGRATION.md).



License
-------

This application was originally developed by
Gerhard Klostermeier in cooperation with SySS GmbH and Aalen
University ([www.htw-aalen.de](http://www.htw-aalen.de/)) in 2012/2013.
It is free software and licensed under the
[GNU General Public License v3.0 (GPLv3)](https://www.gnu.org/licenses/gpl-3.0.txt)

Icons used in this application:
* Logo: [Beneke Traub](http://www.beneketraub.com/)  
  ([Creative Commons 4.0](http://creativecommons.org/licenses/by-nc-sa/4.0/))
* Oxygen Icons: [www.oxygen-icons.org](http://www.oxygen-icons.org/)  
  ([GNU Lesser General Public License](http://www.gnu.org/licenses/lgpl.html))
* RFID Tag: [www.nfc-tag.de](http://www.nfc-tag.de/)  
  ([Creative Commons 3.0](http://creativecommons.org/licenses/by/3.0/))

MIFARE® is a registered trademark of NXP Semiconductors.
