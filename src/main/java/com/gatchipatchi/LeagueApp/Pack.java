package com.gatchipatchi.LeagueApp;

import java.net.URL;

class Pack
{
	String filename;
	String directory;
	URL url;
	
	Pack(String downloadFilename, String downloadDirectory) {
		filename = downloadFilename;
		directory = downloadDirectory;
	}
	
	Pack(String downloadFilename, String downloadDirectory, URL downloadUrl) {
		filename = downloadFilename;
		directory = downloadDirectory;
		url = downloadUrl;
	}
}