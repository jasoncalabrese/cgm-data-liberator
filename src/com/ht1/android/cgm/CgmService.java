package com.ht1.android.cgm;

import com.hoho.android.usbserial.driver.*;
import com.hoho.android.usbserial.util.*;
import com.ht1.android.cgm.records.EgvRecord;
import com.ht1.android.helpers.UploadHelper;
//import com.ht1.cc.USB.SerialInputOutputManager;
//import com.ht1.cc.USB.UsbSerialDriver;
//import com.ht1.cc.USB.UsbSerialProber;

import android.app.Service;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class CgmService extends Service {
	
	private static final String TAG = "CgmService";	
	
    private final IBinder mBinder = new G4ServiceBinder();
    public EgvRecord mostRecentEgv; 
    //public List<EgvRecord> egvRecordList = new ArrayList<EgvRecord>();

    public EgvRecord getMostRecentEgv() {
		return new EgvRecord();
	}

	public class G4ServiceBinder extends Binder {
    	CgmService getService() {
            // Return this instance of G4ServiceBinder so clients can call public methods
            return CgmService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

	/**
	 * The device currently in use, or {@code null}.
	 */
	private UsbSerialDriver mSerialDevice;

	/**
	 * The system's USB service.
	 */
	public UsbManager mUsbManager;
	private UploadHelper uploader;
	private Handler mHandler = new Handler();

	private SerialInputOutputManager mSerialIoManager;
	private WifiManager wifiManager;

	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.i(TAG, "Starting Service...");

	}
	@Override
	public void onDestroy() {
		super.onDestroy();

		
	}

	
}
