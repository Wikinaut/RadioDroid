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
							playingStation.ID)
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

	MediaPlayer thisMediaPlayer = null;
	Context thisContext;

	private final IPlayerService.Stub thisBinder = new IPlayerService.Stub() {

		public void Play( String theJsonRadioStation) throws RemoteException {
			RadioDroid thisApp = (RadioDroid) getApplication();
	
			Gson gson = new Gson();
			playingStation = gson.fromJson( theJsonRadioStation, RadioStation.class );
			
			if ( !thisApp.isPlayingSameLastStationUrl( playingStation.StreamUrl ) ) {
				PlayerService.this.Stop();
				PlayerService.this.PlayUrl( playingStation );

 				if	( !thisApp.isSameLastStationUrl( playingStation.StreamUrl ) ) {

					PreferenceManager.setDefaultValues(thisContext, R.xml.preferences, false);
					SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(thisContext);

					if (settings.getBoolean( "pref_toggle_notify_server_about_play_click", false ) ) {
						BackgroundTask serverCountTask = new BackgroundTask();
				   		serverCountTask.execute( "http://www.radio-browser.info/?action=clicked&id=" + playingStation.ID );
					}
				}
					
			}
	
			thisApp.putJsonRadioStationPersistentStorage( theJsonRadioStation );
			thisApp.setLastStationStatus( "play" );

		}

		public void Stop() throws RemoteException {
			RadioDroid thisApp = (RadioDroid) getApplication();
			PlayerService.this.Stop();
			thisApp.setLastStationStatus( "stop" );
		}

	};

	@Override
	public IBinder onBind(Intent arg0) {
		Log.v("playerservice", "onBind");
		return thisBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.v("playerservice", "onCreate");
		thisContext = this;
	}

	public void SendMessage(String theTitle, String theMessage, String theTicker) {
		
		Intent notificationIntent = new Intent(thisContext, RadioStationDetailActivity.class);
		notificationIntent.putExtra("stationid", playingStation.ID);
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent contentIntent = PendingIntent.getActivity(thisContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification thisNotification = new NotificationCompat
				.Builder(thisContext).setContentIntent(contentIntent).setContentTitle(theTitle)
				.setContentText(theMessage)
				.setWhen(System.currentTimeMillis())
				.setTicker(theTicker)
				.setOngoing(true)
				.setUsesChronometer(true)
				.setSmallIcon(R.drawable.play)
				.setLargeIcon( (
							(BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)
						).getBitmap()
				).build();

		startForeground(NOTIFY_ID, thisNotification);
	}

	@Override
	public void onDestroy() {
		Log.v("playerservice", "onDestroy");
		Stop();
		stopForeground(true);
	}

	public void PlayUrl( RadioStation thisStation ) {
		final String thisStationName = thisStation.Name;
		final String thisStationStreamUrl = thisStation.StreamUrl;
		
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... stations) {

				if (thisMediaPlayer == null) {
					thisMediaPlayer = new MediaPlayer();
					thisMediaPlayer.setOnBufferingUpdateListener(PlayerService.this);
				}

				String decodedUrl = decodeUrl(thisStationStreamUrl);

				RadioDroid thisApp = (RadioDroid) getApplication();
				
				if ( thisMediaPlayer.isPlaying()
					&& !thisApp.isPlayingSameLastStationUrl(thisStationStreamUrl) ) {
					thisMediaPlayer.stop();
					thisMediaPlayer.reset();
				}

				try {
					SendMessage(thisStationName, "Preparing stream", "Preparing stream");
					thisMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
					thisMediaPlayer.setDataSource(decodedUrl);
					thisMediaPlayer.prepare();
					Log.d(TAG, "Start playing "+thisStationStreamUrl);
					SendMessage(thisStationName, "Playing", "Playing '" + thisStationName + "'");
					thisMediaPlayer.start();
				} catch (IllegalArgumentException e) {
					Log.e(TAG, "" + e);
					SendMessage(thisStationName, "Stream url problem", "Stream url problem");
					Stop();
				} catch (IOException e) {
					Log.e(TAG, "" + e);
					SendMessage(thisStationName, "Stream caching problem", "Stream caching problem");
					Stop();
				} catch (Exception e) {
					Log.e(TAG, "" + e);
					SendMessage(thisStationName, "Unable to play stream", "Unable to play stream");
					Stop();
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

	public void Stop() {
		RadioDroid thisApp = (RadioDroid) getApplication();
		thisApp.setLastStationStatus( "stop" );
		if (thisMediaPlayer != null) {
			if (thisMediaPlayer.isPlaying()) {
				thisMediaPlayer.stop();
			}
			thisMediaPlayer.release();
			thisMediaPlayer = null;
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
		SendMessage(playingStation.Name, "Buffering ..", "Buffering .. (" + percent + "%)");
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
