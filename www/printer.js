var BrotherPrinter = function () {}
BrotherPrinter.prototype = {
    findNetworkPrinters: function (onSuccess, onError) {
        cordova.exec(onSuccess, onError, 'BrotherPrinter', 'findNetworkPrinters', [])
    },

    findBluetoothPrinters: function (onSuccess, onError) {
        cordova.exec(onSuccess, onError, 'BrotherPrinter', 'findBluetoothPrinters', []);
    },

    findPrinters: function (onSuccess, onError) {
        cordova.exec(onSuccess, onError, 'BrotherPrinter', 'findPrinters', []);
    },

    setPrinter: function (printer, onSuccess, onError) {
         if (cordova.platformId == 'ios' && printer['paperLabelName'] && printer['paperLabelName'].startsWith('W')) {
            var iosLabelName = IOS_PAPER_LABEL_MAP[printer['paperLabelName']];
            if (iosLabelName) {
                //console.log('Converting paperLabelName to ' + iosLabelName + ' for ios ');
                printer['paperLabelName'] = iosLabelName;
            }
        }
        cordova.exec(onSuccess, onError, 'BrotherPrinter', 'setPrinter', [printer]);
    },

    printViaSDK: function (data, callback) {
        if (!data || !data.length) {
            console.log('No data passed in. Expects a bitmap.')
            return
        }
        cordova.exec(callback, function(err){console.log('error: '+err)}, 'BrotherPrinter', 'printViaSDK', [data])
    },

    sendUSBConfig: function (data, callback) {
        if (!data || !data.length) {
            console.log('No data passed in. Expects print payload string.')
            return
        }
        cordova.exec(callback, function(err){console.log('error: '+err)}, 'BrotherPrinter', 'sendUSBConfig', [data])
    }
}
var plugin = new BrotherPrinter()
module.exports = plugin

//Note that the key is OK for android while for ios, the value with 'mm' is required
const IOS_PAPER_LABEL_MAP = {
    W12: '12mm',
    W29: '29mm',
    W38: '38mm',
    W50: '50mm',
    W54: '54mm',
    W62: '62mm',
    W102: '102mm',
    W103: '103mm',
    W62RB: '62mmRB',
    W17H54: '17mmx54',
    W17H87: '17mm87',
    W23H23: '23mmx23',
    W29H42: '29mmx42',
    W29H90: '29mmx90',
    W38H90: '38mmx90',
    W39H48: '39mmx48',
    W52H29: '52mmx29',
    W54H29: '54mm×29',
    W60H86: '60mmx86',
    W62H29: '62mmx29',
    W62H100: '62mmx100',
    W102H51: '102mmx51',
    W102H152: '102mmx152',
    W103H164: '103mmx164'
}
