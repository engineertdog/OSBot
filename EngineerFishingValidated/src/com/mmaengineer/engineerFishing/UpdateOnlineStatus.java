package com.mmaengineer.engineerFishing;

import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.TimerTask;

class UpdateOnlineStatus extends TimerTask {
	private final String accessKey, authToken, scriptName, username, mode;
	private long startTime;

	public UpdateOnlineStatus(String accessKey, String authToken, String scriptName, String username, long startTime,
			String mode) {
	      this.accessKey = accessKey;
	      this.authToken = authToken;
	      this.scriptName = scriptName;
	      this.username = username;
	      this.startTime = startTime;
	      this.mode = mode;
    }

    public void run() {
    	try {
		    // open a connection to the site
		    URL url = new URL("apiserver/app/osb/onlineUsers/index.php");
		    URLConnection con = url.openConnection();
		    // activate the output
		    con.setDoOutput(true);
		    PrintStream ps = new PrintStream(con.getOutputStream());
		    // send your parameters to your site
		    ps.print("&accessKey=" + this.accessKey);
		    ps.print("&authToken=" + this.authToken);
		    ps.print("&scriptName=" + this.scriptName);
		    ps.print("&username=" + this.username);
		    ps.print("&updateTime=" + System.currentTimeMillis());
			ps.print("&runtime=" + (System.currentTimeMillis() - this.startTime));
			ps.print("&updateMethod=" + this.mode);

		    // we have to get the input stream in order to actually send the request
		    con.getInputStream();

		    // close the print stream
		    ps.close();
	    } catch (MalformedURLException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
    }
}