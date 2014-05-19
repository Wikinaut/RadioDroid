package net.programmierecke.radiodroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.content.Context;
import android.content.pm.PackageManager;

public class Utils {
	public static RadioStation[] DecodeJson(String result) {
		List<RadioStation> aList = new ArrayList<RadioStation>();
		List<String> aTags = new ArrayList<String>();

		try {
			JSONArray jsonArray = new JSONArray(result);

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject o = jsonArray.getJSONObject(i);

				String streamUrl = o.getString("url");
				
				if ( !TextUtils.isEmpty(streamUrl) ) {

					RadioStation aStation = new RadioStation();

					aStation.StreamUrl = html(streamUrl);
					
					String country = html(o.getString("country"));
					country = country.replace("United States of America", "USA");
					
					String subCountry = html(o.getString("subcountry"));

					if ( !country.isEmpty() && !subCountry.isEmpty() ) {
						
						country = country + "/" + subCountry;
						
					} else {
						
						if ( country.isEmpty() && !subCountry.isEmpty() ) {
							country = subCountry;
						}
					
					}
					aStation.Country = country;
					aStation.SubCountry = subCountry;
					
					aStation.ID = html(o.getString("id"));
					aStation.Name = html(o.getString("name"));
					aStation.Votes = o.getInt("votes");
					aStation.NegativeVotes = o.getInt("negativevotes");
					aStation.HomePageUrl = html(o.getString("homepage"));

					String tags = html(o.getString("tags"));
					if ( !tags.isEmpty() ) {
						aTags.clear();
						for (String part : tags.split(",")) {
							if (part.trim() != "") {
								aTags.add(part.trim());
							}
						}
						aStation.TagsAll = TextUtils.join(", ", aTags);
					}					

					aStation.IconUrl = html(o.getString("favicon"));
					aStation.Language = html(o.getString("language"));
				
					aList.add(aStation);
				}
	
			}

		} catch (JSONException e) {
			
			e.printStackTrace();
			
		}
		
		return aList.toArray(new RadioStation[0]);
	}

	public static String downloadFeed(String theURI) {
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(theURI);
		
		try {
			
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			
			if (statusCode == 200) {
				
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
				
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
				
			} else {
				
				Log.e("", "Failed to download file");
				
			}
			
		} catch (ClientProtocolException e) {
			
			Log.e("", "" + e);
			
		} catch (IOException e) {
			
			Log.e("", "" + e);
			
		}
		
		return builder.toString();
	}
	
	public static String getBase64(String theOriginal) {
		return Base64.encodeToString(theOriginal.getBytes(), Base64.URL_SAFE | Base64.NO_PADDING);
	}
	
	public static String html(String str) {
		return Html.fromHtml(str).toString().trim();
	}

	public static String getAppName(Context context) {
		return context.getString(R.string.app_name);
	}

	public static String getVersionName(Context context) {
	    String versionName;
		
	    try {
	      versionName = context.getPackageManager()
	                      .getPackageInfo(context.getPackageName(), 0)
	                      .versionName;
	    } catch (PackageManager.NameNotFoundException e) {
	      throw new AssertionError(e);
	    }
	    return versionName;
	  }
	
	public static String getAppAndVersionName(Context context) {
		return context.getString(R.string.app_name) + " " + getVersionName(context);
	}

	public static boolean hasWifiConnection(Context context) {
		
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		return mWifi.isConnected();

	}

}
