package net.programmierecke.radiodroid;

import android.app.Application;

public class RadioDroid extends Application {
	private String lastStationUrl="";
	
	public RadioDroid() {
		String lastStation = "";
	}
	
	public void setLastStationUrl(String stationUrl) {
		lastStationUrl = stationUrl;
	}

	public boolean isEqualLastStationUrl(String stationUrl) {
		return stationUrl.equals(lastStationUrl);
	}

}
