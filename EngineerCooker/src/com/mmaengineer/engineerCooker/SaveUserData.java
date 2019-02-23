package com.mmaengineer.engineerCooker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

class SaveUserData {
    public static String saveUserDataMethod(String accessKey, String authToken, String scriptName, String username,
    		long startTime, int xpGained, int levelsGained, int itemsCooked, long profit) {
    	String returnedValue = "Saving your data failed. Unknown reason.";

    	try {
    		StringBuilder sb = new StringBuilder("apiserver/app/osb/" + scriptName + "/index.php?accessKey=");
			sb.append(accessKey);
			sb.append("&authToken=" + authToken);
		    sb.append("&scriptName=" + scriptName);
		    sb.append("&username=" + username);
			sb.append("&runtime=" + ((new Date().getTime() / 1000) - startTime));
		    sb.append("&xpGained=" + xpGained);
		    sb.append("&levelsGained=" + levelsGained);
			sb.append("&itemsCooked=" + itemsCooked);
			sb.append("&profit=" + profit);
			InputStream inputStream = new URL(sb.toString()).openStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String line;
			String newString = null;

			while ((line = bufferedReader.readLine()) != null) {
				if (line.contains("{")) {
					sb = new StringBuilder(line);
					sb.deleteCharAt(0);
					newString = sb.substring(0, sb.length()-1);
				}
			}

			newString = newString.replace("\"", "");

			if (newString.equals("success:true")) {
				returnedValue = "success";
			} else {
				String[] returned = newString.split("message:");
				returnedValue = returned[1];
			}
	    } catch (MalformedURLException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }

		return returnedValue;
    }
}