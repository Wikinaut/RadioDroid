package net.programmierecke.radiodroid;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

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

	final int MENU_STOP = 0;
	final int MENU_TOPVOTE = 1;
	final int MENU_TOPCLICK = 2;
	final int MENU_ALLSTATIONS = 3;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_STOP, Menu.NONE, "Stop");
		menu.add(Menu.NONE, MENU_TOPVOTE, Menu.NONE, "TopVote");
		menu.add(Menu.NONE, MENU_TOPCLICK, Menu.NONE, "TopClick");
		menu.add(Menu.NONE, MENU_ALLSTATIONS, Menu.NONE, "AllStations");

		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.v(TAG, "menu click");

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
		// check selected menu item
		if (item.getItemId() == MENU_TOPVOTE) {
			createStationList(topVoteUrl);
			setTitle("TopVote");
			return true;
		}
		if (item.getItemId() == MENU_TOPCLICK) {
			createStationList(topClickUrl);
			setTitle("TopClick");
			return true;
		}
		if (item.getItemId() == MENU_ALLSTATIONS) {
			createStationList(allStationsUrl);
			setTitle("AllStations");
			return true;
		}
		return false;
	}
}
