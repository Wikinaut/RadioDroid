package net.programmierecke.radiodroid;

import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;

public class RadioStation {

	public String id="";
	public String name="";
	public String streamUrl="";
	public String playStatus="";
	public String homePageUrl="";
	public String iconUrl="";
	public String country="";
	public String subCountry="";
	public String tagsAll="";
	public String language="";
	public int votes=0;
	public int negativeVotes=0;
	public boolean detailedViewSeen=false;


	public RadioStation() {
	}
	
	public String stationDetailsShortString() {
		List<String> aList = new ArrayList<String>();

		if ( !country.isEmpty() ) {
			aList.add( country );
		}
		
		if ( !language.isEmpty() ) {
			aList.add(language);
		}
	
		if ( !tagsAll.isEmpty() ) {
			aList.add(tagsAll);
		}
		
		return TextUtils.join(", ", aList);
	}
}
