package net.programmierecke.radiodroid;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import net.programmierecke.radiodroid.Constants;

public class MainActivity extends ListActivity {

	ProgressDialog thisProgressLoading;
	// RadioStationList globalRadioStationList = null;
	BackgroundTaskGetStationList globalGetStationListTask;
	
	private static final String TAG = "RadioDroid";

	private class BackgroundTaskGetStationList extends AsyncTask <String, Void, String> {

		@Override
		protected void onPreExecute() {
			thisProgressLoading = ProgressDialog.show(
				MainActivity.this,
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
				
				RadioDroid.globalRadioStationList.clear();
				
				for (RadioStation aStation : Utils.decodeJson(result)) {
					RadioDroid.globalRadioStationList.add(aStation);
				}
				
				getListView().invalidate();
				thisProgressLoading.dismiss();
			}
			
			super.onPostExecute(result);
			
		}
	}

	private void createStationList(final String stationListUrl) {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		// skip station list creation 
		// if list is already present, and force-always-update preference is unset
		
		if ( !prefs.getBoolean( "pref_toggle_always_refresh_station_lists", true )
				&& RadioDroid.globalRadioStationList != null ) {
			
			setListAdapter(RadioDroid.globalRadioStationList);
		
			return;

		}
	
		// skip starting a new task if another task is already active

		if ( ( globalGetStationListTask != null )
			&& ( globalGetStationListTask.getStatus() == AsyncTask.Status.RUNNING ) ) {
		
			return;
		}
		
		globalGetStationListTask = new BackgroundTaskGetStationList();
		RadioDroid.globalRadioStationList = new RadioStationList(this, R.layout.station_list);
		setListAdapter(RadioDroid.globalRadioStationList);

		// station list creation
		globalGetStationListTask.execute( stationListUrl );
				
	}	
	
	@Override
	protected void onPause() {
		super.onPause();
		PlayerService thisService = new PlayerService();
		thisService.unbindSafely( this, RadioDroid.globalPlayerServiceConnector );
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v("mainactivity","oncreate");
		setTitle( Utils.getAppAndVersionName( this ) + " (" + getString(R.string.top_clicks) + ")" );

		RadioDroid thisApp = (RadioDroid) getApplication();
		RadioStation lastRadioStation = thisApp.getRadioStationPersistentStorage();

        // Read the default values and set them as the current values.  
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);  
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
        // Android bug workaround
        // https://code.google.com/p/android/issues/detail?id=6641
        // for all "false" defaultValues in preferences.xml 
		prefs.edit().putBoolean("pref_toggle_notify_server_about_play_click", prefs.getBoolean("pref_toggle_notify_server_about_play_click", false)).commit();
		prefs.edit().putBoolean("pref_toggle_always_refresh_station_lists", prefs.getBoolean("pref_toggle_always_refresh_station_lists", false)).commit();
		String autoPlayPreferenceValue = prefs.getString("pref_autoplay_settings", "(undefined)" );

		if ( ( autoPlayPreferenceValue.equals( "autoplay_play" )
				|| autoPlayPreferenceValue.equals( "autoplay_last_status" )
				|| autoPlayPreferenceValue.equals( "autoplay_pause" ) )
				&& !lastRadioStation.streamUrl.equals("")
				&& !thisApp.getLastStationDetailedViewSeen() ) {

			// RadioDroid thisApp = (RadioDroid) getApplication();
			// Log.v("mainactivity","oncreate:detailedviewseen:"+(thisApp.getLastStationDetailedViewSeen()?"1":"0") );

			Toast.makeText(this, "Last played stream: " + lastRadioStation.streamUrl, Toast.LENGTH_LONG).show();
			ClickOnItem( lastRadioStation );
				
		} else {
			createStationList(Constants.TOP_CLICKS_URL);
		}

		if ( autoPlayPreferenceValue.equals( "autoplay_pause" ) ) {
			if ( RadioDroid.globalPlayerService != null ) {
				try {
					RadioDroid.globalPlayerService.Stop();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
				}
			}
		}
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.v("mainactivity","onresume");
		
		Intent playerServiceIntent = new Intent(getBaseContext(), PlayerService.class);
		startService(playerServiceIntent);
		bindService(playerServiceIntent, RadioDroid.globalPlayerServiceConnector, BIND_AUTO_CREATE);

        // Read the default values and set them as the current values.  
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);  
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		/*
		if ( !prefs.getBoolean( "pref_toggle_allow_gprs_umts", false )
			&& !Utils.hasWifiConnection(this) ) {
			
			ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
			toneG.startTone(ToneGenerator.TONE_SUP_RADIO_NOTAVAIL, 2000);
			Toast.makeText(
					this,
					Html.fromHtml( String.format(
							getString(R.string.no_wifi_connection),
							Utils.getAppName(this))
					),
					Toast.LENGTH_LONG ).show();
			finish();
			return;
		}
		
		if ( prefs.getBoolean( "pref_toggle_allow_gprs_umts", false )
			&& !Utils.isNetworkAvailable(this) ) {
			ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
			toneG.startTone(ToneGenerator.TONE_SUP_RADIO_NOTAVAIL, 2000);
			Toast.makeText(
					this,
					Html.fromHtml( String.format(
							getString(R.string.no_network_connection),
							Utils.getAppName(this))
					),
					Toast.LENGTH_LONG ).show();
			finish();
			return;
		}
		*/
				
		setTitle( Utils.getAppAndVersionName( this ) + " (" + getString(R.string.top_clicks) + ")" );

		createStationList(Constants.TOP_CLICKS_URL);

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);
		// registerForContextMenu(lv);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Object anObject = parent.getItemAtPosition(position);
				if (anObject instanceof RadioStation) {
					ClickOnItem( (RadioStation) anObject);
				}
			}
		});
		
	}

	void ClickOnItem( RadioStation theStation) {

		Log.v("mainactivity","clickonitem");

		// RadioDroid thisApp = (RadioDroid) getApplication();
		// Log.v("mainactivity","clickonitem:detailedviewseen:"+(thisApp.getLastStationDetailedViewSeen()?"1":"0") );

		PlayerService thisService = new PlayerService();
		thisService.unbindSafely( this, RadioDroid.globalPlayerServiceConnector );

		Intent anIntent = new Intent( this, RadioStationDetailActivity.class);
		anIntent.putExtra("stationid", theStation.id);
		startActivity(anIntent);
	}

	final int MENU_EXIT = 0;
	final int MENU_STOP = 1;
	final int MENU_TOPVOTES = 2;
	final int MENU_TOPCLICKS = 3;
	final int MENU_ALLSTATIONS = 4;
	final int MENU_SEARCHSTATIONS = 5;
	final int MENU_SETTINGS = 9;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(Menu.NONE, MENU_EXIT, Menu.NONE, String.format(
			getString( R.string.exit_app ),
			Utils.getAppName(getApplicationContext()) )
		);
		menu.add(Menu.NONE, MENU_STOP, Menu.NONE, getString( R.string.stop_playing ) );
		menu.add(Menu.NONE, MENU_TOPVOTES, Menu.NONE, getString( R.string.top_votes ) );
		menu.add(Menu.NONE, MENU_TOPCLICKS, Menu.NONE, getString( R.string.top_clicks ) );
		menu.add(Menu.NONE, MENU_ALLSTATIONS, Menu.NONE, getString( R.string.all_stations ) );
		menu.add(Menu.NONE, MENU_SEARCHSTATIONS, Menu.NONE, getString( R.string.search_stations) );
		menu.add(Menu.NONE, MENU_SETTINGS, Menu.NONE, getString( R.string.settings ) );

		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.v(TAG, "menu click");
		Context context = getApplicationContext();
		
		// check selected menu item

		if (item.getItemId() == MENU_EXIT) {
			Log.v(TAG, "menu : exit");
			try {
				RadioDroid.globalPlayerService.Stop();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "" + e);
			}
			finish();
		}

		if (item.getItemId() == MENU_STOP) {
			Log.v(TAG, "menu : stop");
			try {
				RadioDroid.globalPlayerService.Stop();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "" + e);
			}
			return true;
		}

		if (item.getItemId() == MENU_TOPVOTES) {
			createStationList(Constants.TOP_VOTES_URL);
			setTitle( Utils.getAppAndVersionName( context ) + " (" + getString(R.string.top_votes) + ")" );
			return true;
		}

		if (item.getItemId() == MENU_TOPCLICKS) {
			createStationList(Constants.TOP_CLICKS_URL);
			setTitle( Utils.getAppAndVersionName( context ) + " (" + getString(R.string.top_clicks) + ")" );
			return true;
		}

		if (item.getItemId() == MENU_ALLSTATIONS) {
			createStationList(Constants.ALL_STATIONS_URL);
			setTitle( Utils.getAppAndVersionName( context ) + " (" + getString(R.string.all_stations) + ")" );
			return true;
		}

		if (item.getItemId() == MENU_SEARCHSTATIONS) {
			startActivity(new Intent( context, AutoCompleteActivity.class));
			return true;
		}

		if (item.getItemId() == MENU_SETTINGS) {
			startActivity(new Intent( context, ApplicationPreferencesActivity.class));
			return true;
		}

		return false;
	}

}
