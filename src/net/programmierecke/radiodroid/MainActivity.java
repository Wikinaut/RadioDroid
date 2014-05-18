package net.programmierecke.radiodroid;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.Context;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends ListActivity {
	private String topClickUrl = "http://www.radio-browser.info/webservice/json/stations/topclick/200";
	private String topVoteUrl = "http://www.radio-browser.info/webservice/json/stations/topvote/200";
	private String allStationsUrl = "http://www.radio-browser.info/webservice/json/stations/";

	ProgressDialog thisProgressLoading;
	RadioStationList thisArrayAdapter = null;

	private static final String TAG = "RadioDroid";
	IPlayerService thisPlayerService;
	private ServiceConnection svcConn = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder binder) {
			thisPlayerService = IPlayerService.Stub.asInterface(binder);
		}

		public void onServiceDisconnected(ComponentName className) {
			thisPlayerService = null;
		}
	};

	private void createStationList(final String theURL) {
		thisProgressLoading = ProgressDialog.show(MainActivity.this, "", "Loading...");
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				return Utils.downloadFeed(theURL);
			}

			@Override
			protected void onPostExecute(String result) {
				if (!isFinishing()) {
					thisArrayAdapter.clear();
					for (RadioStation aStation : Utils.DecodeJson(result)) {
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

		Intent anIntent = new Intent(this, PlayerService.class);
		bindService(anIntent, svcConn, BIND_AUTO_CREATE);
		startService(anIntent);

		// gui stuff
		thisArrayAdapter = new RadioStationList(this, R.layout.station_list);
		setListAdapter(thisArrayAdapter);

		Context context = getApplicationContext();
		
		if (!Utils.hasWifiConnection(context)) {
			ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
			toneG.startTone(ToneGenerator.TONE_SUP_RADIO_NOTAVAIL, 2000);
			Toast.makeText(
					context,
					Html.fromHtml( String.format(
							getString(R.string.no_wifi_connection),
							Utils.getAppName(context))
					),
					Toast.LENGTH_LONG ).show();
			finish();
		}
		
		setTitle( Utils.getAppAndVersionName( context ) + " (" + getString(R.string.top_clicks) + ")" );
		createStationList(topClickUrl);

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

	void ClickOnItem(RadioStation theStation) {
		Intent anIntent = new Intent(getBaseContext(), RadioStationDetailActivity.class);
		anIntent.putExtra("stationid", theStation.ID);
		startActivity(anIntent);

		// if (thisPlayerService != null) {
		// try {
		// thisPlayerService.Play(aStation.StreamUrl, aStation.Name, aStation.ID);
		// } catch (RemoteException e) {
		// // TODO Auto-generated catch block
		// Log.e(TAG, "" + e);
		// }
		// } else {
		// Log.v(TAG, "SERVICE NOT ONLINE");
		// }
	}

	final int MENU_EXIT = 0;
	final int MENU_STOP = 1;
	final int MENU_TOPVOTE = 2;
	final int MENU_TOPCLICK = 3;
	final int MENU_ALLSTATIONS = 4;
	final int MENU_SEARCHSTATIONS = 5;
	final int MENU_ABOUTAPPLICATION = 9;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(Menu.NONE, MENU_EXIT, Menu.NONE, String.format(
			getString( R.string.exit_app ),
			Utils.getAppName(getApplicationContext()) )
		);
		menu.add(Menu.NONE, MENU_STOP, Menu.NONE, getString( R.string.stop_playing ) );
		menu.add(Menu.NONE, MENU_TOPVOTE, Menu.NONE, getString( R.string.top_votes ) );
		menu.add(Menu.NONE, MENU_TOPCLICK, Menu.NONE, getString( R.string.top_clicks ) );
		menu.add(Menu.NONE, MENU_ALLSTATIONS, Menu.NONE, getString( R.string.all_stations ) );
		menu.add(Menu.NONE, MENU_SEARCHSTATIONS, Menu.NONE, getString( R.string.search_stations) );
		menu.add(Menu.NONE, MENU_ABOUTAPPLICATION, Menu.NONE, getString( R.string.about_application ) );

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
				thisPlayerService.Stop();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "" + e);
			}
			finish();
		}

		if (item.getItemId() == MENU_STOP) {
			Log.v(TAG, "menu : stop");
			try {
				thisPlayerService.Stop();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "" + e);
			}
			return true;
		}

		if (item.getItemId() == MENU_TOPVOTE) {
			createStationList(topVoteUrl);
			setTitle( Utils.getAppAndVersionName( context ) + " (" + getString(R.string.top_votes) + ")" );
			return true;
		}

		if (item.getItemId() == MENU_TOPCLICK) {
			createStationList(topClickUrl);
			setTitle( Utils.getAppAndVersionName( context ) + " (" + getString(R.string.top_clicks) + ")" );
			return true;
		}

		if (item.getItemId() == MENU_ALLSTATIONS) {
			createStationList(allStationsUrl);
			setTitle( Utils.getAppAndVersionName( context ) + " (" + getString(R.string.all_stations) + ")" );
			return true;
		}

		if (item.getItemId() == MENU_SEARCHSTATIONS) {
			startActivity(new Intent( context, AutoCompleteActivity.class));
			return true;
		}

		if (item.getItemId() == MENU_ABOUTAPPLICATION) {
			startActivity(new Intent( context, AboutApplicationActivity.class));
			return true;
		}

		return false;
	}

}
