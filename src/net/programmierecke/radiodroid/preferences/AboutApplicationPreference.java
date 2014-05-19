package net.programmierecke.radiodroid.preferences;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import net.programmierecke.radiodroid.Utils;

public class AboutApplicationPreference extends Preference {

	public AboutApplicationPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setTitle(Utils.getAppAndVersionName(context));

	}

}
