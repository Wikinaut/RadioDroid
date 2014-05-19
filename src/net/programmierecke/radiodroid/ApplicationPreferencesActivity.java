package net.programmierecke.radiodroid;

import android.preference.PreferenceActivity;
import android.preference.Preference;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import net.programmierecke.radiodroid.AboutApplicationActivity;

/**
 * The Activity for application preference display and management.
 *
 */

public final class ApplicationPreferencesActivity extends PreferenceActivity {

	private static final String EXIT_APPLICATION = "pref_key_exit_application";
	private static final String STOP_PLAYING = "pref_key_stop_playing";
	private static final String TOP_VOTES = "pref_key_top_votes";
	private static final String TOP_CLICKS = "pref_key_top_clicks";
	private static final String ALL_STATIONS = "pref_key_all_stations";
	private static final String SEARCH_STATIONS = "pref_key_search_stations";
	private static final String ABOUT_APPLICATION = "pref_key_about_application_version";

  @Override
  protected void onCreate( final Bundle savedInstanceState ) {
    super.onCreate( savedInstanceState );

    addPreferencesFromResource(R.xml.preferences);

    this.findPreference(EXIT_APPLICATION).setOnPreferenceClickListener(new AboutApplicationListener());
    this.findPreference(STOP_PLAYING).setOnPreferenceClickListener(new AboutApplicationListener());
    this.findPreference(TOP_VOTES).setOnPreferenceClickListener(new AboutApplicationListener());
    this.findPreference(TOP_CLICKS).setOnPreferenceClickListener(new AboutApplicationListener());
    this.findPreference(ALL_STATIONS).setOnPreferenceClickListener(new AboutApplicationListener());
    this.findPreference(SEARCH_STATIONS).setOnPreferenceClickListener(new AboutApplicationListener());
    this.findPreference(ABOUT_APPLICATION).setOnPreferenceClickListener(new AboutApplicationListener());

  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {

	switch (item.getItemId()) {
    case android.R.id.home:
      finish();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  
  private class AboutApplicationListener implements Preference.OnPreferenceClickListener {
    @Override
    public boolean onPreferenceClick(Preference preference) {

      Context context = ApplicationPreferencesActivity.this;
      startActivity(new Intent(context, AboutApplicationActivity.class));

      return true;

    }

  }

}
