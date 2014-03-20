package com.ht1.android.cgm;

import java.io.IOException;
import java.util.List;

import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.hoho.android.usbserial.driver.*;
import com.ht1.android.cgm.records.EgvRecord;

import android.os.AsyncTask;
import android.util.Log;


public class CgmReader extends AsyncTask<UsbSerialDriver, Object, Object>{
	
	private static final String TAG = CgmReader.class.getSimpleName();	

	private UsbSerialDriver mSerialDevice;
	private SerialInputOutputManager mSerialIoManager;


	
	public CgmReader (UsbSerialDriver device)
	{
		mSerialDevice = device;
	}

	public List<EgvRecord> readEgvRecords(boolean lastFourPagesOnly)
	{

		if (mSerialDevice != null) {
			startIoManager();
			try {
				mSerialDevice.open();
				byte[] pages = readPages(readEgvDatabasePageRange());
				return parsePages(pages);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return null;
	}

	private List<EgvRecord> parsePages(byte[] pages) {
		// TODO Auto-generated method stub
		return null;
	}

	private byte[] readEgvDatabasePageRange(){

		try {
			mSerialDevice.write(CgmCommand.readEgvPageRangeCommand(), CgmCommand.WRITE_EGV_PAGE_RANGE_RESPONSE_TIMEOUT_MS);
		} catch (IOException e) {
			
		}
		byte[] pageRange = new byte[CgmCommand.READ_EGV_PAGE_RANGE_RESPONSE_ARRAY_SIZE];
		try {
			mSerialDevice.read(pageRange, CgmCommand.READ_EGV_PAGE_RANGE_RESPONSE_TIMEOUT_MS);
		} catch (IOException e) {
			
		}

		return pageRange;
	}
	
	private byte[] readPages(byte [] pageRange)
	{
        byte [] endPage = new byte[]{pageRange[8], pageRange[9], pageRange[10], pageRange[11]};
        byte [] startPage = null;        
          
        byte [] readEgvPage = CgmCommand.readEgvPageCommand(startPage, endPage);
        		              
        try {

        	mSerialDevice.write(readEgvPage, CgmCommand.WRITE_EGV_PAGE_RESPONSE_TIMEOUT_MS);
		} catch (IOException e) {
			
		}
        
        //Get pages
        byte[] dexcomDatabasePages = new byte[CgmCommand.READ_FOUR_EGV_PAGES_RESPONSE_ARRAY_SIZE];

        try {
        	mSerialDevice.read(dexcomDatabasePages, CgmCommand.READ_EGV_PAGE_RESPONSE_TIMEOUT_MS);

		} catch (IOException e) {
			
		}
        
        byte [] databasePages = new byte[CgmCommand.FOUR_EGV_PAGES_ARRAY_SIZE];
        System.arraycopy(dexcomDatabasePages, 4, databasePages, 0, CgmCommand.FOUR_EGV_PAGES_ARRAY_SIZE);
        return databasePages;        
	}
	
	private void stopIoManager() {
		if (mSerialIoManager != null) {
			Log.i(TAG, "Stopping io manager ..");
			mSerialIoManager.stop();
			mSerialIoManager = null;
		}
	}

	private void startIoManager() {
		if (mSerialDevice != null) {
			Log.i(TAG, "Starting io manager ..");
			mSerialIoManager = new SerialInputOutputManager(mSerialDevice);
		}
	}
	

	
	@Override
	protected Object doInBackground(UsbSerialDriver... params) {
		// TODO Auto-generated method stub
		return null;
	}



}
