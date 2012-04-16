package pt.isel.pdm.Yamba;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

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

	public static boolean checkPreferences(Context ctx) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( ctx );
		if(	prefs.getString(KEY_USERNAME,"").isEmpty() || 
				prefs.getString(KEY_PASSWORD,"").isEmpty()||
				prefs.getString(KEY_URL,"").isEmpty()){
			return false;
		}
		return true;
	}
}
