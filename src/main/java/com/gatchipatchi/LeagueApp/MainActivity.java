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
import android.content.Context;
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
import java.io.FileInputStream;
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
	Button refreshButton;
	File asyncFile;
	
	// onCreate
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
		
		// make sure this directory exists
		//initDirectory();
		
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
		refreshButton.setText("refresh data");
		footer.addView(refreshButton);
		refreshButton.setOnClickListener(refreshButtonListener);		
		
		// add listener to search bar
		editText = (EditText) findViewById(R.id.search);
		editText.setOnEditorActionListener(searchListener);

		
		// load champion list into app
		try {
			getChampList();
		} catch (FileNotFoundException e) {
			toast("Could not retrieve champ list");
			showError(e);
		}
		
	}
	
	
	/*----------- MainActivity Private Methods and Handlers --------------*/
	
	void initDirectory() {
		File championDirectory = this.getDir("champs", Context.MODE_PRIVATE);
		if (!championDirectory.exists()) {
			toast("making champ directory");
			championDirectory.mkdir();
		}
	}
	
/* 	void download(String filename, String shortUrl)
	{
		// attempt a file download
		File jsonFile = new File(this.getFilesDir(), filename);
		String baseUrl = "http://ddragon.leagueoflegends.com/cdn/6.11.1/";
		if (!jsonFile.exists())
		{
			toast("File doesnt exist.  Attempting download.", Toast.LENGTH_SHORT);
			AsyncHttpClient client = new AsyncHttpClient();
			asyncHandler.takeFilename(filename);
			client.get(baseUrl + shortUrl, asyncHandler);
		}
		else {
			toast("Up to date");
		}
	} */
	
	void getChampList() throws FileNotFoundException
	{
		File champListFile;
		champListFile = new File(this.getDir("champs", Context.MODE_PRIVATE), "champion.json");
		if (!champListFile.exists()) {
			toast("No champ list file.  Downloading...");
			download("champion.json", "champs", "data/en_US/champion.json");
		}
		
		InputStream in = null;

		in = new BufferedInputStream(new FileInputStream(champListFile));
	
	
		JSONObject json = null;
		try {
			/* Convert input stream to a string for older APIs.
			(Newer JSON libraries dont need this step.) */
			BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			StringBuilder responseStrBuilder = new StringBuilder();
			String inputStr;
			
			while ((inputStr = streamReader.readLine()) != null) {
				responseStrBuilder.append(inputStr);
			}
			
			json = new JSONObject(responseStrBuilder.toString());
			JSONObject champData = json.getJSONObject("data");
			
			// get champ list and put into alpha ordered list
			Iterator<String> champs = champData.keys();
			ArrayList<String> champList = new ArrayList();
			int i = 0;
			while (champs.hasNext())
			{
				champList.add(champs.next());
				i++;
			}
			java.util.Collections.sort(champList);
			
			
			// download champ icons
			File drawableDir = this.getDir("drawable", Context.MODE_PRIVATE);
			if (!drawableDir.exists())
			{
				toast("creating drawable directory");
				drawableDir.mkdir();
			}
			download("Aatrox.png", "drawable", "img/champion/Aatrox.png");
			
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
	
	void download(String filename, String directory, String shortUrl)
	{
		// attempt a file download
		File jsonFile = new File(this.getDir(directory, Context.MODE_PRIVATE), filename);
		String baseUrl = "http://ddragon.leagueoflegends.com/cdn/6.11.1/";
		if (!jsonFile.exists())
		{
			toast("File doesnt exist.  Attempting download.", Toast.LENGTH_SHORT);
			AsyncHttpClient client = new AsyncHttpClient();
			asyncHandler.takeFilename(filename);
			asyncHandler.takeDirectory(directory);
			client.get(baseUrl + shortUrl, asyncHandler);
		}
		else {
			toast("Up to date");
		}
	}
	
	private FilesAsyncHttpResponseHandler asyncHandler = new FilesAsyncHttpResponseHandler() {
		
		@Override
		public void onStart() {
        // called before request is started
		  toast("starting download...");
		}

		@Override
		public void onSuccess(int statusCode, Header[] headers, byte[] response) {
			// called when response HTTP status is "200 OK"
			toast("download successful");
			storeFile(response, filename, directory);
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
	
/* 	void storeFile(byte[] data, String filename)
	{
		File file = new File(this.getDir("drawable", Context.MODE_PRIVATE), "Aatrox.png");

		// save file unless it already exists
		if (!file.exists())
		{
			toast("storing file " + filename + "...", Toast.LENGTH_SHORT);
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
				toast(e.getMessage(), Toast.LENGTH_LONG);
			}
		}
	} */
	
	void storeFile(byte[] data, String filename, String directory)
	{
		File file = new File(this.getDir(directory, Context.MODE_PRIVATE), filename);

		// save file unless it already exists
		if (!file.exists())
		{
			toast("storing file " + filename + "...", Toast.LENGTH_SHORT);
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
				toast(e.getMessage(), Toast.LENGTH_LONG);
			}
		}
		else {
			toast("files up to date");
		}
	}
	
	void storeIcon(String data)
	{
		String filename = "champ_list.txt";
		File file = new File(this.getFilesDir(), filename);

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
	
	void storeTextFile(String data)
	{
		String filename = "champ_list.txt";
		File file = new File(this.getFilesDir(), filename);

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
			download("champion.json", "champs", "data/en_US/champion.json");
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
	
	// stub method for testing
	void stubMethod()
	{
		// stub
		Toast toast = Toast.makeText(getApplicationContext(), "Tada!", Toast.LENGTH_SHORT);
		toast.show();
	}
}
