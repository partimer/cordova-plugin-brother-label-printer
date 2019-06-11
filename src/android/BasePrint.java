/**
 * BasePrint for printing
 *
 * @author Brother Industries, Ltd.
 * @version 2.2
 */

package com.brother.ptouch.sdk.printdemo.printprocess;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.os.Environment;

import com.brother.ptouch.sdk.LabelInfo;
import com.brother.ptouch.sdk.CustomPaperInfo;
import com.brother.ptouch.sdk.Unit;
import com.brother.ptouch.sdk.PaperKind;

import com.brother.ptouch.sdk.Printer;
import com.brother.ptouch.sdk.PrinterInfo;
import com.brother.ptouch.sdk.PrinterInfo.ErrorCode;
import com.brother.ptouch.sdk.PrinterInfo.Model;
import com.brother.ptouch.sdk.PrinterStatus;
import com.brother.ptouch.sdk.TimeoutSetting;
import com.brother.ptouch.sdk.printdemo.common.Common;
import com.brother.ptouch.sdk.printdemo.common.MsgHandle;

import com.littledsys.harps.mobile.R;

import java.nio.file.Paths;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BasePrint {

    static Printer mPrinter;
    static boolean mCancel;
    static Context classContext;
    private final SharedPreferences sharedPreferences;
    private String customSetting;
    PrinterStatus mPrintResult;
    private PrinterInfo mPrinterInfo;
    final MsgHandle mHandle;

    BasePrint(Context context, MsgHandle handle) {
        mHandle = handle;
        
//         raw2file("RJ2150_51x26mm.bin", R.raw.rj2150_51x26mm, context);

        sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        mCancel = false;
        // initialization for print
        mPrinterInfo = new PrinterInfo();
        mPrinter = new Printer();
        mPrinterInfo = mPrinter.getPrinterInfo();
        mPrinter.setMessageHandle(mHandle, Common.MSG_SDK_EVENT);
    }

    public static void cancel() {
        if (mPrinter != null)
            mPrinter.cancel();
        mCancel = true;
    }

    protected abstract void doPrint();

    /**
     * set PrinterInfo
     */
    public void setPrinterInfo() {
        Log.d("BasePrint", " setPrinterInfo::getPreferences");
        getPreferencesRJ2150();
        Log.d("BasePrint", " setPrinterInfo::setCustomPaper");
        // setCustomPaper();
        Log.d("BasePrint", " setPrinterInfo::setPrinterInfo");
        mPrinter.setPrinterInfo(mPrinterInfo);
        if (mPrinterInfo.port == PrinterInfo.Port.USB) {
            while (true) {
                if (Common.mUsbRequest != 0)
                    break;
            }
        }
    }

    /**
     * get PrinterInfo
     */
    public PrinterInfo getPrinterInfo() {
        Log.d("BasePrint", " getPrinterInfo::getPreferences");
        getPreferencesRJ2150();
        return mPrinterInfo;
    }

    /**
     * get Printer
     */
    public Printer getPrinter() {
        return mPrinter;
    }

    /**
     * get Printer
     */
    public PrinterStatus getPrintResult() {
        return mPrintResult;
    }

    /**
     * get Printer
     */
    public void setPrintResult(PrinterStatus printResult) {
        mPrintResult = printResult;
    }

    public void setBluetoothAdapter(BluetoothAdapter bluetoothAdapter) {

        mPrinter.setBluetooth(bluetoothAdapter);
    }

    @TargetApi(12)
    public UsbDevice getUsbDevice(UsbManager usbManager) {
        return mPrinter.getUsbDevice(usbManager);
    }

    /**
     * get the printer settings from the SharedPreferences
     */
    private void getPreferences() {
        Log.d("getPreferences", " PrinterInfo");

        if (mPrinterInfo == null) {
            mPrinterInfo = new PrinterInfo();
            return;
        }
        String input;
        Log.d("getPreferences", " printerModel");
        mPrinterInfo.printerModel = PrinterInfo.Model.valueOf(sharedPreferences
                .getString("printerModel", ""));
        Log.d("getPreferences", " port");
        mPrinterInfo.port = PrinterInfo.Port.valueOf(sharedPreferences
                .getString("port", ""));
        Log.d("getPreferences", " ipAddress");
        mPrinterInfo.ipAddress = sharedPreferences.getString("address", "");
        Log.d("getPreferences", " macAddress");
        mPrinterInfo.macAddress = sharedPreferences.getString("macAddress", "");
        if (isLabelPrinter(mPrinterInfo.printerModel)) {
            Log.d("getPreferences", " isLabelPrinter");
            mPrinterInfo.paperSize = PrinterInfo.PaperSize.CUSTOM;
            switch (mPrinterInfo.printerModel) {
                case QL_710W:
                case QL_720NW:
                case QL_800:
                case QL_810W:
                case QL_820NWB:
                    mPrinterInfo.labelNameIndex = LabelInfo.QL700.valueOf(
                            sharedPreferences.getString("paperSize", LabelInfo.QL700.W62.toString())).ordinal();
                    mPrinterInfo.isAutoCut = Boolean.parseBoolean(sharedPreferences
                            .getString("autoCut", new Boolean(true).toString()));
                    mPrinterInfo.isCutAtEnd = Boolean
                            .parseBoolean(sharedPreferences.getString("endCut", new Boolean(true).toString()));
                    break;

                case PT_E550W:
                case PT_P750W:
                case PT_D800W:
                case PT_E800W:
                case PT_E850TKW:
                case PT_P900W:
                case PT_P950NW:
                    String paper = sharedPreferences.getString("paperSize", "");
                    mPrinterInfo.labelNameIndex = LabelInfo.PT.valueOf(paper)
                            .ordinal();
                    mPrinterInfo.isAutoCut = Boolean.parseBoolean(sharedPreferences
                            .getString("autoCut", ""));
                    mPrinterInfo.isCutAtEnd = Boolean
                            .parseBoolean(sharedPreferences.getString("endCut", ""));
                    mPrinterInfo.isHalfCut = Boolean.parseBoolean(sharedPreferences
                            .getString("halfCut", ""));
                    mPrinterInfo.isSpecialTape = Boolean
                            .parseBoolean(sharedPreferences.getString(
                                    "specialType", ""));
                    break;
                 default:
                    break;
            }
        } else {
            Log.d("getPreferences", " not isLabelPrinter");
            mPrinterInfo.paperSize = PrinterInfo.PaperSize
                    .valueOf(sharedPreferences.getString("paperSize", ""));
        }
        Log.d("getPreferences", " orientation");
        mPrinterInfo.orientation = PrinterInfo.Orientation
                .valueOf(sharedPreferences.getString("orientation", PrinterInfo.Orientation.LANDSCAPE.toString()));
        input = sharedPreferences.getString("numberOfCopies", "1");
        if (input.equals(""))
            input = "1";
        Log.d("getPreferences", " numberOfCopies");
        mPrinterInfo.numberOfCopies = Integer.parseInt(input);
        Log.d("getPreferences", " halftone");
        mPrinterInfo.halftone = PrinterInfo.Halftone.valueOf(sharedPreferences
                .getString("halftone", PrinterInfo.Halftone.PATTERNDITHER.toString()));
        Log.d("getPreferences", " printMode");
        mPrinterInfo.printMode = PrinterInfo.PrintMode
                .valueOf(sharedPreferences.getString("printMode", PrinterInfo.PrintMode.FIT_TO_PAPER.toString()));
        Log.d("getPreferences", " pjCarbon");
        mPrinterInfo.pjCarbon = Boolean.parseBoolean(sharedPreferences
                .getString("pjCarbon", new Boolean(false).toString()));
        input = sharedPreferences.getString("pjDensity", "");
        if (input.equals(""))
            input = "5";
            
        Log.d("getPreferences", " pjDensity");
        mPrinterInfo.pjDensity = Integer.parseInt(input);
        Log.d("getPreferences", " pjFeedMode");
        mPrinterInfo.pjFeedMode = PrinterInfo.PjFeedMode
                .valueOf(sharedPreferences.getString("pjFeedMode", PrinterInfo.PjFeedMode.PJ_FEED_MODE_FIXEDPAGE.toString()));
        Log.d("getPreferences", " align");
        mPrinterInfo.align = PrinterInfo.Align.valueOf(sharedPreferences
                .getString("align", PrinterInfo.Align.LEFT.toString()));
        input = sharedPreferences.getString("leftMargin", "");
        if (input.equals(""))
            input = "0";
            
        Log.d("getPreferences", " margin.left");
        mPrinterInfo.margin.left = Integer.parseInt(input);
        Log.d("getPreferences", " valign");
        mPrinterInfo.valign = PrinterInfo.VAlign.valueOf(sharedPreferences
                .getString("valign", PrinterInfo.VAlign.TOP.toString()));
        input = sharedPreferences.getString("topMargin", "");
        if (input.equals(""))
            input = "0";
            
        Log.d("getPreferences", " margin.top");
        mPrinterInfo.margin.top = Integer.parseInt(input);
        input = sharedPreferences.getString("customPaperWidth", "");
        if (input.equals(""))
            input = "0";

        Log.d("getPreferences", " mirrorPrint");
        mPrinterInfo.mirrorPrint = Boolean.parseBoolean(sharedPreferences
                .getString("mirrorPrint", new Boolean(false).toString()));
                
        Log.d("getPreferences", " customPaperWidth");
        mPrinterInfo.customPaperWidth = Integer.parseInt(input);

        input = sharedPreferences.getString("customPaperLength", "0");
        if (input.equals(""))
            input = "0";

        Log.d("getPreferences", " customPaperLength");
        mPrinterInfo.customPaperLength = Integer.parseInt(input);
        input = sharedPreferences.getString("customFeed", "");
        if (input.equals(""))
            input = "0";
            
        Log.d("getPreferences", " customFeed");
        mPrinterInfo.customFeed = Integer.parseInt(input);

        customSetting = sharedPreferences.getString("customSetting", "");
        Log.d("getPreferences", " paperPosition");
        mPrinterInfo.paperPosition = PrinterInfo.Align
                .valueOf(sharedPreferences.getString("paperPosition", PrinterInfo.Align.LEFT.toString()));
        Log.d("getPreferences", " dashLine");
        mPrinterInfo.dashLine = Boolean.parseBoolean(sharedPreferences
                .getString("dashLine", "false"));

        Log.d("getPreferences", " enabledTethering");
        mPrinterInfo.enabledTethering = Boolean.parseBoolean(sharedPreferences
                .getString("enabledTethering", new Boolean(false).toString()));

        input = sharedPreferences.getString("rjDensity", "");
        if (input.equals(""))
            input = "0";
            
        Log.d("getPreferences", " rjDensity");
        mPrinterInfo.rjDensity = Integer.parseInt(input);

        Log.d("getPreferences", " rotate180");
        mPrinterInfo.rotate180 = Boolean.parseBoolean(sharedPreferences
                .getString("rotate180", ""));
        Log.d("getPreferences", " savePrnPath");
        mPrinterInfo.savePrnPath = sharedPreferences.getString("savePrnPath", "");
        Log.d("getPreferences", " peelMode");
        mPrinterInfo.peelMode = Boolean.parseBoolean(sharedPreferences
                .getString("peelMode", ""));
        Log.d("getPreferences", " mode9");
        mPrinterInfo.mode9 = Boolean.parseBoolean(sharedPreferences
                .getString("mode9", new Boolean(true).toString()));
        Log.d("getPreferences", " overwrite");
        mPrinterInfo.overwrite = Boolean.parseBoolean(sharedPreferences
                .getString("overwrite", new Boolean(true).toString()));
        Log.d("getPreferences", " dashLine");
        mPrinterInfo.dashLine = Boolean.parseBoolean(sharedPreferences
                .getString("dashLine", ""));
        input = sharedPreferences.getString("pjSpeed", "2");
        Log.d("getPreferences", " pjSpeed");
        mPrinterInfo.pjSpeed = Integer.parseInt(input);
        Log.d("getPreferences", " rollPrinterCase");
        mPrinterInfo.rollPrinterCase = PrinterInfo.PjRollCase
                .valueOf(sharedPreferences.getString("printerCase",
                        PrinterInfo.PjRollCase.PJ_ROLLCASE_OFF.toString()));
        Log.d("getPreferences", " skipStatusCheck");
        mPrinterInfo.skipStatusCheck = Boolean.parseBoolean(sharedPreferences
                .getString("skipStatusCheck", new Boolean(false).toString()));
        Log.d("getPreferences", " softFocusing");
        mPrinterInfo.softFocusing = Boolean.parseBoolean(sharedPreferences
                .getString("softFocusing", new Boolean(false).toString()));
        Log.d("getPreferences", " checkPrintEnd");
        mPrinterInfo.checkPrintEnd = PrinterInfo.CheckPrintEnd
                .valueOf(sharedPreferences.getString("checkPrintEnd", PrinterInfo.CheckPrintEnd.CPE_CHECK.toString()));
        Log.d("getPreferences", " printQuality");
        mPrinterInfo.printQuality = PrinterInfo.PrintQuality
                .valueOf(sharedPreferences.getString("printQuality",
                        PrinterInfo.PrintQuality.NORMAL.toString()));
        Log.d("getPreferences", " rawMode");
        mPrinterInfo.rawMode = Boolean.parseBoolean(sharedPreferences
                .getString("rawMode", new Boolean(false).toString()));
        Log.d("getPreferences", " trimTapeAfterData");
        mPrinterInfo.trimTapeAfterData = Boolean.parseBoolean(sharedPreferences
                .getString("trimTapeAfterData", new Boolean(false).toString()));
        input = sharedPreferences.getString("imageThresholding", "");
        if (input.equals(""))
            input = "127";

        Log.d("getPreferences", " thresholdingValue");
        mPrinterInfo.thresholdingValue = Integer.parseInt(input);
        Log.d("getPreferences", " timeout");
        mPrinterInfo.timeout = new TimeoutSetting();

        input = sharedPreferences.getString("scaleValue", "");
        if (input.equals(""))
            input = "1";
        try {
            Log.d("getPreferences", " scaleValue");
            mPrinterInfo.scaleValue = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            Log.d("getPreferences", " scaleValue");
            mPrinterInfo.scaleValue = 1.0;
        }

        Log.d("getPreferences", " printerModel");
        if (mPrinterInfo.printerModel == Model.TD_4000
                || mPrinterInfo.printerModel == Model.TD_4100N) {
            mPrinterInfo.isAutoCut = Boolean.parseBoolean(sharedPreferences
                    .getString("autoCut", ""));
            mPrinterInfo.isCutAtEnd = Boolean.parseBoolean(sharedPreferences
                    .getString("endCut", ""));
        }

    }

        /**
     * get the printer settings from the SharedPreferences
     */
    private void getPreferencesRJ2150() {
        Log.d("getPreferencesRJ2150", " PrinterInfo");

        if (mPrinterInfo == null) {
            mPrinterInfo = new PrinterInfo();
            return;
        }
        String input;
        mPrinterInfo.printerModel = PrinterInfo.Model.valueOf(sharedPreferences
                .getString("printerModel", ""));
        mPrinterInfo.port = PrinterInfo.Port.valueOf(sharedPreferences
                .getString("port", ""));
        mPrinterInfo.ipAddress = sharedPreferences.getString("address", "");
        mPrinterInfo.macAddress = sharedPreferences.getString("macAddress", "");
        mPrinterInfo.orientation = PrinterInfo.Orientation
                .valueOf(sharedPreferences.getString("orientation", PrinterInfo.Orientation.PORTRAIT.toString()));
//         input = sharedPreferences.getString("numberOfCopies", "1");
//         if (input.equals(""))
//             input = "1";
//         mPrinterInfo.numberOfCopies = Integer.parseInt(input);
//         mPrinterInfo.halftone = PrinterInfo.Halftone.valueOf(sharedPreferences
//                 .getString("halftone", PrinterInfo.Halftone.PATTERNDITHER.toString()));
//         mPrinterInfo.paperSize = PrinterInfo.PaperSize.CUSTOM;
        mPrinterInfo.printMode = PrinterInfo.PrintMode.FIT_TO_PAGE;
                
//         Log.d("getPreferencesRJ2150", Common.CUSTOM_PAPER_FOLDER);
// 
//         List<Map<CustomPaperInfo.ErrorParameter, CustomPaperInfo.ErrorDetail>> errors = mPrinterInfo.setCustomPaperInfo(customPaperInfo);
// 
//         if (errors.isEmpty()) {
//         } else {
//             // TODO: Humal Readable
// //             return BasePrintResult.fail(errors.toString());
//             Log.d("Config Error", errors.toString());
// 
//         }
//works
//         Log.d("getPreferencesRJ2150", ( Common.CUSTOM_PAPER_FOLDER + "bsr16act.bin") );
        mPrinterInfo.customPaper = Common.CUSTOM_PAPER_FOLDER + "RJ2150_51x26mm.bin";
//         mPrinterInfo.customPaper = "file:///android_asset/bsr16act.bin";
//         mPrinterInfo.customPaper = "file:///storage/emulated/0/Android/data/com.littledsys.harps.mobile/files/bsr16act.bin";

        Log.d("getPreferencesRJ2150 Common.CUSTOM_PAPER_FOLDER", Common.CUSTOM_PAPER_FOLDER );
//         Log.d("getPreferencesRJ2150 Context.getFilesDir();", Context.getFilesDir() );
//         Log.d("getPreferencesRJ2150 Context.getFilesDir();", Context.getExternalFilesDir(null) );
        Log.d("getPreferencesRJ2150 Paths", Paths.get("").toAbsolutePath().toString() );

        Log.d("getPreferencesRJ2150 user.dir", System.getProperty("user.dir") );
        Log.d("getPreferencesRJ2150 java.home", System.getProperty("java.home") );
        Log.d("getPreferencesRJ2150 user.home", System.getProperty("user.home") );
        Log.d("getPreferencesRJ2150 java.class.path", System.getProperty("java.class.path") );
        
//         raw2file("RJ2150_51x26mm.bin", R.raw.rj2150_51x26mm);

        
    }
    
    /**
     * Launch the thread to print
     */
    public void print() {
        mCancel = false;
        PrinterThread printTread = new PrinterThread();
        Log.d("print", " printTread.start()");
        printTread.start();
    }

    /**
     * Launch the thread to get the printer's status
     */
    public void getPrinterStatus() {
        mCancel = false;
        getStatusThread getTread = new getStatusThread();
        getTread.start();
    }

    
//     /**
//      * copy from raw in resource
//      */
//     private void raw2file(String fileName, int fileID, Context context) {
// 
//         File newdir = new File(Common.CUSTOM_PAPER_FOLDER);
//         if (!newdir.exists()) {
//             newdir.mkdir();
//         }
//         File dstFile = new File(Common.CUSTOM_PAPER_FOLDER + fileName);
//         if (!dstFile.exists()) {
//             try {
//                 InputStream input;
//                 OutputStream output;
//                 input = context.getResources().openRawResource(fileID);
//                 output = new FileOutputStream(dstFile);
//                 int DEFAULT_BUFFER_SIZE = 1024 * 4;
//                 byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
//                 int n;
//                 while (-1 != (n = input.read(buffer))) {
//                     output.write(buffer, 0, n);
//                 }
//                 input.close();
//                 output.close();
//             } catch (IOException ignored) {
//             }
//         }
//     }
    
    /**
     * set custom paper for RJ and TD
     */
    private void setCustomPaper() {

        switch (mPrinterInfo.printerModel) {
            case RJ_4030:
            case RJ_4030Ai:
            case RJ_4040:
            case RJ_3050:
            case RJ_3150:
            case TD_2020:
            case TD_2120N:
            case TD_2130N:
            case TD_4100N:
            case TD_4000:
            case RJ_2030:
            case RJ_2140:
            case RJ_2150:
            case RJ_2050:
            case RJ_3050Ai:
            case RJ_3150Ai:
                mPrinterInfo.customPaper = Common.CUSTOM_PAPER_FOLDER + customSetting;
                break;
            default:
                break;
        }
    }

    /**
     * get the end message of print
     */
    public String showResult() {

        String result;
        if (mPrintResult.errorCode == ErrorCode.ERROR_NONE) {
            result = "Succeeded";
        } else {
            result = mPrintResult.errorCode.toString();
        }

        return result;
    }

    /**
     * show information of battery
     */
    public String getBattery() {

        String battery = "";
        if (mPrinterInfo.printerModel == PrinterInfo.Model.MW_260
                || mPrinterInfo.printerModel == PrinterInfo.Model.MW_260MFi) {
            if (mPrintResult.batteryLevel > 80) {
                battery = Common.BatteryStatus.FULL.toString();
            } else if (30 <= mPrintResult.batteryLevel
                    && mPrintResult.batteryLevel <= 80) {
                battery = Common.BatteryStatus.MIDDLE.toString();
            } else if (0 <= mPrintResult.batteryLevel
                    && mPrintResult.batteryLevel < 30) {
                battery = Common.BatteryStatus.WEAK.toString();
            }
        } else if (mPrinterInfo.printerModel == Model.RJ_4030
                || mPrinterInfo.printerModel == Model.RJ_4030Ai
                || mPrinterInfo.printerModel == Model.RJ_4040
                || mPrinterInfo.printerModel == Model.RJ_3050
                || mPrinterInfo.printerModel == Model.RJ_3150
                || mPrinterInfo.printerModel == Model.PT_E550W
                || mPrinterInfo.printerModel == Model.PT_P750W
                || mPrinterInfo.printerModel == Model.TD_2020
                || mPrinterInfo.printerModel == Model.TD_2120N
                || mPrinterInfo.printerModel == Model.TD_2130N
                || mPrinterInfo.printerModel == Model.PJ_722
                || mPrinterInfo.printerModel == Model.PJ_723
                || mPrinterInfo.printerModel == Model.PJ_762
                || mPrinterInfo.printerModel == Model.PJ_763
                || mPrinterInfo.printerModel == Model.PJ_763MFi
                || mPrinterInfo.printerModel == Model.PJ_773
                || mPrinterInfo.printerModel == Model.PT_P900W
                || mPrinterInfo.printerModel == Model.PT_P950NW
                || mPrinterInfo.printerModel == Model.PT_E850TKW
                || mPrinterInfo.printerModel == Model.PT_E800W
                || mPrinterInfo.printerModel == Model.PT_D800W
                || mPrinterInfo.printerModel == Model.QL_800
                || mPrinterInfo.printerModel == Model.QL_810W
                || mPrinterInfo.printerModel == Model.QL_820NWB
                || mPrinterInfo.printerModel == Model.RJ_2030
                || mPrinterInfo.printerModel == Model.RJ_2050
                || mPrinterInfo.printerModel == Model.RJ_2140
                || mPrinterInfo.printerModel == Model.RJ_2150
                || mPrinterInfo.printerModel == Model.RJ_3050Ai
                || mPrinterInfo.printerModel == Model.RJ_3150Ai) {
            switch (mPrintResult.batteryLevel) {
                case 0:
                    battery = Common.BatteryStatus.FULL.toString();
                    break;
                case 1:
                    battery = Common.BatteryStatus.MIDDLE.toString();
                    break;
                case 2:
                    battery = Common.BatteryStatus.WEAK.toString();
                    break;
                case 3:
                    battery = Common.BatteryStatus.CHARGE.toString();
                    break;
                case 4:
                    battery = Common.BatteryStatus.ACADAPTER.toString();
                    break;
                default:
                    break;
            }
        } else {
            switch (mPrintResult.batteryLevel) {
                case 0:
                    battery = Common.BatteryStatus.ACADAPTER.toString();
                    break;
                case 1:
                    battery = Common.BatteryStatus.WEAK.toString();
                    break;
                case 2:
                    battery = Common.BatteryStatus.MIDDLE.toString();
                    break;
                case 3:
                    battery = Common.BatteryStatus.FULL.toString();
                    break;
                default:
                    break;
            }
        }
        if (mPrintResult.errorCode != ErrorCode.ERROR_NONE)
            battery = "";
        return battery;
    }

    private boolean isLabelPrinter(PrinterInfo.Model model) {
        switch (model) {
            case QL_710W:
            case QL_720NW:
            case PT_E550W:
            case PT_P750W:
            case PT_D800W:
            case PT_E800W:
            case PT_E850TKW:
            case PT_P900W:
            case PT_P950NW:
            case QL_810W:
            case QL_800:
            case QL_820NWB:
                return true;
            default:
                return false;
        }
    }

    /**
     * Thread for printing
     */
    private class PrinterThread extends Thread {
        @Override
        public void run() {
            Log.d("PrinterThread", " setPrinterInfo");
            // set info. for printing
            setPrinterInfo();

            Log.d("print", " sendMessage");
            // start message
            Message msg = mHandle.obtainMessage(Common.MSG_PRINT_START);
            mHandle.sendMessage(msg);

            mPrintResult = new PrinterStatus();

            Log.d("print", " startCommunication");
            mPrinter.startCommunication();
            if (!mCancel) {
                Log.d("print", " doPrint");
                doPrint();
            } else {
                mPrintResult.errorCode = ErrorCode.ERROR_CANCEL;
            }
            Log.d("print", " endCommunication");
            mPrinter.endCommunication();

            // end message
            mHandle.setResult(showResult());
            mHandle.setBattery(getBattery());
            msg = mHandle.obtainMessage(Common.MSG_PRINT_END);
            mHandle.sendMessage(msg);
        }
    }

    /**
     * Thread for getting the printer's status
     */
    private class getStatusThread extends Thread {
        @Override
        public void run() {

            // set info. for printing
            setPrinterInfo();

            // start message
            Message msg = mHandle.obtainMessage(Common.MSG_PRINT_START);
            mHandle.sendMessage(msg);

            mPrintResult = new PrinterStatus();
            if (!mCancel) {
                mPrintResult = mPrinter.getPrinterStatus();
            } else {
                mPrintResult.errorCode = ErrorCode.ERROR_CANCEL;
            }
            // end message
            mHandle.setResult(showResult());
            mHandle.setBattery(getBattery());
            msg = mHandle.obtainMessage(Common.MSG_PRINT_END);
            mHandle.sendMessage(msg);

        }
    }

}
