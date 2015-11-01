package net.programmierecke.radiodroid;

import com.google.gson.Gson;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

public class RadioDroid extends Application {

	public RadioDroid() {
	}

	public static IPlayerService globalPlayerService;
	public static RadioStationList globalRadioStationList;

	public static ServiceConnection globalPlayerServiceConnector = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder binder) {
			globalPlayerService = IPlayerService.Stub.asInterface(binder);
		}

		public void onServiceDisconnected(ComponentName className) {
			globalPlayerService = null;
		}
	};


	public boolean isPlayingSameLastStationUrl(String stationUrl) {

		RadioStation lastStation = getRadioStationPersistentStorage();
		
		if (lastStation != null) {

			boolean isSameStation = ( lastStation.streamUrl != null ) && stationUrl.equals(lastStation.streamUrl);
			boolean isPlaying = ( lastStation.playStatus.equals("play") );
			return isSameStation && isPlaying; 
			
		} else {
			return false;
		}
	}
	
	public boolean isSameLastStationUrl(String stationUrl) {

		RadioStation lastStation = getRadioStationPersistentStorage();
		
		if (lastStation != null) {
			return (lastStation.streamUrl != null) && stationUrl.equals(lastStation.streamUrl);
		} else {
			return false;
		}
	}
	

	public void setLastStationDetailedViewSeen( boolean seen) {

		RadioStation lastStation = getRadioStationPersistentStorage();
		lastStation.detailedViewSeen = seen;
		putRadioStationPersistentStorage( lastStation );		    
	}

	public boolean getLastStationDetailedViewSeen() {

		RadioStation lastStation = getRadioStationPersistentStorage();
		return (lastStation != null ) ? lastStation.detailedViewSeen : false; 		    
	}

	public void setLastStationStatus(String status) {

		Log.v("radiodroid","setlaststatus:"+status);
		RadioStation lastStation = getRadioStationPersistentStorage();
		lastStation.playStatus = status;
		putRadioStationPersistentStorage( lastStation );		    
	}
	

	public String getLastStationStatus() {

		RadioStation lastStation = getRadioStationPersistentStorage();
		return (lastStation != null) ? lastStation.playStatus : "";
	}

	public void setLastStationStreamUrl(String stationUrl) {
		RadioStation lastStation = getRadioStationPersistentStorage();
		lastStation.streamUrl = stationUrl;
		putRadioStationPersistentStorage( lastStation );		    
	}

	public String getLastStationStreamUrl() {

		RadioStation lastStation = getRadioStationPersistentStorage();
		return (lastStation != null) ? lastStation.streamUrl : "";

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
