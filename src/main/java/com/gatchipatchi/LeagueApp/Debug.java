package com.gatchipatchi.LeagueApp;
import android.content.Context;
import android.widget.Toast;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

class Debug {
	final static String LOG_FILE = "log";
	
	static void toast(Context context, String msg) {
		Toast t = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
		t.show();
	}
	
	static void toast(Context context, String msg, int length) {
		Toast t = Toast.makeText(context, msg, length);
		t.show();
	}
	
	static void toast(Context context, int i, int length) {
		String msg = Integer.toString(i);
		Toast t = Toast.makeText(context, msg, length);
		t.show();
	}
	
	static void showError(Context context, Exception e) {
		if (e != null) {
			Toast t = Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG);
			t.show();
		}
	}
	
	static void appendTextFile(Context context, String directory, String filename, String text) {
		File file;
		if (directory == null) {
			file = new File(context.getFilesDir(), filename);
		}
		else {
			file = new File(context.getDir(directory, Context.MODE_PRIVATE), filename);
		}

		// Save file unless it already exists
		
		PrintWriter out;
		
		// Write

		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
			out.println(text);
			
			if (out.checkError()) {
				log(context, "Oops, PrintWriter error'd");
			}
			
			out.close();
		}
		catch (IOException e) {
			log(context, "file write failure");
			logError(context, e);
		}
	}
	
	static void logError(Context context, Exception e) {
		if (e != null) {
			appendTextFile(context, null, LOG_FILE, e.getMessage());
		}
	}
	
	static void log(Context context, String s) {
		appendTextFile(context, null, LOG_FILE, s);
	}
	
	static void log(Context context, int i) {
		appendTextFile(context, null, LOG_FILE, Integer.toString(i));
	}
}