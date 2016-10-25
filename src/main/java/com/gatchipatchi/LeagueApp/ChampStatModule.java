package com.gatchipatchi.LeagueApp;

import android.widget.TextView;
import org.json.JSONObject;

class ChampStatsModule
{
	/*
	 * ChampStatModule Class Definition
	 *
	 * Class for displaying base stats and stats per level.
	 */
		
	String champName;
	JSONObject champ;
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
	
	
	ChampStatsModule(String champName, JSONObject champ)
	{
		this.champ = champ;
		this.champName = champName;
		
		// Set the default values, which will adustable in settings
		
	}
	
	void init()
	{
		/*
		 * Sets up the module.
		 */
		
		setValues(1);
		setResource();
	}
	
	void publish()
	{
		/*
		 * Shows the module on the activity.
		 */
		
		// Resource type
		
	}
	
	void setValues(int level)
	{
		maxHealth = ChampOps.calcStat(champ.getDouble("hp"), champ.getDouble("hpperlevel"), level);
		maxResource = ChampOps.calcStat(champ.getDouble("mp"), champ.getDouble("mpperlevel"), level);
		healthRegen = ChampOps.calcStat(champ.getDouble("hpregen"), champ.getDouble("hpregenperlevel"), level);
		resourceRegen = ChampOps.calcStat(champ.getDouble("mpregen"), champ.getDouble("mpregenperlevel"), level);
		attackDamage = ChampOps.calcStat(champ.getDouble("ad"), champ.getDouble("adperlevel"), level);
		attackSpeed = ChampOps.calcStat(ChampOps.calcBaseAs(champ.getDouble("attackspeedoffset")), champ.getDouble("attackspeedperlevel"), level);
		armor = ChampOps.calcStat(champ.getDouble("armor"), champ.getDouble("armorperlevel"), level);
		magicResist = ChampOps.calcStat(champ.getDouble("spellblock"), champ.getDouble("spellblockperlevel"), level);
		
		if (champName.equals("Tristana"))
		{
			range = ChampOps.calcTristanaRange(champ.getDouble("attackrange"), level);
		}
		else range = champ.getDouble("attackrange");
		
		if (champName.equals("Cassiopeia"))
		{
			moveSpeed = ChampOps.calcCassMoveSpeed(champ.getDouble("movespeed"), level);
		}
		else range = champ.getDouble("movespeed");
	}
	
	private void setResource()
	{
		String parType = champ.getString("partype");
		if (parType.equals("MP"))
		{
			resourceType = "Mana";
			resourceRegenType = "Mana regen";
		}
		else if (parType.equals("Energy"))
		{
			resourceType = "Energy";
			resourceRegenType = "Energy regen";
		}
		else if (champName.equals("Aatrox") || champName.equals("Vladimir") || champName.equals("DrMundo") || champName.equals("Mordekaiser") || champName.equals("Zac"))
		{
			resourceType = "Uses health";
		}
		else if (champName.equals("Rengar"))
		{
			resourceType = "Ferocity";
		}
		else if (champName.equals("RekSai") || champName.equals("Renekton") || champName.equals("Shyvana") || champName.equals("Tryndamere") || champName.equals("Gnar"))
		{
			resourceType = "Fury";
		}
		else if (champName.equals("Rumble")) {
			resourceType = "Heat";
		}
		else {
			resourceType = "No resource";
		}
	}
}