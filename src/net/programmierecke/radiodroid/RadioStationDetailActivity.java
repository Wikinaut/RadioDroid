package net.programmierecke.radiodroid;

import java.util.Locale;

import com.google.gson.Gson;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


public class RadioStationDetailActivity extends Activity {

	IPlayerService thisPlayerService;
	
	ProgressDialog thisProgressLoading;
	RadioStation thisStation;
	RadioStation lastStation;

	@Override
	protected void onPause() {
		super.onPause();
		Log.v("stationdetail","onPause" );
		if ( svcConn != null ) {
			unbindService( svcConn );
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v("stationdetail","onCreate" );
		
		setContentView(R.layout.station_detail);

		Bundle anExtras = getIntent().getExtras();
		final String aStationID = anExtras.getString("stationid");

		Intent playerServiceIntent = new Intent( getBaseContext(), PlayerService.class);
		startService(playerServiceIntent);
		bindService(playerServiceIntent, svcConn, BIND_AUTO_CREATE);
		
		RadioDroid thisApp = (RadioDroid) getApplication(); 
		lastStation = thisApp.getRadioStationPersistentStorage();
		
		if ( aStationID.equals(lastStation.ID) ) {

			createStationDetailView(lastStation);

			new AsyncTask<Void, Void, Void>() {

				@Override
				protected Void doInBackground(Void... params) {
					return null;
				}

				@Override
				protected void onPostExecute(Void result) {
					if (!isFinishing()) {
						Play();
					}
				}

			}.execute();

		} else {

			thisProgressLoading = ProgressDialog.show(
				RadioStationDetailActivity.this,
				"",
				getString(R.string.loading_station_details_from_server)
			);
			new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				return Utils.getFromUrl(String.format(Locale.US, "http://www.radio-browser.info/webservice/json/stations/byid/%s", aStationID));
			}

			@Override
			protected void onPostExecute(String result) {
				if (!isFinishing()) {
					if (result != null) {
						RadioStation[] aStationList = Utils.decodeJson(result);
						if (aStationList.length == 1) {
							thisStation = aStationList[0];
							createStationDetailView(thisStation);
							Play();
						}
					}
				}
				thisProgressLoading.dismiss();
				super.onPostExecute(result);
			}

		}.execute();
		
		}
	}

	private void createStationDetailView(RadioStation radioStation) {
		thisStation = radioStation;
		
		setTitle(radioStation.Name);
		
		TextView aTextViewId = (TextView) findViewById(R.id.stationdetail_id_value);
		String positiveVotes = (radioStation.Votes > 0) ? "+" + radioStation.Votes : "0";
		String negativeVotes = (radioStation.NegativeVotes > 0) ? "-" + radioStation.NegativeVotes : "0";
		aTextViewId.setText( radioStation.ID + " (" + positiveVotes + "/" + negativeVotes + ")" );

		TextView aTextViewCountry = (TextView) findViewById(R.id.stationdetail_country_value);
		aTextViewCountry.setText(radioStation.Country);

		TextView aTextViewLanguage = (TextView) findViewById(R.id.stationdetail_language_value);
		aTextViewLanguage.setText(radioStation.Language);

		TextView aTextViewTags = (TextView) findViewById(R.id.stationdetail_tags_value);
		aTextViewTags.setText(radioStation.TagsAll);

		TextView aTextViewHompageUrl = (TextView) findViewById(R.id.stationdetail_homepage_url_value);
		aTextViewHompageUrl.setText(radioStation.HomePageUrl);

		TextView aTextViewStream = (TextView) findViewById(R.id.detail_stream_url_value);
		aTextViewStream.setText(radioStation.StreamUrl);

		final String aLink = thisStation.HomePageUrl;
		LinearLayout aLinLayoutHompageUrl = (LinearLayout) findViewById(R.id.stationdetail_homepage_url_clickable);
		aLinLayoutHompageUrl.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (aLink.toLowerCase(Locale.US).startsWith("http")) {
					Intent aHompageUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(aLink));
					startActivity(aHompageUrlIntent);
				}
			}
		});

		Button buttonPlay = (Button) findViewById(R.id.detail_button_play);
		buttonPlay.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Play();
			}
		});

		Button buttonStop = (Button) findViewById(R.id.detail_button_stop);
		buttonStop.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Stop();
			}
		});
		
	}

	private void Play() {
		Log.v("stationdetail", "play");
		if ( thisPlayerService == null ) {
			Log.v("stationdetail","PLAY playerservice is null." );
		}

		if ( thisPlayerService != null ) {
			Log.v("stationdetail","PLAY playerservice != null." );
			try {
				Button aButtonPlay = (Button) findViewById(R.id.detail_button_play);
				aButtonPlay.setVisibility(View.GONE);
				Button aButtonStop = (Button) findViewById(R.id.detail_button_stop);
				aButtonStop.setVisibility(View.VISIBLE);
			    Gson gson = new Gson();
				thisPlayerService.Play( gson.toJson(thisStation) );
			} catch (RemoteException e) {
				Log.e("", "" + e);
			}
		}
	}

	private void Stop() {
		Log.v("stationdetail","STOP playerservice." );

		if (thisPlayerService != null) {
			Log.v("stationdetail","STOP playerservice != null." );

			try {
				Button buttonPlay = (Button) findViewById(R.id.detail_button_play);
				buttonPlay.setVisibility(View.VISIBLE);
				Button buttonStop = (Button) findViewById(R.id.detail_button_stop);
				buttonStop.setVisibility(View.GONE);
				thisPlayerService.Stop();
			} catch (RemoteException e) {
				Log.e("", "" + e);
			}
		}
	}
	
	private ServiceConnection svcConn = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder binder) {
			Log.v("", "Service came online");
			thisPlayerService = IPlayerService.Stub.asInterface(binder);
		}

		public void onServiceDisconnected(ComponentName className) {
			Log.v("", "Service offline");
			thisPlayerService = null;
		}
	};
	

}
