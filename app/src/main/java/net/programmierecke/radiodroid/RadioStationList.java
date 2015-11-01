package net.programmierecke.radiodroid;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
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
		public String url;
		public ImageView imageView;

		public QueueItem(String theURL, ImageView theImageView) {
			url = theURL;
			imageView = theImageView;
		}
	}

	HashMap<String, Bitmap> iconCache = new HashMap<String, Bitmap>();
	BlockingQueue<QueueItem> queuedDownloadJobs = new ArrayBlockingQueue<QueueItem>(1000);
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
			
			TextView aTextViewTopLine = (TextView) v.findViewById(R.id.textViewTop);
			TextView aTextViewBottomLine = (TextView) v.findViewById(R.id.textViewBottom);
			
			if (aTextViewTopLine != null) {
				aTextViewTopLine.setText("" + aStation.name);
			}
			
			if (aTextViewBottomLine != null) {
				aTextViewBottomLine.setText("" + aStation.stationDetailsShortString());
			}
			
			ImageView anImageView = (ImageView) v.findViewById(R.id.imageViewIcon);

			// new DownloadImageTask(anImageView).execute(aStation.IconUrl);

			if ( aStation.iconUrl.isEmpty() ) {
				
				anImageView.setImageBitmap(null);
			
			} else if ( iconCache.containsKey(aStation.iconUrl) ) {

				Bitmap aBitmap = iconCache.get(aStation.iconUrl);
				
				if ( aBitmap != null) {
					
					anImageView.setImageBitmap(aBitmap);
					
				} else {
					
					anImageView.setImageBitmap(null);
					
				}
				
			} else {
				
				try {
					
					// check download cache
					String aFileNameIcon = Utils.getBase64(aStation.iconUrl);
					Bitmap anIcon = BitmapFactory.decodeStream(thisContext.openFileInput(aFileNameIcon));
					anImageView.setImageBitmap(anIcon);
					iconCache.put(aStation.iconUrl, anIcon);
					
				} catch (Exception e) {
					
					try {
						
						anImageView.setImageBitmap(null);
						queuedDownloadJobs.put(new QueueItem(aStation.iconUrl, anImageView));
						
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
				
				final QueueItem queuedItem = queuedDownloadJobs.take();
				
				try {
					
					if ( !iconCache.containsKey(queuedItem.url) ) {
						
						InputStream in = new java.net.URL(queuedItem.url).openStream();
						final Bitmap anIcon = BitmapFactory.decodeStream(in);
						iconCache.put(queuedItem.url, anIcon);

						queuedItem.imageView.post(new Runnable() {
							
							public void run() {
								
								if (anIcon != null) {
									
									// set image in view
									queuedItem.imageView.setImageBitmap(anIcon);

									// save image to file
									String aFileName = Utils.getBase64(queuedItem.url);
									
									try {
										
										FileOutputStream aStream = thisContext.openFileOutput(aFileName, Context.MODE_PRIVATE);
										anIcon.compress(Bitmap.CompressFormat.PNG, 100, aStream);
										aStream.close();
										
									} catch (FileNotFoundException e) {
										
										Log.e("iconCache", "FileNotFoundException " + e);
										iconCache.put(queuedItem.url, null);
										
									} catch (UnknownHostException e) {
										
										Log.e("iconCache", "UnknownHostException " + e);
										iconCache.put(queuedItem.url, null);

									} catch (IOException e) {
										
										Log.e("iconCache", "IOException " + e);
										iconCache.put(queuedItem.url, null);
										
									}
									
								}
								
							}
							
						});
					}
				} catch (Exception e) {
					
					Log.e("iconCache", "" + e + " <= " + queuedItem.url);
					iconCache.put(queuedItem.url, null);
					
				}
				
			} catch (Exception e) {
				
				// TODO Auto-generated catch block
				Log.e("Error", "" + e);
				
			}
			
		}
		
	}

}
