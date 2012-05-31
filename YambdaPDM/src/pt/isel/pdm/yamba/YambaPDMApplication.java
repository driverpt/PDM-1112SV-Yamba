package pt.isel.pdm.yamba;

import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.widget.Button;

import pt.isel.pdm.yamba.model.YambaPost;
import pt.isel.pdm.yamba.services.TimelineService;
import pt.isel.pdm.yamba.services.WifiTimelineAutoUpdaterService;

import winterwell.jtwitter.Twitter;

public class YambaPDMApplication extends Application implements OnSharedPreferenceChangeListener, Runnable {
    public Button                          lastSubmit;
    public Button                          lastRefresh;
    private Twitter                        twitter;
    private SharedPreferences              prefs;

    private List< Twitter.Status >         timeline;
    private OnYambaTimelineChangeListener  timelineChangedListener;

    private WifiTimelineAutoUpdaterService timelineService;

    private Runnable                       lastTimelineServiceRunnableToken;
    private Handler                        handler;

    @Override
    public void onCreate() {
        initSharedPrefs();
        prefs.registerOnSharedPreferenceChangeListener( this );
        handler = new Handler();
        
        int applicationFlags = this.getApplicationInfo().flags;
        boolean DEVELOPER_MODE = (applicationFlags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        
        if ( DEVELOPER_MODE ) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    //.detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }

        
        super.onCreate();
    }

    public Twitter getTwitter() {
        if ( twitter == null ) {
            initTwitter();
        }
        return twitter;
    }

    public void setOnYambaTimelineChangeListener( OnYambaTimelineChangeListener listener ) {
        timelineChangedListener = listener;
    }

    public List< Twitter.Status > getCurrentTimeline() {
        return timeline;
    }

    // Synchronized is needed to avoid concurrency issues
    public synchronized void setTimeline( List< Twitter.Status > newTimeline ) {
        timeline = newTimeline;
        if ( timelineChangedListener != null ) {
            notifyOnYambaTimelineChangeListener();
        }
    }

    private void notifyOnYambaTimelineChangeListener() {
        Handler handler = new Handler( Looper.getMainLooper() );
        handler.post( new Runnable() {
            public void run() {
                timelineChangedListener.onYambaTimelineChange();
            }
        } );
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

    public void scheduleTimelineService() {
        if ( lastTimelineServiceRunnableToken != null ) {
            handler.removeCallbacks( lastTimelineServiceRunnableToken );
        }
        publishRunnableOnTimelineService( this );
    }

    public void publishRunnableOnTimelineService( Runnable runnable ) {
        int updateInterval = Integer.parseInt( getSharedPreferences().getString( PrefsActivity.KEY_TIMELINE_REFRESH,
                "0" ) );

        if ( updateInterval == 0 ) {
            return;
        }

        TimeUnit timeunit = TimeUnit.MILLISECONDS;
        long delay = timeunit.convert( updateInterval, TimeUnit.MINUTES );

        handler.postDelayed( runnable, delay );
        
        lastTimelineServiceRunnableToken = runnable;
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

    public void run() {
        Intent intent = new Intent( this, TimelineService.class );
        intent.putExtra( TimelineService.OPERATION, TimelineService.MSG_UPDATE_TIMELINE );
        startService( intent );
    }

    public List< Twitter.Status > getTimeline() {
        return timeline;
    }

    public void startTimelineAutomaticUpdates() {
        if ( timelineService == null )
            timelineService = new WifiTimelineAutoUpdaterService();
        Intent intent = new Intent( this, WifiTimelineAutoUpdaterService.class );
        startService( intent );
    }

    public void stopTimelineAutomaticUpdates() {
        WifiTimelineAutoUpdaterService.serviceInstance.stopSelf();

    }
}
