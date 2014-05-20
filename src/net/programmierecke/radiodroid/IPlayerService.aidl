package net.programmierecke.radiodroid;

interface IPlayerService {
	void Play( /* String theUrl, String theName, String theID, */ String theJsonRadioStation);
	void Stop();
	String getCurrentStationID();
}
