package com.gatchipatchi.LeagueApp;

import com.loopj.android.http.AsyncHttpResponseHandler;

/*
 * Class FileAsyncHttpResponseHandler
 * 
 * Subclass of com.loopj.android.http.AsyncHttpResponseHandler that
 * can take in a filename.  This allows handler re-use for saving
 * multiple different files in a single activity.
 */

public abstract class FilesAsyncHttpResponseHandler extends AsyncHttpResponseHandler {
	
	String filename;
	String directory;
	
	// ingests filename for use in overridden methods like onSuccess()
	void takeFilename(String f) {
		filename = f;
	}
	
	// ditto, but for the app internal memory directory, since android makes you use getDir()
	void takeDirectory(String d) {
		directory = d;
	}
}