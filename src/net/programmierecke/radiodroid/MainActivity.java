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

	IPlayerService thisPlayerService;

	ProgressDialog thisProgressLoading;
	RadioStationList thisArrayAdapter = null;

	private static final String TAG = "RadioDroid";

	private ServiceConnection svcConn = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder binder) {
			thisPlayerService = IPlayerService.Stub.asInterface(binder);
		}

		public void onServiceDisconnected(ComponentName className) {
			thisPlayerService = null;
		}
	};

	private void createStationList(final String theURL) {
		thisProgressLoading = ProgressDialog.show(
			MainActivity.this,
			"",
			getString(R.string.loading_station_list_from_server)
		);
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				return Utils.getFromUrl(theURL);
			}

			@Override
			protected void onPostExecute(String result) {
				if (!isFinishing()) {
					thisArrayAdapter.clear();
					for (RadioStation aStation : Utils.decodeJson(result)) {
						thisArrayAdapter.add(aStation);
					}
					getListView().invalidate();
					thisProgressLoading.dismiss();
				}
				super.onPostExecute(result);
			}
		}.execute();
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v("mainactivity","onCreate" );

		Intent playerServiceIntent = new Intent(getBaseContext(), PlayerService.class);
		startService(playerServiceIntent);
		bindService(playerServiceIntent, svcConn, BIND_AUTO_CREATE);
		
		// gui stuff
		thisArrayAdapter = new RadioStationList(this, R.layout.station_list);
		setListAdapter(thisArrayAdapter);

        // Read the default values and set them as the current values.  
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);  
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
        // Android bug workaround
        // https://code.google.com/p/android/issues/detail?id=6641
        // for all "false" defaultValues in preferences.xml 
		prefs.edit().putBoolean("pref_toggle_allow_gprs_umts", prefs.getBoolean("pref_toggle_allow_gprs_umts", false)).commit();
		prefs.edit().putBoolean("pref_toggle_notify_server_about_play_click", prefs.getBoolean("pref_toggle_notify_server_about_play_click", false)).commit();

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
		
		RadioDroid thisApp = (RadioDroid) getApplication();
		String lastStation = thisApp.getLastStationStreamUrl();
		
		setTitle( Utils.getAppAndVersionName( this ) + " (" + getString(R.string.top_clicks) + ")" );

		if ( prefs.getBoolean( "pref_toggle_play_last_station_on_restart", true )
				&& !lastStation.equals("") ) {
				Toast.makeText(this, "Last played stream was: " + lastStation, Toast.LENGTH_LONG).show();
				ClickOnItem((RadioStation) thisApp.getRadioStationPersistentStorage() );
		} else {

		createStationList(Constants.TOP_CLICKS_URL);

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);
		// registerForContextMenu(lv);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Object anObject = parent.getItemAtPosition(position);
				if (anObject instanceof RadioStation) {
					ClickOnItem((RadioStation) anObject);
				}
			}
		});

		}
	}

	void ClickOnItem(RadioStation theStation) {

		Intent anIntent = new Intent( getBaseContext(), RadioStationDetailActivity.class);
		anIntent.putExtra("stationid", theStation.ID);
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
			/*
			try {
				thisPlayerService.Stop();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "" + e);
			}
			*/
			finish();
		}

		if (item.getItemId() == MENU_STOP) {
			Log.v(TAG, "menu : stop");
			/*
			try {
				thisPlayerService.Stop();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "" + e);
			}
			*/
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
