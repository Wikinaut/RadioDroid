package net.programmierecke.radiodroid;

import com.google.gson.Gson;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
// import android.widget.Toast;

public class RadioDroid extends Application {

	public RadioDroid() {
	}

	public void setLastStationUrl(String stationUrl) {

		SharedPreferences sp = getSharedPreferences("RadioDroid", Activity.MODE_PRIVATE);
		if ( ( stationUrl == null ) || stationUrl.trim().equals("") ) {
			sp.edit().remove( "last_station_url" ).commit();
		} else {
			// Toast.makeText(this, "now playing: " + stationUrl, Toast.LENGTH_LONG).show();
			sp.edit().putString( "last_station_url", stationUrl ).commit();
		}
	}

	public boolean isPlayingSameLastStationUrl(String stationUrl) {

		SharedPreferences sp = getSharedPreferences("RadioDroid", Activity.MODE_PRIVATE);
	    String spLastStation = sp.getString("last_station_url",null);
	    String spLastStationStatus = sp.getString("last_station_status",null);
		
		boolean isSameStation = ( stationUrl.equals(spLastStation) && (spLastStation != null) );
		boolean isPlaying = (spLastStationStatus == "play"); 

		return isSameStation && isPlaying;
		
	}
	
	public boolean isSameLastStationUrl(String stationUrl) {

		SharedPreferences sp = getSharedPreferences("RadioDroid", Activity.MODE_PRIVATE);
	    String spLastStation = sp.getString("last_station_url",null);
		
		return stationUrl.equals(spLastStation) && (spLastStation != null);

	}
	
	public void setLastStationStatus(String status) {

		SharedPreferences sp = getSharedPreferences("RadioDroid", Activity.MODE_PRIVATE);
		if ( ( status == null ) || status.trim().equals("") ) {
			sp.edit().remove( "last_station_status" ).commit();
		} else {
			// Toast.makeText(this, "status: " + status, Toast.LENGTH_LONG).show();
			sp.edit().putString( "last_station_status", status ).commit();
		}
	}

	public final void putRadioStationPersistentStorage(RadioStation station) {

		SharedPreferences sp = getSharedPreferences("RadioDroid", Activity.MODE_PRIVATE);
	    Gson gson = new Gson();
	    
	    String jsonString = gson.toJson( station );
		// Toast.makeText(this, "PUT: " + jsonString, Toast.LENGTH_LONG).show();
	    sp.edit().putString( "RadioDroid", jsonString ).commit();

	}
	  
	public final void putJsonRadioStationPersistentStorage( String jsonRadioStation) {

		SharedPreferences sp = getSharedPreferences("RadioDroid", Activity.MODE_PRIVATE);
		// Toast.makeText(this, "PUT JSON: " + jsonRadioStation, Toast.LENGTH_LONG).show();
	    sp.edit().putString( "RadioDroid", jsonRadioStation ).commit();

	}
	  

	public final RadioStation getRadioStationPersistentStorage() {
	
		SharedPreferences sp = getSharedPreferences( "RadioDroid", Activity.MODE_PRIVATE );
	    String jsonString = sp.getString( "RadioDroid", null );

	    if (jsonString == null) {
	        return null;
	    }

	    Gson gson = new Gson();
	    return gson.fromJson( jsonString, RadioStation.class );

	}
}
