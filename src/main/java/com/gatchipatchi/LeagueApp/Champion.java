package com.gatchipatchi.LeagueApp;

import android.util.Log;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;

class Champion
{
	/*
	 * ChampStatModule Class Definition
	 *
	 * Class for displaying base stats and stats per level.
	 */
		
	String champName;
	JSONObject champion;
	JSONObject champStats;
	double maxHealth;
	double maxResource;
	double healthRegen;
	double resourceRegen;
	double attackDamage;
	double armor;
	double magicResist;
	double attackSpeed;
	double moveSpeed;
	double range;
	String resourceType;
	String resourceRegenType;
	String rangeType;
	
	
	Champion(String champName, JSONObject champion)
	{
		this.champion = champion;
		this.champName = champName;
		
		// Set the default values, which will adustable in settings
		
	}
	
	void init() {
		/*
		 * Sets up the module.
		 */
		
		try {
			champStats = champion.getJSONObject("stats");
		}
		catch(JSONException e) {
			Log.e(Debug.TAG, "JSONException in Champion:");
			Log.e(Debug.TAG, "Couldnt obtain stats JSONObject");
			Log.e(Debug.TAG, e.getMessage());
		}
		
		try {
			setValues(1);
		}
		catch(JSONException e) {
			Log.e(Debug.TAG, "JSONException in Champion:");
			Log.e(Debug.TAG, "setValues() failed in init()");
			Log.e(Debug.TAG, e.getMessage());
		}
		
		try {
			setResource();
		}
		catch(JSONException e) {
			Log.e(Debug.TAG, "JSONException in Champion:");
			Log.e(Debug.TAG, "setResource() failed in init()");
			Log.e(Debug.TAG, e.getMessage());
		}
	}
	
	void setValues(int level) throws JSONException
	{
		maxHealth = ChampOps.calcStat(champStats.getDouble("hp"), champStats.getDouble("hpperlevel"), level);
		maxResource = ChampOps.calcStat(champStats.getDouble("mp"), champStats.getDouble("mpperlevel"), level);
		healthRegen = ChampOps.calcStat(champStats.getDouble("hpregen"), champStats.getDouble("hpregenperlevel"), level);
		resourceRegen = ChampOps.calcStat(champStats.getDouble("mpregen"), champStats.getDouble("mpregenperlevel"), level);
		attackDamage = ChampOps.calcStat(champStats.getDouble("attackdamage"), champStats.getDouble("attackdamageperlevel"), level);
		attackSpeed = ChampOps.calcStat(ChampOps.calcBaseAs(champStats.getDouble("attackspeedoffset")), champStats.getDouble("attackspeedperlevel"), level);
		armor = ChampOps.calcStat(champStats.getDouble("armor"), champStats.getDouble("armorperlevel"), level);
		magicResist = ChampOps.calcStat(champStats.getDouble("spellblock"), champStats.getDouble("spellblockperlevel"), level);
		
		if (champName.equals("Tristana"))
		{
			range = ChampOps.calcTristanaRange(champStats.getDouble("attackrange"), level);
		}
		else range = champStats.getDouble("attackrange");
		
		if (champName.equals("Cassiopeia"))
		{
			moveSpeed = ChampOps.calcCassMoveSpeed(champStats.getDouble("movespeed"), level);
		}
		else moveSpeed = champStats.getDouble("movespeed");
		
		if (champStats.getDouble("attackrange") < 300)
		{
			rangeType = "Melee";
		}
		else if (champStats.getDouble("attackrange") >= 300)
		{
			rangeType = "Ranged";
		}
	}
	
	private void setResource() throws JSONException
	{
		String parType = champion.getString("partype");
		if (parType.equals("MP"))
		{
			resourceType = "Mana";
			resourceRegenType = "Mana regen per 5s";
		}
		else if (parType.equals("Energy"))
		{
			resourceType = "Energy";
			resourceRegenType = "Energy regen per 5s";
		}
		else if (champName.equals("Aatrox") || champName.equals("Vladimir") || champName.equals("DrMundo") || champName.equals("Mordekaiser") || champName.equals("Zac"))
		{
			resourceType = "Uses health";
			maxResource = 0;	/* This is necessary as some of these have invalid resource numbers */
			resourceRegen = 0;
		}
		else if (champName.equals("Rengar"))
		{
			resourceType = "Ferocity";
			resourceRegen = 0;
		}
		else if (champName.equals("RekSai") || champName.equals("Renekton") || champName.equals("Shyvana") || champName.equals("Tryndamere") || champName.equals("Gnar"))
		{
			resourceType = "Fury";
			resourceRegen = 0;
		}
		else if (champName.equals("Rumble")) {
			resourceType = "Heat";
			resourceRegen = 0;
		}
		else {
			resourceType = "No resource";
			maxResource = 0;
			resourceRegen = 0;
		}
	}
}

