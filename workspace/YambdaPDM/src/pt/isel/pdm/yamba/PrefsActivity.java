package pt.isel.pdm.yamba;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class PrefsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    public static final String    KEY_MAX_PRESENTED_CHARS       = "max_chars";
    public static final String    KEY_MAX_PRESENTED_TWEETS      = "max_tweets";
    public static final String    KEY_USERNAME                  = "user";
    public static final String    KEY_PASSWORD                  = "pass";
    public static final String    KEY_URL                       = "url";
    public static final String    KEY_TIMELINE_REFRESH          = "timeline_refresh_interval";
    public static final String    KEY_AUTOMATIC_TIMELINE_UPDATE = "auto_timeline_update";

    protected static final String PASSWORD_FIELD                = "*******";

    protected static final String NO_AUTOMATIC_UPDATE_STRING    = "0";
    protected static final int    NO_AUTOMATIC_UPDATE           = 0;

    protected SharedPreferences   appPrefs;

    private YambaPDMApplication   app;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        addPreferencesFromResource( R.xml.app_prefs );
        app = ( YambaPDMApplication ) getApplication();
        appPrefs = app.getSharedPreferences();
        appPrefs.registerOnSharedPreferenceChangeListener( this );

        
        
        updateAllPrefs();
    }

    private void updateAllPrefs() {
        updatePreference( KEY_USERNAME );
        updatePreference( KEY_PASSWORD );
        updatePreference( KEY_URL );
        updatePreference( KEY_MAX_PRESENTED_CHARS );
        updatePreference( KEY_MAX_PRESENTED_TWEETS );
        updatePreference( KEY_AUTOMATIC_TIMELINE_UPDATE );
        updatePreference( KEY_TIMELINE_REFRESH );
    }
    
    private void defaultUpdateSummary( String key, String value, int id) {
        if( value.isEmpty() ) {
            setPreferenceSummary( key, getString( R.string.userSum ) );
        }
        else {
            setPreferenceSummary( key, value );
        }
        
    }
    
    private void updatePreference( String key ) {
        try {
            if ( key.equals( KEY_USERNAME ) ) {
                String user = appPrefs.getString( key, "" );
                defaultUpdateSummary(key, user, R.string.userSum);
                return;
            }
            
            if ( key.equals( KEY_PASSWORD ) ) {
                setPreferenceSummary( key, PASSWORD_FIELD );
                return;
            }
            
            if ( key.equals( KEY_URL ) ) {
                String Url = appPrefs.getString( key, "" );
                defaultUpdateSummary(key, Url, R.string.userSum);
                return;
            }
            
            if ( key.equals( KEY_MAX_PRESENTED_CHARS ) ) {
                String maxPresentedChars = appPrefs.getString( key, "" );
                defaultUpdateSummary(key, maxPresentedChars, R.string.userSum);
            }
            
            if ( key.equals( KEY_MAX_PRESENTED_TWEETS ) ) {
                String maxPresentedTweets = appPrefs.getString( key, "" );
                defaultUpdateSummary(key, maxPresentedTweets, R.string.max_presented_tweets_sum);
            }
            
            // We don't want to change the preference summary text, it's a
            // checkbox, the value is quite obvious
            if ( key.equals( KEY_AUTOMATIC_TIMELINE_UPDATE ) ) {
                // if the value is true, then start the service that
                boolean isAutomaticUpdate = appPrefs.getBoolean( KEY_AUTOMATIC_TIMELINE_UPDATE, false );
                getPreferenceScreen().findPreference( KEY_TIMELINE_REFRESH ).setEnabled( isAutomaticUpdate );
                // YambaPDMApplication already listens for SharedPreference changes and schedules
                return;
            }
            
            if ( key.equals( KEY_TIMELINE_REFRESH ) ) {
                String timelineRefreshString = appPrefs.getString( key, NO_AUTOMATIC_UPDATE_STRING );
                int interval = Integer.parseInt( timelineRefreshString );

                if ( interval == NO_AUTOMATIC_UPDATE ) {
                    setPreferenceSummary( key, getString( R.string.timeline_refresh_interval_sum ) );
                } else {
                    setPreferenceSummary( key, getString( R.string.timeline_refresh_interval_sum_filled, interval ) );
                }
                return;
            }

            setPreferenceSummary( key, appPrefs.getString( key, getString( R.string.no_pref_available ) ) );
        } catch ( NullPointerException exception ) {
            Log.d( "PDM", String.format( "Preference Key not present in the menu: %s", key ), exception );
        }
    }

    public static boolean checkPreferences( Context ctx ) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( ctx );
        if ( prefs.getString( KEY_USERNAME, "" ).isEmpty() || prefs.getString( KEY_PASSWORD, "" ).isEmpty()
                || prefs.getString( KEY_URL, "" ).isEmpty() ) {
            return false;
        }
        return true;
    }

    private void setPreferenceSummary( String key, String summary ) {
        getPreferenceScreen().findPreference( key ).setSummary( summary );
    }

    public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key ) {
        updatePreference( key );
    }

    @Override
    protected void onDestroy() {
        app.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(
                this );
        super.onDestroy();
    }
}
