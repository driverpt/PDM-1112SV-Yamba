package pt.isel.pdm.Yamba;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.widget.Button;

import winterwell.jtwitter.Twitter;

public class App extends Application {
    public  Button            lastSubmit;
    public  Button            lastRefresh;
    private Twitter           twitter;
    private SharedPreferences prefs;

    @Override
    public void onCreate() {
        initSharedPrefs();
        prefs.registerOnSharedPreferenceChangeListener( new OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key ) {
                twitter = null;
            }
        } );
        super.onCreate();
    }

    public Twitter getTwitter() {
        if ( twitter == null ) {
            initTwitter();
        }
        return twitter;
    }

    public SharedPreferences getSharedPreferences() {
        return prefs;
    }

    private void initSharedPrefs() {
        prefs = PreferenceManager.getDefaultSharedPreferences( this );
    }

    private void initTwitter() {
        String username = prefs.getString( "user", "" );
        String password = prefs.getString( "pass", "" );
        String url = prefs.getString( "url", "" );
        twitter = new Twitter( username, password );
        twitter.setAPIRootUrl( url );
    }
}
