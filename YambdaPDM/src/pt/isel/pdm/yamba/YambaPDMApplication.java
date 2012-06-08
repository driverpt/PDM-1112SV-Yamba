package pt.isel.pdm.yamba;

import java.util.concurrent.TimeUnit;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ApplicationInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;

import pt.isel.pdm.yamba.services.StatusPublishingService;
import pt.isel.pdm.yamba.services.TimelineService;
import winterwell.jtwitter.Twitter;

public class YambaPDMApplication extends Application implements OnSharedPreferenceChangeListener {
    public Button                              lastSubmit;
    public Button                              lastRefresh;
    private Twitter                            twitter;
    private SharedPreferences                  prefs;

    private Handler                            handler;

    private PendingIntent                      mScheduleTimelinePendingIntent;

    public static final String                 ACTION_YAMBA_TIMELINE_UPDATED = "pt.isel.pdm.yamba.TIMELINE_UPDATED";

    private ConnectivityAwareBroadcastReceiver mWifiBroadcastReceiver;
    private ConnectivityAwareBroadcastReceiver mInternetConnectivityBroadcastReceiver;

    private IntentFilter                       mConnectivityIntentFilter     = new IntentFilter(
                                                                                     ConnectivityManager.CONNECTIVITY_ACTION );

    private class WifiConnectivityAwareBroadcastReceiver extends ConnectivityAwareBroadcastReceiver {

        @Override
        public boolean onCheckConnectivityChange( NetworkInfo networkInfo ) {
            return ( networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI && networkInfo
                    .isConnected() );
        }

        @Override
        public void onPostConnectivityChange() {
            scheduleTimelineService();
        }
    }
    
    public IntentFilter getConnectivityIntentFilter() {
        return mConnectivityIntentFilter;
    }
    
    @Override
    public void onCreate() {
        initSharedPrefs();

        prefs.registerOnSharedPreferenceChangeListener( this );
        handler = new Handler();

        int applicationFlags = this.getApplicationInfo().flags;
        boolean DEVELOPER_MODE = ( applicationFlags & ApplicationInfo.FLAG_DEBUGGABLE ) != 0;

        if ( DEVELOPER_MODE ) {
            StrictMode.setThreadPolicy( new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites()
                    .detectNetwork() // or .detectAll() for all detectable problems
                    .penaltyLog().build() );
            StrictMode.setVmPolicy( new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects()
            // .detectLeakedClosableObjects()
                    .penaltyLog().penaltyDeath().build() );
        }

        mWifiBroadcastReceiver = new WifiConnectivityAwareBroadcastReceiver();

        mInternetConnectivityBroadcastReceiver = new ConnectivityAwareBroadcastReceiver() {
            
            @Override
            public void onPostConnectivityChange() {
                redeliverPostPonedStatusPublish();
            }

        };
        
        registerReceiver( mWifiBroadcastReceiver, mConnectivityIntentFilter );
        registerReceiver( mInternetConnectivityBroadcastReceiver, mConnectivityIntentFilter );

        super.onCreate();
    }

    private void initPendingIntent() {
        Intent intent = new Intent( this, TimelineService.class );
        intent.putExtra( TimelineService.OPERATION, TimelineService.MSG_UPDATE_TIMELINE );
        mScheduleTimelinePendingIntent = PendingIntent.getService( this, 0, intent, 0 );
    }

    public Twitter getTwitter() {
        if ( twitter == null ) {
            initTwitter();
        }
        return twitter;
    }

    public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key ) {
        if ( key.equals( PrefsActivity.KEY_USERNAME ) || key.equals( PrefsActivity.KEY_URL )
                || key.equals( PrefsActivity.KEY_PASSWORD ) ) {
            twitter = null;
        }

        if ( key.equals( PrefsActivity.KEY_TIMELINE_REFRESH ) ) {
            scheduleTimelineService();
        }
    }

    public void redeliverPostPonedStatusPublish() {
        Intent intent = new Intent(this, StatusPublishingService.class );
        startService( intent );
    }
    
    public void scheduleTimelineService() {
        initPendingIntent();

        AlarmManager alarmManager = ( AlarmManager ) getSystemService( ALARM_SERVICE );
        alarmManager.cancel( mScheduleTimelinePendingIntent );

        if ( !mWifiBroadcastReceiver.hasConnectivity() ) {
            return;
        }

        SharedPreferences prefs = getSharedPreferences();
        boolean isToBeUpdated = prefs.getBoolean( PrefsActivity.KEY_AUTOMATIC_TIMELINE_UPDATE, false );

        if ( isToBeUpdated ) {
            int timelineInterval = Integer.parseInt( prefs.getString( PrefsActivity.KEY_TIMELINE_REFRESH, "0" ) );
            if ( timelineInterval > 0 ) {
                TimeUnit converter = TimeUnit.MILLISECONDS;
                long convertedMinutesToMillis = converter.convert( timelineInterval, TimeUnit.MINUTES );
                alarmManager.setRepeating( AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
                        convertedMinutesToMillis, mScheduleTimelinePendingIntent );
                Log.d( this.getClass().getSimpleName(), String.format( "Alarm set every %d minutes", timelineInterval ) );
            }
        }
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
