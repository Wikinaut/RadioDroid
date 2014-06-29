package net.programmierecke.radiodroid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.view.MenuItem;
import android.content.Context;
import net.programmierecke.radiodroid.Constants;
import net.programmierecke.radiodroid.Utils;

public final class AboutApplicationActivity extends PreferenceActivity {

  private static final String KEY_ABOUT_VERSION = "about_version";
  private static final String KEY_ABOUT_CHANGELOG = "about_changelog";
  private static final String KEY_ABOUT_COMMUNITYRADIO = "about_communityradio";
  private static final String KEY_ABOUT_SOURCE = "about_source_code";
  private static final String KEY_ABOUT_LICENSE = "about_license";

  @Override
  protected void onCreate(final Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);

    addPreferencesFromResource(R.xml.about_preferences);

    Context context = AboutApplicationActivity.this;
    findPreference(KEY_ABOUT_VERSION).setSummary(Utils.getAppAndVersionName(context));
    findPreference(KEY_ABOUT_CHANGELOG).setSummary(Constants.CHANGELOG_URL);
    findPreference(KEY_ABOUT_COMMUNITYRADIO).setSummary(Constants.COMMUNITYRADIO_URL);
    findPreference(KEY_ABOUT_SOURCE).setSummary(Constants.SOURCE_URL);
    findPreference(KEY_ABOUT_LICENSE).setSummary(Constants.LICENSE_URL);

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


  @Override
  public boolean onPreferenceTreeClick(final PreferenceScreen preferenceScreen, final Preference preference) {

    final String key = preference.getKey();

    if (KEY_ABOUT_CHANGELOG.equals(key)) {
      startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.CHANGELOG_URL)));
    } else if (KEY_ABOUT_COMMUNITYRADIO.equals(key)) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.COMMUNITYRADIO_URL)));
    } else if (KEY_ABOUT_SOURCE.equals(key)) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.SOURCE_URL)));
    } else if (KEY_ABOUT_LICENSE.equals(key)) {
      startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.LICENSE_URL)));
    }

    return false;

  }

}
