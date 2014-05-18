package net.programmierecke.radiodroid;

import android.os.Bundle;
import android.app.Activity;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

public class AutoCompleteActivity extends Activity {

	 private AutoCompleteTextView autoComplete;
	 private ArrayAdapter<String> adapter;
	   
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.station_search);
		
		// get the defined string-array 
		String[] stations = getResources().getStringArray(R.array.stationList);
		
		adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,stations);

		autoComplete = (AutoCompleteTextView) findViewById(R.id.autoComplete);
		
		// set adapter for the auto complete fields
		autoComplete.setAdapter(adapter);
		
		// specify the minimum type of characters before drop-down list is shown
		autoComplete.setThreshold(1);

	}

}
