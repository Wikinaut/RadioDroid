package net.programmierecke.radiodroid;
 
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.util.Log;
import android.widget.ArrayAdapter;
 
public class MainActivity2 extends Activity {

	ProgressDialog thisProgressLoading;
	BackgroundTaskGetStationList2 globalGetStationListTask2;

    // for our logs
    public static final String TAG = "MainActivity2.java";
 
    /*
     * Change to type CustomAutoCompleteView instead of AutoCompleteTextView
     * since we are extending to customize the view and disable filter
     * The same with the XML view, type will be CustomAutoCompleteView
     */
    CustomAutoCompleteView myAutoComplete;
     
    // adapter for auto-complete
    ArrayAdapter<String> myAdapter;
     
    // for database operations
    DatabaseHandler databaseH;
     
    // just to add some initial value
    String[] item = new String[] {"Please search..."};
     
    @Override
    protected void onCreate(Bundle savedInstanceState) {
         
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         
        try{
             
            // instantiate database handler
            databaseH = new DatabaseHandler(MainActivity2.this);
            
            createStationListDatabase();
             
            // autocompletetextview is in activity_main.xml
            myAutoComplete = (CustomAutoCompleteView) findViewById(R.id.myautocomplete);
             
            // add the listener so it will tries to suggest while the user types
            myAutoComplete.addTextChangedListener(new CustomAutoCompleteTextChangedListener(this));
             
            // set our adapter
            myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, item);
            myAutoComplete.setAdapter(myAdapter);
         
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
          
    // this function is used in CustomAutoCompleteTextChangedListener.java
    public String[] getItemsFromDb(String searchTerm){
         
        // add items on the array dynamically
        List<MyObject> products = databaseH.read(searchTerm);
        int rowCount = products.size();
         
        String[] item = new String[rowCount];
        int x = 0;
         
        for (MyObject record : products) {
             
            item[x] = record.objectName;
        	// Log.e(TAG, "item: " + item[x]);
           
            x++;
        }
         
        return item;
    }
    
	private class BackgroundTaskGetStationList2 extends AsyncTask <String, Void, String> {

		@Override
		protected void onPreExecute() {
			thisProgressLoading = ProgressDialog.show(
				MainActivity2.this,
				"",
				getString(R.string.loading_station_list_from_server)
			);
		}
		
		@Override
		protected String doInBackground(String... params) {
			return Utils.getFromUrl( params[0] );
		}

		@Override
		protected void onPostExecute(String result) {
			
			if (!isFinishing()) {
				
				int i=0;

				for (RadioStation aStation : Utils.decodeJson(result)) {
					
		    		// Log.e(TAG, aStation.name + " created.");
		    		databaseH.create( aStation );
		    		i++;

				}

				Log.e(TAG, String.valueOf(i) + " station entries created.");
				thisProgressLoading.dismiss();
			}
			
			super.onPostExecute(result);
			
		}
	}

	
    
    public void createStationListDatabase() {
	
    	globalGetStationListTask2 = new BackgroundTaskGetStationList2();
    	globalGetStationListTask2.execute( Constants.ALL_STATIONS_URL );
  	
	}
 
}
