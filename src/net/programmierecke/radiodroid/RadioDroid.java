package net.programmierecke.radiodroid;

import com.google.gson.Gson;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;

public class RadioDroid extends Application {

	public RadioDroid() {
	}

	public boolean isPlayingSameLastStationUrl(String stationUrl) {

		RadioStation lastStation = getRadioStationPersistentStorage();
		
		if (lastStation != null) {

			boolean isSameStation = (lastStation.StreamUrl != null) && stationUrl.equals(lastStation.StreamUrl);
			boolean isPlaying = (lastStation.Status == "play");
			
			return isSameStation && isPlaying;
			
		} else {
			return false;
		}
	}
	
	public boolean isSameLastStationUrl(String stationUrl) {

		RadioStation lastStation = getRadioStationPersistentStorage();
		
		if (lastStation != null) {
			return (lastStation.StreamUrl != null) && stationUrl.equals(lastStation.StreamUrl);
		} else {
			return false;
		}
	}
	

	public void setLastStationStatus(String status) {

		RadioStation lastStation = getRadioStationPersistentStorage();
		lastStation.Status = status;
		putRadioStationPersistentStorage( lastStation );		    
	}

	public String getLastStationStatus() {

		RadioStation lastStation = getRadioStationPersistentStorage();
		return (lastStation != null) ? lastStation.Status : "";
	}

	public void setLastStationStreamUrl(String stationUrl) {
		RadioStation lastStation = getRadioStationPersistentStorage();
		lastStation.StreamUrl = stationUrl;
		putRadioStationPersistentStorage( lastStation );		    
	}

	public String getLastStationStreamUrl() {

		RadioStation lastStation = getRadioStationPersistentStorage();
		return (lastStation != null) ? lastStation.StreamUrl : "";

	}

	public final void putRadioStationPersistentStorage(RadioStation station) {

		SharedPreferences sp = getSharedPreferences("RadioDroid", Activity.MODE_PRIVATE);
	    Gson gson = new Gson();
	    
	    String jsonString = gson.toJson( station );
	    sp.edit().putString( "RadioDroid", jsonString ).commit();

	}
	  
	public final void putJsonRadioStationPersistentStorage( String jsonRadioStation) {

		SharedPreferences sp = getSharedPreferences("RadioDroid", Activity.MODE_PRIVATE);
	    sp.edit().putString( "RadioDroid", jsonRadioStation ).commit();

	}
	  

	public final RadioStation getRadioStationPersistentStorage() {
	
		SharedPreferences sp = getSharedPreferences( "RadioDroid", Activity.MODE_PRIVATE );
		String jsonString = sp.getString( "RadioDroid", null );

	    if (jsonString == null) {

			RadioStation initStation = new RadioStation();
			putRadioStationPersistentStorage(initStation);
			sp = getSharedPreferences( "RadioDroid", Activity.MODE_PRIVATE );
			jsonString = sp.getString( "RadioDroid", null );

	    }

	    Gson gson = new Gson();
	    return gson.fromJson( jsonString, RadioStation.class );

	}
}
