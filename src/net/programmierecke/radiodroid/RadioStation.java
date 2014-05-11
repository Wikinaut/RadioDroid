package net.programmierecke.radiodroid;

import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;

public class RadioStation {

	public String ID="";
	public String Name="";
	public String StreamUrl="";
	public String HomePageUrl="";
	public String IconUrl="";
	public String Country="";
	public String SubCountry="";
	public String TagsAll="";
	public String Language="";
	public int Votes=0;
	public int NegativeVotes=0;

	public RadioStation() {
	}
	
	public String stationDetailsShortString() {
		List<String> aList = new ArrayList<String>();

		if ( !Country.isEmpty() ) {
			aList.add( Country );
		}
		
		if ( !Language.isEmpty() ) {
			aList.add(Language);
		}
	
		if ( !TagsAll.isEmpty() ) {
			aList.add(TagsAll);
		}
		
		return TextUtils.join(", ", aList);
	}
}
