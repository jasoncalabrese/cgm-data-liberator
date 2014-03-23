package com.ht1.android.helpers;

import java.net.MalformedURLException;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
public class UploadHelper extends AsyncTask<String, Integer, Long> {

	Context context;
	Destination destination;
	Bundle bundle;

	public UploadHelper(Destination destination, Bundle bundle, Context context) {
		this.context = context;
		this.destination = destination;
	}
	@Override
	protected Long doInBackground(String... data) {

		switch (destination)
		{
		case AZURE_MOBILE_SERVICE:
			//get data from bundle
			String serviceUrl = "";
			String serviceKey = "";
			uploadToAzureMobileService(serviceUrl, serviceKey);
		default:
			break;
		}


		return 1L;
	}

	private void uploadToAzureMobileService(String sUrl, String sKey) {
		//unpack bundle for settings
		
		//upload
		MobileServiceClient mClient;
		//web service
		try {
			mClient = new MobileServiceClient(
					sUrl,
					sKey,
					context
					);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
		}

	}
	@Override
	protected void onPostExecute(Long result) {
		super.onPostExecute(result);
		Log.i("Uploader", result + " Status: FINISHED");

	}

}
