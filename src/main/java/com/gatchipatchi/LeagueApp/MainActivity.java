package com.gatchipatchi.LeagueApp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ImageView.ScaleType;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Gravity;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collections;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.StringWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import org.json.JSONObject;
import org.json.JSONException;
import cz.msebera.android.httpclient.Header;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.AsyncHttpClient;

public class MainActivity extends Activity 
{
	// objects
	EditText editText;
	ImageButton champButton1, champButton2, champButton3, champButton4;
	Button refreshButton;
	
	// onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
		
		// add champ buttons iteratively
		LinearLayout ll = (LinearLayout)findViewById(R.id.button_layout);
		for(int i=0; i<4; i++)
		{
			ImageButton ib = new ImageButton(this);
			
			// set icon
			Resources res = getResources();
			TypedArray champIcons = res.obtainTypedArray(R.array.champ_icons);
			ib.setImageResource(champIcons.getResourceId(i, 0));
			champIcons.recycle();
			
			ib.setAdjustViewBounds(true);
			ib.setScaleType(ScaleType.FIT_CENTER);
			ib.setCropToPadding(false);
			ib.setPadding(0,0,0,0);
			ib.setOnClickListener(champButtonListener);
			ib.setId(i);
			ll.addView(ib, 87, 87);
			
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(87, 87);
			params.setMargins(2,2,2,2);
			ib.setLayoutParams(params);
		}
		
		// add data refresh button
		LinearLayout footer = (LinearLayout) findViewById(R.id.footer);
		refreshButton = new Button(this);
		// RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		// refreshButton.setLayoutParams(params);
		refreshButton.setText("refresh data");
		// refreshButton.setGravity(Gravity.BOTTOM | Gravity.CENTER);
		footer.addView(refreshButton);
		
		// add listener to refresh button
		refreshButton.setOnClickListener(refreshButtonListener);		
		
		// add listener to search bar
		editText = (EditText) findViewById(R.id.search);
		editText.setOnEditorActionListener(searchListener);
		
//		// buttons (this is gonna be long)
//		champButton1 = (ImageButton) findViewById(R.id.champbutton1);
//		champButton1.setOnClickListener(champButtonListener);
//
//		champButton2 = (ImageButton) findViewById(R.id.champbutton2);
//		champButton2.setOnClickListener(champButtonListener);
//		
//		champButton3 = (ImageButton) findViewById(R.id.champbutton3);
//		champButton3.setOnClickListener(champButtonListener);
//		
//		champButton4 = (ImageButton) findViewById(R.id.champbutton4);
//		champButton4.setOnClickListener(champButtonListener);

		// actual button code
		
		// load the JSON data
		InputStream in = null;
		try {
			in = new BufferedInputStream(this.openFileInput("champion.json"));
		}
		catch (FileNotFoundException e) {
			toast(e.getMessage(), Toast.LENGTH_LONG);
		}
		
		// put data into a JSONObject
		JSONObject json = null;
		try {
			// Convert input stream to a string for older APIs.
			// (Newer JSON libraries dont need this step.)
			BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			StringBuilder responseStrBuilder = new StringBuilder();
			String inputStr;
			
			while ((inputStr = streamReader.readLine()) != null) {
				responseStrBuilder.append(inputStr);
			}
			
			json = new JSONObject(responseStrBuilder.toString());
			JSONObject champData = json.getJSONObject("data");
			
			// get champ list
			Iterator<String> champs = champData.keys();
			
			// put it into an ordered list 
			ArrayList<String> champList = new ArrayList();
			int i = 0;
			while (champs.hasNext()) {
				champList.add(champs.next());
				i++;
			}
			
			// sort the list into alpha order
			java.util.Collections.sort(champList);
			
			for (int c=0; c<4; c++) {
				toast(champList.get(c));
			}
			// StringWriter sw = new StringWriter();
			// while (champs.hasNext()) {
				// sw.append(champs.next() + ",");
			// }
			// String champList = sw.toString();
			// storeTextFile(champList);
			
		} catch (UnsupportedEncodingException e) {
			toast("UnsupportedEncodingException", Toast.LENGTH_LONG);
			showError(e);
		} catch (JSONException e) {
			toast("JSONException", Toast.LENGTH_LONG);
			showError(e);
		} catch (IOException e) {
			toast("IOException", Toast.LENGTH_LONG);
			showError(e);
		}
	}
	
	// stub method for testing
	void stubMethod()
	{
		// stub
		Toast toast = Toast.makeText(getApplicationContext(), "Tada!", Toast.LENGTH_SHORT);
		toast.show();
	}
	
	void downloadChamps()
	{
		// attempt a file download
		File jsonFile = new File(this.getFilesDir(), "champion.json");
		String url = "http://ddragon.leagueoflegends.com/cdn/6.10.1/data/en_US/champion.json";
		if (!jsonFile.exists())
		{
			toast("File doesnt exist.  Attempting download.", Toast.LENGTH_LONG);
			AsyncHttpClient client = new AsyncHttpClient();
			client.get(url, asyncHandler);
		}
		else {
			toast("Up to date");
		}
	}
	
	private AsyncHttpResponseHandler asyncHandler = new AsyncHttpResponseHandler() {
		
		@Override
		public void onStart() {
        // called before request is started
		  toast("starting download...");
		}

		@Override
		public void onSuccess(int statusCode, Header[] headers, byte[] response) {
			// called when response HTTP status is "200 OK"
			// String data;
			// try {
				// data = new String(response, "UTF-8");
				// saveJson(data);
			// }
			// catch (UnsupportedEncodingException e) {
				// toast(e.getMessage());
			// }
			storeJson(response);
			toast("download successful");
		}

		@Override
		public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
        // called when response HTTP status is "4XX" (eg. 401, 403, 404)
		  toast("didnt work");
		  toast(statusCode, Toast.LENGTH_LONG);
		  toast(e.getMessage(), Toast.LENGTH_LONG);
		}

		@Override
		public void onRetry(int retryNo) {
        // called when request is retried
		  toast("retrying...");
		}
	};
	
	void storeJson(byte[] data)
	{
		String filename = "champion.json";
		File file = new File(this.getFilesDir(), filename);
		// toast(this.getFilesDir().toString(), Toast.LENGTH_LONG);
		
		// if (file.exists()) {
			// file.delete();
		// }
		// save file unless it already exists
		if (!file.exists())
		{
			toast("storing file...", Toast.LENGTH_LONG);
			OutputStream out = null;
			
			// write to file
			try {
				out = new BufferedOutputStream(new FileOutputStream(file));
				out.write(data);
				out.close();
				toast("file written");
			}
			catch (IOException e) {
				toast("file write failure");
				toast(e.getMessage(), Toast.LENGTH_SHORT);
			}
		}
	}
	
	void storeTextFile(String data)
	{
		String filename = "champ_list.txt";
		File file = new File(this.getFilesDir(), filename);
		// toast(this.getFilesDir().toString(), Toast.LENGTH_LONG);
		
		// if (file.exists()) {
			// file.delete();
		// }
		// save file unless it already exists
		if (!file.exists())
		{
			toast("storing file...", Toast.LENGTH_LONG);
			BufferedWriter out = null;
			
			// write to file
			try {
				out = new BufferedWriter(new FileWriter(file));
				out.write(data);
				out.close();
				toast("file written");
			}
			catch (IOException e) {
				toast("file write failure");
				toast(e.getMessage(), Toast.LENGTH_SHORT);
			}
		}
	}
	
	// refresh button listener
	private OnClickListener refreshButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View p1)
		{
			// button click action
			downloadChamps();
		}
	};
	
	// champ button onClick listener
	private OnClickListener champButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View p1)
		{
			// button click action
			//Toast toast = Toast.makeText(getApplicationContext(), Integer.toString(p1.getId()), Toast.LENGTH_LONG);
			//toast.show();
			Intent intent = new Intent(MainActivity.this, ChampInfo.class);
			intent.putExtra("button id", p1.getId());
			startActivity(intent);
		}
	};
	
	private OnEditorActionListener searchListener = new OnEditorActionListener()
	{
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
		{
			if(actionId == EditorInfo.IME_ACTION_SEARCH)
			{
				// perform Search
				stubMethod();
				return true;
			}
			Toast errorMesg = Toast.makeText(getApplicationContext(), "Something went wrong.", Toast.LENGTH_SHORT);
			errorMesg.show();
			return false;
		}
	};
	
	void toast(String msg) {
		Toast t = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
		t.show();
	}
	
	void toast(String msg, int length) {
		Toast t = Toast.makeText(this, msg, length);
		t.show();
	}
	
	void toast(int i, int length) {
		String msg = Integer.toString(i);
		Toast t = Toast.makeText(this, msg, length);
		t.show();
	}
	
	public void showError(Exception e)
	{
		Toast t = Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG);
		t.show();
	}
	
}
