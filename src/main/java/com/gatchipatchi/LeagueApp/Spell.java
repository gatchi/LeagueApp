package com.gatchipatchi.LeagueApp;

class Spell
{
	String name;
	String costText;
	String cooldownText;
	String description;
	int[] cost;
	int[] cooldown;
	
	Spell() {}
	
	Spell(String name, String costText, String cooldownText, String description)
	{
		this.name = name;
		this.costText = costText;
		this.cooldownText = cooldownText;
		this.description = description;
	}
}