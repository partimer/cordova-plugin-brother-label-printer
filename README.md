# Cordova Brother Label Printer Plugin

This is a specialized version of Menelic Software's version of the plugin.

Refactored to be usable with only the RJ-2150 brother label printer on a custom roll size (die-cut 57x32mm)

## Target mobile printers:
```
PocketJet PJ-722, PJ-723, PJ-762, PJ-763, PJ-763MFi, PJ-773
PocketJet PJ-622, PJ-623, PJ-662, PJ-663
PocketJet PJ-520, PJ-522, PJ-523, PJ-560, PJ-562, PJ-563
MPrint MW-145MFi, MW-260MFi
MPrint MW-140BT, MW-145BT, MW-260
RJ-4030Ai, RJ-4030, RJ-4040
TD-2020, TD-2120N, TD-2130N, TD-4000, TD-4100N
QL-710W, QL-720NW
PT-E550W, PT-P750W
RJ-3050, RJ-3150
PT-E800W, PT-D800W, PT-E850TKW
PT-P900W, PT-P950NW
```

__Tested models:__ `RJ-2150`

## Usage

See here for JS interfaces to the plugin: `www/printer.js`

There are six available methods... 

* [findNetworkPrinters(success, failure)](#findnetworkprinters)
* [findBluetoothPrinters(success, failure)](#findbluetoothprinters)
* [findPrinters(success, failure)](#findprinters)
* [setPrinter(printer, success, failure)](#setprinter)
* [printViaSDK(data, success)](#printviasdk)
* [sendUSBConfig(data, success)](#sendusbconfig)

### findNetworkPrinters

Upon success, [`findNetworkPrinters`](#findNetworkPrinters) will provide a list of printers that were discovered on the network (likely using WiFi). It is not considered an error for no printers to be found, and in this case the list will just be empty.

```typescript
function findNetworkPrinters(success: (printers: Printer[]) => void, failure: (reason: string) => void): void
```

### findBluetoothPrinters

Upon success, [`findBluetoothPrinters`](#findBluetoothPrinters) will provide a list of printers that were discovered that have already been paired via Bluetooth. It is not considered an error for no printers to be found, and in this case the list will just be empty.

```typescript
function findBluetoothPrinters(success: (printers: Printer[]) => void, failure: (reason: string) => void): void
```

### findPrinters

[`findPrinters`](#findPrinters) is a convenience function that will perform the actions of both [`findNetworkPrinters`](#findNetworkPrinters) and [`findBluetoothPrinters`](#findBluetoothPrinters), and combine the the results into a single continuous list.

```typescript
function findPrinters(success: (printers: Printer[]) => void, failure: (reason: string) => void): void
```

### setPrinter

must be called before [`printViaSDK`](#printViaSDK). It takes a single object that should be one of the objects returned from [`findNetworkPrinters`](#findNetworkPrinters), [`findBluetoothPrinters`](#findBluetoothPrinters), or [`findPrinters`](#findPrinters). Upon successfully setting the printer, the success callback
will be invoked.  Otherwise, the error callback will be invoked with a string for an error message.

```typescript
function setPrinter(printer: Printer, success: () => void, failure: (reason: string) => void): void
```

### printViaSDK

takes one parameter, which is a base64 encoded bitmap image. The result should be a status code that is passed directly from the SDK. The status codes are documnted in the Brother SDK Appendix in section 4.2.2.5.Error Code. If everything works, the response should be `"ERROR_NONE"`.

__Clarification__:
> A bitmap image in this case can be any image with an encoding that is supported by the platform.


```typescript
function printViaSDK(data: string, success: () => void): void
```

### sendUSBConfig

calls the Brother SDK's `printFile` method. The expected input is a string containing raw print commands, which is written to a temporary file in the app cache directory, and is then sent to the `printFile` method and deleted afterwards. You will need a device that supports USB-OTG and a USB-OTG cable. On first run the app will request USB permissions, and it should be saved after that for subsequent prints. As-is, this method is used to send raw commands in PCL (Printer Control Language) to the printer... For example, to configure the network settings of the printer, etc... You will need to reach out to Brother for documentation of the PCL commands. You can probably find them by searching for "[Brother Printer Command Reference](https://duckduckgo.com/?q=Brother+Printer+Command+Reference)" and appending your model number. This method could be extended easily to accept other types of file input, so you could, for example, print JPG images, etc... See here for a simple way to generate a PJL file to reconfigure the network: https://github.com/gordol/PJL-Generator


```typescript
function sendUSBConfig(data: string, success: () => void): void
```
### Interface Reference

```typescript
interface Printer {
    model: string // Usually of the form 'QL_720NW' on Android
    port: 'NET' | 'BLUETOOTH'
    modelName: string // Usually of the form 'Brother QL-720NW'
    ipAddress?: string
    macAddress?: string
    serialNumber?: string
    nodeName?: string
    location?: string
    paperLabelName?: string // 'W17H54'|'W17H87'|'W23H23'|'W29H42'|'W29H90'|'W38H90'|'W39H48'|'W52H29'|'W62H29'|'W62H100'|'W12'|'W29'|'W38'|'W50'|'W54'|'W62'|'W60H86'|'W54H29'|'W62RB' 
}
```
