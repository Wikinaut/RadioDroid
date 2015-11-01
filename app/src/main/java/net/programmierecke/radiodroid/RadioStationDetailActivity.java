package net.programmierecke.radiodroid;

import java.util.Locale;

import com.google.gson.Gson;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RadioStationDetailActivity extends Activity {

	ProgressDialog thisProgressLoading;
	RadioStation thisStation;
	RadioStation lastRadioStation;

	@Override
	protected void onPause() {
		super.onPause();
		Log.v("stationdetail","onpause");
		PlayerService thisService = new PlayerService();
		thisService.unbindSafely( this, RadioDroid.globalPlayerServiceConnector );

		RadioDroid thisApp = (RadioDroid) getApplication(); 
		thisApp.setLastStationDetailedViewSeen(true);

	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v("stationdetail","onCreate" );
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.v("stationdetail","onResume" );
		
		RadioDroid thisApp = (RadioDroid) getApplication(); 

		lastRadioStation = thisApp.getRadioStationPersistentStorage();

		setContentView(R.layout.station_detail);

		Bundle anExtras = getIntent().getExtras();
		final String aStationID = anExtras.getString("stationid");

		Intent playerServiceIntent = new Intent( getBaseContext(), PlayerService.class);
		startService(playerServiceIntent);
		bindService(playerServiceIntent, RadioDroid.globalPlayerServiceConnector, BIND_AUTO_CREATE);
	
		if ( aStationID.equals(lastRadioStation.id) ) {

			createStationDetailView(lastRadioStation);

			new AsyncTask<Void, Void, Void>() {

				@Override
				protected Void doInBackground(Void... params) {
					return null;
				}

				@Override
				protected void onPostExecute(Void result) {
					if (!isFinishing()) {

						RadioDroid thisApp = (RadioDroid) getApplication(); 
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(thisApp);
						String autoPlayPreferenceValue = prefs.getString("pref_autoplay_settings", "(undefined)" );

						Log.v("stationdetail","pref:autplay:"+autoPlayPreferenceValue);
						Log.v("stationdetail","laststatus:"+thisApp.getLastStationStatus());

						if ( autoPlayPreferenceValue.equals( "autoplay_play" ) ) {
							Play();
						} else if ( autoPlayPreferenceValue.equals( "autoplay_pause" ) ) {
							Stop();
						} else if ( autoPlayPreferenceValue.equals( "autoplay_last_status" ) ) {
							if ( thisApp.getLastStationStatus().equals( "play" ) ) {
								Play();
							} else {
								Stop();
							}
						}
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

		// RadioDroid thisApp = (RadioDroid) getApplication();
		// thisApp.setLastStationDetailedViewSeen();

		setTitle(radioStation.name);
		
		TextView aTextViewId = (TextView) findViewById(R.id.stationdetail_id_value);
		String positiveVotes = (radioStation.votes > 0) ? "+" + radioStation.votes : "0";
		String negativeVotes = (radioStation.negativeVotes > 0) ? "-" + radioStation.negativeVotes : "0";
		aTextViewId.setText( radioStation.id + " (" + positiveVotes + "/" + negativeVotes + ")" );

		TextView aTextViewCountry = (TextView) findViewById(R.id.stationdetail_country_value);
		aTextViewCountry.setText(radioStation.country);

		TextView aTextViewLanguage = (TextView) findViewById(R.id.stationdetail_language_value);
		aTextViewLanguage.setText(radioStation.language);

		TextView aTextViewTags = (TextView) findViewById(R.id.stationdetail_tags_value);
		aTextViewTags.setText(radioStation.tagsAll);

		TextView aTextViewHompageUrl = (TextView) findViewById(R.id.stationdetail_homepage_url_value);
		aTextViewHompageUrl.setText(radioStation.homePageUrl);

		TextView aTextViewStream = (TextView) findViewById(R.id.detail_stream_url_value);
		aTextViewStream.setText(radioStation.streamUrl);

		final String aLink = thisStation.homePageUrl;
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
		if ( RadioDroid.globalPlayerService == null ) {
			Log.v("stationdetail","PLAY playerservice is null." );
		}

		if ( RadioDroid.globalPlayerService != null ) {
			Log.v("stationdetail","PLAY playerservice != null." );
			try {
				Button aButtonPlay = (Button) findViewById(R.id.detail_button_play);
				aButtonPlay.setVisibility(View.GONE);
				Button aButtonStop = (Button) findViewById(R.id.detail_button_stop);
				aButtonStop.setVisibility(View.VISIBLE);
			    Gson gson = new Gson();
			    RadioDroid.globalPlayerService.Play( gson.toJson(thisStation) );
			} catch (RemoteException e) {
				Log.e("", "" + e);
			}
		}
	}

	private void Stop() {
		Log.v("stationdetail","STOP playerservice." );

		if (RadioDroid.globalPlayerService != null) {
			Log.v("stationdetail","STOP playerservice != null." );

			try {
				Button buttonPlay = (Button) findViewById(R.id.detail_button_play);
				buttonPlay.setVisibility(View.VISIBLE);
				Button buttonStop = (Button) findViewById(R.id.detail_button_stop);
				buttonStop.setVisibility(View.GONE);
				RadioDroid.globalPlayerService.Stop();
			} catch (RemoteException e) {
				Log.e("", "" + e);
			}
		}
	}
	
}
