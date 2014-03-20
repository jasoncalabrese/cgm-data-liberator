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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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

	private Handler mHandler = new Handler();

	CgmService mService;
	boolean mBound = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = new Intent(this, CgmService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		
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
		mHandler.post(updateDataView);

	}

	@Override
	protected void onPause() {
		super.onPause();

	}
	
	@Override
	protected void onStop() {
		super.onStop();
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
				
				EgvRecord record = mService.getMostRecentEgv();
				mTitleTextView.setTextColor(Color.GREEN);
				mTitleTextView.setText("CGM Service Started");
				
				mDumpTextView.setTextColor(Color.WHITE);
				mDumpTextView.setText("\n" + record.displayTime + "\n" + record.bGValue
						+ "\n" + record.trendArrow + "\n");
				
				
			} else {
				
			}
			mHandler.postDelayed(updateDataView, 5000);
		}
	};

}
