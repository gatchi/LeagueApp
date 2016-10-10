package com.gatchipatchi.LeagueApp;

import android.app.Activity;
import android.os.Bundle;;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.ImageView.ScaleType;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Gravity;
import android.content.Intent;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.util.Log;
import java.util.Iterator;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import org.json.JSONException;

public class MainActivity extends Activity 
{
	/*
	 * MainActivity Class Info
	 *
	 * The main screen is a champ select screen.  Each champ is represented
	 * by its icon, which also function as buttons.  The buttons are
	 * iteratively laid out in a grid.  Originally, this was done in a table
	 * format, but currently the creator is trying to implement a searchable
	 * list w/ list adapter that functions as a GridLayout, but can use the
	 * search bar at the top to instantly narrow down which champ icons are
	 * shown.
	 */
	
	final static int TABLE_ROW_WIDTH_DEFAULT = 5;
	final static int TABLE_ROW_WIDTH_PORTRAIT = 5;
	final static int TABLE_ROW_WIDTH_LANDSCAPE = 8;
	
	
	//---------------- Class Objects ------------------//
	
	EditText editText;
	TextView warningText;
	Button refreshButton;
	Button clearButton;
	Button logClearButton;
	ArrayList<String> champList;
	int tvId = View.generateViewId();
	
	
	//--------------- onCreate (main) -----------------//
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
		
		// Make sure this directory exists
		Setup.initDirectories(this);
		
		// Setup champ buttons
		boolean champListFileExists = FileOps.checkIfFileExists(this, FileOps.CHAMP_DIR, FileOps.CHAMP_LIST_FILE);
		if (champListFileExists)
		{
			// Load champ list
			
			/*
			 * This should not be here!! It should be in ChampOps
			 */
			
			Log.v(Debug.TAG, "Champ list found.  Loading...");
			
			try {
				champList = FileOps.listReader(FileOps.openFile(this, FileOps.CHAMP_DIR, FileOps.CHAMP_LIST_FILE));
				
				/*
				 * This is the part where you would take that list,
				 * add it to the GridView, and allow it to be filterable.
				 * But ImageAdapter isn't working yet.
				 */
				
				/* GridView iconGrid = (GridView)findViewById(R.id.icon_grid);
				iconGrid.setAdapter(new ImageAdapter(getApplicationContext(), champList));
				iconGrid.setTextFilterEnabled(true); */
				
				// Add champ buttons iteratively
				Iterator<String> champIterator = champList.listIterator();
				LinearLayout ll = new LinearLayout(this);
				TableLayout tl = (TableLayout)findViewById(R.id.button_layout);
				int count = 0;
				
				while (champIterator.hasNext())
				{
					if (warningText != null) {
						warningText.setVisibility(View.GONE);
					}
					
					// Check orientation, adjust row size if needed
					int width = TABLE_ROW_WIDTH_DEFAULT;
					int orientation = getResources().getConfiguration().orientation;
					if (orientation == Configuration.ORIENTATION_PORTRAIT) {
						width = TABLE_ROW_WIDTH_PORTRAIT;
					}
					else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
						width = TABLE_ROW_WIDTH_LANDSCAPE;
					}
						
					if (count % width == 0)
					{
						ll = new LinearLayout(this);
						ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
						ll.setGravity(Gravity.CENTER_HORIZONTAL);
						tl.addView(ll);
					}
					
					ImageButton ib = new ImageButton(this);
					
					// Populate icons
					String champName = champIterator.next();
					InputStream iconStream = FileOps.openInputStream(this, "drawable", champName + ".png");
					ib.setImageBitmap(BitmapFactory.decodeStream(iconStream));
					ib.setAdjustViewBounds(true);
					ib.setScaleType(ScaleType.FIT_CENTER);
					ib.setCropToPadding(false);
					ib.setPadding(0,0,0,0);
					ib.setOnClickListener(champButtonListener);
					ib.setId(count);
					ll.addView(ib, 87, 87);
					
					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(87, 87);
					params.setMargins(2,2,2,2);
					ib.setLayoutParams(params);
					
					count++;
				}
				
				Log.v(Debug.TAG, "Layout generated");
			}
			catch (FileNotFoundException e)
			{
				Debug.toast(this, "Champ missing. Try updating");
				Log.w(Debug.TAG, "FileNotFoundException in MainActivity");
				Log.w(Debug.TAG, e.getMessage());
			}
			catch (IOException e)
			{
				Log.e(Debug.TAG, "IOException");
				Log.e(Debug.TAG, e.getMessage());
			}
		}
		else
		{
			// Ask to update
			Log.v(Debug.TAG, "Adding simple TextView to empty View...");
			LinearLayout ll = (LinearLayout)findViewById(R.id.button_layout);
			warningText = new TextView(this);
			warningText.setText(R.string.welcome_text);
			warningText.setGravity(Gravity.CENTER);
			ll.addView(warningText);
			Log.v(Debug.TAG, "Added.");
			Debug.toast(this, "Tap update to fetch data");
		}
		
		// Add data refresh button
		LinearLayout footer = (LinearLayout) findViewById(R.id.footer);
		refreshButton = new Button(this);
		refreshButton.setText("update");
		footer.addView(refreshButton);
		refreshButton.setOnClickListener(updateButtonListener);
		
		// Add log clear button
		logClearButton = new Button(this);
		logClearButton.setText("clear log");
		footer.addView(logClearButton);
		logClearButton.setOnClickListener(logClearButtonListener);
		
		// Add listener to search bar
		editText = (EditText) findViewById(R.id.search);
		editText.setOnEditorActionListener(searchListener);
	}
	
	
	//----------------- Listeners ---------------------//
	
	private OnClickListener updateButtonListener = new OnClickListener() {
		
		@Override
		public void onClick(View p1)
		{
			// button click action
			Log.i(Debug.TAG, "Updating...");
			try {
				Updater.checkInventory(MainActivity.this);
			}
			catch (JSONException e)
			{
				Log.e(Debug.TAG, e.getMessage());
				Log.e(Debug.TAG, "checkInventory failed while accessing JSON files");
			}
		}
	};
	
	private OnClickListener logClearButtonListener = new OnClickListener() {
		
		@Override
		public void onClick(View p1)
		{
			FileOps.clearMainLog(MainActivity.this);
		}
	};
	
	private OnClickListener champButtonListener = new OnClickListener() {
		
		@Override
		public void onClick(View p1)
		{
			Intent intent = new Intent(MainActivity.this, ChampInfoActivity.class);
			intent.putExtra("button id", p1.getId());
			intent.putExtra("champ list", champList);
			startActivity(intent);
		}
	};
	
	private OnEditorActionListener searchListener = new OnEditorActionListener() {
		
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
		{
			if(actionId == EditorInfo.IME_ACTION_SEARCH)
			{
				// perform Search
				stubMethod();
				return true;
			}
			Toast errorMesg = Toast.makeText(getApplicationContext(), "Something went wrong.", Toast.LENGTH_SHORT);
			errorMesg.show();
			return false;
		}
	};
	
	
	//---------------- Debug Methods ------------------//
	
	void stubMethod() {
		Toast toast = Toast.makeText(getApplicationContext(), "Not implemented", Toast.LENGTH_SHORT);
		toast.show();
	}
}
