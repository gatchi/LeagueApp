package com.gatchipatchi.LeagueApp;

import android.content.Context;
import java.io.File;

class Setup {

	static void initDirectories(Context context) {

		// Make sure director structure is correct (new install)
		
		File championDirectory = context.getDir("champs", Context.MODE_PRIVATE);
		if (!championDirectory.exists()) {
			Debug.toast(context, "making champ directory");
			championDirectory.mkdir();
		}
		
		File drawableDir = context.getDir("drawable", Context.MODE_PRIVATE);
		if (!drawableDir.exists()) {
			Debug.toast(context, "creating drawable directory");
			drawableDir.mkdir();
		}
		
	}
}