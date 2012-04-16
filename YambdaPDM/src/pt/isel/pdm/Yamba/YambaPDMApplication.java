package pt.isel.pdm.Yamba;

import winterwell.jtwitter.Twitter;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.widget.Button;

public class YambaPDMApplication extends Application implements OnSharedPreferenceChangeListener {
	public Button             lastSubmit;
	public Button             lastRefresh;
	private Twitter           twitter;
	private SharedPreferences prefs;

	@Override
	public void onCreate() {
		initSharedPrefs();
		prefs.registerOnSharedPreferenceChangeListener( this );
		super.onCreate();
	}



	public Twitter getTwitter() {
		if ( twitter == null ) {
			initTwitter();
		}
		return twitter;
	}

	public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key ) {
		twitter = null;
	}

	public SharedPreferences getSharedPreferences() {
		return prefs;
	}

	private void initSharedPrefs() {
		prefs = PreferenceManager.getDefaultSharedPreferences( this );
	}

	private void initTwitter() {
		String username = prefs.getString( PrefsActivity.KEY_USERNAME, "" );
		String password = prefs.getString( PrefsActivity.KEY_PASSWORD, "" );
		String url = prefs.getString( PrefsActivity.KEY_URL, "" );
		twitter = new Twitter( username, password );
		twitter.setAPIRootUrl( url );
	}
}
