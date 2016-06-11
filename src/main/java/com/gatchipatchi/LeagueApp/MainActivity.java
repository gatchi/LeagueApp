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
import android.os.AsyncTask;
import java.util.Queue;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;
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
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.lang.Runnable;
import org.json.JSONObject;
import org.json.JSONException;
import cz.msebera.android.httpclient.Header;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

public class MainActivity extends Activity 
{
	
	//---------------- Nested Classes ------------------//
	
	class Pack {
		
		String filename;
		String directory;
		URL url;
		
		Pack(String downloadFilename, String downloadDirectory) {
			filename = downloadFilename;
			directory = downloadDirectory;
		}
		
		Pack(String downloadFilename, String downloadDirectory, URL downloadUrl) {
			filename = downloadFilename;
			directory = downloadDirectory;
			url = downloadUrl;
		}
	}
	
	class DownloadFilesTask extends AsyncTask<String, Integer, InputStream> {
		
		String[] filenames;
		String[] directories;
		Queue<Pack> queue;
		
		DownloadFilesTask(Queue<Pack> downloadQueue){
			
			queue = downloadQueue;
		}
		int i = 0;
		
		protected InputStream doInBackground(String... s)
		{
			Pack pack;
			HttpURLConnection urlConnection;
			URL url;
			File file;
			InputStream in = null;
			byte[] buffer;
			
			try {
				while (queue.peek() != null)
				{
					// Just some declarations
					pack = queue.remove();
					url = pack.url;
					urlConnection = (HttpURLConnection) url.openConnection();
					file = new File(getApplicationContext().getDir(pack.directory, Context.MODE_PRIVATE), pack.filename);
					
					// Check to see if files are even in need of downloading
					if(!file.exists())
					{
						// Download
						in = new BufferedInputStream(urlConnection.getInputStream());
					
						// Convert to a byte buffer for filewriting
						buffer = new byte[1024];
						int len;
						OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
						while ((len = in.read(buffer)) != -1) {
							out.write(buffer, 0, len);
						}
					
						// Store file
						try {
							/* toast("Writing file..."); */
							out.write(buffer);
						}
						catch (IOException e) {
							/* toast("Couldnt write to file");
							showError(e); */
							return null;
						}
						finally {
							try {
								out.close();
							}
							catch (IOException e) { }
						}
						
					}
				}
				
				return in;
				
			} catch (IOException e) {
				// No toasting on asynctask
				return null;
			}
		}
		
		protected void onProgressUpdate(Integer... progress) {
			// Can we toast here tho?
			toast(Integer.toString(progress[0]));
			toast("Anything?");
		}
		
		protected void onPostExecute(InputStream in)
		{
			if (in != null) {
				toast("Update complete");
			}
			else {
				toast("Update failed or not needed");
			}
		}
	}
	
	/* class Downloader implements Runnable {
		
		Queue<Pack> downloadQueue;
		
		Downloader(Queue<Pack> queue) {
			downloadQueue = queue;
		}
		
		public void run() {
			
			// While downloadQueue is full, send each pack to the HTTPClient
			while (downloadQueue.peek() != null)
			{
				currentDownload = downloadQueue.remove();
				File file = new File(getApplicationContext().getDir(currentDownload.directory, Context.MODE_PRIVATE), currentDownload.filename);
				String baseUrl = "http://ddragon.leagueoflegends.com/cdn/6.11.1/";
				
				// Using SyncClient so only one download at a time can proceed
				SyncHttpClient httpClient = new SyncHttpClient();
				
				// Only download if file doesnt already exist
				if (!file.exists())
				{
					toast("File " + currentDownload.filename + " doesnt exist. Attempting download.", Toast.LENGTH_SHORT);
					httpClient.get(baseUrl + currentDownload.shortUrl, asyncHandler);
				}
				else {
					toast("Download error");
					toast(currentDownload.filename + " already exists");
				}
			}
		}
		
	} */
			
	//-------------- Public Objects ------------------//
	
	EditText editText;
	Button refreshButton;
	Button clearButton;
	final String BASE_DOWNLOAD_URL = "http://ddragon.leagueoflegends.com/cdn/6.11.1/";
	/* ExecutorService threadpool = Executors.newFixedThreadPool(1); */
	/* int threadNum = 0;
	String downloadDirectory;
	String downloadFilename;
	final ReentrantLock lock = new ReentrantLock(); */
	
	/* // Must stay up here so its accesible by the HttpClient and Downloader
	ArrayDeque<Pack> downloadQueue;
	Pack currentDownload; */
	
	
	//--------------- onCreate (main) -----------------//
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
		
		// Make sure this directory exists
		
		initDirectories();
		
		// Add champ buttons iteratively
		
		LinearLayout ll = (LinearLayout)findViewById(R.id.button_layout);
		for(int i=0; i<4; i++) {
			
			ImageButton ib = new ImageButton(this);
			
			// Populate icons
			
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
		
		// Add data refresh button
		
		LinearLayout footer = (LinearLayout) findViewById(R.id.footer);
		refreshButton = new Button(this);
		refreshButton.setText("refresh data");
		footer.addView(refreshButton);
		refreshButton.setOnClickListener(refreshButtonListener);
		
		// Add data clear button
		
		clearButton = new Button(this);
		clearButton.setText("clear data");
		footer.addView(clearButton);
		clearButton.setOnClickListener(clearButtonListener);
		
		// Add listener to search bar
		
		editText = (EditText) findViewById(R.id.search);
		editText.setOnEditorActionListener(searchListener);

		
		// Load champion list into app

	/* getChampList();
		getIcons(); */
	/* Queue<Pack> queue = new ArrayDeque<Pack>(2);
		queue.add(new Pack("champion.json", "champs", "data/en_US/champion.json"));
		queue.add(new Pack("Aatrox.png", "drawable", "img/champion/Aatrox.png"));
		download(queue);
		download("champion.json", "champs", "data/en_US/champion.json");
		download("Aatrox.png", "drawable", "img/champion/Aatrox.png"); */
/* 		Queue<Pack> queue = new ArrayDeque<Pack>();
		Pack pack = new Pack("champion.json", "champs");
		queue.add(pack);
		URL url;
		toast("Attempting download");
		try {
			url = new URL(BASE_DOWNLOAD_URL + "data/en_US/champion.json");
			new DownloadFilesTask(queue).execute(url);
		} catch (MalformedURLException e) {
			toast("you fucked up the url");
			showError(e);
			toast("abandoned download");
		} */
		downloadData();
		
/* 		} catch (FileNotFoundException e) {
			toast("Could not retrieve champ list");
			showError(e);
		} */
		
	}
	
	
	//---------- MainActivity Private Methods and Handlers ------------//	
	
	void initDirectories() {
		
		// Make sure director structure is correct (new install)
		
		File championDirectory = this.getDir("champs", Context.MODE_PRIVATE);
		if (!championDirectory.exists()) {
			toast("making champ directory");
			championDirectory.mkdir();
		}
		
		File drawableDir = this.getDir("drawable", Context.MODE_PRIVATE);
		if (!drawableDir.exists()) {
			toast("creating drawable directory");
			drawableDir.mkdir();
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
	
	void downloadData() {
		
		// Make & prepare download queue
		Queue<Pack> queue = new ArrayDeque<Pack>();
		URL url;
		try {
			url = new URL(BASE_DOWNLOAD_URL + "data/en_US/champion.json");
			Pack pack = new Pack("champion.json", "champs", url);
			queue.add(pack);
			url = new URL(BASE_DOWNLOAD_URL + "img/champion/Aatrox.png");
			pack = new Pack("Aatrox.png", "drawable", url);
			queue.add(pack);
		
			// Download
			toast("Attempting download");
			new DownloadFilesTask(queue).execute();
			
		} catch (MalformedURLException e) {
			toast("you fucked up the url");
			showError(e);
			toast("abandoned download");
		}
	}
	
	/* void getChampList() {
		
		File champListFile;
		champListFile = new File(this.getDir("champs", Context.MODE_PRIVATE), "champion.json");
		if (!champListFile.exists())
		{
			toast("No champ list file.  Downloading...");
			download("champion.json", "champs", "data/en_US/champion.json");
		}
		
	} */
	
	/* void getIcons() {
		
		// Download champ icons
		
		download("Aatrox.png", "drawable", "img/champion/Aatrox.png");
		
	} */
	
/*	void whatTheFuckIsThis() throws UnsupportedEncodingException, JSONException, IOException {
		
		// Load champlist into JSONObject
		
		File champListFile;
		champListFile = new File(this.getDir("champs", Context.MODE_PRIVATE), "champion.json");
		JSONObject json = null;
		InputStream in = null;
		in = new BufferedInputStream(new FileInputStream(champListFile));
		
	
		// Convert input stream to a string (Android version of org.json requires this)
		
		BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		StringBuilder responseStrBuilder = new StringBuilder();
		String inputStr;
		while ((inputStr = streamReader.readLine()) != null) {
			responseStrBuilder.append(inputStr);
		}	
		json = new JSONObject(responseStrBuilder.toString());
		JSONObject champData = json.getJSONObject("data");
		
		// Get champ list and put into alpha ordered list
		
		Iterator<String> champs = champData.keys();
		ArrayList<StringStringhampList = new ArrayList();
		int i = 0;
		while (champs.hasNext())
		{
			champList.add(champs.next());
			i++;
		}
		java.util.Collections.sort(champList);
		
		
		// Download champ icons
		
		File drawableDir = this.getDir("drawable", Context.MODE_PRIVATE);
		if (!drawableDir.exists())
		{
			toast("creating drawable directory");
			drawableDir.mkdir();
		}
		download("Aatrox.png", "drawable", "img/champion/Aatrox.png");
		
	} */
	
/* 		} catch (UnsupportedEncodingException e) {
			toast("UnsupportedEncodingException", Toast.LENGTH_LONG);
			showError(e);
		} catch (JSONException e) {
			toast("JSONException", Toast.LENGTH_LONG);
			showError(e);
		} catch (IOException e) {
			toast("IOException", Toast.LENGTH_LONG);
			showError(e);
		}
	} */
	
/*	void download(String filename, String directory, String shortUrl) {
		
		// Attempt a file download
		File file = new File(this.getDir(directory, Context.MODE_PRIVATE), filename);
		String baseUrl = "http://ddragon.leagueoflegends.com/cdn/6.11.1/";
		if (!file.exists())
		{
			toast("File " + filename + " doesnt exist. Attempting download.", Toast.LENGTH_SHORT);
			currentDownload = new Pack(filename, directory);
			AsyncHttpClient client = new AsyncHttpClient();
			while (!downloadQueue.isEmpty()) {
				client.get(baseUrl + shortUrl, asyncHandler);
			}
		}
		else {
			toast("Canceled download");
			toast(filename + " already exists");
		}
	} */
	
/*	void download(Queue<Packvoidueue) {
		
		threadNum = threadNum + 1;
		toast("threadnum: " + Integer.toString(threadNum));
		Pack pack;
		
		// Attempt a file download
		while (queue.peek() != null)
		{
			pack = queue.remove();
			File jsonFile = new File(this.getDir(pack.directory, Context.MODE_PRIVATE), pack.filename);
			String baseUrl = "http://ddragon.leagueoflegends.com/cdn/6.11.1/";
			if (!jsonFile.exists())
			{
				toast("File " + pack.filename + " doesnt exist. Attempting download.", Toast.LENGTH_SHORT);
				AsyncHttpClient client = new AsyncHttpClient();
				downloadDirectory = pack.directory;
				downloadFilename = pack.filename;
				client.get(baseUrl + pack.url, asyncHandler);
			}
			else {
				toast("Download error");
				toast(pack.filename + " already exists");
			}
		}
	} */
	
	/* private AsyncHttpResponseHandler asyncHandler = new AsyncHttpResponseHandler() {
		
		@Override
		public void onStart() {
        // called before request is started
		  toast("starting download...");
		}

		@Override
		public synchronized void onSuccess(int statusCode, Header[] headers, byte[] response) {
			// called when response HTTP status is "200 OK"
			toast("download successful");
			try {
				storeFile(response, currentDownload.filename, currentDownload.directory);
			}
			catch (NullPointerException e) {
				toast("Couldnt save file");
				showError(e);
			}
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
	}; */
	
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
	
	void storeFile(byte[] data, String filename, String directory) throws NullPointerException {
		
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
			toast("file already on card");
		}
	}
	
	/* void storeIcon(String data)
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
	} */
	
	/* void storeTextFile(String data)
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
	} */
	
	void clearDownloads() {
		
		// Delete all downloaded files
		File file;
		file = new File(this.getDir("champs", Context.MODE_PRIVATE), "champion.json");
		if (file.exists()) {
			file.delete();
			toast("Deleted champion.json");
		}
		file = new File(this.getDir("drawable", Context.MODE_PRIVATE), "Aatrox.png");
		if (file.exists()) {
			file.delete();
			toast("Deleted Aatrox.png");
		}
	}
	
	// refresh button listener
	private OnClickListener refreshButtonListener = new OnClickListener() {
		
		@Override
		public void onClick(View p1)
		{
			// button click action
			/* download("champion.json", "champs", "data/en_US/champion.json"); */
			downloadData();
			
		}
	};
	
	// clear button listener
	private OnClickListener clearButtonListener = new OnClickListener() {
		
		@Override
		public void onClick(View p1)
		{
			// button click action
			clearDownloads();
		}
	};
	
	// champ button onClick listener
	private OnClickListener champButtonListener = new OnClickListener() {
		
		@Override
		public void onClick(View p1)
		{
		/* button click action
			Toast toast = Toast.makeText(getApplicationContext(), Integer.toString(p1.getId()), Toast.LENGTH_LONG);
			toast.show(); */
			Intent intent = new Intent(MainActivity.this, ChampInfo.class);
			intent.putExtra("button id", p1.getId());
			startActivity(intent);
		}
	};
	
	private OnEditorActionListener searchListener = new OnEditorActionListener() {
		
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
	
	public void showError(Exception e) {
		Toast t = Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG);
		t.show();
	}
	
	// stub method for testing
	void stubMethod() {
		// stub
		Toast toast = Toast.makeText(getApplicationContext(), "Tada!", Toast.LENGTH_SHORT);
		toast.show();
	}
}
