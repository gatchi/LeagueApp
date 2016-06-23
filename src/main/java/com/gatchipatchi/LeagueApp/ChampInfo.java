package com.gatchipatchi.LeagueApp;

import android.app.Activity;
import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;
import android.view.View;
import android.view.MenuItem;
import android.content.Intent;
import android.content.res.Resources;
import android.util.JsonReader;
import java.util.Iterator;
import java.util.ArrayList;
import java.lang.String;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStreamReader;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.OutputStream;
import org.json.JSONTokener;
import org.json.JSONObject;
import org.json.JSONException;

public class ChampInfo extends Activity implements OnItemSelectedListener
{
	/*
	 * ChampInfo Class Info
	 * 
	 * This class is for the screen that shows all the stats for a champ.
	 * It should only display after tapping on a champ icon on the main
	 * activity.
	 * Once this activity starts, the first thing it does is pull from
	 * the corresponding champion JSON file.  It pulls whatever
	 * JSON objects and arrays (and therefore champ data) it needs and
	 * puts them into objects.  Code that does things like update the
	 * values on screen or compute values at a certain level all share
	 * these objects.
	 */

	public static final short MAX_LEVEL = 18;
	public static final short LARGE = 0;
	public static final short PERCENT = 1;
	public static final short SMALL = 2;
	
	//--------------- Public Objects ------------------//
	
	Resources res;
	int champId;
	String champName;
	String parType;
	double hpBase;
	double hpPerLevel;
	double mpBase;
	double mpPerLevel;
	double moveSpeed;
	double armorBase;
	double armorPerLevel;
	double mrBase;
	double mrPerLevel;
	double attackRange;
	double hpRegenBase;
	double hpRegenPerLevel;
	double mpRegenBase;
	double mpRegenPerLevel;
	double adBase;
	double adPerLevel;
	double asOffset;
	double asPerLevel;
	double asBase;
	
	ArrayList<String> champList = new ArrayList();
	
	//--------------- onCreate (main) -----------------//
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.champinfo);
		
		aestheticSetup();
		
		// Get champ ID (button pressed ID)
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		champId = bundle.getInt("button id");
		
		// Load champ list from JSON
		
		champList = (ArrayList<String>) bundle.get("champ list");
		/* File championListFile = new File(this.getDir(MainActivity.CHAMPIONS_DIR, Context.MODE_PRIVATE), "champion_list.txt");
		if (championListFile.exists())
		{
			try {
				InputStream in = new BufferedInputStream(new FileInputStream(championListFile));
			}
			catch (FileNotFoundException e) {
				showError(e);
			}
		} */
		champName = champList.get(champId);
		TextView champNameView = (TextView) findViewById(R.id.champ_name);
		champNameView.setText(champName);
		toast(champName);
		
		// Load champ list from string array resource.
		
		res = getResources();
		String[] champs = res.getStringArray(R.array.champ_names);
		/* champName = champs[champId];
		TextView champNameView = (TextView) findViewById(R.id.champ_name);
		champNameView.setText(champName); */
		
		// Load the JSON data
		
		InputStream in = null;
		File championFile = new File(this.getDir(MainActivity.CHAMPIONS_DIR, Context.MODE_PRIVATE), champName + ".json");
		try {
			in = new BufferedInputStream(new FileInputStream(championFile));
		}
		catch (FileNotFoundException e) {
			toast(e.getMessage(), Toast.LENGTH_LONG);
		}
		
		// Put it into a JSONObject
		
		JSONObject json = null;
		try {
			
			// Convert input stream to a string for older APIs.
			
			BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			StringBuilder responseStrBuilder = new StringBuilder();
			String inputStr;
			
			while ((inputStr = streamReader.readLine()) != null) {
				responseStrBuilder.append(inputStr);
			}
			
			json = new JSONObject(responseStrBuilder.toString());
			JSONObject champData = json.getJSONObject("data");
			JSONObject champ = champData.getJSONObject(champName);
			JSONObject champStats = champ.getJSONObject("stats");
			
			// Get stats
			
			parType = champ.getString("partype");
			hpBase = champStats.getDouble("hp");
			hpPerLevel = champStats.getDouble("hpperlevel");
			mpBase = champStats.getDouble("mp");
			mpPerLevel = champStats.getDouble("mpperlevel");
			moveSpeed = champStats.getDouble("movespeed");
			armorBase = champStats.getDouble("armor");
			armorPerLevel = champStats.getDouble("armorperlevel");
			mrBase = champStats.getDouble("spellblock");
			mrPerLevel = champStats.getDouble("spellblockperlevel");
			attackRange = champStats.getDouble("attackrange");
			hpRegenBase = champStats.getDouble("hpregen");
			hpRegenPerLevel = champStats.getDouble("hpregenperlevel");
			mpRegenBase = champStats.getDouble("mpregen");
			mpRegenPerLevel = champStats.getDouble("mpregenperlevel");
			adBase = champStats.getDouble("attackdamage");
			adPerLevel = champStats.getDouble("attackdamageperlevel");
			asOffset = champStats.getDouble("attackspeedoffset");
			asPerLevel = champStats.getDouble("attackspeedperlevel");
			asBase = calcAs(asOffset);

			// Set resource type on stat page
			
			TextView resourceTypeText = (TextView) findViewById(R.id.champ_resource_type);
			TextView resourceRegenTypeText = (TextView) findViewById(R.id.resource_regen_type);
			
			if (parType.equals("MP"))
			{
				resourceTypeText.setText("mana");
				resourceRegenTypeText.setText("mana regen");
			}
			else if (parType.equals("Energy"))
			{
				resourceTypeText.setText("energy");
				resourceRegenTypeText.setText("energy regen");
			}
			else if (champName.equals("Aatrox") || champName.equals("Vladimir") || champName.equals("DrMundo") || champName.equals("Mordekaiser") || champName.equals("Zac"))
			{
				resourceTypeText.setText("uses health");
			}
			else if (champName.equals("Rengar"))
			{
				resourceTypeText.setText("ferocity");
			}
			else if (champName.equals("RekSai") || champName.equals("Renekton") || champName.equals("Shyvana") || champName.equals("Tryndamere") || champName.equals("Gnar")) {
				resourceTypeText.setText("fury");
			}
			else if (champName.equals("Rumble")) {
				resourceTypeText.setText("heat");
			}
			else {
				resourceTypeText.setText("no resource");
			}

			// Set range on stat page
			
			TextView rangeTypeText = (TextView) findViewById(R.id.range_type);
			TextView rangeText = (TextView) findViewById(R.id.range);
			
			if (attackRange < 300)
			{
				rangeTypeText.setText("melee");
			}
			else if (attackRange >= 300)
			{
				rangeTypeText.setText("ranged");
			}
			rangeText.setText(Double.toString(attackRange));
			
			// Set movespeed on stat page
			
			TextView movespeedText = (TextView) findViewById(R.id.movespeed);
			movespeedText.setText(Double.toString(moveSpeed));
		
		} catch (UnsupportedEncodingException e) {
			toast("UnsupportedEncodingException");
			showError(e);
		} catch (JSONException e) {
			toast("JSONException");
			showError(e);
		} catch (IOException e) {
			toast("IOException");
			showError(e);
		} catch (NullPointerException e) {
			toast("NullPointerException");
			showError(e);
		}
		
	}

	/* String inputStreamToString(InputStream in) {
		
		// Convert input stream to a string
		BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		StringBuilder responseStrBuilder = new StringBuilder();
		String inputStr;
		while ((inputStr = streamReader.readLine()) != null) {
			responseStrBuilder.append(inputStr);
		}	
		return responseStrBuilder.toString();
	} */
	
	private void statUpdate(String level) throws NullPointerException {
		/* 
		 * When any level or level range is selected from the spinner,
		 * this method updates the stats displayed using that spinner entry.
		 * 
		 * Eventually REPLACE WITH JSON.
		 */
		

		// keep this until you write AS JSON conversion method
		// String[] asBase = res.getStringArray(R.array.champ_AS_base);
		// String[] asGrowth = res.getStringArray(R.array.champ_AS_growth);
		
		// TextViews for putting the stats into
		TextView healthText = (TextView) findViewById(R.id.health);
		TextView healthRegenText = (TextView) findViewById(R.id.health_regen);
		TextView resourceText = (TextView) findViewById(R.id.resource_points);
		TextView resourceRegenText = (TextView) findViewById(R.id.resource_regen);
		TextView adText = (TextView) findViewById(R.id.attack_damage);
		TextView asText = (TextView) findViewById(R.id.attack_speed);
		TextView armorText = (TextView) findViewById(R.id.armor);
		TextView mrText = (TextView) findViewById(R.id.magic_resist);
		
		if (level.equals("n"))
		{
			setPerLevel(healthText, LARGE, hpBase, hpPerLevel);
			setPerLevel(healthRegenText, SMALL, hpRegenBase, hpRegenPerLevel);
			if (parType.equals("MP") || parType.equals("Energy"))
			{
				setPerLevel(resourceText, LARGE, mpBase, mpPerLevel);
				setPerLevel(resourceRegenText, SMALL, mpRegenBase, mpRegenPerLevel);
			}
			setPerLevel(adText, SMALL, adBase, adPerLevel);
			setPerLevel(asText, PERCENT, asBase, asPerLevel);
			setPerLevel(armorText, SMALL, armorBase, armorPerLevel);
			setPerLevel(mrText, SMALL, mrBase, mrPerLevel);
		}
		else if (level.equals("1 – 18"))
		{
			setRange(hpBase, hpPerLevel, LARGE, healthText);
			setRange(hpRegenBase, hpRegenPerLevel, LARGE, healthRegenText);
			if (parType.equals("MP") || parType.equals("Energy"))
			{
				setRange(mpBase, mpPerLevel, LARGE, resourceText);
				setRange(mpRegenBase, mpRegenPerLevel, LARGE, resourceRegenText);
			}
			setRange(adBase, adPerLevel, LARGE, adText);
			setRange(asBase, asPerLevel, PERCENT, asText);
			setRange(armorBase, armorPerLevel, LARGE, armorText);
			setRange(mrBase, mrPerLevel, LARGE, mrText);
		}
		else // individual levels
		{
			setCurrentLevel(hpBase, hpPerLevel, LARGE, level, healthText);
			setCurrentLevel(hpRegenBase, hpRegenPerLevel, LARGE, level, healthRegenText);
			if (parType.equals("MP") || parType.equals("Energy"))
			{
				setCurrentLevel(mpBase, mpPerLevel, LARGE, level, resourceText);
				setCurrentLevel(mpRegenBase, mpRegenPerLevel, LARGE, level, resourceRegenText);
			}
			setCurrentLevel(adBase, adPerLevel, LARGE, level, adText);
			setCurrentLevel(asBase, asPerLevel, PERCENT, level, asText);
			setCurrentLevel(armorBase, armorPerLevel, LARGE, level, armorText);
			setCurrentLevel(mrBase, mrPerLevel, LARGE, level, mrText);
		}
		
	}
	
	/* 
	 * Stat Calculations
	 * 
	 * All these calc methods are outdated.
	 * They use a mix of ints and floats for... performance reasons?
	 * Anyway new functions will use doubles for everything cause thats what
	 * the JSON data uses.  Also it's simpler, and no need to go change shit
	 * later cause you need a decimal point or more precision or whatever.
	 * PRUNE LATER.
	 * 
	 * Keep the double methods until AS is fixed.  Thats the only function that
	 * still uses the methods that take Strings as input
	 */
	
/* 	void setPerLevel(TextView text, int valueType, String[] baseValue, String[] growthValue) {
		if (!baseValue[champId].isEmpty() && !growthValue[champId].isEmpty()) {
			if (valueType == LARGE) {
				text.setText(baseValue[champId] + " (+" + growthValue[champId] + ")");
			} else if (valueType == PERCENT) {
				text.setText(baseValue[champId] + " (+" + growthValue[champId] + "%)");
			} else {
				toast("whoops wrong function");
			}
		}
	} */
	
	
	void setPerLevel(TextView text, int valueType, double baseValue, double growthValue) {
		if ((baseValue != 0)) {
			if (valueType == LARGE) {
				text.setText(String.format("%.1f (+%.0f)", baseValue, growthValue));
			} else if (valueType == SMALL) {
				text.setText(String.format("%.2f (+%.1f)", baseValue, growthValue));
			} else if (valueType == PERCENT) {
				text.setText(String.format("%.3f (+%.1f%%)", baseValue, growthValue));
			}
		} else {
			// toast("Set error");
			// toast(Double.toString(baseValue));
			// toast(Double.toString(growthValue));
		}
	}
	
	void setCurrentLevel(double base, double growth, int valueType, String level, TextView view) {
		double levelValue = Double.parseDouble(level);
		double stat = base + growth * (levelValue - 1);
		if (stat != 0) {
			if (valueType == LARGE) {
				view.setText(String.format("%.1f", stat));
			} else if (valueType == SMALL) {
				view.setText(String.format("%.2f", stat));
			} else if (valueType == PERCENT) {
				view.setText(String.format("%.3f (+%.1f%%)", base, (growth * (levelValue - 1))));
			}
		}
	}
	
	void setRange(double base, double growth, int rangeType, TextView view) {
		if ((base != 0)) {
			if (rangeType == LARGE) {
				view.setText(String.format("%.1f – %.1f", base, (base + 17 * growth)));
			} else if (rangeType == SMALL) {
				view.setText(String.format("%.2f – %.2f", base, (base + 17 * growth)));
			} else if (rangeType == PERCENT) {
				view.setText(String.format("%.3f (+0%% – +%.1f%%)", base, (growth * 17)));
			}
		}
	}
	
	double calcAs(double offset) {
		return (0.625 / (1 + offset));
	}
	
	
	void aestheticSetup() {
		ActionBar actionBar = getActionBar();
		actionBar.setTitle("Champ Stats");
		actionBar.setDisplayHomeAsUpEnabled(true);
		Spinner levelSelect = (Spinner) findViewById(R.id.level);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.champ_level_select, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		levelSelect.setAdapter(adapter);
		levelSelect.setOnItemSelectedListener(this);
	}
	
	// back button handler
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// back button
		if (item.getItemId() == android.R.id.home) finish();
		// auto generated code
		return super.onOptionsItemSelected(item);
		
	}
	
	// spinner handler
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
	{
		// An item was selected. You can retrieve the selected item using
		// parent.getItemAtPosition(pos)
		String item = (String) parent.getItemAtPosition(pos);
		try {
			statUpdate(item);
		}
		catch (NullPointerException e) {
			showError(e);
		}
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent)
	{
		// not used
	}
	
	public void stubMethod()
	{
		// Simple test method for stubs.
		Toast toast = Toast.makeText(this, "Tada!", Toast.LENGTH_SHORT);
		toast.show();
	}
	
	public void showError(Exception e)
	{
		Toast t = Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG);
		t.show();
	}
	
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
	
	void toast(double d, int length) {
		String msg = Double.toString(d);
		Toast t = Toast.makeText(this, msg, length);
		t.show();
	}
}
