package com.ht1.android.cgm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hoho.android.usbserial.driver.*;
import com.hoho.android.usbserial.util.*;
import com.ht1.android.cgm.records.EgvRecord;
import com.ht1.android.helpers.UploadHelper;
import com.ht1.android.helpers.UsbPowerHelper;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class CgmService extends Service {

	private static final String TAG = "CgmService";	
	private static final int USB_POWER_TOGGLE_DELAY_DURATION_MS = 2000;
	private static final boolean LAST_FOUR_PAGES_ONLY = true;

	private final IBinder mBinder = new G4ServiceBinder();

	private UsbSerialDriver mSerialDevice;
	private UsbManager mUsbManager;
	private WifiManager mWifiManager;
	private UploadHelper mUploader;
	private Handler mHandler = new Handler();
	private Intent mBatteryStatus;	

	//public EgvRecord mostRecentEgv = new EgvRecord(); 
	public boolean dataConnected;
	public boolean cgmConnected;
	public int batteryLifePercentageRemaining;
	public List<EgvRecord> egvRecordList = new ArrayList<EgvRecord>();

	public EgvRecord getMostRecentEgv() {
		int listSize = egvRecordList.size();
		if (listSize > 0) {
			return egvRecordList.get(egvRecordList.size() - 1);
		}
		else {
			EgvRecord junk = new EgvRecord();
			junk.bGValue = "Recent Data Unavailable";
			return junk;
		}
	}

	public class G4ServiceBinder extends Binder {
		CgmService getService() {
			Log.i(TAG, "G4ServiceBinder");
			return CgmService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i(TAG, "on bind");
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "Starting Service...");

		mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		mUsbManager = (UsbManager) this.getSystemService(Context.USB_SERVICE);
		mBatteryStatus = this.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

		//Run (eventually from config) the requested logic
		//For now, we will start a handler that runs every 45 seconds, get most recent Egv data.
		mHandler.post(readCgmData);


	}
	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	private Runnable readCgmData = new Runnable() {
		public void run() {

			getBatteryLifePercentageRemaining(); 

			try {
				if (isCgmConnected() && isDataConnected()) {		
					USBOn();
					mSerialDevice = null;
					mSerialDevice = UsbSerialProber.acquire(mUsbManager);
					CgmReader reader = new CgmReader(mSerialDevice);
					egvRecordList = reader.readEgvRecords(LAST_FOUR_PAGES_ONLY);
					egvRecordList.removeAll(Collections.singleton(null));
					//doReadAndUpload();
					USBOff();
				} else {
					USBOn();
					USBOff();
				}

			} catch (Exception e) {

				Log.e(TAG, "Unable to readAndUpload", e);
			}

			mHandler.postDelayed(readCgmData, 45000);
		}
	};

	private void USBOff() {
		if(isBatteryPowered())
		{
			if (mSerialDevice != null) {
				try {
					mSerialDevice.close();
				} catch (IOException e) {
					Log.e(TAG, "Unable to close serial device prior to power off", e);
				}
				try {
					Thread.sleep(USB_POWER_TOGGLE_DELAY_DURATION_MS);
				} catch (InterruptedException e) {

				}
				UsbPowerHelper.PowerOff();
				Log.i(TAG, "USB OFF");
			} else {
				Log.w(TAG, "USBOff called, but mSerialDevice is null.");
			}

		} else
		{
			Log.i(TAG, "Skipping USB Power Off command");
		}
	}

	private void USBOn() {
		if(isBatteryPowered())
		{
			if (mSerialDevice != null) {
				try {
					mSerialDevice.close();
				} catch (IOException e) {
					Log.e(TAG, "Unable to close serial device prior to power on", e);
				}
				UsbPowerHelper.PowerOn();
				try {
					Thread.sleep(USB_POWER_TOGGLE_DELAY_DURATION_MS);
				} catch (InterruptedException e) {

				}
				Log.i(TAG, "USB ON");
			} else {
				Log.w(TAG, "USBOn called, but mSerialDevice is null.");
			}
		} else {
			Log.i(TAG, "Skipping USB Power On command");
		}
	}

	private boolean isDataConnected() {
		ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = manager.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			Log.i(TAG, "dataConnected = true");
			this.dataConnected = true;
		} else {
			Log.i(TAG, "dataConnected = false");
			this.dataConnected = false;
		}
		return this.dataConnected;
	}

	private boolean isCgmConnected() {
		mSerialDevice = UsbSerialProber.acquire(mUsbManager);
		if (mSerialDevice == null) {
			Log.i(TAG, "isCgmConnected = false");
			this.cgmConnected = false;
		}  else {
			Log.i(TAG, "isCgmConnected = true");
			this.cgmConnected = true;
		}
		return this.cgmConnected;
	}

	public int getBatteryLifePercentageRemaining() {
		Intent i= mBatteryStatus;
		this.batteryLifePercentageRemaining = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		Log.i(TAG, "battery life = " + this.batteryLifePercentageRemaining);
		return this.batteryLifePercentageRemaining;
	}

	public boolean isBatteryPowered() {
		int plugged = mBatteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		Log.i(TAG, "plugged:" + plugged);
        if (plugged == BatteryManager.BATTERY_PLUGGED_AC) {
        	Log.i(TAG, "plugged in");
        	return false;
        } else if (plugged == BatteryManager.BATTERY_PLUGGED_USB) {
        	Log.i(TAG, "usb power plugged in");
            return false;
        } else if (plugged == 0) {
        	Log.i(TAG, "not plugged in");
            return true;
        } else {
        	Log.i(TAG, "cannot determine BATTERY_PLUGGED state");
            return true; //unknown
        }
	}
	
	
}


