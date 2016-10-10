package com.gatchipatchi.LeagueApp;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.Context;
import android.content.res.Resources;

import java.util.ArrayList;
import android.os.AsyncTask;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import android.net.ConnectivityManager;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.IOException;
import java.util.Iterator;
import android.util.Log;
import android.net.NetworkInfo;
import java.io.OutputStream;
import android.widget.ProgressBar;
import java.util.Queue;
import java.net.URL;
import android.view.View;


class DownloadTask extends AsyncTask<String, Integer, InputStream> {
	
	/*
	 * DownloadTask Class Info
	 * 
	 * Ok, so, this shouldnt need the Pack system i originally designed.
	 * This should only be run per batch, with separate batches for different
	 * file types and urls.
	 * So like, when downloading the icons, that should be its own task.
	 * So all that is really needed is the list of URLs, not a list of packs.
	 * Cause the download directory isnt a list, it doesnt change for this instance.
	 * And you could either do a list of URLs, or a list of url parts (like champ names)
	 * and feed a base url into the constructor.  But the latter is less desirable, as it could
	 * be a pain in re-use.  You can just combine the list names and urls while making the input
	 * list. 
	 */
	
	// Settings 
	boolean SETT_RETRY = false;
	
	// Error codes
	final int DEC_NO_INTERNET = 1;
	final int DEC_NO_AVAIL_UPDATES = 2;
	final int DEC_IO_EXCEPTION = 3;

	int downloadErrorCode;
	Exception downloadException;
	Activity activity;
	String downloadDir;
	ArrayList<String> downloadList;
	String extension;
	URL urlBase;
	ProgressBar pBar;
	
	DownloadTask(Activity activity, String downloadDir, ArrayList<String> downloadList, String extension, URL urlBase)
	{
		this.activity = activity;
		this.downloadList = downloadList;
		this.downloadDir = downloadDir;
		this.urlBase = urlBase;
		this.extension = extension;
	}
	
	protected void onPreExecute()
	{
		// Consider putting this job on the MainActivity right before calling all the tasks
		pBar = (ProgressBar)activity.findViewById(R.id.update_progress);
		if (pBar != null) {
			pBar.setVisibility(View.VISIBLE);
		}
		else {
			Log.e(Debug.TAG, "Cant make progress bar visible as it doesnt exist");
		}
	}
	
	protected InputStream doInBackground(String... s)
	{		
		HttpURLConnection urlConnection;
		File file;
		InputStream download = null;
		byte[] buffer;
		int listLength;
		int count=0;
		
		ConnectivityManager cm = (ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
		
		if (isConnected | SETT_RETRY)
		{		
			try {
				// Set listLength for calculating the current progress (progressBar)
				listLength =  downloadList.size();
				
				// Get iterator
				Iterator<String> iterator = downloadList.iterator();
				
				/* while (queue.peek() != null)
				{	
					pack = queue.remove();
					url = pack.url;
					urlConnection = (HttpURLConnection) url.openConnection();
					file = new File(activity.getDir(pack.directory, Context.MODE_PRIVATE), pack.filename);
					
					// Check to see if files are even in need of downloading
					if(!file.exists())
					{
						// Download
						
						Debug.log(context, "Downloading " + pack.filename + "...");
						download = new BufferedInputStream(urlConnection.getInputStream());
					
						// Convert to a byte buffer for filewriting
						
						buffer = new byte[1024];
						int len;
						
						OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
						
						while ((len = download.read(buffer)) != -1) {
							out.write(buffer, 0, len);
						}
					
						// Store file
						
						try {
							Debug.log(context, "Writing " +  pack.filename + "...");
							out.write(buffer);
						}
						catch (IOException e) {
							Debug.log(context, "Write failed");
							Debug.logError(context, e);
							return null;
						}
						finally {
							try {
								out.close();
							}
							catch (IOException e) {
								Debug.log(context, "Couldnt close file");
								Debug.logError(context, e);
							}
						}
						
						count++;
						publishProgress(count, listLength);
					}
				} */
				
				while (iterator.hasNext())
				{
					/* // Get next pack
					pack = iterator.next();
					url = pack.url;
					urlConnection = (HttpURLConnection) url.openConnection(); */
					
					// Get next filename and finish URL
					String filename = iterator.next();
					URL url = new URL(urlBase, filename + extension);
					urlConnection = (HttpURLConnection) url.openConnection();
					
					// Open file for output
					File outFile = FileOps.openFile(activity, downloadDir, filename + extension);
					
					// But first check if its already there
					if (!outFile.exists())
					{
						// Download
						download = new BufferedInputStream(urlConnection.getInputStream());
						buffer = new byte[1024];
						int len;
						OutputStream out = new BufferedOutputStream(new FileOutputStream(outFile));
						while ((len = download.read(buffer)) != -1)
						{
							out.write(buffer, 0, len);
						}
						
						// Store result
						try {
							Log.v(Debug.TAG, "Writing " +  filename + "...");
							out.write(buffer);
						}
						catch (IOException e)
						{
							Log.v(Debug.TAG, "Write failed");
							Log.v(Debug.TAG, e.getMessage());
							return null;
						}
						finally {
							try {
								out.close();
							}
							catch (IOException e)
							{
								Log.v(Debug.TAG, "Couldnt close file");
								Log.v(Debug.TAG, e.getMessage());
							}
						}
					}
						
					count++;
					publishProgress(count, listLength);
				}
				
				// onPostExecute uses this for computing download success
				return download;
			}
			catch (IOException e)
			{
				Log.e(Debug.TAG, "Unexpected download error");
				downloadErrorCode = DEC_IO_EXCEPTION;
				downloadException = e;
				return null;
			}
		}
		else {
			Log.w(Debug.TAG, "No internet connection.");
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
	
		double percentage = (double)progress[0] / (double)progress[1] * 100;
		pBar.setProgress((int)percentage);
	}
	
	protected void onPostExecute(InputStream download)
	{	
		/*
		 * All this really does is make the pBar invisible again, check for and
		 * report errors & messages, and recreates the current activity
		 * (which should always be MainActivity)
		 */
		
		if (pBar != null) {
			pBar.setVisibility(View.GONE);
		}
			
		if (download != null) {
			Debug.toast(activity, "Updated complete");
			Log.i(Debug.TAG, "Update completed.");
			activity.recreate();
		}
		else if (downloadErrorCode == DEC_NO_INTERNET) {
			Debug.toast(activity, "Cant download, no internet");
			Log.v(Debug.TAG, "Update canceled. No internet connection.");
		}
		else if (downloadErrorCode == DEC_NO_AVAIL_UPDATES) {
			Debug.toast(activity, "Update not needed");
			Log.i(Debug.TAG, "No updates to download.");
			Log.i(Debug.TAG, "Update canceled.");
		}
		else if (downloadErrorCode == DEC_IO_EXCEPTION) {
			Debug.toast(activity, "Update fucked up");
			Log.e(Debug.TAG, "Download canceled: IOException.");
			Log.e(Debug.TAG, downloadException.getMessage());
			activity.recreate();
		}
		else {
			Debug.toast(activity, "Update not needed");
			Log.i(Debug.TAG, "Update canceled: no updates to download.");
		}
	}
}