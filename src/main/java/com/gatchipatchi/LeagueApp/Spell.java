package com.gatchipatchi.LeagueApp;

class Spell
{
	String name;
	String costText;
	String cooldownText;
	String description;
	int spellNum;
	int[] cost;
	int[] cooldown;
	
	Spell() {}
	
	Spell(int spellNum)
	{
		this.spellNum = spellNum;
	}
	
	Spell(String name, String costText, String cooldownText, String description)
	{
		this.name = name;
		this.costText = costText;
		this.cooldownText = cooldownText;
		this.description = description;
	}
}