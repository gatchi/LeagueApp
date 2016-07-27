package com.gatchipatchi.LeagueApp;

import android.app.Activity;
import android.os.Bundle;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Gravity;
import android.content.Intent;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.graphics.BitmapFactory;
import java.util.Queue;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.ArrayList;
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
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import org.json.JSONObject;
import org.json.JSONException;

public class MainActivity extends Activity 
{
	final static String BASE_DOWNLOAD_URL = "http://ddragon.leagueoflegends.com/cdn/6.14.2/";
	final static int TABLE_ROW_WIDTH_DEFAULT = 5;
	final static int TABLE_ROW_WIDTH_PORTRAIT = 5;
	final static int TABLE_ROW_WIDTH_LANDSCAPE = 8;
	
	// Error codes
	final int DEC_NO_INTERNET = 1;
	final int DEC_NO_AVAIL_UPDATES = 2;
	final int DEC_IO_EXCEPTION = 3;
	
	// Settings 
	boolean SETT_RETRY = false;
	
	
	//--------------- Public Objects ------------------//
	
	EditText editText;
	TextView warningText;
	Button refreshButton;
	Button clearButton;
	Button logClearButton;
	ArrayList<String> champList = new ArrayList();
	int tvId = View.generateViewId();
	int downloadErrorCode;
	
	
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
	
	class DownloadFilesTask extends AsyncTask<String, Integer, InputStream> {
		
		String[] filenames;
		String[] directories;
		Queue<Pack> queue;
		ProgressBar pBar;
		
		DownloadFilesTask(Queue<Pack> downloadQueue) {
			
			queue = downloadQueue;
		}
		
		protected void onPreExecute() {
			
			pBar = (ProgressBar)findViewById(R.id.update_progress);
			
			if (pBar != null) {
				pBar.setVisibility(View.VISIBLE);
			}
			else {
				Debug.log(getApplicationContext(), "Cant make progress bar visible as it doesnt exist");
			}
		}
		
		protected InputStream doInBackground(String... s) {
			
			Pack pack;
			HttpURLConnection urlConnection;
			URL url;
			File file;
			InputStream in = null;
			byte[] buffer;
			int queueLength;
			int count=0;
			
			ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
			boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
			
			if (isConnected | SETT_RETRY) {
			
				try {
					
					queueLength = queue.size();
					
					while (queue.peek() != null)
					{	
						pack = queue.remove();
						url = pack.url;
						urlConnection = (HttpURLConnection) url.openConnection();
						file = new File(getApplicationContext().getDir(pack.directory, Context.MODE_PRIVATE), pack.filename);
						
						// Check to see if files are even in need of downloading
						
						if(!file.exists())
						{
							// Download
							
							/* Debug.log(this, "Downloading " + pack.filename + "..."); */
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
								/* Debug.log(this, "Writing " +  pack.filename + "..."); */
								out.write(buffer);
							}
							catch (IOException e) {
								Debug.log(getApplicationContext(), "Write failed");
								Debug.logError(getApplicationContext(), e);
								return null;
							}
							finally {
								try {
									out.close();
								}
								catch (IOException e) {
									Debug.log(getApplicationContext(), "Couldnt close file");
									Debug.logError(getApplicationContext(), e);
								}
							}
							
							count++;
							publishProgress(count, queueLength);
						}
					}
					
					return in;
					
				}
				catch (IOException e) {
					Debug.log(getApplicationContext(), "Unexpected download error");
					downloadErrorCode = DEC_IO_EXCEPTION;
					return null;
				}
			}
			else {
				Debug.log(getApplicationContext(), "No internet connection.");
				downloadErrorCode = DEC_NO_INTERNET;
				return null;
			}
		}
		
		protected void onProgressUpdate(Integer... progress) {
			
			/*
			 * progress[0] is how many things are processed
			 * progress[1] is the size of the list of things to processed
			 * 
			 * Divide the two to get the percentage of completion
			 */
			
			/* Debug.log(Integer.toString(progress[0]));
			Debug.log(Integer.toString(progress[1])); */
			double percentage = (double)progress[0] / (double)progress[1] * 100;
			pBar.setProgress((int)percentage);
			/* Debug.log((int)percentage); */
		}
		
		protected void onPostExecute(InputStream in) {
			
			if (pBar != null) {
				pBar.setVisibility(View.GONE);
			}
				
			if (in != null) {
				Debug.toast(getApplicationContext(), "Updated complete");
				Debug.log(getApplicationContext(), "Update completed.");
			}
			else if (downloadErrorCode == DEC_NO_INTERNET) {
				Debug.toast(getApplicationContext(), "Cant download, no internet");
				Debug.log(getApplicationContext(), "Update canceled. No internet connection.");
			}
			/* else if (downloadErrorCode == DEC_NO_AVAIL_UPDATES) {
				Debug.toast(getApplicationContext(), "Update not needed");
				Debug.log(getApplicationContext(), "No updates to download.");
				Debug.log(getApplicationContext(), "Update canceled.");
			} */
			else if (downloadErrorCode == DEC_IO_EXCEPTION) {
				Debug.toast(getApplicationContext(), "Update fucked up");
				Debug.log(getApplicationContext(), "Download canceled: IOException.");
			}
			else {
				Debug.toast(getApplicationContext(), "Update not needed");
				Debug.log(getApplicationContext(), "Update canceled: no updates to download.");
			}
			recreate();
		}
	}
	
	
	//--------------- onCreate (main) -----------------//
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
		
		// Make sure this directory exists
		initDirectories();
		
		// Setup champ buttons
		File champListFile = new File(this.getDir(ChampInfo.CHAMPIONS_DIR, Context.MODE_PRIVATE), ChampInfo.CHAMPIONS_FILE);
		if (champListFile.exists()) {
			
			// Load champ list
			Debug.log(this, "Champ list found.  Loading...");
			
			try
			{
				// Champ list parser (comma is delimiter)
				BufferedReader listReader = new BufferedReader(new FileReader(champListFile));
				int ic = 0;
				char c = 0;
				int d = 0;
				String word = "";
				
				while ((c != ']') & (ic != -1))
				{
					do {
						ic = listReader.read();
						c = (char)ic;
						
						if ((c != '[') & (c != ',') & (c != ']')) {
							word = word + c;
						}
					} while ((c != ',') & (c != ']') & (ic != -1));
					
					champList.add(word);
					
					if (c != ']')
					{
						listReader.skip(1);
						word = "";
						c = 0;
					}
					
					// To prevent hanging
					if (d > 600)
					{
						Debug.log(this, "Force break of list-reading loop");
						break;
					}
					else d++;
				}
			
				Debug.log(this, "Champ list generated");
				
				// Add champ buttons iteratively
				Iterator<String> champIterator = champList.listIterator();
				LinearLayout ll = new LinearLayout(this);
				TableLayout tl = (TableLayout)findViewById(R.id.button_layout);
				int count = 0;
				
				while (champIterator.hasNext()) {
				
					if (warningText != null) {
						warningText.setVisibility(View.GONE);
					}
					
					// Check orientation, adjust row size if needed
					int width = TABLE_ROW_WIDTH_DEFAULT;
					int orientation = getResources().getConfiguration().orientation;
					if (orientation == Configuration.ORIENTATION_PORTRAIT) {
						width = TABLE_ROW_WIDTH_PORTRAIT;
					}
					else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
						width = TABLE_ROW_WIDTH_LANDSCAPE;
					}
						
					if (count % width == 0) {
						ll = new LinearLayout(this);
						ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
						ll.setGravity(Gravity.CENTER_HORIZONTAL);
						tl.addView(ll);
					}
					
					ImageButton ib = new ImageButton(this);
					
					// Populate icons
					String champName = champIterator.next();
					File championIconFile = new File(this.getDir("drawable", Context.MODE_PRIVATE), champName + ".png");
					InputStream iconStream = new BufferedInputStream(new FileInputStream(championIconFile));
					ib.setImageBitmap(BitmapFactory.decodeStream(iconStream));
					ib.setAdjustViewBounds(true);
					ib.setScaleType(ScaleType.FIT_CENTER);
					ib.setCropToPadding(false);
					ib.setPadding(0,0,0,0);
					ib.setOnClickListener(champButtonListener);
					ib.setId(count);
					ll.addView(ib, 87, 87);
					
					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(87, 87);
					params.setMargins(2,2,2,2);
					ib.setLayoutParams(params);
					
					count++;
				}
				Debug.log(this, "Layout generated");
			}
			catch (FileNotFoundException e) {
				Debug.toast(this, "Champ missing. Try updating");
				Debug.log(this, "FileNotFoundException");
				Debug.logError(this, e);
			}
			catch (IOException e) {
				Debug.log(this, "IOException");
				Debug.showError(this, e);
				Debug.logError(this, e);
			}
		}
		else {
			
			// Ask to update
			Debug.log(this, "Adding simple TextView to empty View...");
			TableLayout tl = (TableLayout)findViewById(R.id.button_layout);
			warningText = new TextView(this);
			warningText.setText("Welcome to LeagueApp!  If this is your first use, please update twice.");
			warningText.setGravity(Gravity.CENTER);
			tl.addView(warningText);
			Debug.log(this, "Added.");
			Debug.toast(this, "Tap update to fetch data");
		}
		
		// Add data refresh button
		LinearLayout footer = (LinearLayout) findViewById(R.id.footer);
		refreshButton = new Button(this);
		refreshButton.setText("update");
		footer.addView(refreshButton);
		refreshButton.setOnClickListener(updateButtonListener);
		
		/* // Add data clear button
		
		clearButton = new Button(this);
		clearButton.setText("clear data");
		footer.addView(clearButton);
		clearButton.setOnClickListener(clearButtonListener); */
		
		// Add log clear button
		logClearButton = new Button(this);
		logClearButton.setText("clear log");
		footer.addView(logClearButton);
		logClearButton.setOnClickListener(logClearButtonListener);
		
		// Add listener to search bar
		editText = (EditText) findViewById(R.id.search);
		editText.setOnEditorActionListener(searchListener);
	}
	
	
	//--------------- Class Methods -------------------//
	
	void storeFile(byte[] data, String filename, String directory) throws NullPointerException {
		
		File file = new File(this.getDir(directory, Context.MODE_PRIVATE), filename);

		// Save file unless it already exists
		
		if (!file.exists()) {
		
			Debug.toast(this, "storing file " + filename + "...", Toast.LENGTH_SHORT);
			OutputStream out = null;
			
			// Write to file
			
			try {
				out = new BufferedOutputStream(new FileOutputStream(file));
				out.write(data);
				out.close();
				Debug.log(this, filename + " written");
			}
			catch (IOException e) {
				Debug.log(this, filename + " write failure");
				Debug.logError(this, e);
			}
		}
		else {
			Debug.log(this, filename + " already on card");
		}
	}
	
	void writeTextFile(String directory, String filename, String text) {
		
		File file;
		if (directory == null) {
			file = new File(getApplicationContext().getFilesDir(), filename);
		}
		else {
			file = new File(getApplicationContext().getDir(directory, Context.MODE_PRIVATE), filename);
		}

		// Save file unless it already exists
		
		if (!file.exists()) {
		
			Debug.log(this, "storing file... " + filename);
			BufferedWriter out = null;
			
			// Write

			try {
				out = new BufferedWriter(new FileWriter(file));
				out.write(text);
				out.close();
				Debug.log(this, filename + " written");
			}
			catch (IOException e) {
				Debug.log(this, filename + " write failure");
				Debug.logError(this, e);
			}
		}
	}
	
	void downloadData() {
		
		// Check for to see if champ list exists
		File champListFile = new File(getApplicationContext().getDir(ChampInfo.CHAMPIONS_DIR, Context.MODE_PRIVATE), ChampInfo.CHAMPIONS_FILE);
		File championJsonFile = new File(getApplicationContext().getDir(ChampInfo.CHAMPIONS_DIR, Context.MODE_PRIVATE), ChampInfo.CHAMPIONS_JSON);
		
		if (!champListFile.exists()) {
			
			Debug.log(this, "Champ list not found.");
			
			// If champ file doesnt exist, check for champ json and generate the list from it
			if (championJsonFile.exists()) {
				
				Debug.log(this, "Champ JSON found.  Generating list...");
				
				JSONObject json = null;
				InputStream in = null;
				
				try {
					
					in = new BufferedInputStream(new FileInputStream(championJsonFile));
					
					JSONObject championJson = new JSONObject(inputStreamToString(in));
					JSONObject champData = championJson.getJSONObject("data");
					
					// Get champ list from JSON object and put into alpha ordered list
					Iterator<String> champs = champData.keys();
					
					while (champs.hasNext()) {
						champList.add(champs.next());
					}
					
					java.util.Collections.sort(champList);
					Debug.log(this, "Champ list generated");
					
					// Save champ list
					writeTextFile("champs", "champ_list.txt", champList.toString());
					Debug.log(this, "Champ list saved");
					
					// Make & prepare download queue
					Pack pack;
					URL url;
					Iterator<String> champIterator = champList.iterator();
					Queue<Pack> queue = new ArrayDeque<Pack>(champList.size() * 2);
					String champName;
					
					try {
						
						Debug.log(this, "Prepping data download...");
						
						while (champIterator.hasNext()) {
						
							champName = champIterator.next();
							
							url = new URL(BASE_DOWNLOAD_URL + "img/champion/" + champName + ".png");
							pack = new Pack(champName + ".png", ChampInfo.ICONS_DIR, url);
							queue.add(pack);
							
							url = new URL(BASE_DOWNLOAD_URL + "data/en_US/champion/" + champName + ".json");
							pack = new Pack(champName + ".json", ChampInfo.CHAMPIONS_DIR, url);
							queue.add(pack);
						}
						
						// Download data
						
						Debug.log(this, "Attempting data download");
						new DownloadFilesTask(queue).execute();
						/* startUpdate(queue); */
						/* Intent intent = new Intent(this, ChampInfo.class);
						intent.putExtra("downloadQueue", queue);
						startActivity(intent); */
						
					}
					catch (MalformedURLException e) {
						Debug.log(this, "Download abandoned");
						Debug.log(this, "you fucked up the url");
						Debug.showError(this, e);
						Debug.logError(this, e);
					}
					catch (NullPointerException e) {
						Debug.log(this, "Download abandoned");
						Debug.log(this, "NullPointerException");
						Debug.showError(this, e);
						Debug.logError(this, e);
					}
					catch (NoSuchElementException e) {
						Debug.log(this, "Download abandoned");
						Debug.log(this, "champList not initialized");
						Debug.showError(this, e);
						Debug.logError(this, e);
					}
				}
				catch (FileNotFoundException e) {
					Debug.log(this, "List generation failed");
					Debug.log(this, "FileNotFoundException");
					Debug.showError(this, e);
					Debug.logError(this, e);
				}
				catch (NoSuchElementException e) {
					Debug.log(this, "List generation failed");
					Debug.log(this, "Cant get keys from champion.json");
					Debug.showError(this, e);
					Debug.logError(this, e);
				}
				catch (UnsupportedEncodingException e) {
					Debug.log(this, "List generation failed");
					Debug.log(this, "Something is wrong with the champion json file");
					Debug.showError(this, e);
					Debug.logError(this, e);
				}
				catch (JSONException e) {
					Debug.log(this, "List generation failed");
					Debug.log(this, "JSONException");
					Debug.showError(this, e);
					Debug.logError(this, e);
				}
				catch (IOException e) {
					Debug.log(this, "List generation failed");
					Debug.log(this, "IOException");
					Debug.showError(this, e);
					Debug.logError(this, e);
				}
			}
			
			else if (!champListFile.exists() & !championJsonFile.exists()) {

				// If neither exists, download champ json and restart activity
				Debug.log(this, "No champ list or JSON");
				
				try {
					Debug.log(this, "Prepping champ JSON download...");
					
					Queue<Pack> queue = new ArrayDeque<Pack>(1);
					URL url = new URL(BASE_DOWNLOAD_URL + "data/en_US/champion.json");
					Pack pack = new Pack(ChampInfo.CHAMPIONS_JSON, ChampInfo.CHAMPIONS_DIR, url);
					queue.add(pack);
					
					// Download champion JSON
					Debug.log(this, "Attempting champ JSON download");
					new DownloadFilesTask(queue).execute();
					/* startUpdate(queue); */
					/* Intent intent = new Intent(this, ChampInfo.class);
					intent.putExtra("download queue", queue);
					startActivity(intent); */
				}
				catch (MalformedURLException e) {
					Debug.log(this, "JSON download abandoned");
					Debug.log(this, "you fucked up the url buddy");
					Debug.showError(this, e);
					Debug.logError(this, e);
				}
			}
			
			else {
				Debug.toast(this, "how did you even get here");
				Debug.toast(this, "nothing downloaded everything is fucked");
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
				
				Debug.log(this, "Prepping data download...");
				
				while (champIterator.hasNext()) {
				
					champName = champIterator.next();
					/* Debug.log(champName); */
					
					url = new URL(BASE_DOWNLOAD_URL + "img/champion/" + champName + ".png");
					pack = new Pack(champName + ".png", ChampInfo.ICONS_DIR, url);
					queue.add(pack);
					
					url = new URL(BASE_DOWNLOAD_URL + "data/en_US/champion/" + champName + ".json");
					pack = new Pack(champName + ".json", ChampInfo.CHAMPIONS_DIR, url);
					queue.add(pack);
				}
				
				// Download data
				Debug.log(this, "Attempting data download");
				new DownloadFilesTask(queue).execute();
				/* startUpdate(queue); */
				
			}
			catch (MalformedURLException e) {
				Debug.log(this, "Download abandoned");
				Debug.log(this, "you fucked up the url");
				Debug.showError(this, e);
				Debug.logError(this, e);
			}
			catch (NullPointerException e) {
				Debug.log(this, "Download abandoned");
				Debug.log(this, "NullPointerException");
				Debug.showError(this, e);
				Debug.logError(this, e);
			}
			catch (NoSuchElementException e) {
				Debug.log(this, "Download abandoned");
				Debug.log(this, "champList not initialized");
				Debug.showError(this, e);
				Debug.logError(this, e);
			}
		}
	}
	
	void startUpdate(Queue<Pack> queue) {
		
		Intent intent = new Intent(this, ChampInfo.class);
		/* intent.putExtra("downloadQueue", queue); */
		startActivity(intent);
	}
	
	void clearDownloads() {
		
		// Delete all downloaded files
		
		File file;
		File dir;
		
		file = new File(this.getDir(ChampInfo.CHAMPIONS_DIR, Context.MODE_PRIVATE), "champ_list.txt");
		if (file.exists())
		{
			file.delete();
			Debug.log(this, "Deleted champ_list.txt");
		}
		
		file = new File(this.getDir(ChampInfo.CHAMPIONS_DIR, Context.MODE_PRIVATE), "champion.json");
		if (file.exists())
		{
			file.delete();
			Debug.log(this, "Deleted champion.json");
		}
		
		file = new File(this.getDir(ChampInfo.CHAMPIONS_DIR, Context.MODE_PRIVATE), "Aatrox.json");
		if (file.exists())
		{
			file.delete();
			Debug.log(this, "Deleted Aatrox.json");
		}
		
		file = new File(this.getDir(ChampInfo.CHAMPIONS_DIR, Context.MODE_PRIVATE), "Ahri.json");
		if (file.exists())
		{
			file.delete();
			Debug.log(this, "Deleted Ahri.json");
		}
		
		file = new File(this.getDir(ChampInfo.CHAMPIONS_DIR, Context.MODE_PRIVATE), "Akali.json");
		if (file.exists())
		{
			file.delete();
			Debug.log(this, "Deleted Akali.json");
		}
		
		file = new File(this.getDir(ChampInfo.CHAMPIONS_DIR, Context.MODE_PRIVATE), "Alistar.json");
		if (file.exists())
		{
			file.delete();
			Debug.log(this, "Deleted Alistar.json");
		}
		
		file = new File(this.getDir(ChampInfo.ICONS_DIR, Context.MODE_PRIVATE), "Aatrox.png");
		if (file.exists())
		{
			file.delete();
			Debug.log(this, "Deleted Aatrox.png");
		}
		
		file = new File(this.getDir(ChampInfo.ICONS_DIR, Context.MODE_PRIVATE), "Ahri.png");
		if (file.exists())
		{
			file.delete();
			Debug.log(this, "Deleted Ahri.png");
		}
		
		file = new File(this.getDir(ChampInfo.ICONS_DIR, Context.MODE_PRIVATE), "Akali.png");
		if (file.exists())
		{
			file.delete();
			Debug.log(this, "Deleted Akali.png");
		}
		
		file = new File(this.getDir(ChampInfo.ICONS_DIR, Context.MODE_PRIVATE), "Alistar.png");
		if (file.exists())
		{
			file.delete();
			Debug.log(this, "Deleted Alistar.png");
		}
		
		file = new File(getApplicationContext().getDir(ChampInfo.CHAMPIONS_DIR, Context.MODE_PRIVATE), ChampInfo.CHAMPIONS_FILE);
		dir = file.getParentFile();
		Debug.log(this, dir.toString());
		if (dir.exists())
		{
			dir.delete();
			Debug.log(this, "Deleted app_champs directory");
		}
		
		file = new File(getApplicationContext().getDir(ChampInfo.ICONS_DIR, Context.MODE_PRIVATE), "Aatrox.png");
		dir = file.getParentFile();
		Debug.log(this, dir.toString());
		if (dir.exists())
		{
			dir.delete();
			Debug.log(this, "Deleted app_drawable directory");
		}
		
		Debug.toast(this, "Data deleted");
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
			Debug.toast(this, "making champ directory");
			championDirectory.mkdir();
		}
		
		File drawableDir = this.getDir("drawable", Context.MODE_PRIVATE);
		if (!drawableDir.exists()) {
			Debug.toast(this, "creating drawable directory");
			drawableDir.mkdir();
		}
		
	}
	
	
	//----------------- Listeners ---------------------//
	
	private OnClickListener updateButtonListener = new OnClickListener() {
		
		@Override
		public void onClick(View p1)
		{
			// button click action
			/* download("champion.json", "champs", "data/en_US/champion.json"); */
			Debug.toast(getApplicationContext(), "Updating...");
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
	
	
	//---------------- Debug Methods ------------------//
	
	/* static void toast(Context context, String msg) {
		Toast t = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
		t.show();
	}
	
	static void toast(Context context, String msg, int length) {
		Toast t = Toast.makeText(context, msg, length);
		t.show();
	}
	
	static void toast(Context context, int i, int length) {
		String msg = Integer.toString(i);
		Toast t = Toast.makeText(context, msg, length);
		t.show();
	} */
	
	/* static void showError(Context context, Exception e) {
		if (e != null) {
			Toast t = Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG);
			t.show();
		}
	} */
	
	/* static void appendTextFile(Context context, String directory, String filename, String text)
	{	
		File file;
		if (directory == null) {
			file = new File(context.getFilesDir(), filename);
		}
		else {
			file = new File(context.getDir(directory, Context.MODE_PRIVATE), filename);
		}

		// Save file unless it already exists
		
		PrintWriter out;
		
		// Write

		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
			out.println(text);
			
			if (out.checkError()) {
				Debug.show(getApplicationContext(), "Oops, PrintWriter error'd");
			}
			
			out.close();
		}
		catch (IOException e) {
			Debug.toast(context, "file write failure");
			Debug.toast(context, e.getMessage(), Toast.LENGTH_SHORT);
		}
	} */
	
	/* static void logError(Context context, Exception e) {
		if (e != null) {
			appendTextFile(context, null, Debug.LOG_FILE, e.getMessage());
		}
	}
	
	static void log(Context context, String s) {
		appendTextFile(context, null, Debug.LOG_FILE, s);
	}
	
	static void log(Context context, int i) {
		appendTextFile(context, null, Debug.LOG_FILE, Integer.toString(i));
	} */
	
	void clearLog() {
		File file = new File(getApplicationContext().getFilesDir(), Debug.LOG_FILE);
		if (file.exists()) {
			file.delete();
			Debug.toast(getApplicationContext(), "Log cleared");
		}
	}
	
	void stubMethod() {
		Toast toast = Toast.makeText(getApplicationContext(), "Not implemented", Toast.LENGTH_SHORT);
		toast.show();
	}
}
