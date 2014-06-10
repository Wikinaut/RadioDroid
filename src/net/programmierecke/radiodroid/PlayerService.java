package net.programmierecke.radiodroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

import com.google.gson.Gson;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;


public class PlayerService extends Service implements OnBufferingUpdateListener {

	private class BackgroundTask extends AsyncTask <String, Void, Void> {
		  
	    @Override
	    protected void onPreExecute() {
			Toast.makeText(
					thisContext,
					Html.fromHtml( String.format(
							getString(R.string.notify_server_about_play_click),
							playingStation.id)
					),
					Toast.LENGTH_LONG ).show();
	    }
	     
	    @Override
	    protected void onPostExecute(Void result) {
	    }
	     
	    @Override
	    protected Void doInBackground(String... params) {
	   		Utils.getFromUrl( params[0] );
	        return null;
	    }
	     
	}
	
	
	protected static final int NOTIFY_ID = 1;
	final String TAG = "PlayerService";
	private RadioStation playingStation;

	MediaPlayer thisPlayer = null;
	Context thisContext;

	private final IPlayerService.Stub playerServiceBinder = new IPlayerService.Stub() {

		public void Play( String theJsonRadioStation) throws RemoteException {
			RadioDroid thisApp = (RadioDroid) getApplication();
	
			Gson gson = new Gson();
			playingStation = gson.fromJson( theJsonRadioStation, RadioStation.class );
			
			if ( !thisApp.isPlayingSameLastStationUrl( playingStation.streamUrl ) ) {
				PlayerService.this.stop();
				PlayerService.this.playUrl( playingStation );

 				if	( !thisApp.isSameLastStationUrl( playingStation.streamUrl ) ) {

					PreferenceManager.setDefaultValues(thisContext, R.xml.preferences, false);
					SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(thisContext);

					if (settings.getBoolean( "pref_toggle_notify_server_about_play_click", false ) ) {
						BackgroundTask serverCountTask = new BackgroundTask();
				   		serverCountTask.execute( "http://www.radio-browser.info/?action=clicked&id=" + playingStation.id );
					}
				}
					
			}
	
			// thisApp.setLastStationStatus( "play" );
			// thisApp.putJsonRadioStationPersistentStorage( theJsonRadioStation );
	
		}

		public void Stop() throws RemoteException {
			RadioDroid thisApp = (RadioDroid) getApplication();
			PlayerService.this.stop();
		}

	};

	@Override
	public IBinder onBind(Intent arg0) {
		Log.v("playerservice", "onBind");
		return playerServiceBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.v("playerservice", "onCreate");
		thisContext = this;
	}

	public void showNotificationMessage(String theTitle, String theMessage, String theTicker) {
		
		Intent notificationIntent = new Intent(thisContext, RadioStationDetailActivity.class);
		notificationIntent.putExtra("stationid", playingStation.id);
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent contentIntent = PendingIntent.getActivity(thisContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification thisNotification = new NotificationCompat
				.Builder(thisContext)
					.setContentIntent(contentIntent).setContentTitle(theTitle)
				.setContentText(theMessage)
				.setWhen(System.currentTimeMillis())
				.setTicker(theTicker)
				.setOngoing(true)
				.setUsesChronometer(true)
				.setSmallIcon(R.drawable.play)
				.setLargeIcon(
						((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap()
					).build();

		startForeground(NOTIFY_ID, thisNotification);
	}

	@Override
	public void onDestroy() {
		Log.v("playerservice", "onDestroy");
		stop();
		stopForeground(true);
	}

	public void playUrl( RadioStation station ) {
		final RadioStation thisStation = station;
		final String thisStationName = thisStation.name;
		final String thisStationStreamUrl = thisStation.streamUrl;

		Log.v("playerservice","playurl");
		RadioDroid thisApp = (RadioDroid) getApplication();
		thisApp.setLastStationStatus( "play" );

		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... stations) {

				if (thisPlayer == null) {
					thisPlayer = new MediaPlayer();
					thisPlayer.setOnBufferingUpdateListener(PlayerService.this);
				}

				String decodedUrl = decodeUrl(thisStationStreamUrl);

				RadioDroid thisApp = (RadioDroid) getApplication();
				
				if ( thisPlayer.isPlaying()
					&& !thisApp.isPlayingSameLastStationUrl(thisStationStreamUrl) ) {
					thisPlayer.stop();
					thisPlayer.reset();
				}

				try {
					showNotificationMessage(thisStationName, "Preparing stream", "Preparing stream");
					thisPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
					thisPlayer.setDataSource(decodedUrl);
					thisPlayer.prepare();
					Log.d(TAG, "Start playing "+thisStationStreamUrl);
					showNotificationMessage(thisStationName, "Playing", "Playing '" + thisStationName + "'");
					thisApp.putRadioStationPersistentStorage( thisStation );
					thisApp.setLastStationStatus( "play" );
					thisPlayer.start();
				} catch (IllegalArgumentException e) {
					Log.e(TAG, "" + e);
					showNotificationMessage(thisStationName, "Stream url problem", "Stream url problem");
					stop();
				} catch (IOException e) {
					Log.e(TAG, "" + e);
					showNotificationMessage(thisStationName, "Stream caching problem", "Stream caching problem");
					stop();
				} catch (Exception e) {
					Log.e(TAG, "" + e);
					showNotificationMessage(thisStationName, "Unable to play stream", "Unable to play stream");
					stop();
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				Log.d(TAG, "Play task finished");
				super.onPostExecute(result);
			}

		}.execute();
	}

	public void stop() {
		Log.v("playerservice","stop");
		RadioDroid thisApp = (RadioDroid) getApplication();
		thisApp.setLastStationStatus( "stop" );
		if (thisPlayer != null) {
			if (thisPlayer.isPlaying()) {
				thisPlayer.stop();
			}
			thisPlayer.release();
			thisPlayer = null;
		}
		stopForeground(true);
	}


	String decodeUrl(String theUrl) {
		try {
			URL anUrl = new URL(theUrl);
			String aFileName = anUrl.getFile();

			int aQueryIndex = aFileName.indexOf('?');
			if (aQueryIndex >= 0) {
				aFileName = aFileName.substring(0, aQueryIndex);
			}

			if (aFileName.endsWith(".pls")) {
				Log.v(TAG, "Found PLS file");
				String theFile = Utils.getFromUrl(theUrl);
				BufferedReader aReader = new BufferedReader(new StringReader(theFile));
				String str;
				while ((str = aReader.readLine()) != null) {
					if (str.substring(0, 4).equals("File")) {
						int anIndex = str.indexOf('=');
						if (anIndex >= 0) {
							return str.substring(anIndex + 1);
						}
					}
				}
			} else if (aFileName.endsWith(".m3u")) {
				Log.v(TAG, "Found M3U file");
				String theFile = Utils.getFromUrl(theUrl);
				BufferedReader aReader = new BufferedReader(new StringReader(theFile));
				String str;
				while ((str = aReader.readLine()) != null) {
					if (!str.substring(0, 1).equals("#")) {
						return str.trim();
					}
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "" + e);
		}
		return theUrl;
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		// http://stackoverflow.com/questions/21925454/android-mediaplayer-onbufferingupdatelistener-percentage-of-buffered-content-i
		// https://code.google.com/p/android/issues/detail?id=65564
		// Log.v(TAG, "Buffering:" + percent);

		if (percent < 0 || percent > 100) {
            percent = (int) Math.round((((Math.abs(percent)-1)*100.0/Integer.MAX_VALUE)));
        }
		showNotificationMessage(playingStation.name, "Buffering ..", "Buffering .. (" + percent + "%)");
	}
	
	public void unbindSafely(Context appContext, ServiceConnection connection) {
	    try {
	        appContext.unbindService(connection);
	    } catch (Exception e) {
	        // We were unable to unbind, e.g. because no such service binding
	        // exists. This should be rare, but is possible, e.g. if the
	        // service was killed by Android in the meantime.
	        // We ignore this.
	    }
	}
	 
}
