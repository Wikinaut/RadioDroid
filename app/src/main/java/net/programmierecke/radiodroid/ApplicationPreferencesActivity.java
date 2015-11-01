package net.programmierecke.radiodroid;

import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import net.programmierecke.radiodroid.AboutApplicationActivity;
import net.programmierecke.radiodroid.RadioStationList;

/**
 * The Activity for application preference display and management.
 *
 */

public final class ApplicationPreferencesActivity extends PreferenceActivity {

	ProgressDialog thisProgressLoading;
	RadioStationList thisArrayAdapter = null;

	private static final String STOP_PLAYING = "pref_key_stop_playing";
	private static final String TOP_VOTES = "pref_key_top_votes";
	private static final String TOP_CLICKS = "pref_key_top_clicks";
	private static final String ALL_STATIONS = "pref_key_all_stations";
	private static final String SEARCH_STATIONS = "pref_key_search_stations";
	private static final String ABOUT_APPLICATION = "pref_key_about_application";
	private static final String AUTOPLAY_SETTINGS = "pref_autoplay_settings";
	private static final String NETWORK_SETTINGS = "pref_network_settings";
	private static final String COMMUNITY_RADIO = "pref_key_community_radio";
	


	@Override
	protected void onPause() {
		super.onPause();
		PlayerService thisService = new PlayerService();
		thisService.unbindSafely( this, RadioDroid.globalPlayerServiceConnector );
	}
	
    @Override
    protected void onCreate( final Bundle savedInstanceState ) {
	    super.onCreate( savedInstanceState );

	setTitle( Utils.getAppAndVersionName( this ) );

    addPreferencesFromResource(R.xml.preferences);

    this.findPreference(STOP_PLAYING).setOnPreferenceClickListener(new AboutApplicationListener());
    this.findPreference(TOP_VOTES).setOnPreferenceClickListener(new StationListListener());
    this.findPreference(TOP_CLICKS).setOnPreferenceClickListener(new StationListListener());
    this.findPreference(ALL_STATIONS).setOnPreferenceClickListener(new StationListListener());
    this.findPreference(SEARCH_STATIONS).setOnPreferenceClickListener(new AboutApplicationListener());
    this.findPreference(ABOUT_APPLICATION).setOnPreferenceClickListener(new AboutApplicationListener());
    this.findPreference(AUTOPLAY_SETTINGS).setOnPreferenceChangeListener(new ListSummaryListener());
    this.findPreference(NETWORK_SETTINGS).setOnPreferenceChangeListener(new ListSummaryListener());
    this.findPreference(COMMUNITY_RADIO).setOnPreferenceClickListener(new UrlClickListener());

    initializeListSummary((ListPreference) findPreference(AUTOPLAY_SETTINGS));
    initializeListSummary((ListPreference) findPreference(NETWORK_SETTINGS));

  }
  
	final int MENU_STOP = 1;
	final int MENU_TOPVOTES = 2;
	final int MENU_TOPCLICKS = 3;
	final int MENU_ALLSTATIONS = 4;
	final int MENU_SEARCHSTATIONS = 5;
	final int MENU_ABOUT_APPLICATION = 9;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(Menu.NONE, MENU_STOP, Menu.NONE, getString( R.string.stop_playing ) );
		menu.add(Menu.NONE, MENU_TOPVOTES, Menu.NONE, getString( R.string.top_votes ) );
		menu.add(Menu.NONE, MENU_TOPCLICKS, Menu.NONE, getString( R.string.top_clicks ) );
		menu.add(Menu.NONE, MENU_ALLSTATIONS, Menu.NONE, getString( R.string.all_stations ) );
		menu.add(Menu.NONE, MENU_SEARCHSTATIONS, Menu.NONE, getString( R.string.search_stations) );
		menu.add(Menu.NONE, MENU_ABOUT_APPLICATION, Menu.NONE, getString( R.string.settings ) );

		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Context context = getApplicationContext();

		// check selected menu item

		if (item.getItemId() == MENU_STOP) {
			try {
				Intent anIntent = new Intent(this, PlayerService.class);
				bindService(anIntent, RadioDroid.globalPlayerServiceConnector, BIND_AUTO_CREATE);
				startService(anIntent);

				RadioDroid.globalPlayerService.Stop();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
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

		if (item.getItemId() == MENU_ABOUT_APPLICATION) {
			startActivity(new Intent( context, ApplicationPreferencesActivity.class));
			return true;
		}

		return false;
	}

  
	  private void createStationList(final String theURL) {
			Context context = ApplicationPreferencesActivity.this;
			thisProgressLoading = ProgressDialog.show(context, "", "Loading...");

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

  private class StationListListener implements Preference.OnPreferenceClickListener {
	  @Override
	  public boolean onPreferenceClick(Preference preference) {
		  
		Context context = ApplicationPreferencesActivity.this;
	    setTitle( Utils.getAppAndVersionName( context ) + " (" + getString(R.string.top_clicks) + ")" );
	      
	    setListAdapter(RadioDroid.globalRadioStationList);
	    
	    String prefKey = preference.getKey();
	    
	  	if ( prefKey.equals(TOP_CLICKS) ) {
	  		setTitle( Utils.getAppAndVersionName( context ) + " (" + getString(R.string.top_clicks) + ")" );
	    	createStationList(Constants.TOP_CLICKS_URL);
	  	} else if ( prefKey.equals(TOP_VOTES) ) {
			setTitle( Utils.getAppAndVersionName( context ) + " (" + getString(R.string.top_votes) + ")" );
			createStationList(Constants.TOP_VOTES_URL);
		} else if ( prefKey.equals(ALL_STATIONS) ) {
			setTitle( Utils.getAppAndVersionName( context ) + " (" + getString(R.string.all_stations) + ")" );
  			createStationList(Constants.ALL_STATIONS_URL);
		} else {
			return false;
		}
		
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
		
		return true;
		
	}

	void ClickOnItem(RadioStation theStation) {
		Intent anIntent = new Intent(getBaseContext(), RadioStationDetailActivity.class);
		anIntent.putExtra("stationid", theStation.id);
		startActivity(anIntent);
	}

  }

  private class UrlClickListener implements Preference.OnPreferenceClickListener {
	    @Override
	    public boolean onPreferenceClick(Preference preference) {

	    	startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.COMMUNITYRADIO_URL)));
	    	return true;

	    }
  }
  
  private class AboutApplicationListener implements Preference.OnPreferenceClickListener {
	    @Override
	    public boolean onPreferenceClick(Preference preference) {

	    	Context context = ApplicationPreferencesActivity.this;
	    	startActivity(new Intent(context, AboutApplicationActivity.class));

	    	return true;

	    }
  }

  // https://code.google.com/p/android/issues/detail?id=4611#c35
  @SuppressWarnings("deprecation")
  @Override
  public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
  	super.onPreferenceTreeClick(preferenceScreen, preference);
  	if (preference!=null)
	    	if (preference instanceof PreferenceScreen)
	        	if (((PreferenceScreen)preference).getDialog()!=null)
	        		((PreferenceScreen)preference).getDialog().getWindow().getDecorView().setBackgroundDrawable(this.getWindow().getDecorView().getBackground().getConstantState().newDrawable());
  	return false;
  }

  
  private void initializeListSummary(ListPreference pref) {
	    /*
	  	pref.setSummary( String.format(
	    	getString( R.string.preferences__network_settings_summary ),
	    	pref.getEntry()
	    	)
	    );
	    */
	  	pref.setSummary( pref.getEntry() );
	  
	  }

  private class ListSummaryListener implements Preference.OnPreferenceChangeListener {
	    @Override
	    public boolean onPreferenceChange(Preference preference, Object value) {
	    	ListPreference asList = (ListPreference) preference;

	    	int index = 0;
	    	for (; index < asList.getEntryValues().length; index++) {
	    		if (value.equals(asList.getEntryValues()[index])) {
	    			break;
	    		}
	    	}

	    	/*
	    	asList.setSummary( String.format(
		    		getString( R.string.preferences__network_settings_summary ),
		    		asList.getEntries()[index]
		    		)
	    	);
	    	*/
	    	asList.setSummary( asList.getEntries()[index] );
	    	
	    	return true;
	    }
	  }

   
}
