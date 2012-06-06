package pt.isel.pdm.yamba.services;

import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import pt.isel.pdm.yamba.YambaPDMApplication;
import pt.isel.pdm.yamba.provider.contract.TweetContract;
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
    }

    @Override
    protected void onHandleIntent( Intent intent ) {
        int operation = intent.getIntExtra( OPERATION, INVALID_OPERATION );
        Log.d( LOGGER_TAG, String.format( "onHandleIntent() called, operation = %d", operation ) );

        YambaPDMApplication app = ( YambaPDMApplication ) getApplication();

        switch ( operation ) {
            case MSG_UPDATE_TIMELINE: {
                try {
                    if ( hasConnectivity() ) {
                        final Twitter twitter = app.getTwitter();
                        ContentResolver contentResolver = getContentResolver();
                        Cursor mCursor = contentResolver.query( TweetContract.CONTENT_URI
                                                              , new String[] { "max( " + TweetContract._ID + ")" }
                                                              , null
                                                              , null
                                                              , null 
                                                              );
                        long lastId = 0;
                        try {
                            if ( mCursor.moveToNext() ) {
                                lastId = mCursor.getLong( 0 );
                            }
                        } finally {
                            mCursor.close();
                        }
                        if( lastId != 0 ) {
                            twitter.setSinceId( lastId );
                        }
                        
                        final List< Twitter.Status > twitterStatus = twitter.getHomeTimeline();
                        
                        for( Twitter.Status status : twitterStatus ) {
                            ContentValues values = new ContentValues();
                            values.put( TweetContract._ID, status.getId() );
                            values.put( TweetContract.DATE, status.getCreatedAt().toString() );
                            values.put( TweetContract.TIMESTAMP, status.getCreatedAt().getTime() );
                            values.put( TweetContract.USER, status.getUser().getScreenName() );
                            values.put( TweetContract.TWEET, status.getText() );
                            getContentResolver().insert( TweetContract.CONTENT_URI, values );
                        }
                        
                        LocalBroadcastManager localBcast = LocalBroadcastManager.getInstance( this );
                        if ( mTimelineUpdateIntent == null ) {
                            mTimelineUpdateIntent = new Intent();
                            mTimelineUpdateIntent.setAction( YambaPDMApplication.ACTION_YAMBA_TIMELINE_UPDATED );
                        }
                        localBcast.sendBroadcast( mTimelineUpdateIntent );
                    }
                } catch ( final Exception e ) {
                    Log.e(LOGGER_TAG, "Exception Occurred on updating timeline", e);
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

//        app.scheduleTimelineService();
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

    @Override
    protected void onConnectivityAvailable() {
        super.onConnectivityAvailable();
        Log.d( LOGGER_TAG, "onConnectivityAvailable() called" );
    }

    @Override
    protected void onConnectivityUnavailable() {
        super.onConnectivityUnavailable();
        Log.d( LOGGER_TAG, "onConnectivityUnavailable() called" );
    }
}
