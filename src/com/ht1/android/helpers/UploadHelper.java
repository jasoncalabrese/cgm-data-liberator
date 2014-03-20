package com.ht1.android.helpers;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
public class UploadHelper extends AsyncTask<String, Integer, Long> {

	Context context;
	
    public UploadHelper(Context context) {
        this.context = context;
    }
	@Override
	protected Long doInBackground(String... data) {

		return 1L;
	}

	@Override
	protected void onPostExecute(Long result) {
		super.onPostExecute(result);
		Log.i("Uploader", result + " Status: FINISHED");

	}

}
