package net.programmierecke.radiodroid;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RadioStationList extends ArrayAdapter<RadioStation> implements Runnable {
	public class QueueItem {
		public String thisURL;
		public ImageView thisImageView;

		public QueueItem(String theURL, ImageView theImageView) {
			thisURL = theURL;
			thisImageView = theImageView;
		}
	}

	HashMap<String, Bitmap> thisIconCache = new HashMap<String, Bitmap>();
	BlockingQueue<QueueItem> thisQueuedDownloadJobs = new ArrayBlockingQueue<QueueItem>(1000);
	Thread thisThread;

	public RadioStationList(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		thisContext = context;
		thisThread = new Thread(this);
		thisThread.start();
	}

	Context thisContext;

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) thisContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.station_list, null);
		}
		RadioStation aStation = getItem(position);
		if (aStation != null) {
			TextView aTextViewTop = (TextView) v.findViewById(R.id.textViewTop);
			TextView aTextViewBottom = (TextView) v.findViewById(R.id.textViewBottom);
			if (aTextViewTop != null) {
				aTextViewTop.setText("" + aStation.Name);
			}
			if (aTextViewBottom != null) {
				aTextViewBottom.setText("" + aStation.stationDetailsShortString());
			}
			ImageView anImageView = (ImageView) v.findViewById(R.id.imageViewIcon);

			// new DownloadImageTask(anImageView).execute(aStation.IconUrl);
			if (thisIconCache.containsKey(aStation.IconUrl)) {
				Bitmap aBitmap = thisIconCache.get(aStation.IconUrl);
				if (aBitmap != null)
					anImageView.setImageBitmap(aBitmap);
				else
					anImageView.setImageBitmap(null);
			} else {
				try {
					// check download cache
					String aFileNameIcon = Utils.getBase64(aStation.IconUrl);
					Bitmap anIcon = BitmapFactory.decodeStream(thisContext.openFileInput(aFileNameIcon));
					anImageView.setImageBitmap(anIcon);
					thisIconCache.put(aStation.IconUrl, anIcon);
				} catch (Exception e) {
					try {
						anImageView.setImageBitmap(null);
						thisQueuedDownloadJobs.put(new QueueItem(aStation.IconUrl, anImageView));
					} catch (InterruptedException e2) {
						Log.e("Error", "" + e2);
					}
				}
			}
		}
		return v;
	}

	@Override
	public void run() {
		while (true) {
			try {
				final QueueItem anItem = thisQueuedDownloadJobs.take();
				try {
					if (!thisIconCache.containsKey(anItem.thisURL)) {
						InputStream in = new java.net.URL(anItem.thisURL).openStream();
						final Bitmap anIcon = BitmapFactory.decodeStream(in);
						thisIconCache.put(anItem.thisURL, anIcon);

						anItem.thisImageView.post(new Runnable() {
							public void run() {
								if (anIcon != null) {
									// set image in view
									anItem.thisImageView.setImageBitmap(anIcon);

									// save image to file
									String aFileName = Utils.getBase64(anItem.thisURL);
									Log.v("", "" + anItem.thisURL + "->" + aFileName);
									try {
										FileOutputStream aStream = thisContext.openFileOutput(aFileName, Context.MODE_PRIVATE);
										anIcon.compress(Bitmap.CompressFormat.PNG, 100, aStream);
										aStream.close();
									} catch (FileNotFoundException e) {
										Log.e("", "" + e);
									} catch (IOException e) {
										Log.e("", "" + e);
									}
								}
							}
						});
					}
				} catch (Exception e) {
					Log.e("Error", "" + e);
					thisIconCache.put(anItem.thisURL, null);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e("Error", "" + e);
			}
		}
	}

}
