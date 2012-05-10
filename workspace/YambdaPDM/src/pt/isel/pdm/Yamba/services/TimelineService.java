package pt.isel.pdm.yamba.services;

import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import pt.isel.pdm.yamba.YambaPDMApplication;
import winterwell.jtwitter.Twitter;

public class TimelineService extends IntentService implements Runnable {

    private static final String LOGGER_TAG            = "Timeline Service";

    public static final int     TIMELINE_UPDATE_OK    = 0;
    public static final int     TIMELINE_UPDATE_ERROR = -2;

    public static final int     MSG_UPDATE_TIMELINE   = 1;

    public static final int     INVALID_OPERATION     = -1;
    public static final String  OPERATION             = "Timeline Operation";

    private static final String SCHEDULED_START       = "Timeline Scheduled Start";

    private Handler             mainThreadHandler;
    private Handler             localHandler;

    public TimelineService() {
        super( "TimelineService" );
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d( LOGGER_TAG, "onCreate()" );
        mainThreadHandler = new Handler();
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

        if( intent.getBooleanExtra( SCHEDULED_START, false ) ) {
            app.publishRunnableOnTimelineService( this );
        }
    }

    @Override
    public void onDestroy() {
        Log.d( LOGGER_TAG, "onDestroy()" );
        super.onDestroy();
    }

    @Override
    public IBinder onBind( Intent intent ) {
        return null;
    }

    public void run() {
        Intent intent = new Intent( this, TimelineService.class );
        intent.putExtra( TimelineService.OPERATION, TimelineService.MSG_UPDATE_TIMELINE );
        intent.putExtra( TimelineService.SCHEDULED_START, true );
        startService( intent );
    }
}
