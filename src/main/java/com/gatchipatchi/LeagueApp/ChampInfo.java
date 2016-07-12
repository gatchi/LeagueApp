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
	
	final static String CHAMPIONS_FILE = "champ_list.txt";
	final static String CHAMPIONS_JSON = "champion.json";
	final static String CHAMPIONS_DIR = "champs";
	final static String ICONS_DIR = "drawable";
	final static int JSON_OBJECT = 1;
	static final short MAX_LEVEL = 18;
	static final short LESS_PRECISE = 0;
	static final short PERCENT = 1;
	static final short MORE_PRECISE = 2;
	static final short MAGIC_RESIST = 3;
	static final short MID_PRECISE = 4;
	
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
		
		// Load champb and set champ name on the view
		
		champList = (ArrayList<String>) bundle.get("champ list");
		champName = champList.get(champId);
		TextView champNameView = (TextView) findViewById(R.id.champ_name);
		if (champName.equals("MonkeyKing")) {
			champNameView.setText("Wukong");
		}
		else {
			champNameView.setText(champName);
		}
		
		// Load the JSON data
		
		try {
			
			JSONObject champJson = loadJson(this, CHAMPIONS_DIR, champName + ".json", JSON_OBJECT);
			JSONObject champData = champJson.getJSONObject("data");
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
			
			rangeText.setText(String.format("%.0f", attackRange));
			
			// Set movespeed on stat page
			
			TextView movespeedText = (TextView) findViewById(R.id.movespeed);
			movespeedText.setText(String.format("%.0f" ,moveSpeed));
			
		} catch (UnsupportedEncodingException e) {
			Debug.toast(this, "UnsupportedEncodingException");
			Debug.logError(this, e);
			
		} catch (JSONException e) {
			Debug.toast(this, "JSONException");
			Debug.logError(this, e);
			
		} catch (IOException e) {
			Debug.toast(this, "IOException");
			Debug.logError(this, e);
			
		} catch (NullPointerException e) {
			Debug.toast(this, "NullPointerException");
			Debug.log(this, "NullPointerException in onCreate on stat set");
		}
			
		// Load skills (champion spells)
		
		try {
			JSONObject champJson = loadJson(this, CHAMPIONS_DIR, champName + ".json", JSON_OBJECT);
			JSONObject champData = champJson.getJSONObject("data");
			JSONObject champ = champData.getJSONObject(champName);
			
			JSONArray spells = champ.getJSONArray("spells");
			JSONObject qSpell = (JSONObject)spells.get(0);
			JSONObject wSpell = (JSONObject)spells.get(1);
			JSONObject eSpell = (JSONObject)spells.get(2);
			JSONObject rSpell = (JSONObject)spells.get(3);
			JSONObject passive = champ.getJSONObject("passive");
			
			TextView qView = (TextView)findViewById(R.id.q);
			TextView wView = (TextView)findViewById(R.id.w);
			TextView eView = (TextView)findViewById(R.id.e);
			TextView rView = (TextView)findViewById(R.id.r);
			TextView passiveView = (TextView)findViewById(R.id.passive);
			
			/* qView.setText(Html.fromHtml(q.getString("tooltip")));
			wView.setText(Html.fromHtml(w.getString("tooltip")));
			eView.setText(Html.fromHtml(e.getString("tooltip")));
			rView.setText(Html.fromHtml(r.getString("tooltip"))); */
			passiveView.setText(Html.fromHtml(passive.getString("description")));
			
			String qtt = qSpell.getString("tooltip");
			String wtt = wSpell.getString("tooltip");
			String ett = eSpell.getString("tooltip");
			String rtt = rSpell.getString("tooltip");
			String parsedQtt = "";
			String parsedWtt = "";
			String parsedEtt = "";
			String parsedRtt = "";

			try {
				parsedQtt = ttParser(qtt);
				parsedWtt = ttParser(wtt);
				parsedEtt = ttParser(ett);
				parsedRtt = ttParser(rtt);
			} catch (IOException e) {
				Debug.log(this, e.getMessage());
			}
			
			qView.setText(Html.fromHtml(parsedQtt));
			wView.setText(Html.fromHtml(parsedWtt));
			eView.setText(Html.fromHtml(parsedEtt));
			rView.setText(Html.fromHtml(parsedRtt));
		
		} catch (JSONException e) {
			Debug.log(this, e.getMessage());
		} catch (FileNotFoundException e) {
			Debug.log(this, e.getMessage());
		} catch (IOException e) {
			Debug.log(this, e.getMessage());
		}
		
		
	}
	
	
	//--------------- Class Methods -------------------//
	
	String ttParser(String input) throws IOException {
		/*
		 * Spell JSON arrays use codes in their tooltips.
		 * The codes translate to numbers found in other parts of the
		 * champion JSON object.
		 * This method generates a new string with codes replaced with numbers
		 * using the inputted string.
		 * 
		 * Depends on method ChampInfo.translate()
		 */
		
		StringReader sr = new StringReader(input);
		int rawChar = 0;
		char c = 0;
		String result = "";
		String code = "";
		String word = "";
		
		do
		{
			rawChar = sr.read();
			if (rawChar != -1)
			{
				c = (char)rawChar;
				if (c == '{')
				{
					sr.mark(1);
					c = (char)sr.read();
					
					if (c == '{')
					{
						sr.skip(1);
						c = (char)sr.read();
						code = code + c;
						c = (char)sr.read();
						code = code + c;
						word = translate(code);
						result = result + word;
						sr.skip(3);
					}
					else
					{
						sr.reset();
						c = (char)sr.read();
						result = result + c;
					}
				}
				else result = result + c;
			}
		} while (rawChar != -1);
		
		return result;
	}
	
	String translate(String code) {
		/*
		 * Looks up the code and returns the value.
		 */
		return "0";
	}
	
	static JSONObject loadJson(Context context, String directory, String filename, int type) throws FileNotFoundException, IOException, JSONException {
		
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
	}
	
	private void statUpdate(String level) throws NullPointerException {
		/* 
		 * When any level or level range is selected from the spinner,
		 * this method updates the stats displayed using that spinner entry.
		 * 
		 */
				
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
	public boolean onOptionsItemSelected(MenuItem item) {
		// back button
		if (item.getItemId() == android.R.id.home) finish();
		// auto generated code
		return super.onOptionsItemSelected(item);
		
	}
	
	// spinner handler
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		// An item was selected. You can retrieve the selected item using
		// parent.getItemAtPosition(pos)
		String item = (String) parent.getItemAtPosition(pos);
		try {
			statUpdate(item);
		}
		catch (NullPointerException e) {
			Debug.log(getApplicationContext(), "NullPointerException in statUpdate");
		}
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// not used
	}
	
	public void stubMethod() {
		// Simple test method for stubs.
		Toast toast = Toast.makeText(this, "Tada!", Toast.LENGTH_SHORT);
		toast.show();
	}
	
}
