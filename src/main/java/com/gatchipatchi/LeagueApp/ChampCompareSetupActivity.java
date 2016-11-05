package com.gatchipatchi.LeagueApp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ChampCompareSetupActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.champ_compare_setup_activity);
		
		// Populate spinner 1
		try {
			Spinner spinner1 = (Spinner) findViewById(R.id.compare_options_1);
			ArrayAdapter<CharSequence> spinner1Adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, FileOps.retrieveList(this, FileOps.CHAMP_DIR, FileOps.CHAMP_LIST_FILE));
			spinner1.setAdapter(spinner1Adapter);
		}
		catch(FileNotFoundException e) {
			Log.e(Debug.TAG, "FileNotFoundException in ChampCompareSetupActivity:");
			Log.e(Debug.TAG, "Population of spinner 1 failed");
			Log.e(Debug.TAG, e.getMessage());
		}
		catch(IOException e) {
			Log.wtf(Debug.TAG, e.getMessage());
		}
	}
}