package com.mmaengineer.engineerFishing;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class ValidateScript {
	public static String getData(String accessKey, String authToken, String userKey, String username, String scriptName) {
		try {
			StringBuilder sb = new StringBuilder("apiserver/app/osb/validateScript/index.php?accessKey=");
			sb.append(accessKey);
			sb.append("&authToken=" + authToken);
			sb.append("&scriptName=" + scriptName);
			sb.append("&username=" + username);
			sb.append("&userKey=" + userKey);
			InputStream inputStream = new URL(sb.toString()).openStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String line;

			while ((line = bufferedReader.readLine()) != null) {
				if (line.contains("{")) {
					sb = new StringBuilder(line);
					//Remove { and }
					sb.deleteCharAt(0);
					String returned = sb.substring(0, sb.length()-1);
					return returned;
				}
			}
		}
		catch (Exception e) {
			return e.getMessage();
		}

		return null;
	}

	public static String validateScript(String accessKey, String authToken, String userKey, String username,
			String scriptName) {
		String data = getData(accessKey, authToken, userKey, username, scriptName);
		data = data.replace("\"", "");
		String[] returned = data.split(":");
		return returned[1];
	}
}
