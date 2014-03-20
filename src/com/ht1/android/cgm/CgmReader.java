package com.ht1.android.cgm;

import com.hoho.android.usbserial.driver.*;
import android.os.AsyncTask;


public class CgmReader extends AsyncTask<UsbSerialDriver, Object, Object>{
	
	private static final String TAG = CgmReader.class.getSimpleName();	

	private UsbSerialDriver mSerialDevice;

	
	public CgmReader (UsbSerialDriver device)
	{
		mSerialDevice = device;
	}

	
	
	@Override
	protected Object doInBackground(UsbSerialDriver... params) {
		// TODO Auto-generated method stub
		return null;
	}



}
