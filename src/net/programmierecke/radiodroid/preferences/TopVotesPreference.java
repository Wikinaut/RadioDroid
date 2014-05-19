package net.programmierecke.radiodroid.preferences;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import net.programmierecke.radiodroid.MainActivity;
import net.programmierecke.radiodroid.R;
import net.programmierecke.radiodroid.Constants;

public class TopVotesPreference extends Preference {

	public TopVotesPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		setTitle( context.getString(R.string.top_votes) );
		// MainActivity.createStationList(Constants.TOP_VOTES_URL);

	}

}
