package com.mmaengineer.com;

import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
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
        Date currentDate = new Date();

        try {
            URL url = new URL("apiserver/app/osb/onlineUsers/index.php");
            URLConnection con = url.openConnection();
            con.setDoOutput(true);
            PrintStream ps = new PrintStream(con.getOutputStream());
            ps.print("&accessKey=" + this.accessKey);
            ps.print("&authToken=" + this.authToken);
            ps.print("&scriptName=" + this.scriptName);
            ps.print("&username=" + this.username);
            ps.print("&updateTime=" + (currentDate.getTime() / 1000));
            ps.print("&runtime=" + ((currentDate.getTime() / 1000) - this.startTime));
            ps.print("&updateMethod=" + this.mode);

            con.getInputStream();
            ps.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
