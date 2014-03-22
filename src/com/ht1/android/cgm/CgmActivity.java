package com.ht1.android.cgm;


import java.io.File;

import com.ht1.android.cgm.CgmService.G4ServiceBinder;
import com.ht1.android.cgm.records.EgvRecord;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/* Main activity for the DexcomG4Activity program */
public class CgmActivity extends Activity {

	private static final String TAG = "CgmActivity";	

	//ui
	private TextView mTitleTextView;
	private TextView mDumpTextView;
	private ScrollView mScrollView;

	public UsbManager mUsbManager;

	private Handler mHandler = new Handler();

	CgmService mService;
	Messenger mServiceMessenger = null;
	boolean mBound = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);



		setContentView(R.layout.adb);
		mTitleTextView = (TextView) findViewById(R.id.demoTitle);
		mDumpTextView = (TextView) findViewById(R.id.demoText);
		mScrollView = (ScrollView) findViewById(R.id.demoScroller);

		mDumpTextView.setTextColor(Color.WHITE);
		mDumpTextView.setText("\n" + "Loading..." + "\n");
		mTitleTextView.setTextColor(Color.YELLOW);
		mTitleTextView.setText("CGM Service Starting...");

		LinearLayout lnr = (LinearLayout) findViewById(R.id.container);
		
	}

	@Override
	protected void onStart() {
		super.onStart();
		Intent intent = new Intent(this, CgmService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		Log.i(TAG, "onStart");

	}

	@Override
	protected void onPause() {
		super.onPause();
		mHandler.removeCallbacks(updateDataView);
		Log.i(TAG, "onPause");

	}

	@Override
	protected void onResume() {
		super.onResume();
		mHandler.post(updateDataView);
		Log.i(TAG, "onResume");
		
	}

	@Override
	protected void onStop() {
		super.onStop();
		mHandler.removeCallbacks(updateDataView);
		Log.i(TAG, "onStop");
		// Unbind from the service
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}

	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className,
				IBinder service) {
			G4ServiceBinder binder = (G4ServiceBinder) service;
			mService = binder.getService();
			mBound = true;
			mHandler.post(updateDataView);
			Log.i(TAG, "ServiceConnected Callback");
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};


	private Runnable updateDataView = new Runnable() {
		@Override
		public void run() {
			if (mBound) {
				Log.i(TAG, "updateDataView found bound service, refreshing UI");
				refreshUI();
			} else {
				Log.i(TAG, "updateDataView NO bound service, cannot refresh UI");
			}
			mHandler.postDelayed(updateDataView, 15000);
		}
	};
	
	private void refreshUI() {
		EgvRecord record = mService.getMostRecentEgv();
		mTitleTextView.setTextColor(Color.GREEN);
		mTitleTextView.setText("CGM Service Started");

		if(mService.cgmConnected){
			Log.i(TAG, "cgmConnected = true");
			mDumpTextView.setTextColor(Color.WHITE);
			mDumpTextView.setText("\n" + record.simpleTime + "\n" + record.bGValue
					+ "\n" + record.trendArrow + "\n");
		} else {
			Log.i(TAG, "cgmConnected = false");
			mDumpTextView.setTextColor(Color.RED);
			mDumpTextView.setText("\n" + "CGM Disconnected\n" + record.simpleTime + "\n" + record.bGValue
					+ "\n" + record.trendArrow +  "\n");
		}
	}


}
