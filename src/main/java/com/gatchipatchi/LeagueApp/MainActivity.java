package com.gatchipatchi.LeagueApp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ImageView.ScaleType;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;

public class MainActivity extends Activity 
{
	// objects
	EditText editText;
	ImageButton champButton1, champButton2, champButton3, champButton4;
	
	// onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
		
		// some test code
		LinearLayout ll = (LinearLayout)findViewById(R.id.button_layout);
		for(int i=0; i<4; i++)
		{
			ImageButton ib = new ImageButton(this);
			
			// set icon
			Resources res = getResources();
			TypedArray champIcons = res.obtainTypedArray(R.array.champ_icons);
			ib.setImageResource(champIcons.getResourceId(i, 0));
			champIcons.recycle();
			
			ib.setAdjustViewBounds(true);
			ib.setScaleType(ScaleType.FIT_CENTER);
			ib.setCropToPadding(false);
			ib.setPadding(0,0,0,0);
			ib.setOnClickListener(champButtonListener);
			ib.setId(i);
			ll.addView(ib, 87, 87);
			
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(87, 87);
			params.setMargins(2,2,2,2);
			ib.setLayoutParams(params);
		}
		
		// search bar
		editText = (EditText) findViewById(R.id.search);
		editText.setOnEditorActionListener(searchListener);
		
//		// buttons (this is gonna be long)
//		champButton1 = (ImageButton) findViewById(R.id.champbutton1);
//		champButton1.setOnClickListener(champButtonListener);
//
//		champButton2 = (ImageButton) findViewById(R.id.champbutton2);
//		champButton2.setOnClickListener(champButtonListener);
//		
//		champButton3 = (ImageButton) findViewById(R.id.champbutton3);
//		champButton3.setOnClickListener(champButtonListener);
//		
//		champButton4 = (ImageButton) findViewById(R.id.champbutton4);
//		champButton4.setOnClickListener(champButtonListener);
    }
	
	// stub method for testing
	void stubMethod()
	{
		// stub
		Toast toast = Toast.makeText(getApplicationContext(), "Tada!", Toast.LENGTH_SHORT);
		toast.show();
	}
	
	// champ button onClick listener
	private OnClickListener champButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View p1)
		{
			// button click action
			//Toast toast = Toast.makeText(getApplicationContext(), Integer.toString(p1.getId()), Toast.LENGTH_LONG);
			//toast.show();
			Intent intent = new Intent(MainActivity.this, ChampInfo.class);
			intent.putExtra("button id", p1.getId());
			startActivity(intent);
		}
	};
	
	private OnEditorActionListener searchListener = new OnEditorActionListener()
	{
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
	
}
