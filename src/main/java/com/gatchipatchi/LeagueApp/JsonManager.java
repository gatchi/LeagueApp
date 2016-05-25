package com.gatchipatchi.LeagueApp;

import android.os.Bundle;
import android.content.res.Resources;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.StringBuilder;
import org.json.JSONObject;
import org.json.JSONException;

public class JsonManager
{
	JSONObject createJsonObject(InputStream jsonFileData)
	{
		BufferedReader streamReader = null;
		try {
			streamReader = new BufferedReader(new InputStreamReader(jsonFileData, "UTF-8"));
		}
		catch (UnsupportedEncodingException e) {}
		StringBuilder responseStrBuilder = new StringBuilder();
		String inputStr;
		try {
			while ((inputStr = streamReader.readLine()) != null)
				responseStrBuilder.append(inputStr);
		}
		catch (IOException e) {}
		JSONObject json = null;
		try{
			json = new JSONObject(responseStrBuilder.toString());
		}
		catch (JSONException e) {}
		return json;
	}
}
