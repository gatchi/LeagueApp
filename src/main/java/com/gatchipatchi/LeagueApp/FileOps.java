package com.gatchipatchi.LeagueApp;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;

class FileOps
{
	final static String CHAMP_LIST_FILE = "champ_list.txt";
	final static String CHAMP_JSON_FILE = "champion.json";
	final static String CHAMP_DIR = "champs";
	final static String ICON_DIR = "drawable";
	
	static File openFile(Context context, String directory, String filename)
	{
		File file = new File(context.getDir(directory, Context.MODE_PRIVATE), filename);
		return file;
	}
	
	static InputStream openInputStream(Activity activity, String directory, String filename) throws FileNotFoundException
	{
		File file = openFile(activity, directory, filename);
		return new BufferedInputStream(new FileInputStream(file));
	}
	
	static JSONObject retrieveJson(Activity activity, String dirName, String filename)
	{
		/*
		 * retrieveJson Description
		 * Depends on: inputStreamToString()
		 * 
		 * Produce a JSON object from the inputted filename.
		 * 
		 * Returns: JSON object
		 */
		
		InputStream in;
		JSONObject json;
		File jsonFile;
		
		try {
			jsonFile = FileOps.openFile(activity, dirName, filename);
			in = new BufferedInputStream(new FileInputStream(jsonFile));
		}
		catch (FileNotFoundException e)
		{
			Log.e(Debug.TAG, "retrieveJson could not find file, check should have prevented this");
			Log.e(Debug.TAG, e.getMessage());
			return null;
		}
		/* catch (IOException e)
		{
			Log.wtf(Debug.TAG, "retrieveJson failed to open FileInputStream: bad filename?");
			Log.wtf(Debug.TAG, e.getMessage());
			return null;
		} */
		
		try {
			json = new JSONObject(FileOps.inputStreamToString(in));
		}
		catch (JSONException e)
		{
			Log.e(Debug.TAG, "retrieveJson: Could not make JSON, invalid input string");
			Log.e(Debug.TAG, e.getMessage());
			return null;
		}
		return json;
	}
	
	static ArrayList<String> retrieveList(Activity activity, String dir, String filename) throws FileNotFoundException, IOException
	{
		// Get file, then pipe into listReader()
		File listFile = openFile(activity, dir, filename);
		ArrayList<String> list = listReader(listFile);
		return list;
	}
	
	static ArrayList<String> listReader(File file) throws IOException
	{
		/*
		 * champListReader Description
		 * 
		 * Made for the reading of champlist files, which are comma delimited text files
		 * that start and end with square brackets (since they are generated from jsonObject.keys()),
		 * but could be used for any file list formatted in this way.  ArrayList starting size is 180
		 * since thats approximately how many champs in the game at this time (ArrayLists can
		 * dynamically resize so it doesnt super matter, just helps with memory management and
		 * therefore performance).
		 */
		
		// Comma is delimiter
		ArrayList<String> list = new ArrayList<String>(180);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		int ic = 0;
		char c = 0;
		int d = 0;
		String word = "";
		
		while ((c != ']') & (ic != -1))
		{
			do {
				ic = reader.read();
				c = (char)ic;
				
				if ((c != '[') & (c != ',') & (c != ']')) {
					word = word + c;
				}
			} while ((c != ',') & (c != ']') & (ic != -1));
			
			list.add(word);
			
			if (c != ']')
			{
				reader.skip(1);
				word = "";
				c = 0;
			}
			
			// To prevent hanging
			if (d > 600)
			{
				/* Log.d(Debug.TAG, "Force break of list-reading loop"); */
				break;
			}
			else d++;
		}
	
		/* Log.v(Debug.TAG, "Champ list generated"); */
		
		return list;
	}
	
	static String inputStreamToString(InputStream in)
	{
		/*
		 * inputStreamToString Description
		 * 
		 * This method was made to easily create JSON objects from files using the
		 * JSON library included in the Android API, which unfortunately cannot create
		 * objects directly from files or strings, like other implementations of org.json do.
		 * And replacing Android's org.json with another org.json package proved to be a pain.
		 * 
		 * Returns: JSON-encoded string, unless the input is bad.  Then it returns an error.
		 */
		
		try {
			// Convert input stream to a string
			BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			StringBuilder responseStrBuilder = new StringBuilder();
			String inputStr;
			
			while ((inputStr = streamReader.readLine()) != null)
			{
				responseStrBuilder.append(inputStr);
			}
			
			return responseStrBuilder.toString();
		}
		catch (UnsupportedEncodingException e)
		{
			// If this happens then the input is seriously wrong
			Log.e(Debug.TAG, "inputStreamToString received an improper input.");
			Log.e(Debug.TAG, e.getMessage());
			return "bad value";
		}
		catch (IOException e)
		{
			// Same here
			Log.e(Debug.TAG, "inputStreamToString encountered an IOException");
			Log.e(Debug.TAG, e.getMessage());
			return "bad value";
		}
	}
	
	static boolean checkIfFileExists(Context context, String directory, String filename)
	{
		File testfile = new File(context.getDir(directory, Context.MODE_PRIVATE), filename);
		return testfile.exists();
	}
	
	static void storeFile(Context context, byte[] data, String filename, String directory) throws NullPointerException
	{	
		File file = new File(context.getDir(directory, Context.MODE_PRIVATE), filename);

		// Save file unless it already exists
		if (!file.exists())
		{		
			Debug.toast(context, "storing file " + filename + "...", Debug.TOAST_LENGTH_SHORT);
			OutputStream out = null;
			
			// Write to file
			try {
				out = new BufferedOutputStream(new FileOutputStream(file));
				out.write(data);
				out.close();
				Log.v(Debug.TAG, filename + " written");
			}
			catch (IOException e)
			{
				Log.e(Debug.TAG, filename + " write failure");
				Log.e(Debug.TAG, e.getMessage());
			}
		}
		else {
			Log.i(Debug.TAG, filename + " already on card");
		}
	}
	
	static void writeTextFile(Context context, String directory, String filename, String text)
	{		
		File file;
		if (directory == null)
		{
			file = new File(context.getFilesDir(), filename);
		}
		else {
			file = new File(context.getDir(directory, Context.MODE_PRIVATE), filename);
		}

		// Save file unless it already exists
		if (!file.exists())
		{		
			Log.v(Debug.TAG, "storing file... " + filename);
			BufferedWriter out = null;
			
			// Write
			try {
				out = new BufferedWriter(new FileWriter(file));
				out.write(text);
				out.close();
				Log.v(Debug.TAG, filename + " written");
			}
			catch (IOException e)
			{
				Log.e(Debug.TAG, filename + " write failure");
				Log.e(Debug.TAG, e.getMessage());
			}
		}
	}
	
	static void clearMainLog(Context context)
	{
		File file = new File(context.getFilesDir(), Debug.LOG_FILE);
		if (file.exists())
		{
			file.delete();
			Debug.toast(context, "Log cleared");
		}
	}
}