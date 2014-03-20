package com.ht1.android.cgm;

import java.io.IOException;
import java.util.ArrayList;
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
import android.os.Handler;
import android.os.IBinder;
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

	private boolean toggleUsbPower = true;

	//public EgvRecord mostRecentEgv = new EgvRecord(); 
	public boolean dataConnected;
	public boolean cgmConnected;
	public int batteryLifePercentageRemaining;
	public List<EgvRecord> egvRecordList = new ArrayList<EgvRecord>();

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

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "Starting Service...");

		mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		mUsbManager = (UsbManager) this.getSystemService(Context.USB_SERVICE);

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
					reader.readEgvRecords(LAST_FOUR_PAGES_ONLY);
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
		if(toggleUsbPower)
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
			Log.i(TAG, "toggleUsbPower = " + toggleUsbPower);
		}
	}

	private void USBOn() {
		if(toggleUsbPower)
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
			Log.i(TAG, "toggleUsbPower = " + toggleUsbPower);
		}
	}

	public boolean isToggleUsbPower() {
		return toggleUsbPower;
	}

	public void setToggleUsbPower(boolean toggleUsbPower) {
		this.toggleUsbPower = toggleUsbPower;
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
		Intent i= this.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		this.batteryLifePercentageRemaining = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		Log.i(TAG, "battery life = " + this.batteryLifePercentageRemaining);
		return this.batteryLifePercentageRemaining;
	}




}
