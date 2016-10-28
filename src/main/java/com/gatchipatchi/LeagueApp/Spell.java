package com.gatchipatchi.LeagueApp;

class Spell
{
	String name;
	String description;
	int cost;
	int cooldown;
	
	Spell(String name, int cost, int cooldown, String description)
	{
		this.name = name;
		this.cost = cost;
		this.cooldown = cooldown;
		this.description = description;
	}
}