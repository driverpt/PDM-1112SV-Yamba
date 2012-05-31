package pt.isel.pdm.yamba.services;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import pt.isel.pdm.yamba.YambaPDMApplication;
import winterwell.jtwitter.Twitter;

public class TimelineService extends ConnectivityAwareIntentService {

    private static final String LOGGER_TAG            = "Timeline Service";

    public static final int     TIMELINE_UPDATE_OK    = 0;
    public static final int     TIMELINE_UPDATE_ERROR = -2;

    public static final int     MSG_UPDATE_TIMELINE   = 1;

    public static final int     INVALID_OPERATION     = -1;
    public static final String  OPERATION             = "Timeline Operation";

    private Handler             mainThreadHandler;

    private Intent              mTimelineUpdateIntent;
    
    public TimelineService() {
        super( "TimelineService" );
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d( LOGGER_TAG, "onCreate()" );
        mainThreadHandler = new Handler();
        
        ConnectivityManager connManager = (ConnectivityManager) getSystemService( Context.CONNECTIVITY_SERVICE );
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo(); 

    }

    @Override
    protected void onHandleIntent( Intent intent ) {
        int operation = intent.getIntExtra( OPERATION, INVALID_OPERATION );
        Log.d( "PDM", String.format( "onHandleIntent() called, operation = %d", operation ) );

        YambaPDMApplication app = (YambaPDMApplication) getApplication();

        switch ( operation ) {
            case MSG_UPDATE_TIMELINE: {
                try {
                    final List< Twitter.Status > twitterStatus = app.getTwitter().getPublicTimeline();
                    app.setTimeline( twitterStatus );
                    LocalBroadcastManager localBcast = LocalBroadcastManager.getInstance( this );
                    if ( mTimelineUpdateIntent == null ) {
                        mTimelineUpdateIntent = new Intent();
                        mTimelineUpdateIntent.setAction( YambaPDMApplication.ACTION_YAMBA_TIMELINE_UPDATED );
                    }
                    localBcast.sendBroadcast( mTimelineUpdateIntent );
                } catch ( final Exception e ) {
                    mainThreadHandler.post( new Runnable() {
                        public void run() {
                            Toast.makeText( TimelineService.this,
                                    String.format( "Error while updating timeline: %s", e.getMessage() ),
                                    Toast.LENGTH_LONG ).show();
                        }
                    } );
                }
                break;
            }
        }

        app.scheduleTimelineService();
    }

    @Override
    public void onDestroy() {
        Log.d( LOGGER_TAG, "onDestroy()" );
        super.onDestroy();
        unregisterReceiver( mConnectivityReceiver );
    }

    @Override
    public IBinder onBind( Intent intent ) {
        return null;
    }
    
    @Override
    protected void onConnectivityAvailable() {
        super.onConnectivityAvailable();
    }
    
    @Override
    protected void onConnectivityUnavailable() {
        // TODO Auto-generated method stub
        super.onConnectivityUnavailable();
    }
}
