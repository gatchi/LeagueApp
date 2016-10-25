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
	
	
	ChampStatsModule(String champName, JSONObject champ)
	{
		this.champ = champ;
		this.champName = champName;
		
		// Set the default values, which will adustable in settings
		
	}
	
	void configure()
	{
		/*
		 * Sets up the module.
		 */
		
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
		attackSpeed = ChampOps.calcStat(ChampOps.calcBaseAs(champ.getDouble("attackspeedoffset"), champ.getDouble("attackspeedperlevel"), level);
		armor = ChampOps.calcStat(champ.getDouble("armor"), champ.getDoubl("armorperlevel"), level);
		magicResist = ChampOps.calcStat(champ.getDouble("spellblock"), level);
		
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
	
	
	private void statUpdate(String level) throws NullPointerException
	{
		/* 
		 * When any level or level range is selected from the spinner,
		 * this method updates the stats displayed using that spinner entry.
		 * 
		 */
		
		
		if (level.equals("n"))
		{
			setPerLevel(healthText, LESS_PRECISE, hpBase, hpPerLevel);
			setPerLevel(healthRegenText, MORE_PRECISE, hpRegenBase, hpRegenPerLevel);
			if (parType.equals("MP") || parType.equals("Energy"))
			{
				setPerLevel(resourceText, LESS_PRECISE, mpBase, mpPerLevel);
				setPerLevel(resourceRegenText, MORE_PRECISE, mpRegenBase, mpRegenPerLevel);
			}
			setPerLevel(adText, MORE_PRECISE, adBase, adPerLevel);
			setPerLevel(asText, PERCENT, asBase, asPerLevel);
			setPerLevel(armorText, MORE_PRECISE, armorBase, armorPerLevel);
			setPerLevel(mrText, MAGIC_RESIST, mrBase, mrPerLevel);
		}
		else if (level.equals("1 – 18"))
		{
			setRange(hpBase, hpPerLevel, LESS_PRECISE, healthText);
			setRange(hpRegenBase, hpRegenPerLevel, MORE_PRECISE, healthRegenText);
			if (parType.equals("MP") || parType.equals("Energy"))
			{
				setRange(mpBase, mpPerLevel, LESS_PRECISE, resourceText);
				setRange(mpRegenBase, mpRegenPerLevel, MID_PRECISE, resourceRegenText);
			}
			setRange(adBase, adPerLevel, MORE_PRECISE, adText);
			setRange(asBase, asPerLevel, PERCENT, asText);
			setRange(armorBase, armorPerLevel, MORE_PRECISE, armorText);
			setRange(mrBase, mrPerLevel, LESS_PRECISE, mrText);
		}
		else // selected level
		{
			setCurrentLevel(hpBase, hpPerLevel, LESS_PRECISE, level, healthText);
			setCurrentLevel(hpRegenBase, hpRegenPerLevel, MORE_PRECISE, level, healthRegenText);
			if (parType.equals("MP") || parType.equals("Energy"))
			{
				setCurrentLevel(mpBase, mpPerLevel, LESS_PRECISE, level, resourceText);
				setCurrentLevel(mpRegenBase, mpRegenPerLevel, LESS_PRECISE, level, resourceRegenText);
			}
			setCurrentLevel(adBase, adPerLevel, MORE_PRECISE, level, adText);
			setCurrentLevel(asBase, asPerLevel, PERCENT, level, asText);
			setCurrentLevel(armorBase, armorPerLevel, MORE_PRECISE, level, armorText);
			setCurrentLevel(mrBase, mrPerLevel, MID_PRECISE, level, mrText);
		}
		
	}
	
	private void setResource()
	{
		String parType = champ.getString("partype");
		if (parType.equals("MP"))
		{
			resourceId = R.string.mana;
			resourceTypeText.setText(R.string.mana);
			resourceRegenTypeText.setText(R.string.mana_regen);
		}
		else if (parType.equals("Energy"))
		{
			resourceTypeText.setText(R.string.energy);
			resourceRegenTypeText.setText(R.string.energy_regen);
		}
		else if (champName.equals("Aatrox") || champName.equals("Vladimir") || champName.equals("DrMundo") || champName.equals("Mordekaiser") || champName.equals("Zac"))
		{
			resourceTypeText.setText(R.string.uses_health);
		}
		else if (champName.equals("Rengar"))
		{
			resourceTypeText.setText(R.string.ferocity);
		}
		else if (champName.equals("RekSai") || champName.equals("Renekton") || champName.equals("Shyvana") || champName.equals("Tryndamere") || champName.equals("Gnar"))
		{
			resourceTypeText.setText(R.string.fury);
		}
		else if (champName.equals("Rumble")) {
			resourceTypeText.setText(R.string.heat);
		}
		else {
			resourceTypeText.setText(R.string.no_resource);
		}
	}
}