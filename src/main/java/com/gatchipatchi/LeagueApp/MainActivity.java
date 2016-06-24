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
import android.widget.TableLayout;
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
import android.graphics.BitmapFactory;
import java.util.Queue;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.lang.Runnable;
import org.json.JSONObject;
import org.json.JSONException;

public class MainActivity extends Activity 
{
	final static String CHAMPIONS_FILE = "champ_list.txt";
	final static String CHAMPIONS_JSON = "champion.json";
	final static String CHAMPIONS_DIR = "champs";
	final static String ICONS_DIR = "drawable";
	final static String ERROR_LOG = "error.log";
	final static String LOG_FILE = "log";
	final static String BASE_DOWNLOAD_URL = "http://ddragon.leagueoflegends.com/cdn/6.11.1/";
	final static int TABLE_ROW_WIDTH = 5;
	
	
	//--------------- Public Objects ------------------//
	
	EditText editText;
	TextView warningText;
	Button refreshButton;
	Button clearButton;
	Button logClearButton;
	ArrayList<String> champList = new ArrayList();
	int tvId = View.generateViewId();
	
	/* // Must stay up here so its accesible by the HttpClient and Downloader
	ArrayDeque<Pack> downloadQueue;
	Pack currentDownload; */

	
	
	//--------------- Nested Classes ------------------//
	
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
	
	class DownloadFilesTask extends AsyncTask<String, Object, InputStream> {
		
		String[] filenames;
		String[] directories;
		Queue<Pack> queue;
		
		DownloadFilesTask(Queue<Pack> downloadQueue){
			
			queue = downloadQueue;
		}
		int i = 0;
		
		protected InputStream doInBackground(String... s) {
			
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
						
						/* publishProgress("Downloading " + pack.filename + "...", null); */
						log("Downloading " + pack.filename + "...");
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
							/* publishProgress("Writing " +  pack.filename + "...", null); */
							log("Writing " +  pack.filename + "...");
							out.write(buffer);
						}
						catch (IOException e) {
							/* publishProgress("Write failed", e); */
							/* appendTextFile(null, LOG_FILE, e.getMessage()); */
							log("Write failed");
							logError(e);
							return null;
						}
						finally {
							try {
								out.close();
							}
							catch (IOException e) {
								/* publishProgress("Couldnt close file", e); */
								/* appendTextFile(null, LOG_FILE, e.getMessage()); */
								log("Couldnt close file");
								logError(e);
							}
						}
						
					}
				}
				
				return in;
				
			} catch (IOException e) {
				publishProgress("Unexpected download error", e);
				return null;
			}
		}
		
		protected void onProgressUpdate(Object... objArray) {
			
			/*
			 * Takes in "params..." which means it can take in any amount of
			 * arguments, which are put into an array that the method can use.
			 * However, for this method, the following convention should be used:
			 * 
			 * param 1: a message (String)
			 * param 2: an error (Exception)
			 * 
			 * Use null for unused elements.
			 * 
			 * Lastly, since the params is untyped (plain Objects), dont forget to cast each
			 * member of the array before using.
			 * 
			 */
			
			if (objArray[0] != null) {
				String message = (String)objArray[0];
				toast(message);
			}
			if (objArray[1] != null) {
				Exception errorMesg = (Exception)objArray[1];
				showError(errorMesg);
			}
			
		}
		
		protected void onPostExecute(InputStream in) {
			
			if (in != null) {
				toast("Update complete");
			}
			else {
				toast("Update failed or not needed");
			}
			recreate();
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
			
		
	//--------------- onCreate (main) -----------------//
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
		
		// Make sure this directory exists
		
		initDirectories();
		
		// Setup champ buttons
		
		File champListFile = new File(this.getDir(CHAMPIONS_DIR, Context.MODE_PRIVATE), CHAMPIONS_FILE);
		/* File championJsonFile = new File(this.getDir(CHAMPIONS_DIR, Context.MODE_PRIVATE), CHAMPIONS_JSON); */
		
		if (champListFile.exists()) {
			
			// Load champ list
			/* toast("Champ list found.  Loading..."); */
			log("Champ list found.  Loading...");
			
			try {
				
				BufferedReader listReader = new BufferedReader(new FileReader(champListFile));
				int ic = 0;
				char c = 0;
				int d = 0;
				String word = "";
				
				/* log("Entering loop..."); */
				
				while ((c != ']') & (ic != -1)) {
					
					// Comma is the delimiter
					
					do {
					
						ic = listReader.read();
						c = (char)ic;
						/* log(String.valueOf(c)); */
						
						if ((c != '[') & (c != ',') & (c != ']')) {
							word = word + c;
						}
					} while ((c != ',') & (c != ']') & (ic != -1));
					
					champList.add(word);
					/* log("Made word"); */
					/* log(word); */
					
					if (c != ']') {
						listReader.skip(1);
						word = "";
						c = 0;
					}
					
					// To prevent hanging
					if (d > 600) {
						log("Force break of list-reading loop");
						break;
					}
					else {
						d++;
					}
				}
			
				/* log("Left loop."); */
				/* java.util.Collections.sort(champList); */
				log("Champ list generated");
				
				// Add champ buttons iteratively
					
				Iterator<String> champIterator = champList.listIterator();
				LinearLayout ll = new LinearLayout(this);
				TableLayout tl = (TableLayout)findViewById(R.id.button_layout);
				/* TableRow tr = new TableRow(this); */
				int count = 0;
				
				for (int i=0; i<161; i++) {
				
					if (warningText != null) {
						warningText.setVisibility(View.GONE);
					}
					
					if (count % TABLE_ROW_WIDTH == 0) {
						ll = new LinearLayout(this);
						ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
						ll.setGravity(Gravity.CENTER_HORIZONTAL);
						/* log("Added row"); */
						tl.addView(ll);
					}
					
					ImageButton ib = new ImageButton(this);
					
					// Populate icons
					
					String champName = champIterator.next();
					File championIconFile = new File(this.getDir("drawable", Context.MODE_PRIVATE), champName + ".png");
					InputStream iconStream = new BufferedInputStream(new FileInputStream(championIconFile));
					/* Resources res = getResources();
					TypedArray champIcons = res.obtainTypedArray(R.array.champ_icons);
					ib.setImageResource(champIcons.getResourceId(i, 0)); */
					ib.setImageBitmap(BitmapFactory.decodeStream(iconStream));
					/* champIcons.recycle(); */
					
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
					
					/* log("Added button."); */
					count++;
				}
				toast("Layout generated");
			}
			catch (FileNotFoundException e) {
				toast("Champ missing. Try updating");
				log("FileNotFoundException");
				/* showError(e); */
				logError(e);
			}
			catch (IOException e) {
				log("IOException");
				showError(e);
				logError(e);
			}
		}
		else {
			
			// Ask to update
			
			log("Adding simple TextView to empty View...");
			TableLayout tl = (TableLayout)findViewById(R.id.button_layout);
			warningText = new TextView(this);
			warningText.setText("No data");
			warningText.setBackgroundColor(0xFFFF);
			tl.addView(warningText);
			log("Added.");
			
			/* 
			
				
				
			} catch (FileNotFoundException e) {
				showError(e);
			} catch (JSONException e) {
				showError(e);
			} catch (UnsupportedEncodingException e) {
				toast("InputStream to String conversion failed");
				showError(e);
			} catch (IOException e) {
				toast("InputStream to String conversion failed");
				showError(e);
			} */
		}
		
		// Add data refresh button
		
		LinearLayout footer = (LinearLayout) findViewById(R.id.footer);
		refreshButton = new Button(this);
		refreshButton.setText("update");
		footer.addView(refreshButton);
		refreshButton.setOnClickListener(refreshButtonListener);
		
		// Add data clear button
		
		clearButton = new Button(this);
		clearButton.setText("clear data");
		footer.addView(clearButton);
		clearButton.setOnClickListener(clearButtonListener);
		
		// Add log clear button
		
		logClearButton = new Button(this);
		logClearButton.setText("clear log");
		footer.addView(logClearButton);
		logClearButton.setOnClickListener(logClearButtonListener);
		
		// Add listener to search bar
		
		editText = (EditText) findViewById(R.id.search);
		editText.setOnEditorActionListener(searchListener);
	}
	
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
	
	
	//--------------- Class Methods -------------------//
	
	void storeFile(byte[] data, String filename, String directory) throws NullPointerException {
		
		File file = new File(this.getDir(directory, Context.MODE_PRIVATE), filename);

		// Save file unless it already exists
		
		if (!file.exists())
		{
			toast("storing file " + filename + "...", Toast.LENGTH_SHORT);
			OutputStream out = null;
			
			// Write to file
			
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
	
	void writeTextFile(String directory, String filename, String text)
	{	
		File file;
		if (directory == null) {
			file = new File(getApplicationContext().getFilesDir(), filename);
		}
		else {
			file = new File(getApplicationContext().getDir(directory, Context.MODE_PRIVATE), filename);
		}

		// Save file unless it already exists
		
		if (!file.exists())
		{
			toast("storing file...", Toast.LENGTH_LONG);
			BufferedWriter out = null;
			
			// Write

			try {
				out = new BufferedWriter(new FileWriter(file));
				out.write(text);
				out.close();
				toast("file written");
			}
			catch (IOException e) {
				toast("file write failure");
				toast(e.getMessage(), Toast.LENGTH_SHORT);
			}
		}
	}
	
	void appendTextFile(String directory, String filename, String text)
	{
		/* toast("Attempting to append log..."); */
		File file;
		if (directory == null) {
			file = new File(getApplicationContext().getFilesDir(), filename);
		}
		else {
			file = new File(getApplicationContext().getDir(directory, Context.MODE_PRIVATE), filename);
		}

		// Save file unless it already exists
		
		/* toast("storing file...", Toast.LENGTH_LONG); */
		PrintWriter out;
		
		// Write

		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
			out.println(text);
			
			if (out.checkError()) {
				/* toast("Oops, PrintWriter error'd"); */
			}
			else {
				/* toast("PrintWriter claimed it worked..."); */
			}
			
			out.close();
			/* toast("file written"); */
		}
		catch (IOException e) {
			toast("file write failure");
			toast(e.getMessage(), Toast.LENGTH_SHORT);
		}
	}
	
	void downloadData() {
		
		// Check for to see if champ list exists
		
		File champListFile = new File(getApplicationContext().getDir(CHAMPIONS_DIR, Context.MODE_PRIVATE), CHAMPIONS_FILE);
		File championJsonFile = new File(getApplicationContext().getDir(CHAMPIONS_DIR, Context.MODE_PRIVATE), CHAMPIONS_JSON);
		
		if (!champListFile.exists()) {
			
			log("Champ list not found.");
			
			// If champ file doesnt exist, check for champ json and generate the list from it
			
			if (championJsonFile.exists()) {
				
				log("Champ JSON found.  Generating list...");
				
				JSONObject json = null;
				InputStream in = null;
				
				try {
					
					in = new BufferedInputStream(new FileInputStream(championJsonFile));
					
					JSONObject championJson = new JSONObject(inputStreamToString(in));
					JSONObject champData = championJson.getJSONObject("data");
					
					// Get champ list from JSON object and put into alpha ordered list
					
					Iterator<String> champs = champData.keys();
					
					while (champs.hasNext()) {
					/* for (int i=0; i<161; i++) { */
						champList.add(champs.next());
					}
					
					java.util.Collections.sort(champList);
					log("Champ list generated");
					
					// Save champ list
					
					writeTextFile("champs", "champ_list.txt", champList.toString());
					log("Champ list saved");
					
					// Make & prepare download queue
			
					Pack pack;
					URL url;
					Iterator<String> champIterator = champList.iterator();
					Queue<Pack> queue = new ArrayDeque<Pack>(champList.size() * 2);
					String champName;
					
					try {
						
						log("Prepping data download...");
						
						for (int i=0; i<161; i++) {
						
							champName = champIterator.next();
							
							url = new URL(BASE_DOWNLOAD_URL + "img/champion/" + champName + ".png");
							pack = new Pack(champName + ".png", ICONS_DIR, url);
							queue.add(pack);
							
							url = new URL(BASE_DOWNLOAD_URL + "data/en_US/champion/" + champName + ".json");
							pack = new Pack(champName + ".json", CHAMPIONS_DIR, url);
							queue.add(pack);
						}
						
						// Download data
						
						log("Attempting download");
						new DownloadFilesTask(queue).execute();
						
					}
					catch (MalformedURLException e) {
						log("Download abandoned");
						log("you fucked up the url");
						showError(e);
						logError(e);
					}
					catch (NullPointerException e) {
						log("Download abandoned");
						log("NullPointerException");
						showError(e);
						logError(e);
					}
					catch (NoSuchElementException e) {
						log("Download abandoned");
						log("champList not initialized");
						showError(e);
						logError(e);
					}
				}
				catch (FileNotFoundException e) {
					log("List generation failed");
					log("FileNotFoundException");
					showError(e);
					logError(e);
				}
				catch (NoSuchElementException e) {
					log("List generation failed");
					log("Cant get keys from champion.json");
					showError(e);
					logError(e);
				}
				catch (UnsupportedEncodingException e) {
					log("List generation failed");
					log("Something is wrong with the champion json file");
					showError(e);
					logError(e);
				}
				catch (JSONException e) {
					log("List generation failed");
					log("JSONException");
					showError(e);
					logError(e);
				}
				catch (IOException e) {
					log("List generation failed");
					log("IOException");
					showError(e);
					logError(e);
				}
			}
			
			else if (!champListFile.exists() & !championJsonFile.exists()) {

				// If neither exists, download champ json and restart activity
				
				log("No champ list or JSON");
				
				try {
					log("Prepping champ JSON download...");
					
					Queue<Pack> queue = new ArrayDeque<Pack>(1);
					URL url = new URL(BASE_DOWNLOAD_URL + "data/en_US/champion.json");
					Pack pack = new Pack(CHAMPIONS_JSON, CHAMPIONS_DIR, url);
					queue.add(pack);
					
					// Download champion JSON
					
					log("Attempting download");
					new DownloadFilesTask(queue).execute();
				}
				catch (MalformedURLException e) {
					log("JSON download abandoned");
					log("you fucked up the url buddy");
					showError(e);
					logError(e);
				}
			}
			
			else {
				toast("how did you even get here");
				toast("nothing downloaded everything is fucked");
			}
			
		}
		else {
			
			// Make & prepare download queue
			
			Pack pack;
			URL url;
			Iterator<String> champIterator = champList.iterator();
			Queue<Pack> queue = new ArrayDeque<Pack>(champList.size() * 2);
			String champName;
			
			try {
				
				log("Prepping data download...");
				
				for (int i=0; i<161; i++) {
				
					champName = champIterator.next();
					
					url = new URL(BASE_DOWNLOAD_URL + "img/champion/" + champName + ".png");
					pack = new Pack(champName + ".png", ICONS_DIR, url);
					queue.add(pack);
					
					url = new URL(BASE_DOWNLOAD_URL + "data/en_US/champion/" + champName + ".json");
					pack = new Pack(champName + ".json", CHAMPIONS_DIR, url);
					queue.add(pack);
				}
				
				// Download data
				
				log("Attempting download");
				new DownloadFilesTask(queue).execute();
				
			}
			catch (MalformedURLException e) {
				log("Download abandoned");
				log("you fucked up the url");
				showError(e);
				logError(e);
			}
			catch (NullPointerException e) {
				log("Download abandoned");
				log("NullPointerException");
				showError(e);
				logError(e);
			}
			catch (NoSuchElementException e) {
				log("Download abandoned");
				log("champList not initialized");
				showError(e);
				logError(e);
			}
		}
	}
	
	void clearDownloads() {
		
		// Delete all downloaded files
		
		File file;
		File dir;
		
		file = new File(this.getDir(CHAMPIONS_DIR, Context.MODE_PRIVATE), "champ_list.txt");
		if (file.exists())
		{
			file.delete();
			log("Deleted champ_list.txt");
		}
		
		file = new File(this.getDir(CHAMPIONS_DIR, Context.MODE_PRIVATE), "champion.json");
		if (file.exists())
		{
			file.delete();
			log("Deleted champion.json");
		}
		
		file = new File(this.getDir(CHAMPIONS_DIR, Context.MODE_PRIVATE), "Aatrox.json");
		if (file.exists())
		{
			file.delete();
			log("Deleted Aatrox.json");
		}
		
		file = new File(this.getDir(CHAMPIONS_DIR, Context.MODE_PRIVATE), "Ahri.json");
		if (file.exists())
		{
			file.delete();
			log("Deleted Ahri.json");
		}
		
		file = new File(this.getDir(CHAMPIONS_DIR, Context.MODE_PRIVATE), "Akali.json");
		if (file.exists())
		{
			file.delete();
			log("Deleted Akali.json");
		}
		
		file = new File(this.getDir(CHAMPIONS_DIR, Context.MODE_PRIVATE), "Alistar.json");
		if (file.exists())
		{
			file.delete();
			log("Deleted Alistar.json");
		}
		
		file = new File(this.getDir(ICONS_DIR, Context.MODE_PRIVATE), "Aatrox.png");
		if (file.exists())
		{
			file.delete();
			log("Deleted Aatrox.png");
		}
		
		file = new File(this.getDir(ICONS_DIR, Context.MODE_PRIVATE), "Ahri.png");
		if (file.exists())
		{
			file.delete();
			log("Deleted Ahri.png");
		}
		
		file = new File(this.getDir(ICONS_DIR, Context.MODE_PRIVATE), "Akali.png");
		if (file.exists())
		{
			file.delete();
			log("Deleted Akali.png");
		}
		
		file = new File(this.getDir(ICONS_DIR, Context.MODE_PRIVATE), "Alistar.png");
		if (file.exists())
		{
			file.delete();
			log("Deleted Alistar.png");
		}
		
		file = new File(getApplicationContext().getDir(CHAMPIONS_DIR, Context.MODE_PRIVATE), CHAMPIONS_FILE);
		dir = file.getParentFile();
		log(dir.toString());
		if (dir.exists())
		{
			dir.delete();
			log("Deleted app_champs directory");
		}
		
		file = new File(getApplicationContext().getDir(ICONS_DIR, Context.MODE_PRIVATE), "Aatrox.png");
		dir = file.getParentFile();
		log(dir.toString());
		if (dir.exists())
		{
			dir.delete();
			log("Deleted app_drawable directory");
		}
		
		toast("Data deleted");
		recreate();
	}
	
	String inputStreamToString(InputStream in) throws UnsupportedEncodingException, IOException {
		
		// Convert input stream to a string
		
		BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		StringBuilder responseStrBuilder = new StringBuilder();
		String inputStr;
		while ((inputStr = streamReader.readLine()) != null) {
			responseStrBuilder.append(inputStr);
		}	
		return responseStrBuilder.toString();
	}
	
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
	
	
	//----------------- Listeners ---------------------//
	
	private OnClickListener refreshButtonListener = new OnClickListener() {
		
		@Override
		public void onClick(View p1)
		{
			// button click action
			/* download("champion.json", "champs", "data/en_US/champion.json"); */
			toast("Updating...");
			downloadData();
		}
	};
	
	private OnClickListener clearButtonListener = new OnClickListener() {
		
		@Override
		public void onClick(View p1)
		{
			// button click action
			clearDownloads();
		}
	};
	
	private OnClickListener logClearButtonListener = new OnClickListener() {
		
		@Override
		public void onClick(View p1)
		{
			clearLog();
		}
	};
	
	private OnClickListener champButtonListener = new OnClickListener() {
		
		@Override
		public void onClick(View p1)
		{
		/* button click action
			Toast toast = Toast.makeText(getApplicationContext(), Integer.toString(p1.getId()), Toast.LENGTH_LONG);
			toast.show(); */
			Intent intent = new Intent(MainActivity.this, ChampInfo.class);
			intent.putExtra("button id", p1.getId());
			intent.putExtra("champ list", champList);
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
	
	
	//----------- Temporary Debug Methods -------------//
	
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
	
	void showError(Exception e) {
		if (e != null) {
			Toast t = Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG);
			t.show();
		}
	}
	
	void logError(Exception e) {
		if (e != null) {
			appendTextFile(null, LOG_FILE, e.getMessage());
		}
	}
	
	void log(String s) {
		appendTextFile(null, LOG_FILE, s);
	}
	
	void clearLog() {
		File file = new File(getApplicationContext().getFilesDir(), LOG_FILE);
		if (file.exists()) {
			file.delete();
			toast("Log cleared");
		}
	}
	
	void stubMethod() {
		Toast toast = Toast.makeText(getApplicationContext(), "Tada!", Toast.LENGTH_SHORT);
		toast.show();
	}
}
