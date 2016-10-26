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

public class ChampInfoActivity extends Activity implements OnItemSelectedListener
{
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
    protected void onCreate(Bundle savedInstanceState)
    {
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
		catch (NullPointerException e)
		{
			Log.e(Debug.TAG, "NullPointerException in ChampInfoActivity onCreate:");
			Log.e(Debug.TAG, "either champId or champList is null, or champList is incomplete");
			Debug.toast(this, "Whoops, cant open champ info page");
			finish();
			return;
		}
		
		// Set and display champ name at the top of the activity
		TextView champNameView = (TextView) findViewById(R.id.champ_name);
		if (champName.equals("MonkeyKing")) {
			champNameView.setText(R.string.wukong);
		}
		else {
			champNameView.setText(champName);
		}
		
		//------------ Champ Stats --------------//
		

		// Open JSON
		/* champJson = loadJson(this, FileOps.CHAMP_DIR, champName + ".json", JSON_OBJECT); */
		champJson = FileOps.retrieveJson(this, FileOps.CHAMP_DIR, champName);
		JSONObject statsJson;
		try {
			JSONObject dataJson = champJson.getJSONObject("data");
			subChampJson = dataJson.getJSONObject(champName);
			statsJson = subChampJson.getJSONObject("stats");
		}
		catch (JSONException e)
		{
			Log.e(Debug.TAG, "JSONException in ChampInfoActivity:");
			Log.e(Debug.TAG, e.getMessage());
			finish();
			return;
		}
		
		// Instantiate module
		champion = new Champion(champName, subChampJson);
		champion.init();
		
		// Create views
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
		
		// Set view values for the first time
		updateViews();
		
		/*

		// Set range on stat page
		
		rangeText.setText(String.format("%.0f", attackRange));
		
		// Set movespeed on stat page
		TextView movespeedText = (TextView) findViewById(R.id.movespeed);
		movespeedText.setText(String.format("%.0f" ,moveSpeed)); */
		
		// Load skills (champion spells)
		try
		{
			// Pull spells from JSON
			JSONArray spells = subChampJson.getJSONArray("spells");
			JSONObject qSpell = (JSONObject)spells.get(0);
			JSONObject wSpell = (JSONObject)spells.get(1);
			JSONObject eSpell = (JSONObject)spells.get(2);
			JSONObject rSpell = (JSONObject)spells.get(3);
			JSONObject passive = subChampJson.getJSONObject("passive");
			
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
			
			// Grab full raw descriptions from spells
			String qtt = qSpell.getString("tooltip");
			String wtt = wSpell.getString("tooltip");
			String ett = eSpell.getString("tooltip");
			String rtt = rSpell.getString("tooltip");
			String passiveName = passive.getString("name");
			String qName = qSpell.getString("name");
			String wName = wSpell.getString("name");
			String eName = eSpell.getString("name");
			String rName = rSpell.getString("name");
			
			String parsedQtt = "";
			String parsedWtt = "";
			String parsedEtt = "";
			String parsedRtt = "";

			// Try to parse the descriptions
			try
			{
				parsedQtt = ttParser(qtt, 1);
				if(champName.equals("Shyvana")) parsedWtt = "Shyvana deals 20/32/45//57 <font color=\"#FF8C00\">(+0.2 bonus AD)</font> <font class=\"#99FF99\">(+0.1 AP)</font> magic damage per second to nearby enemies and gains a bonus 30/35/40/45/50% movement speed that decays over 3 seconds.<br><br>While Burnout is active, basic attacks deal 5/8/11.25/14.25/17.5 <font color=\"#FF8C00\">(+0.2 bonus AD)</font> <font color=\"#99FF99\">(+0.1 AP)</font> magic damage to nearby enemies and extend its duration by 1 second.<br><br><font color=\"#FF3300\">Dragon Form: </font>Burnout scorches the earth, continuing to damage enemies that stand on it.<br><br><font color=\"#919191\"><i>Burnout deals +20% damage to monsters.<br>Burnout has a maximum duration of 7 seconds.</i></font>";
				else parsedWtt = ttParser(wtt, 2);
				parsedEtt = ttParser(ett, 3);
				parsedRtt = ttParser(rtt, 4);
			} 
			catch (IOException e) {
				Debug.log(this, "IOException while parsing spell tooltips");
				Debug.log(this, e.getMessage());
			} catch (JSONException e) {
				Debug.log(this, "JSONException while translating codes");
				Debug.log(this, e.getMessage());
			}
			
			// Write spell text to screen
			qView.setText(Html.fromHtml(parsedQtt));
			wView.setText(Html.fromHtml(parsedWtt));
			eView.setText(Html.fromHtml(parsedEtt));
			rView.setText(Html.fromHtml(parsedRtt));
			qNameView.setText("q:  " + qName);
			wNameView.setText("w:  " + wName);
			eNameView.setText("e:  " + eName);
			rNameView.setText("r:  " + rName);
			if (champName.equals("Aatrox")) passiveView.setText(Html.fromHtml("Whenever Aatrox consumes <font color=\"" + COLOR_HEALTH + "\"> a portion of his health</font>, he stores it into his Blood Well. which can hold up to 105 - 870 (based on level) health. The Blood Well depletes by 2% per second if Aatrox hasn't dealt or received damage in the last 5 seconds.<br/><br/>Aatrox gains 0.3 - 0.55 (based on level)% bonus attack speed for every 1% in his Blood Well, up to a maximum of 30 - 55 (based on level)% bonus attack speed.<br/><br/>Upon taking fatal damage, Aatrox is cleansed of all debuffs, enters Stasis icon stasis and drains his Blood Well, healing himself for 35% of Blood Well's maximum capacity over the next 3 seconds for 36.75 - 304.5 (based on level) health (+100% of Blood Well's stored health) up to a maximum of 141.75 - 1174.5 (based on level) health."));
			else passiveView.setText(Html.fromHtml(passive.getString("description")));
			passiveNameView.setText("Passive:  " + passiveName);
			
		
		} catch (JSONException e) {
			Debug.log(this, "JSONException in Load Skills");
			Debug.log(this, e.getMessage());
		}
	}
	
	
	//--------------- Class Methods -------------------//
	
	String ttParser(String input, int spellNum) throws IOException, JSONException {
		/*
		 * Spell JSON arrays use codes in their tooltips.
		 * The codes translate to numbers found in other parts of the
		 * champion JSON object.
		 * This method generates a new string with codes replaced with numbers
		 * using the inputted string.
		 * 
		 * "spellNum" is which spell is being parsed (required for ChampInfo.translate
		 * to work.  So if it's the first spell (q), you put 1.  Second (w), 2.  And so on.
		 * 
		 * This method also translates bad HTML supplied by DataDragon that's supposed to
		 * be fore syntax highlighting.  It replaces <span class="colorclass"></span> with
		 * <font color="#hexval"></font>.  This is possible since the color classes are
		 * named after the hex colors they represent (for example, #ff9900 is class "colorFF9900").
		 * 
		 * Depends on method ChampInfo.translate()
		 */
		
		StringReader sr = new StringReader(input);
		int rawChar = 0;
		char c = 0;
		String result = "";
		String code = "";
		String word = "";
		int lilCounter = 0;
		int bigCounter = 0;
		int biggerCounter = 0;
		
		do
		{
			rawChar = sr.read();
			if (rawChar != -1)
			{
				c = (char)rawChar;
				sr.mark(1);
				
				// If html
				if (c == '<')
				{
					result = result + c;
					
					// Run until end bracket
					while (c != '>')
					{
						c = (char)sr.read();
						
						// Look for words
						while ( (c != ' ') && (c != '=') && (c != '>') )
						{
							word = word + c;
							if (word.equals("span"))
							{
								word = "font";
							}
							else if (word.equals("/span"))
							{
								word = "/font";
							}
							else if (word.equals("class"))
							{
								word = "color";
							}
							else if (word.equals("\"color"))
							{
								word = "\"#";
								while (c != '\"')
								{
									c = (char)sr.read();
									word = word + c;
								}
							}
							c = (char)sr.read();
						}

						result = result + word + c;
						word = "";
					}
				}
				
				// If a dd code
				else if (c == '{')
				{
					sr.mark(1);
					c = (char)sr.read();
					
					if (c == '{')
					{
						// Skip first space char
						sr.skip(1);
						
						c = (char)sr.read();
						while (c != ' ')
						{
							// Loop until hitting second space char
							code = code + c;
							c = (char)sr.read();
						}
						word = translate(code, spellNum);
						result = result + word;
						
						// Skip the two closing curly braces and clear code
						sr.skip(2);
						code = "";
					}
					else
					{
						// False alarm; not a code, so return and add brace normally
						sr.reset();
						c = (char)sr.read();
						result = result + c;
					}
				}
				else result = result + c;
			}
			word = "";
		} while (rawChar != -1);
		
		return result;
	}
	
	String translate(String code, int spellNum) throws JSONException {
		/*
		 * Looks up the code and returns the value.
		 * 
		 * CODES
		 * =====
		 * en		nth element of "effectburn"
		 * an		"coeff" value of element of "vars" with "key" "an"
		 * fn		same as for an
		 */
		
		String result = "";
		JSONArray spells = subChampJson.getJSONArray("spells");
		JSONObject selSpell = (JSONObject)spells.get(spellNum - 1);
		
		if (code.equals("maxammo"))
		{
			if (selSpell.has("maxammo"))
			{
				String maxAmmo = selSpell.getString("maxammo");
				return maxAmmo;
			}
			Debug.toast(this, "missing maxammo");
			return "0";
		}
		if (code.equals("cost"))
		{
			String cost = selSpell.getString("costBurn");
			return cost;
		}
		
		char alpha = code.charAt(0);
		int num = 0;
		try
		{
			if (code.length() == 3) num = Integer.valueOf("" + code.charAt(1) + code.charAt(2));
			else num = Integer.valueOf("" + code.charAt(1));
		}
		catch(java.lang.NumberFormatException e)
		{
			Debug.log(this, e.getMessage());
			Debug.log(this, "code: " + code);
		}
	
		// This top block is here for champs with values that need overwritten
		if(champName.equals("Darius"))
		{
			if(spellNum == 4)
			{
				if(code.equals("f3")) return "0";
			}
		}
		if(champName.equals("Ekko"))
		{
			if(spellNum == 4)
			{
				if(code.equals("a2")) return "(for every 100AP) 0.06667";
			}
		}
		if(champName.equals("Illaoi"))
		{
			if(spellNum == 2)
			{
				if(code.equals("f1")) return "";
				if(code.equals("f2")) return "0.5";
			}
		}
		if(champName.equals("Jax"))
		{
			if(spellNum == 4)
			{
				if(code.equals("f2")) return "";
				if(code.equals("f1")) return "";
			}
		}
		if(champName.equals("Nasus"))
		{
			if(spellNum == 1)
			{
				if(code.equals("f1")) return "0";
			}
		}
		if(champName.equals("Shyvana"))
		{
			if(spellNum == 1)
			{
				if(code.equals("f1")) return "0.4/0.55/0.7/0.85/1.0 AD";
			}
		}
		if(champName.equals("Ziggs"))
		{
			if(spellNum == 2)
			{
				if(code.equals("f1")) return "25/27.5/30/32.5/35";
			}
		}
		if(champName.equals("Zyra"))
		{
			if(spellNum == 2)
			{
				if(code.equals("ammorechargetime")) return "20/18/16/14/12";
			}
		}
		if (alpha == 'e')
		{
			JSONArray effectBurn = selSpell.getJSONArray("effectBurn");
			result = (String)effectBurn.get(num);
			return result;
		}
		else if (alpha == 'a')
		{
			JSONArray vars = selSpell.getJSONArray("vars");
			
			int index = 0;
			JSONObject holder;
			String type;
			String coeff;
			String fullType = "";
			
			// Step through the vars array, find the object with the key
			while (!vars.isNull(index))
			{
				holder = (JSONObject)vars.get(index);
				if (holder.getString("key").equals(code))
				{
					if (holder.get("coeff") instanceof String)
					{
						coeff = holder.getString("coeff");
					}
					else if (holder.get("coeff") instanceof Double)
					{
						coeff = Double.toString(holder.getDouble("coeff"));
					}
					else if (holder.get("coeff") instanceof JSONArray)
					{
						JSONArray coeffArray = holder.getJSONArray("coeff");
						coeff = "";
						for(int i=0; i<coeffArray.length(); i++)
						{
							coeff = coeff + Double.toString(coeffArray.getDouble(i)) + "/";
						}
					}
					else {
						coeff = "error";
					}
					
					if(holder.has("link")) type = holder.getString("link");
					else type = null;
					
					if (type.equals("attackdamage")) fullType = "AD";
					if (type.equals("bonusattackdamage")) fullType = "bonus AD";
					if (type.equals("spelldamage")) fullType = "AP";
					if (type.equals("armor")) fullType = "armor";
					if (type.equals("mana")) fullType = "mana";
					if (type.equals("health")) fullType = "health";
					if (type.equals("bonushealth")) fullType = "bonus health";
					
					String fullString = coeff + " " + fullType;
					return fullString;
				}
				else
				{
					index++;
				}
			}
			
			Debug.toast(this, "something fucky happened with the coeff");
			Debug.log(this, "cant find " + code);
		}
		else if (alpha == 'f')
		{
			JSONArray vars = selSpell.getJSONArray("vars");
			
			int index = 0;
			JSONObject holder;
			String coeff;
			String type;
			String fullType = "";
			
			// Step through the vars array, find the object with the key
			while (!vars.isNull(index))
			{
				holder = (JSONObject)vars.get(index);
				if(holder.getString("key").equals(code))
				{
					if (holder.get("coeff") instanceof String)
					{
						coeff = holder.getString("coeff");
					}
					else if (holder.get("coeff") instanceof Integer)
					{
						coeff = Integer.toString(holder.getInt("coeff"));
					}
					else if (holder.get("coeff") instanceof Double)
					{
						coeff = Double.toString(holder.getDouble("coeff"));
					}
					else if (holder.get("coeff") instanceof JSONArray)
					{
						JSONArray coeffArray = holder.getJSONArray("coeff");
						coeff = "";
						for(int i=0; i<coeffArray.length()-1; i++)
						{
							coeff = coeff + Double.toString(coeffArray.getDouble(i)) + "/";
						}
						coeff = coeff + Double.toString(coeffArray.getDouble(coeffArray.length() - 1));
					}
					else {
						coeff = "error";
					}
					
					if(holder.has("link")) type = holder.getString("link");
					else type = "";
					
					// Units
					if (type.equals("attackdamage")) fullType = "AD";
					if (type.equals("bonusattackdamage")) fullType = "bonus AD";
					if (type.equals("magicdamage")) fullType = "AP";
					if (type.equals("spelldamage")) fullType = "AP";
					if (type.equals("armor")) fullType = "armor";
					if (type.equals("bonusarmor")) fullType = "of bonus armor";
					if (type.equals("bonusspellblock")) fullType = "of bonus magic resist";
					if (type.equals("mana")) fullType = "mana";
					if (type.equals("health")) fullType = "health";
					if (type.equals("bonushealth")) fullType = "of bonus health";
					if (type.equals("@dynamic.abilitypower")) fullType = "AP";
					
					String fullString = coeff + " " + fullType;
					return fullString;
				}
				else
				{
					index++;
				}
			}
			
			if (result.isEmpty())
			{
				// This is the list of missing data and the rules that go with them
				
				if (champName.equals("Aatrox"))
				{
					if (code.equals("f5")) return "";
					if (code.equals("f4")) return "a portion of his";
				}
				if (champName.equals("Ahri"))
				{
					if (code.equals("f1")) return "1.60 damage";
				}
				if (champName.equals("Anivia"))
				{
					if (code.equals("f1")) return ", based on Glacial Storm's rank, 20/20/30/40";
				}
				if (champName.equals("Annie"))
				{
					if (code.equals("f1")) return ".15 AP";
				}
				if (champName.equals("Ashe"))
				{
					if (code.equals("f1")) return "1.15/1.20/1.25/1.30/1.35 AD";
				}
				if (champName.equals("AurelionSol"))
				{
					if (code.equals("f1")) return "30-145.5 (based on level) (+15/30/45/60//75)";
					if (code.equals("f2")) return "(+0.27-0.525 (based on level) AP)";
				}
				if (champName.equals("Azir"))
				{
					if (spellNum == 2)
					{
						if (code.equals("f2")) return "(based on level) 50-70";
						if (code.equals("f1")) return "12/11/10/9/8";
					}
					if (spellNum == 3)
					{
						if (code.equals("f1")) return "0";
					}
				}
				if (champName.equals("Bard"))
				{
					if (code.equals("f1")) return "0";
					if (code.equals("f2")) return "0";
				}
				if (champName.equals("Braum"))
				{
					if (code.equals("f1")) return "0";
				}
				if (champName.equals("Caitlyn"))
				{
					if (spellNum == 1)
					{
						if (code.equals("f1")) return "1.30/1.40/1.50/1.60/1.70 AD";
					}
					if (spellNum == 2)
					{
						if (code.equals("f1")) return "0.7 AD";
					}
				}
				if (champName.equals("Cassiopeia"))
				{
					if (code.equals("f2")) return "0.1 AP";
				}
				if (champName.equals("Corki"))
				{
					if (spellNum == 1)
					{
						if (code.equals("f1")) return "0.5 AD";
					}
					if (spellNum == 2)
					{
						if (code.equals("f1")) return "60/90/120/150/180";
						if (code.equals("f2")) return "0.2 AP";
					}
				}
				if (champName.equals("Darius"))
				{
					if (code.equals("f1")) return "0.4 AD bonus";
				}
				if (champName.equals("DrMundo"))
				{
					if(spellNum == 3)
					{
						if(code.equals("f1")) return "0";
						if(code.equals("f2")) return "0";
					}
					if(spellNum == 4)
					{
						if(code.equals("f1")) return "0";
					}
				}
				if (champName.equals("Evelynn"))
				{
					if (code.equals("f2")) return "0.35/0.4/0.45/0.5/0.55 AP";
				}
				if (champName.equals("Ezreal"))
				{
					if (spellNum == 1)
					{
						if (code.equals("f3")) return "1.1 AD";
					}
					if (spellNum == 3)
					{
						if (code.equals("f1")) return "0.5 bonus AD";
					}
				}
				if (champName.equals("FiddleSticks"))
				{
					if (spellNum == 2)
					{
						if (code.equals("f1")) return "300/450/600/750/900";
						if (code.equals("f2")) return "2.25 AP";
					}
					if (spellNum == 4)
					{
						if(code.equals("f1")) return "625/1125/1625";
						if(code.equals("f2")) return "2.25 AP";
					}
				}
				if(champName.equals("Fiora"))
				{
					if(spellNum == 2)
					{
						if(code.equals("f1")) return "1.0 AP";
						
					}
					if(spellNum == 3)
					{
						if(code.equals("f4")) return "140/155/170/185/200%";
						if(code.equals("f3")) return "0";
					}
					if (spellNum == 4)
					{
						if(code.equals("f8")) return "bonus (8 + 18 per 100 bonus AD)";
						if(code.equals("f6")) return "20/30/40/50";
						if(code.equals("f9")) return "0.6 bonus AD";
					}
				}
				if(champName.equals("Gangplank"))
				{
					if (spellNum == 3)
					{
						if(code.equals("f5")) return "2/1/0.5";
					}
					if(spellNum == 4)
					{
						if(code.equals("f3")) return "12";
					}
				}
				if(champName.equals("Garen"))
				{
					if(spellNum == 2)
					{
						if(code.equals("f2")) return "0.25";
						if(code.equals("f1")) return "";
					}
					if(spellNum == 3)
					{
						if(code.equals("f3")) return "1 per every 3 levels";
						if(code.equals("f1")) return "0";
						if(code.equals("f2")) return "5";
					}
				}
				if(champName.equals("Gnar"))
				{
					if(spellNum == 1)
					{
						if(code.equals("f1")) return "45/50/55/60";
					}
					if(spellNum == 2)
					{
						if(code.equals("f1")) return "30/45/60/75";
					}
					if(spellNum == 3)
					{
						if(code.equals("f1")) return "0";
					}
				}
				if(champName.equals("Gragas"))
				{
					if(code.equals("f1")) return "3";
				}
				if(champName.equals("Graves"))
				{
					if(spellNum == 1)
					{
						if(code.equals("f1")) return "0.75 bonus AD";
						if(code.equals("f2")) return "0.4/0.6/0.8/1.0/1.2 bonus AD";
					}
					if(spellNum == 4)
					{
						if(code.equals("f1")) return "1.5 bonus AD";
						if(code.equals("f2")) return "1.2 bonus AD";
					}
				}
				if(champName.equals("Illaoi"))
				{
					if(spellNum == 1)
					{
						if(code.equals("f1")) return "0";
					}
					if(spellNum == 3)
					{
						if(code.equals("f5")) return "(8 per 100 AD)";
						if(code.equals("f1")) return "5/4/3";
					}
				}
				if(champName.equals("Irelia"))
				{
					if(code.equals("f1")) return "1.2 AD";
				}
				if(champName.equals("Jayce"))
				{
					if(code.equals("f3")) return "10/15/20/25";
				}
				if(champName.equals("Jhin"))
				{
					if(spellNum == 1)
					{
						if(code.equals("f1")) return "0.3/0.35/0.4/0.45/0.5 AD";
					}
					if(spellNum == 3)
					{
						if(code.equals("f1")) return "28/27/26/25/24";
					}
					if(spellNum == 4)
					{
						if (code.equals("f1")) return "(1 + bonus crit damage) x 100";
					}
				}
				if(champName.equals("Jinx"))
				{
					if(code.equals("f4")) return "0-70";
				}
				if(champName.equals("Kalista"))
				{
					if(code.equals("f1")) return "10-27 (based on level)";
				}
				if(champName.equals("Karma"))
				{
					if(spellNum == 1)
					{
						if(code.equals("f3")) return "25";
					}
					if(spellNum == 2)
					{
						if(code.equals("f3")) return "for every 100 AP, 1";
						if(code.equals("f2")) return "0.5/0.75/1/1.25";
					}
					if(spellNum == 3)
					{
						if(code.equals("f2")) return "50";
						if(code.equals("f3")) return "60";
					}
				}
				if(champName.equals("Kassadin"))
				{
					if(code.equals("f2")) return "2% maximum mana";
					if(code.equals("f1")) return "1% maximum mana";
					if(code.equals("f3")) return "0.1 AP";
				}
				if(champName.equals("Khazix"))
				{
					if(code.equals("f3")) return "(based on level) 10-180";
					if(code.equals("f2")) return "1.04 AD";
				}
				if(champName.equals("Kindred"))
				{
					if(spellNum == 1)
					{
						if(code.equals("f2")) return "55/75/95/115/135 + 5 damage per stack of Mark of the Kindred";
						if(code.equals("f1")) return "0.2 AD";
					}
					if(spellNum == 2)
					{
						if(code.equals("f2")) return "0.4 AD";
					}
					if(spellNum == 3)
					{
						if(code.equals("f1")) return "0.2 AD";
					}
				}
				if(champName.equals("KogMaw"))
				{
					if(spellNum == 2)
					{
						if(code.equals("f1")) return "0.75% per 100 AP";
					}
					if(spellNum == 4)
					{
						if(code.equals("f3")) return "1.3 AD";
						if(code.equals("f2")) return "0.5 AP";
						if(code.equals("f4")) return "210/330/450";
						if(code.equals("f6")) return "1.95 AD";
						if(code.equals("f5")) return "0.75 AP";
					}
				}
				if(champName.equals("Lulu"))
				{
					if(code.equals("f4")) return "56/87.5/119/150.5/182";
					if(code.equals("f5")) return "0.35 AP";
					if(code.equals("f6")) return "80/125/170/215/260 +0.5 AP";
				}
				if(champName.equals("Malphite"))
				{
					if(code.equals("f1")) return "0";
					if(code.equals("f2")) return "0.1 armor";
				}
				if(champName.equals("Malzahar"))
				{
					if(spellNum == 2)
					{
						if(code.equals("f1")) return "0.3/0.325/0.35/0.375/0.40 AD";
					}
					if(spellNum == 3)
					{
						if(code.equals("f1")) return "";
					}
				}
				if(champName.equals("MasterYi"))
				{
					if(code.equals("f1")) return "0";
				}
				if(champName.equals("MissFortune"))
				{
					if(spellNum == 1)
					{
						if(code.equals("f1")) return "0.85 AD";
						if(code.equals("f2")) return "1.0 AD";
					}
					if(spellNum == 2)
					{
						if(code.equals("f2")) return "(affected by cooldown reduction) 2";
					}
					if(spellNum == 4)
					{
						if(code.equals("f3")) return "(1 + bonus crit damage) x 20";
						if(code.equals("f2")) return "0";
					}
				}
				if(champName.equals("Mordekaiser"))
				{
					if(spellNum == 1)
					{
						if(code.equals("f5")) return "20/40/60/80/100";
						if(code.equals("f3")) return "2.0/2.4/2.8/3.2/3.6 AD";
						if(code.equals("f4")) return "2.4 AP";
					}
					if(spellNum == 2)
					{
						if(code.equals("f3")) return "0";
					}
					if(spellNum == 4)
					{
						if(code.equals("f1")) return "100% of Morde's bonus attack damage as ";
						if(code.equals("f2")) return "15% of Morde's max health as bonus ";
					}
				}
				if(champName.equals("Nami"))
				{
					if(code.equals("f1")) return "85 (+0.075 AP)";
				}
				if(champName.equals("Nunu"))
				{
					if(spellNum == 1)
					{
						if(code.equals("f3")) return "10";
						if(code.equals("f4")) return "15";
						if(code.equals("f5")) return "1";
					}
					if(spellNum == 4)
					{
						if(code.equals("f2")) return "78.1/109.4/140.6 (+0.3125 AP)";
					}
				}
				if(champName.equals("Poppy"))
				{
					if(code.equals("f1")) return "0";
					if(code.equals("f2")) return "0";
				}
				if(champName.equals("Quinn"))
				{
					if(code.equals("f2")) return "0.8/0.9/1.0/1.1/1.2 AD";
				}
				if(champName.equals("RekSai"))
				{
					if(spellNum == 2)
					{
						if(code.equals("f1")) return "15/20/25/30";
					}
					if(spellNum == 3)
					{
						if(code.equals("f1")) return "0.8/0.9/1.0/1.1/1.2 AD";
						if(code.equals("f2")) return "1.6/1.8/2.0/2.2/2.4 AD";
					}
				}
				if(champName.equals("Renekton"))
				{
					if(spellNum == 1)
					{
						if(code.equals("f3")) return "this whole";
						if(code.equals("f4")) return "fucking bullshit";
						if(code.equals("f5")) return "needs to be";
						if(code.equals("f6")) return "overridden";
					}
					if(spellNum == 2)
					{
						if(code.equals("f3")) return "2.25 AD";
					}
				}
				if(champName.equals("Rengar"))
				{
					if(spellNum == 1)
					{
						if(code.equals("f3")) return "0/0.5/0.1/0.15/0.2 AD";
						if(code.equals("f2")) return "30-240";
						if(code.equals("f4")) return "50-102";
					}
					if(spellNum == 2)
					{
						if(code.equals("f2")) return "40-240";
						if(code.equals("f1")) return "12-80";
						if(code.equals("f3")) return "75-500";
					}
					if(spellNum == 3)
					{
						if(code.equals("f1")) return "50-340";
					}
				}
				if(champName.equals("Ryze"))
				{
					if(spellNum == 1)
					{
						if(code.equals("f1")) return "3% bonus mana";
						if(code.equals("f3")) return "60-200 (based on level)";
						if(code.equals("f2")) return "3% bonus mana";
					}
					if(spellNum == 2)
					{
						if(code.equals("f1")) return "1% bonus mana";
					}
					if(spellNum == 3)
					{
						if(code.equals("f1")) return "2% bonus mana";
					}
				}
				if(champName.equals("Shen"))
				{
					if(spellNum == 3)
					{
						if(code.equals("f1")) return "30/35/40";
						if(code.equals("f2")) return "12% bonus health";
					}
					if(spellNum == 4)
					{
						if(code.equals("f1")) return "3";
					}
				}
				if(champName.equals("Shyvana"))
				{
					if(code.equals("f2")) return "1.0 AD";
				}
				if(champName.equals("Sion"))
				{
					if(code.equals("f2")) return "0";
				}
				if(champName.equals("Skarner"))
				{
					if(code.equals("f1")) return "";
				}
				if(champName.equals("Soraka"))
				{
					if(spellNum == 1)
					{
						if(code.equals("f1")) return "0.05 AP";
					}
					if(spellNum == 2)
					{
						if(code.equals("f1")) return "3/3.5/4/4.5/5";
					}
				}
				if(champName.equals("Swain"))
				{
					if(spellNum == 1)
					{
						if(code.equals("f1")) return "60/96/130/166/200";
						if(code.equals("f2")) return "120/190/260/330/400";
						if(code.equals("f3")) return "1.2 AP";
						if(code.equals("f4")) return "240/380/520/660/800";
						if(code.equals("f5")) return "2.4 AP";
					}
					if(spellNum == 4)
					{
						if(code.equals("f9")) return "0.12 AP";
						if(code.equals("f10")) return "0.08 AP";
					}
				}
				if(champName.equals("Syndra"))
				{
					if(code.equals("f1")) return "6";
				}
				if(champName.equals("Taliyah"))
				{
					if(spellNum == 1)
					{
						if(code.equals("f1")) return "(affected by cooldown reduction) 140-77";
						if(code.equals("f2")) return "(based on level) 10-20%";
					}
					if(spellNum == 3)
					{
						if(code.equals("f2")) return "(20 + 4 per 100 AP)";
						if(code.equals("f3")) return "40/52.5/65/77.5/90";
						if(code.equals("f1")) return "0.2 AP";
						if(code.equals("f4")) return "15";
					}
					
				}
				if(champName.equals("Taric"))
				{
					if(spellNum == 1)
					{
						if(code.equals("f1")) return "1.5% bonus health";
						if(code.equals("f2")) return "4.5% bonus health";
						if(code.equals("f3")) return "(affected by cooldown reduction) 6-3.3";
					}
					if(spellNum == 2)
					{
						if(code.equals("f1")) return "10/12.5/15/17.5/20 bonus armor";
					}
					if(spellNum == 3)
					{
						if(code.equals("f1")) return "30% bonus armor";
					}
				}
				if(champName.equals("Teemo"))
				{
					if(spellNum == 4)
					{
						if(code.equals("f1")) return "(affected by cooldown reduction) 30/25/20";
					}
				}
				if(champName.equals("Thresh"))
				{
					if(spellNum == 2)
					{
						if(code.equals("f6")) return "1 per soul collected";
					}
					if(spellNum == 3)
					{
						if(code.equals("f3")) return "# of collected souls";
						if(code.equals("f2")) return "8.0/1.1/1.4/1.7/2.0 AD";
					}
				}
				if(champName.equals("Tristana"))
				{
					if(spellNum == 3)
					{
						if(code.equals("f1")) return "0.5/0.65/0.8/0.95/1.1 AD";
					}
				}
				if(champName.equals("Tryndamere"))
				{
					if(spellNum == 1)
					{
						if(code.equals("f2")) return "0.012 AP";
					}
					if(spellNum == 3)
					{
						if(code.equals("f1")) return "1.2 bonus AD";
					}
				}
				if(champName.equals("Twitch"))
				{
					if(spellNum == 3)
					{
						if(code.equals("f1")) return "0.25 bonus AD";
					}
				}
				if(champName.equals("Urgot"))
				{
					if(spellNum == 2)
					{
						if(code.equals("f1")) return "0";
					}
				}
				if(champName.equals("Viktor"))
				{
					if(spellNum == 1)
					{
						if(code.equals("f1")) return "8% maximum mana";
						if(code.equals("f2")) return "0.15 AP";
					}
				}
				if(champName.equals("Vladimir"))
				{
					if(spellNum == 1)
					{
						if(code.equals("f7")) return "(based on level) 40-240";
						if(code.equals("f6")) return "for every 100 AP, 4";
					}
					if(spellNum == 3)
					{
						if(code.equals("f3")) return "10% of max health";
						if(code.equals("f2")) return "2.5% of max health";
					}
					if(spellNum == 4)
					{
						if(code.equals("f4")) return "150/250/350";
						if(code.equals("f3")) return "0.7 AP";
					}
				}
				if(champName.equals("Xerath"))
				{
					if(spellNum == 2)
					{
						if(code.equals("f1")) return "90/135/180/225/270";
						if(code.equals("f2")) return "0.9 AP";
					}
				}
				if(champName.equals("XinZhao"))
				{
					if(spellNum == 2)
					{
						if(code.equals("f1")) return "0.20 bonus AD";
					}
				}
				if(champName.equals("Zed"))
				{
					if(spellNum == 2)
					{
						if(code.equals("f3")) return "0.04/0.08/0.12/0.16/0.2 bonus AD";
					}
				}
				if(champName.equals("Zyra"))
				{
					if(spellNum == 1)
					{
						if(code.equals("f1")) return "(based on level) 29-114";
						if(code.equals("f2")) return "(based on level) 5-7.5";
					}
					if(spellNum == 3)
					{
						if(code.equals("f1")) return "(based on level) 29-114";
						if(code.equals("f2")) return "(based on level) 5-7.5";
					}
				}
			}
			
			// Code should not get here
			Debug.toast(this, "something fucky happened with the coeff");
			Debug.log(this, "cant find " + code);
		}
		
		return "error";
	}
	
	/* static JSONObject loadJson(Context context, String directory, String filename, int type) throws FileNotFoundException, IOException, JSONException {
		
		InputStream in = null;
		File jsonFile = new File(context.getDir(directory, Context.MODE_PRIVATE), filename);
		in = new BufferedInputStream(new FileInputStream(jsonFile));
	
		JSONObject json = null;			
		BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		StringBuilder responseStrBuilder = new StringBuilder();
		String inputStr;
		
		while ((inputStr = streamReader.readLine()) != null) {
			responseStrBuilder.append(inputStr);
		}
		
		json = new JSONObject(responseStrBuilder.toString());
		return json;
	} */
	
	private void statUpdate(String level) throws NullPointerException {
		/* 
		 * When any level or level range is selected from the spinner,
		 * this method updates the stats displayed using that spinner entry.
		 * 
		 */
		
		
		if (level.equals("n"))
		{
			// Some special textview update
		}
		else if (level.equals("1 – 18"))
		{
			// Set champ to lvl 1, make a copy at lvl 18, and use both for the textview update
		}
		else // selected level
		{
			try {
				champion.setValues(Integer.parseInt(level));
			}
			catch(JSONException e) {
				Log.e(Debug.TAG, "JSONException in ChampInfoActivity:");
				Log.e(Debug.TAG, "statUpdate failed");
				Log.e(Debug.TAG, "Couldnt update values at selected level");
			}
			updateViews();
		}
		
	}
	
	void updateViews() {
		healthText.setText(Double.toString(champion.maxHealth));
		healthRegenText.setText(Double.toString(champion.healthRegen));
		if(champion.maxResource > 0) resourceText.setText(Double.toString(champion.maxResource));
		if(champion.resourceRegen > 0) resourceRegenText.setText(Double.toString(champion.resourceRegen));
		adText.setText(Double.toString(champion.attackDamage));
		asText.setText(Double.toString(champion.attackSpeed));
		armorText.setText(Double.toString(champion.armor));
		mrText.setText(Double.toString(champion.magicResist));
		resourceTypeText.setText(champion.resourceType);
		resourceRegenTypeText.setText(champion.resourceRegenType);
		rangeTypeText.setText(champion.rangeType);
		rangeText.setText(Double.toString(champion.range));
		moveSpeedText.setText(Double.toString(champion.moveSpeed));
	}
	
	void setPerLevel(TextView text, int valueType, double baseValue, double growthValue) {	
		if ((baseValue != 0)) {
			if (valueType == LESS_PRECISE) {
				text.setText(String.format("%.1f (+%.0f)", baseValue, growthValue));
			}
			else if (valueType == MORE_PRECISE) {
				text.setText(String.format("%.3f (+%.2f)", baseValue, growthValue));
			}
			else if (valueType == PERCENT) {
				text.setText(String.format("%.3f (+%.1f%%)", baseValue, growthValue));
			}
			else if (valueType == MAGIC_RESIST) {
				text.setText(String.format("%.1f (+%.2f)", baseValue, growthValue));
			}
		}
	}
	
	void setCurrentLevel(double base, double growth, int valueType, String level, TextView view) {
		
		double levelValue = Double.parseDouble(level);
		double stat = base + growth * (levelValue - 1);
		
		if (stat != 0) {
			if (valueType == LESS_PRECISE) {
				view.setText(String.format("%.1f", stat));
			}
			else if (valueType == MID_PRECISE) {
				view.setText(String.format("%.2f", stat));
			}
			else if (valueType == MORE_PRECISE) {
				view.setText(String.format("%.3f", stat));
			}
			else if (valueType == PERCENT) {
				view.setText(String.format("%.3f (+%.1f%%)", base, (growth * (levelValue - 1))));
			}
		}
	}
	
	void setRange(double base, double growth, int rangeType, TextView view) {
		if ((base != 0)) {
			if (rangeType == LESS_PRECISE) {
				view.setText(String.format("%.1f – %.1f", base, (base + 17 * growth)));
			}
			else if (rangeType == MID_PRECISE) {
				view.setText(String.format("%.2f – %.2f", base, (base + 17 * growth)));
			}
			else if (rangeType == MORE_PRECISE) {
				view.setText(String.format("%.3f – %.3f", base, (base + 17 * growth)));
			}
			else if (rangeType == PERCENT) {
				view.setText(String.format("%.3f (+0%% – +%.1f%%)", base, (growth * 17)));
			}
		}
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
