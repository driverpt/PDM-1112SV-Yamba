package pt.isel.pdm.Yamba;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.widget.Toast;

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

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			if(!checkPreferences(this)){
				Toast t = Toast.makeText( this, R.string.fill_required_preferences,Toast.LENGTH_LONG);
				t.setDuration(1000);
				t.show();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	public static boolean checkPreferences(Context ctx) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( ctx );
		if(	prefs.getString(KEY_USERNAME,"").isEmpty() || 
				prefs.getString(KEY_PASSWORD,"").isEmpty()){
			return false;
		}
		return true;
	}
}
