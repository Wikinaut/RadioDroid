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
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RadioStationDetailActivity extends Activity {
	ProgressDialog thisProgressLoading;
	RadioStation thisStation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.station_detail);

		Bundle anExtras = getIntent().getExtras();
		final String aStationID = anExtras.getString("stationid");

		Intent anIntent = new Intent(this, PlayerService.class);
		bindService(anIntent, svcConn, BIND_AUTO_CREATE);
		startService(anIntent);

		thisProgressLoading = ProgressDialog.show(RadioStationDetailActivity.this, "", "Loading...");
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				return Utils.getStationDataFromServer(String.format(Locale.US, "http://www.radio-browser.info/webservice/json/stations/byid/%s", aStationID));
			}

			@Override
			protected void onPostExecute(String result) {
				if (!isFinishing()) {
					if (result != null) {
						RadioStation[] aStationList = Utils.DecodeJson(result);
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

		Button aButtonPlay = (Button) findViewById(R.id.detail_button_play);
		aButtonPlay.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Play();
			}
		});

		Button aButtonStop = (Button) findViewById(R.id.detail_button_stop);
		aButtonStop.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Stop();
			}
		});
	}

	private void Play() {
		if (thisPlayerService != null) {
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
		if (thisPlayerService != null) {
			try {
				Button aButtonPlay = (Button) findViewById(R.id.detail_button_play);
				aButtonPlay.setVisibility(View.VISIBLE);
				Button aButtonStop = (Button) findViewById(R.id.detail_button_stop);
				aButtonStop.setVisibility(View.GONE);
				thisPlayerService.Stop();
			} catch (RemoteException e) {
				Log.e("", "" + e);
			}
		}
	}

	IPlayerService thisPlayerService;
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
