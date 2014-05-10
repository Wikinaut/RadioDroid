package net.programmierecke.radiodroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class PlayerService extends Service implements OnBufferingUpdateListener {
	protected static final int NOTIFY_ID = 1;
	final String TAG = "PlayerService";
	MediaPlayer thisMediaPlayer = null;

	private final IPlayerService.Stub thisBinder = new IPlayerService.Stub() {

		public void Play(String theUrl, String theName, String theID) throws RemoteException {
			PlayerService.this.PlayUrl(theUrl, theName, theID);
		}

		public void Stop() throws RemoteException {
			PlayerService.this.Stop();
		}

		@Override
		public String getCurrentStationID() throws RemoteException {
			if (thisMediaPlayer == null)
				return null;
			if (!thisMediaPlayer.isPlaying())
				return null;
			return thisStationID;
		}
	};

	@Override
	public IBinder onBind(Intent arg0) {
		return thisBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		thisContext = this;
	}

	public void SendMessage(String theTitle, String theMessage, String theTicker) {
		Intent notificationIntent = new Intent(thisContext, RadioStationDetailActivity.class);
		notificationIntent.putExtra("stationid", thisStationID);
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent contentIntent = PendingIntent.getActivity(thisContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		Notification thisNotification = new NotificationCompat.Builder(thisContext).setContentIntent(contentIntent).setContentTitle(theTitle)
				.setContentText(theMessage).setWhen(System.currentTimeMillis()).setTicker(theTicker).setOngoing(true).setUsesChronometer(true)
				.setSmallIcon(R.drawable.play).setLargeIcon((((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap())).build();

		startForeground(NOTIFY_ID, thisNotification);
	}

	@Override
	public void onDestroy() {
		Stop();
		stopForeground(true);
	}

	Context thisContext;

	private String thisStationID;
	private String thisStationName;
	private String thisStationURL;

	public void PlayUrl(String theURL, String theName, String theID) {
		thisStationID = theID;
		thisStationName = theName;
		thisStationURL = theURL;
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... stations) {
				String aStation = thisStationURL;

				Log.v(TAG, "Stream url:" + aStation);
				SendMessage(thisStationName, "Decoding URL", "Decoding URL");
				String aDecodedURL = DecodeURL(aStation);

				Log.v(TAG, "Stream url decoded:" + aDecodedURL);
				if (thisMediaPlayer == null) {
					thisMediaPlayer = new MediaPlayer();
					thisMediaPlayer.setOnBufferingUpdateListener(PlayerService.this);
				}
				if (thisMediaPlayer.isPlaying()) {
					thisMediaPlayer.stop();
					thisMediaPlayer.reset();
				}
				try {
					SendMessage(thisStationName, "Preparing stream", "Preparing stream");
					thisMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
					thisMediaPlayer.setDataSource(aDecodedURL);
					thisMediaPlayer.prepare();
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
		if (thisMediaPlayer != null) {
			if (thisMediaPlayer.isPlaying()) {
				thisMediaPlayer.stop();
			}
			thisMediaPlayer.release();
			thisMediaPlayer = null;
		}
		stopForeground(true);
	}

	public String downloadFeed(String theURI) {
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(theURI);
		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
					builder.append('\n');
				}
			} else {
				Log.e(TAG, "Failed to download file");
			}
		} catch (ClientProtocolException e) {
			Log.e(TAG, "" + e);
		} catch (IOException e) {
			Log.e(TAG, "" + e);
		}
		return builder.toString();
	}

	String DecodeURL(String theUrl) {
		try {
			URL anUrl = new URL(theUrl);
			String aFileName = anUrl.getFile();

			Log.v("", "Decoded filename:" + aFileName);
			Log.v("", "Decoded query:" + anUrl.getQuery());

			int aQueryIndex = aFileName.indexOf('?');
			if (aQueryIndex >= 0) {
				aFileName = aFileName.substring(0, aQueryIndex);
			}

			if (aFileName.endsWith(".pls")) {
				Log.v(TAG, "Found PLS file");
				String theFile = downloadFeed(theUrl);
				BufferedReader aReader = new BufferedReader(new StringReader(theFile));
				String str;
				while ((str = aReader.readLine()) != null) {
					Log.v(TAG, " -> " + str);
					if (str.substring(0, 4).equals("File")) {
						int anIndex = str.indexOf('=');
						if (anIndex >= 0) {
							return str.substring(anIndex + 1);
						}
					}
				}
			} else if (aFileName.endsWith(".m3u")) {
				Log.v(TAG, "Found M3U file");
				String theFile = downloadFeed(theUrl);
				BufferedReader aReader = new BufferedReader(new StringReader(theFile));
				String str;
				while ((str = aReader.readLine()) != null) {
					Log.v(TAG, " -> " + str);
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
		SendMessage(thisStationName, "Buffering..", "Buffering .. (" + percent + "%)");
	}
}
