package com.gatchipatchi.LeagueApp;

import android.app.Activity;
import android.content.Context;	
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;

class ChampOps
{
	static ArrayList<String> generateChampList(Activity activity, String dirName, String jsonFilename)
	{
		// Retrieve JSON object from given JSON file
		JSONObject championJson = FileOps.retrieveJson(activity, dirName, jsonFilename);
		JSONObject champData;
		try {
			champData = championJson.getJSONObject("data");
		}
		catch (JSONException e)
		{
			Log.e(Debug.TAG, e.getMessage());
			Log.e(Debug.TAG, "Couldn't find champData in the supplied JSON in generateChampList");

			return null;
		}
		catch (NullPointerException e)
		{
			/* Log.e(Debug.TAG, e.getMessage()); */
			Log.e(Debug.TAG, "in generateChampList(): championJson didnt recieve a JSON");
			
			return null;
		}
		
		// Champ names are the keys from champData
		ArrayList<String> champList = new ArrayList();
		Iterator<String> champs = champData.keys();
		while (champs.hasNext()) {
			champList.add(champs.next());
		}
		
		// Sort into alpha-ordered list now to save from doing it later
		java.util.Collections.sort(champList);

		return champList;
	}
}