package com.gatchipatchi.LeagueApp;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import org.json.JSONException;

class Updater
{
	final static String BASE_DOWNLOAD_URL = "ddragon.leagueoflegends.com/cdn/6.20.1/";
	final static String IMAGES_URL_FRAG = "img/champion/";
	final static String CHAMP_JSON_URL_FRAG = "data/en_US/champion/";
	final static String CHAMPION_JSON_URL_FRAG = "data/en_US/";
	final static String JSON_EXT = ".json";
	final static String ICON_EXT = ".png";
	
	static void checkInventory(Activity activity) throws JSONException
	{
		/*
		 * checkInventory Description 
		 * 
		 * This method is for managing champ specific downloads.
		 * It determines what needs to be downloaded and executed by looking at whats
		 * on file.  Whatever is missing it gets, whatever needs to be updated it updates.
		 * 
		 * You must use packs when submitting download requests instead of simple lists cause
		 * cause otherwise the download task doesnt know where to save the files.  They dont
		 * just all go to the same location.  Packs must contain the url, the directory
		 * (cause how android wants file ops to be), and the filename.
		 * 
		 * Wait no you dont have to use Packs if you just do a separate download request for each
		 * download type (icons, champ jsons, etc) cause they all have the same base url and the
		 * same disk destination.  The only problem is then the app will start multiple tasks,
		 * which means there needs to be some sort of blocking mechanism?
		 */
		
		// DownloadTask inputs
		ArrayList<String> champList;
		String dir;
		String ext;
		URL url;
		
		// Check for to see if list of champ names exists first
		boolean champListFileCheck = FileOps.checkIfFileExists(activity, FileOps.CHAMP_DIR, FileOps.CHAMP_LIST_FILE);
		
		// If champ list file doesnt exist, check for champ json and generate the list from it
		if (!champListFileCheck)
		{			
			Log.i(Debug.TAG, "Champ list not found.  Looking for champ json file...");
			
			boolean champJsonFileCheck = FileOps.checkIfFileExists(activity, FileOps.CHAMP_DIR, FileOps.CHAMP_JSON_FILE);
			
			// If neither exists, download champ json and restart activity
			if (!champJsonFileCheck)
			{
				Log.i(Debug.TAG, "No champ list or JSON");
				Log.i(Debug.TAG, "Prepping champ JSON download...");
				
				// DownloadTask only takes lists, so make list of one
				ArrayList<String> jsonList = new ArrayList<String>(1);
				jsonList.add("champion");
				
				/* // Make & prepare download queue					
				ArrayList<Pack> downloadQueue = Downloader.prepareDownloadList(jsonList, 1, BASE_DOWNLOAD_URL + CHAMP_JSON_URL_FRAG, JSON_EXT, FileOps.CHAMP_DIR); */
				
				// Start champ json download task
				Log.v(Debug.TAG, "Attempting data download");
				try {
					new DownloadTask(activity, FileOps.CHAMP_DIR, jsonList, ".json", new URL("http://" + BASE_DOWNLOAD_URL + CHAMPION_JSON_URL_FRAG)).execute();
				}
				catch (MalformedURLException e)
				{
					Log.e(Debug.TAG, e.getMessage());
					Log.e(Debug.TAG, "hey fix this error message");
				}
				
				return;
			}
			
			/*
			 * Load or generate the champlist.  This is needed for all champ-related downloads.
			 */
			
			// If the champions.json exists but the champlist doesnt, generate it
			else if (champJsonFileCheck)
			{
				Log.i(Debug.TAG, "Champ JSON found.  Generating list...");
				
				// Generate list of champ names
				champList = ChampOps.generateChampList(activity, FileOps.CHAMP_DIR, FileOps.CHAMP_JSON_FILE);
				
				// Save champ list
				try {
					FileOps.writeTextFile(activity, "champs", "champ_list.txt", champList.toString());
					Log.i(Debug.TAG, "Champ list saved");
				}
				catch (NullPointerException e)
				{
					Log.e(Debug.TAG, "champList never initialized.  checkInventory failed.");
					return;
				}
			}
			
			else {
				Log.wtf(Debug.TAG, "checkInventory() failed trying to get the champlist");
				return;
			}
			
		}
		
		// The champlist file exists, so load it	
		else
		{	
			try {
				champList = FileOps.retrieveList(activity, FileOps.CHAMP_DIR, FileOps.CHAMP_LIST_FILE);
			}
			catch (FileNotFoundException e)
			{
				Log.w(Debug.TAG, "checkInventory thinks champlist file exists, but FileOps.retrieveList cant find it");
				Log.w(Debug.TAG, e.getMessage());
				return;
			}
			catch (IOException e)
			{
				Log.w(Debug.TAG, "listReader encountered an IOException");
				Log.w(Debug.TAG, e.getMessage());
				return;
			}
		}
		
		// @TODO: implement conditional downloading?
		
		// Generate champ json list
		try {
			new DownloadTask(activity, FileOps.CHAMP_DIR, champList, ".json", new URL("http://" + BASE_DOWNLOAD_URL + CHAMP_JSON_URL_FRAG)).execute();
		}
		catch (MalformedURLException e)
		{
			Log.e(Debug.TAG, e.getMessage());
			Log.e(Debug.TAG, "Invalid champ json download URL");
		}	
		
		// Generate champ icon lists		
		try {
			new DownloadTask(activity, FileOps.ICON_DIR, champList, ".png", new URL("http://" + BASE_DOWNLOAD_URL + IMAGES_URL_FRAG)).execute();
		}
		catch (MalformedURLException e)
		{
			Log.e(Debug.TAG, e.getMessage());
			Log.e(Debug.TAG, "Invalid champ icon download URL");
		}
	}
}