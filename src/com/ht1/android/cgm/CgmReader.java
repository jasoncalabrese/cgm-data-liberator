package com.ht1.android.cgm;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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

	public List<EgvRecord> readEgvRecords(boolean lastFourPagesOnly) throws ParseException
	{

		if (mSerialDevice != null) {
			startIoManager();
			try {
				mSerialDevice.open();
				byte[] pages = readPages(readEgvDatabasePageRange());
				stopIoManager();
				return parsePages(pages);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		return null;
	}

	private List<EgvRecord> parsePages(byte[] pages)  throws ParseException {

	        byte [][] fourPages = new byte[4][528];
	        int [] recordCounts = new int[4];
	        int totalRecordCount = 0;
	        
	        //we parse 4 pages at a time, calculate total record count while we do this
	        for (int i = 0; i < 4; i++)
	        {
	        	System.arraycopy(pages, 528*i, fourPages[i], 0, 528);
	        	recordCounts[i] = fourPages[i][4];
	        	totalRecordCount = totalRecordCount + recordCounts[i];
	        }
	        
	        List<EgvRecord>  recordsToReturn = new ArrayList<EgvRecord>();
	        int k = 0;
	        
	        //parse each record, plenty of room for improvement
	        byte [] tempRecord = new byte[13];
	        for (int i = 0; i < 4; i++)
	        {
	        	for (int j = 0; j < recordCounts[i]; j++)
	        	{
	        		System.arraycopy(fourPages[i], 28 + j*13, tempRecord, 0, 13);
	        		
	        		byte [] egv = new byte[]{tempRecord[8],tempRecord[9]};
	                int egValue = ((egv[1]<<8) + (egv[0] & 0xff)) & 0x3ff;
	                   
	                byte [] transmissionTime = new byte[]{tempRecord[7],tempRecord[6],tempRecord[5],tempRecord[4]};              
	                ByteBuffer buffer = ByteBuffer.wrap(transmissionTime);
	                int transmissionTimeMs = buffer.getInt();
	               
	                String string_date = "1-January-2009";
	                SimpleDateFormat f = new SimpleDateFormat("dd-MMM-yyyy");
	                Date d = f.parse(string_date);
	                long milliseconds = d.getTime();
	                long timeAdd = milliseconds + (1000L*transmissionTimeMs);
	                TimeZone tz = TimeZone.getDefault();
	                
	                if (tz.inDaylightTime(new Date()))
	                	timeAdd = timeAdd - 3600000L;
	                
	        		Date display = new Date(timeAdd);
	        		
	        		byte trendByteValue = (byte) (tempRecord[10] & (byte)15);
	        		String trendText = "Not Mapped";
	        		String trendArrowAscii = " x ";
	        		
	        		switch (trendByteValue) {	

	        		case (0):
	        			trendArrowAscii = " ";
	        			trendText = "NONE";
	        			break;
	        		case (1):
	        			trendArrowAscii = "\u21C8";
	        			trendText = "DoubleUp";
	        			break;
	        		case (2):
	        			trendArrowAscii = "\u2191";
	        			trendText = "SingleUp";
	        			break;
	        		case (3):
	        			trendArrowAscii = "\u2197";
	        			trendText = "FortyFiveUp";
	        			break;
	        		case (4):
	        			trendArrowAscii = "\u2192";
	        			trendText = "Flat";
	        			break;
	        		case (5):
	        			trendArrowAscii = "\u2198";
	        			trendText = "FortyFiveDown";
	        			break;
	        		case (6):
	        			trendArrowAscii = "\u2193";
	        			trendText = "SingleDown";
	        			break;
	        		case (7):
	        			trendArrowAscii = "\u21CA";
	        			trendText = "DoubleDown";
	        			break;
	        		case (8):
	        			trendArrowAscii = " x ";
	        			trendText = "NOT COMPUTABLE";
	        			break;
	        		case (9):
	        			trendArrowAscii = " ";
	        			trendText = "RATE OUT OF RANGE";
	        			break;
	        		}


	        	    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ") {
	        	        public StringBuffer format(Date date, StringBuffer toAppendTo, java.text.FieldPosition pos) {
	        	            StringBuffer toFix = super.format(date, toAppendTo, pos);
	        	            return toFix.insert(toFix.length()-2, ':');
	        	        };
	        	    };
	        	    
	        	    SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yyyy  -  hh:mm a");
	        		
	        	      		
	        	    EgvRecord record  = new EgvRecord();
	        		record.setBGValue(Integer.toString(egValue));
	        		record.setDisplayTime(df.format(display));
	        		record.setSimpleTime(sdf.format(display));
	        		record.setTrend(trendText);
	        		record.setTrendArrow(trendArrowAscii);
	        		
	        		recordsToReturn.add(record);
	        	}
	        }     
	        
			return recordsToReturn;
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
