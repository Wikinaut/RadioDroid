package net.programmierecke.radiodroid;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

public class AboutApplicationPreference extends Preference {

	public AboutApplicationPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setTitle(Utils.getAppAndVersionName(context));

	}

}
