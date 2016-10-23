package it.rest.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RestUtils {

	public static String restRequest(String myURL)
	{
	    try {
	    	URL url = new URL(myURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(500);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));

			StringBuffer buffer = new StringBuffer();
	        int read;
	        char[] chars = new char[1024];
	        while ((read = br.read(chars)) != -1)
	            buffer.append(chars, 0, read); 

	        if (br != null)
	            br.close();
			conn.disconnect();
			/*String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}*/
	        return buffer.toString();

	    } catch (java.net.SocketTimeoutException e) {
	    	  System.out.println("Non Trovata su questa porta, Timeout");
		      return "NotFoundOnPort";
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return "Error";
	}
	
	public static boolean testEndpoint(String myURL)
	{
	    try {
	    	URL url = new URL(myURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(200);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}
			conn.disconnect();
			/*String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}*/
	        return true;

	    } catch (java.net.SocketTimeoutException e) {
	    	  System.out.println("Non Trovata su questa porta, Timeout");
		      return false;
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return false;
	}
}
