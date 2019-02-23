package com.mmaengineer.engineerFishing;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class GrandExchange {
	public static HashMap<Integer, Integer> cache = new HashMap<Integer, Integer>();
	
	public static String getData(int itemID) {
		try {
			StringBuilder sb = new StringBuilder("https://api.rsbuddy.com/grandExchange?a=guidePrice&i=");
			sb.append(String.valueOf(itemID));
			InputStream inputStream = new URL(sb.toString()).openStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String line;
			
			while ((line = bufferedReader.readLine()) != null) {
				if (line.contains("{")) {
					sb = new StringBuilder(line);
					//Remove { and }
					sb.deleteCharAt(0);
					//sb.deleteCharAt((line.length() - 1));
					return sb.toString();
				}
			}
		}
		catch (Exception e) {
			return e.getMessage();
		}
		
		return null;
	}
	
	public static String[] parseData(String data) {
		ArrayList<String> holder = new ArrayList<String>();
		String[] parts = data.split(","); //Now we have strings in format "x":y
		for (String s : parts) {
			s = s.replace("\"", ""); //Remove " - now in format x:y
			holder.add(s.split(":")[1]); //Extract y from format x:y
		}
		String[] ret = new String[holder.size()];
		return holder.toArray(ret);
	}
	
	public static int getPrice(int itemID) {
		if (itemID == 995) return 1;
		
		if (cache.containsKey(itemID)) {
			return cache.get(itemID);
		}
		String[] data = parseData(getData(itemID));
		int price = Integer.valueOf(data[0]);
		cache.put(itemID, price);
		return price;
	}
}