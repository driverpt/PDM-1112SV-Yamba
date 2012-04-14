package pt.isel.pdm.Yamba;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PrefsActivity extends PreferenceActivity {

    public static final String KEY_MAX_PRESENTED_CHARS  = "max_chars";
    public static final String KEY_MAX_PRESENTED_TWEETS = "max_tweets";
    public static final String KEY_USERNAME             = "user";
    public static final String KEY_PASSWORD             = "pass";
    public static final String KEY_URL                  = "url";

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        addPreferencesFromResource( R.xml.app_prefs );
    }
}
