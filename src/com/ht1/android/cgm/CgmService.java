package com.ht1.android.cgm;

import java.util.ArrayList;
import java.util.List;

import com.hoho.android.usbserial.driver.*;
import com.hoho.android.usbserial.util.*;
import com.ht1.android.cgm.records.EgvRecord;
import com.ht1.android.helpers.UploadHelper;


import android.app.Service;
import android.content.Context;
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
    
	private UsbSerialDriver mSerialDevice;
	public UsbManager mUsbManager;
	private UploadHelper uploader;
	private Handler mHandler = new Handler();

	private SerialInputOutputManager mSerialIoManager;
	private WifiManager mWifiManager;

    public EgvRecord mostRecentEgv = new EgvRecord(); 
    public List<EgvRecord> egvRecordList = new ArrayList<EgvRecord>();

    public EgvRecord getMostRecentEgv() {
		return mostRecentEgv;
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

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "Starting Service...");
		
		mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		mUsbManager = (UsbManager) this.getSystemService(Context.USB_SERVICE);
		
		//Run (eventually from config) the requested logic
		//For now, we will start a handler that runs every 45 seconds, get's most recent Egv data.
		mHandler.post(readAndUpload);
		

	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		
	}
	
	private Runnable readAndUpload = new Runnable() {
		public void run() {

		try {
			

		} catch (Exception e) {
			
			Log.e(TAG, "Unable to readAndUpload", e);
		}

		mHandler.postDelayed(readAndUpload, 45000);
		}
	};


	
}
