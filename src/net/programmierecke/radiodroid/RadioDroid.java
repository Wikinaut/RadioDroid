package net.programmierecke.radiodroid;

import android.app.Application;

public class RadioDroid extends Application {
	private RadioStation lastStation;
		
	public void setLastStationUrl(String stationUrl) {
		if (lastStation == null) {
			lastStation = new RadioStation();
		}
		lastStation.StreamUrl = stationUrl;
	}

	public boolean isEqualLastStationUrl(String stationUrl) {
		if (lastStation == null) {
			lastStation = new RadioStation();
			return false;
		} else {
			return stationUrl.equals(lastStation.StreamUrl);
		}
	}

}
