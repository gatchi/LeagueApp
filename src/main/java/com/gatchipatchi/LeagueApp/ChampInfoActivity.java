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
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.MenuItem;
import android.content.Intent;
import android.content.res.Resources;
import java.util.Iterator;
import java.util.ArrayList;
import java.lang.String;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class ChampInfoActivity extends Activity implements OnItemSelectedListener {
	/*
	 * ChampInfo Class Info
	 * 
	 * This class is for the screen that shows all the stats for a champ.
	 * It should only display after tapping on a champ icon on the main
	 * activity.
	 * 
	 * The activity can be broken down into three main sections: the
	 * champ name, the champ stats, and the champ abilitios (more
	 * modules may be added in the future).
	 *
	 * This file needs heavy refactoring as it's too long.  Many of these
	 * functions should be made methods in ChampOps.  Delete this section
	 * when this is done.
	 */
	
	//--------------- Class Constants ------------------//
	
	final static int JSON_OBJECT = 1;
	static final short MAX_LEVEL = 18;
	static final short LESS_PRECISE = 0;
	static final short PERCENT = 1;
	static final short MORE_PRECISE = 2;
	static final short MAGIC_RESIST = 3;
	static final short MID_PRECISE = 4;
	final int SPELL_Q = 0;
	final int SPELL_W = 1;
	final int SPELL_E = 2;
	final int SPELL_R = 3;
	final String COLOR_HEALTH = "#cc3300";
	
	//---------------- Class Objects ------------------//
	
	Resources res;
	int champId;
	String champName;
	Champion champion;
	JSONObject champJson;
	JSONObject subChampJson;
	
	TextView healthText;
	TextView healthRegenText;
	TextView resourceText;
	TextView resourceRegenText;
	TextView adText;
	TextView asText;
	TextView armorText;
	TextView mrText;
	TextView resourceTypeText;
	TextView resourceRegenTypeText;
	TextView rangeTypeText;
	TextView rangeText;
	TextView moveSpeedText;
	
	ArrayList<String> champList = new ArrayList();
	
	//--------------- onCreate (main) -----------------//
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.champinfo);
		
		aestheticSetup();
		
		
		//------------- Champ Name ------------//
		
		// Get champ ID (button pressed ID)
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		champId = bundle.getInt("button id");
		
		// Load champ and set champ name on the view
		champList = (ArrayList<String>) bundle.get("champ list");
		try {
			champName = champList.get(champId);
		}
		catch(NullPointerException e) {
			Log.e(Debug.TAG, "NullPointerException in ChampInfoActivity onCreate:");
			Log.e(Debug.TAG, "either champId or champList is null, or champList is incomplete");
			Debug.toast(this, "Whoops, cant open champ info page");
			finish();
			return;
		}
		
		// Set and display champ name at the top of the activity
		TextView champNameView = (TextView) findViewById(R.id.champ_name);
		if(champName.equals("MonkeyKing")) {
			champNameView.setText(R.string.wukong);
		}
		else {
			champNameView.setText(champName);
		}
		
		
		//------------ Champ Stats --------------//

		// Open JSON
		champJson = FileOps.retrieveJson(this, FileOps.CHAMP_DIR, champName);
		JSONObject statsJson;
		try {
			JSONObject dataJson = champJson.getJSONObject("data");
			subChampJson = dataJson.getJSONObject(champName);
			statsJson = subChampJson.getJSONObject("stats");
		}
		catch(JSONException e) {
			Log.e(Debug.TAG, "JSONException in ChampInfoActivity:");
			Log.e(Debug.TAG, e.getMessage());
			finish();
			return;
		}
		
		// Instantiate module
		try {
			champion = new Champion(champName, subChampJson);
		}
		catch(JSONException e) {
			Log.e(Debug.TAG, "JSONException in ChampInfoActivity:");
			Log.e(Debug.TAG, "Couldnt create Champion object");
			Log.e(Debug.TAG, e.getMessage());
			Debug.toast(this, "BUG: Couldnt open champ page. Please report");
			finish();
			return;
		}
		champion.init();
		
		// Initialize table views
		healthText = (TextView) findViewById(R.id.health);
		healthRegenText = (TextView) findViewById(R.id.health_regen);
		resourceText = (TextView) findViewById(R.id.resource_points);
		resourceRegenText = (TextView) findViewById(R.id.resource_regen);
		adText = (TextView) findViewById(R.id.attack_damage);
		asText = (TextView) findViewById(R.id.attack_speed);
		armorText = (TextView) findViewById(R.id.armor);
		mrText = (TextView) findViewById(R.id.magic_resist);
		resourceTypeText = (TextView) findViewById(R.id.champ_resource_type);
		resourceRegenTypeText = (TextView) findViewById(R.id.resource_regen_type);
		rangeTypeText = (TextView) findViewById(R.id.range_type);
		rangeText = (TextView) findViewById(R.id.range);
		moveSpeedText = (TextView) findViewById(R.id.movespeed);
		
		// Throw some numbers on it
		updateStatTable();
		
		
		//------------ Champ Skills -------------//
		
		// Get TextView handles
		TextView qNameView = (TextView)findViewById(R.id.q_name);
		TextView wNameView = (TextView)findViewById(R.id.w_name);
		TextView eNameView = (TextView)findViewById(R.id.e_name);
		TextView rNameView = (TextView)findViewById(R.id.r_name);
		TextView qView = (TextView)findViewById(R.id.q);
		TextView wView = (TextView)findViewById(R.id.w);
		TextView eView = (TextView)findViewById(R.id.e);
		TextView rView = (TextView)findViewById(R.id.r);
		TextView passiveNameView = (TextView)findViewById(R.id.passive_name);
		TextView passiveView = (TextView)findViewById(R.id.passive);
		
		// Write spell text to screen
		qView.setText(Html.fromHtml(champion.basicAbility1.description));
		wView.setText(Html.fromHtml(champion.basicAbility2.description));
		eView.setText(Html.fromHtml(champion.basicAbility3.description));
		rView.setText(Html.fromHtml(champion.ultimateAbility.description));
		qNameView.setText("q:  " + champion.basicAbility1.name);
		wNameView.setText("w:  " + champion.basicAbility2.name);
		eNameView.setText("e:  " + champion.basicAbility3.name);
		rNameView.setText("r:  " + champion.ultimateAbility.name);
		if(champName.equals("Aatrox")) passiveView.setText(Html.fromHtml("Whenever Aatrox consumes <font color=\"" + COLOR_HEALTH + "\"> a portion of his health</font>, he stores it into his Blood Well. which can hold up to 105 - 870 (based on level) health. The Blood Well depletes by 2% per second if Aatrox hasn't dealt or received damage in the last 5 seconds.<br/><br/>Aatrox gains 0.3 - 0.55 (based on level)% bonus attack speed for every 1% in his Blood Well, up to a maximum of 30 - 55 (based on level)% bonus attack speed.<br/><br/>Upon taking fatal damage, Aatrox is cleansed of all debuffs, enters Stasis icon stasis and drains his Blood Well, healing himself for 35% of Blood Well's maximum capacity over the next 3 seconds for 36.75 - 304.5 (based on level) health (+100% of Blood Well's stored health) up to a maximum of 141.75 - 1174.5 (based on level) health."));
		else passiveView.setText(Html.fromHtml(champion.passive.description));
		passiveNameView.setText("Passive:  " + champion.passive.name);
	}
	
	
	//--------------- Class Methods -------------------//
		
	private void statUpdate(String level) throws NullPointerException {
		/* 
		 * When any level or level range is selected from the spinner,
		 * this method updates the stats displayed using that spinner entry.
		 * 
		 */
		
		if (level.equals("n")) {
			setTableToN();
		}
		else if (level.equals("1 – 18")) {
			// Set champ to lvl 1, make a copy at lvl 18, and use both for the textview update
		}
		else { // selected level
			try {
				champion.setValues(Integer.parseInt(level));
			}
			catch(JSONException e) {
				Log.e(Debug.TAG, "JSONException in ChampInfoActivity:");
				Log.e(Debug.TAG, "statUpdate failed");
				Log.e(Debug.TAG, "Couldnt update values at selected level");
			}
			updateStatTable();
		}
		
	}
	
	void setTableToN() {
		healthText.setText(String.format("%.0f", champion.maxHealthGrowthRate));
		healthRegenText.setText(String.format("%.1f", champion.healthRegenGrowthRate));
		if(champion.maxResource > 0) resourceText.setText(String.format("%.0f", champion.maxResourceGrowthRate));
		if(champion.resourceRegen > 0) resourceRegenText.setText(String.format("%.1f", champion.resourceRegenGrowthRate));
		adText.setText(String.format("%.0f", champion.attackDamageGrowthRate));
		asText.setText(String.format("%.1f%%", champion.attackSpeedGrowthRate));
		armorText.setText(String.format("%.0f", champion.armorGrowthRate));
		mrText.setText(String.format("%.0f", champion.magicResistGrowthRate));
		resourceTypeText.setText(champion.resourceType);
		resourceRegenTypeText.setText(champion.resourceRegenType);
		rangeTypeText.setText(champion.rangeType);
		rangeText.setText(String.format("%.0f", champion.range));
		moveSpeedText.setText(String.format("%.0f", champion.moveSpeed));
	}
	
	void updateStatTable() {
		// Default format is in-game style (low precision)
		healthText.setText(String.format("%.0f", champion.maxHealth));
		healthRegenText.setText(String.format("%.1f", champion.healthRegen));
		if(champion.resourceType.equals("Uses health") || champion.resourceType.equals("No resource")) {
			resourceText.setVisibility(View.INVISIBLE);
			resourceRegenText.setVisibility(View.INVISIBLE);
		}
		else if(champion.resourceType.equals("Fury")) {
			resourceText.setText(String.format("%.0f", champion.maxResource));
			resourceText.setVisibility(View.VISIBLE);
			resourceRegenText.setVisibility(View.INVISIBLE);
		}
		else {
			resourceText.setText(String.format("%.0f", champion.maxResource));
			resourceRegenText.setText(String.format("%.1f", champion.resourceRegen));
			resourceText.setVisibility(View.VISIBLE);
			resourceRegenText.setVisibility(View.VISIBLE);
		}
		adText.setText(String.format("%.0f", champion.attackDamage));
		asText.setText(String.format("%.3f", champion.attackSpeed));
		armorText.setText(String.format("%.0f", champion.armor));
		mrText.setText(String.format("%.0f", champion.magicResist));
		resourceTypeText.setText(champion.resourceType);
		resourceRegenTypeText.setText(champion.resourceRegenType);
		rangeTypeText.setText(champion.rangeType);
		rangeText.setText(String.format("%.0f", champion.range));
		moveSpeedText.setText(String.format("%.0f", champion.moveSpeed));
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
	public boolean onOptionsItemSelected(MenuItem item) {
		// back button
		if (item.getItemId() == android.R.id.home) finish();
		// auto generated code
		return super.onOptionsItemSelected(item);
		
	}
	
	// spinner handler
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		/*
		 * An item was selected. You can retrieve the selected item using
		 * parent.getItemAtPosition(pos)
		 */
		String item = (String) parent.getItemAtPosition(pos);
		try {
			statUpdate(item);
		}
		catch (NullPointerException e) {
			Log.e(Debug.TAG, "NullPointerException in statUpdate:");
			Log.e(Debug.TAG, e.getMessage());
		}
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		/* not used but necessary? t*/
	}
	
	public void stubMethod() {
		// Simple test method for stubs
		Toast toast = Toast.makeText(this, "Tada!", Toast.LENGTH_SHORT);
		toast.show();
	}
	
}
