package com.gatchipatchi.LeagueApp;

import android.util.Log;
import android.widget.TextView;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class Champion
{
	/*
	 * ChampStatModule Class Definition
	 *
	 * Class for displaying base stats and stats per level.
	 */
	
	final static String COLOR_AD = "#FC8A01";
	final static String COLOR_AP = "#97FC97";
	final static String COLOR_AS = "#C80000";
	final static String COLOR_ARMOR = "#EFF002";
	final static String COLOR_MAGIC_RESIST = "#CA1F7B";
	final static String COLOR_HEALTH = "#C03300";
	final static String COLOR_MANA = "#0099CC";
	final static String COLOR_ENERGY = "#FFFF00";
	final static String COLOR_CRIT_CHANCE = "#E56013";
	final static String COLOR_BASE_CRIT = "#944B00";
	
	String champName;
	JSONObject champion;
	JSONObject champStats;
	JSONArray champAbilities;
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
	Spell passive;
	Spell basicAbility1;
	Spell basicAbility2;
	Spell basicAbility3;
	Spell ultimateAbility;
	final double maxHealthGrowthRate;
	final double maxResourceGrowthRate;
	final double healthRegenGrowthRate;
	final double resourceRegenGrowthRate;
	final double attackDamageGrowthRate;
	final double armorGrowthRate;
	final double magicResistGrowthRate;
	final double attackSpeedGrowthRate;
	
	
	Champion(String champName, JSONObject champion) throws JSONException
	{
		this.champion = champion;
		this.champName = champName;
		this.passive = new Spell();
		this.basicAbility1 = new Spell(1);
		this.basicAbility2 = new Spell(2);
		this.basicAbility3 = new Spell(3);
		this.ultimateAbility = new Spell(4);
		
		champStats = champion.getJSONObject("stats");

		maxHealthGrowthRate = champStats.getDouble("hpperlevel");
		maxResourceGrowthRate = champStats.getDouble("mpperlevel");
		healthRegenGrowthRate = champStats.getDouble("hpregenperlevel");
		resourceRegenGrowthRate = champStats.getDouble("mpregenperlevel");
		attackDamageGrowthRate = champStats.getDouble("attackdamageperlevel");
		attackSpeedGrowthRate = champStats.getDouble("attackspeedperlevel");
		armorGrowthRate = champStats.getDouble("armorperlevel");
		magicResistGrowthRate = champStats.getDouble("spellblockperlevel");
		
		setValues(1);  // Set values to lvl 1
		setResource(champion.getString("partype"));
		loadPassive(passive);
		loadAbility(basicAbility1, 1);
		loadAbility(basicAbility2, 2);
		loadAbility(basicAbility3, 3);
		loadAbility(ultimateAbility, 4);
	}
	
	Champion(Champion champToCopy)
	{
		this.champName = champToCopy.champName;
		this.champion = champToCopy.champion;
		this.champStats = champToCopy.champStats;
		this.champAbilities = champToCopy.champAbilities;
		this.maxHealth = champToCopy.maxHealth;
		this.maxResource = champToCopy.maxResource;
		this.healthRegen = champToCopy.healthRegen;
		this.resourceRegen = champToCopy.resourceRegen;
		this.attackDamage = champToCopy.attackDamage;
		this.armor = champToCopy.armor;
		this.magicResist = champToCopy.magicResist;
		this.attackSpeed = champToCopy.attackSpeed;
		this.moveSpeed = champToCopy.moveSpeed;
		this.range = champToCopy.range;
		this.resourceType = champToCopy.resourceType;
		this.resourceRegenType = champToCopy.resourceRegenType;
		this.rangeType = champToCopy.rangeType;
		this.passive = champToCopy.passive;
		this.basicAbility1 = champToCopy.basicAbility1;
		this.basicAbility2 = champToCopy.basicAbility2;
		this.basicAbility3 = champToCopy.basicAbility3;
		this.ultimateAbility = champToCopy.ultimateAbility;
		this.maxHealthGrowthRate = champToCopy.maxHealthGrowthRate;
		this.maxResourceGrowthRate = champToCopy.maxResourceGrowthRate;
		this.healthRegenGrowthRate = champToCopy.healthRegenGrowthRate;
		this.resourceRegenGrowthRate = champToCopy.resourceRegenGrowthRate;
		this.attackDamageGrowthRate = champToCopy.attackDamageGrowthRate;
		this.armorGrowthRate = champToCopy.armorGrowthRate;
		this.magicResistGrowthRate = champToCopy.magicResistGrowthRate;
		this.attackSpeedGrowthRate = champToCopy.attackSpeedGrowthRate;
	}
	
	void setValues(int level) throws JSONException
	{		
		maxHealth = ChampOps.calcStat(champStats.getDouble("hp"), maxHealthGrowthRate, level);
		maxResource = ChampOps.calcStat(champStats.getDouble("mp"), maxResourceGrowthRate, level);
		healthRegen = ChampOps.calcStat(champStats.getDouble("hpregen"), healthRegenGrowthRate, level);
		resourceRegen = ChampOps.calcStat(champStats.getDouble("mpregen"), resourceRegenGrowthRate, level);
		attackDamage = ChampOps.calcStat(champStats.getDouble("attackdamage"), attackDamageGrowthRate, level);
		attackSpeed = ChampOps.calcAs(champStats.getDouble("attackspeedoffset"), attackSpeedGrowthRate, level);
		armor = ChampOps.calcStat(champStats.getDouble("armor"), armorGrowthRate, level);
		magicResist = ChampOps.calcStat(champStats.getDouble("spellblock"), magicResistGrowthRate, level);
		
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
	
	private void setResource(String parType) throws JSONException
	{
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

	private void loadPassive(Spell target) throws JSONException
	{
		JSONObject passiveJson = champion.getJSONObject("passive");
		target.name = passiveJson.getString("name");
		target.description = passiveJson.getString("description");
	}
	
	private void loadAbility(Spell target, int abilityArrayNum) throws JSONException
	{
		JSONArray champAbilities = champion.getJSONArray("spells");
		JSONObject abilityJson = champAbilities.getJSONObject(abilityArrayNum - 1);
		target.name = abilityJson.getString("name");
		target.costText = abilityJson.getString("costBurn");
		target.cooldownText = abilityJson.getString("cooldown");
		target.description = abilityJson.getString("tooltip");
		
		// Try to parse the descriptions
		try
		{
			if(champName.equals("Shyvana") && (abilityArrayNum == 2)) target.description = "Shyvana deals 20/32/45//57 <font color=\"#FF8C00\">(+0.2 bonus AD)</font> <font color=\"#99FF99\">(+0.1 AP)</font> magic damage per second to nearby enemies and gains a bonus 30/35/40/45/50% movement speed that decays over 3 seconds.<br><br>While Burnout is active, basic attacks deal 5/8/11.25/14.25/17.5 <font color=\"#FF8C00\">(+0.2 bonus AD)</font> <font color=\"#99FF99\">(+0.1 AP)</font> magic damage to nearby enemies and extend its duration by 1 second.<br><br><font color=\"#FF3300\">Dragon Form: </font>Burnout scorches the earth, continuing to damage enemies that stand on it.<br><br><font color=\"#919191\"><i>Burnout deals +20% damage to monsters.<br>Burnout has a maximum duration of 7 seconds.</i></font>";
			else target.description = ChampOps.ttParser(champName, target.description, abilityJson, abilityArrayNum);
		} 
		catch (IOException e) {
			Log.w(Debug.TAG, "IOException while parsing spell tooltips");
			Log.w(Debug.TAG, e.getMessage());
		} catch (JSONException e) {
			Log.w(Debug.TAG, "JSONException while translating codes");
			Log.w(Debug.TAG, e.getMessage());
		}
	}
}

