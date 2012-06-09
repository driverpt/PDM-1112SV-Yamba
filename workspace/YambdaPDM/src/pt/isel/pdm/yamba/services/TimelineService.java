package pt.isel.pdm.yamba.services;

import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import pt.isel.pdm.yamba.R;
import pt.isel.pdm.yamba.TimelineActivity;
import pt.isel.pdm.yamba.YambaPDMApplication;
import pt.isel.pdm.yamba.provider.contract.TweetContract;
import winterwell.jtwitter.Status;
import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.TwitterException;

public class TimelineService extends ConnectivityAwareIntentService {

    private static final String   LOGGER_TAG            = "Timeline Service";

    public static final int       TIMELINE_UPDATE_OK    = 0;
    public static final int       TIMELINE_UPDATE_ERROR = -2;

    public static final int       MSG_UPDATE_TIMELINE   = 1;
    public static final int       MSG_CLEAR_CACHE       = 2;

    public static final int       INVALID_OPERATION     = -1;
    public static final String    OPERATION             = "Timeline Operation";
    
    // 43690 = 101010101010 = XOXOX :P
    public static final int       NOTIFICATION_BAR_INTEGER_TAG = 43690;

    private Handler               mainThreadHandler;

    private Intent                mTimelineUpdateIntent;
    
    private PendingIntent         mNotificationBarPendingIntent;

    public TimelineService() {
        super( "TimelineService" );
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d( LOGGER_TAG, "onCreate()" );
        mainThreadHandler = new Handler();
    }

    private void initTimelineUpdateIntent() {
        if ( mTimelineUpdateIntent == null ) {
            mTimelineUpdateIntent = new Intent();
            mTimelineUpdateIntent.setAction( YambaPDMApplication.ACTION_YAMBA_TIMELINE_UPDATED );
        }
    }

    private void initNotificationBarPendingIntent() {
        if ( mNotificationBarPendingIntent == null ) {
            Intent startActivityIntent = new Intent( getApplicationContext(), TimelineActivity.class );
            startActivityIntent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
            mNotificationBarPendingIntent = PendingIntent.getActivity( getApplicationContext(), 0, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT );
        }
    }    
    
    private void sendTimelineUpdateBroadcast() {
        sendBroadcast( mTimelineUpdateIntent );
    }

    @Override
    protected void onHandleIntent( Intent intent ) {
        int operation = intent.getIntExtra( OPERATION, INVALID_OPERATION );
        Log.d( LOGGER_TAG, String.format( "onHandleIntent() called, operation = %d", operation ) );

        YambaPDMApplication app = ( YambaPDMApplication ) getApplication();

        switch ( operation ) {
            case MSG_CLEAR_CACHE:
                getContentResolver().delete( TweetContract.CONTENT_URI, null, null );
            case MSG_UPDATE_TIMELINE:
                try {
                    final Twitter twitter = app.getTwitter();
                    ContentResolver contentResolver = getContentResolver();
                    Cursor mCursor = contentResolver.query( TweetContract.CONTENT_URI, new String[] { "max( "
                            + TweetContract._ID + ")" }, null, null, null );
                    long lastId = 0;
                    try {
                        if ( mCursor.moveToNext() ) {
                            lastId = mCursor.getLong( 0 );
                        }
                    } finally {
                        mCursor.close();
                    }
                    if ( lastId != 0 ) {
                        twitter.setSinceId( lastId );
                    }

                    final List< Status > twitterStatus = twitter.getPublicTimeline();

                    for ( Status status : twitterStatus ) {
                        ContentValues values = new ContentValues();
                        values.put( TweetContract._ID, status.getId().longValue() );
                        values.put( TweetContract.DATE, status.getCreatedAt().toString() );
                        values.put( TweetContract.TIMESTAMP, status.getCreatedAt().getTime() );
                        values.put( TweetContract.USER, status.getUser().getScreenName() );
                        values.put( TweetContract.TWEET, status.getText() );
                        getContentResolver().insert( TweetContract.CONTENT_URI, values );
                    }

                    if ( twitterStatus.size() != 0 ) {
                        initNotificationBarPendingIntent();
                        Notification notification = new Notification( android.R.drawable.ic_popup_sync
                                                                    , getString( R.string.notification_new_tweets_ticker )
                                                                    , System.currentTimeMillis() 
                                                                    );
                        NotificationManager mNotificationManager = ( NotificationManager ) getSystemService( NOTIFICATION_SERVICE );
                        notification.flags = Notification.FLAG_AUTO_CANCEL;
                        notification.setLatestEventInfo( getApplicationContext()
                                                       , getString( R.string.notification_new_tweets_short )
                                                       , getString( R.string.notification_new_tweets_detail )
                                                       , mNotificationBarPendingIntent 
                                                       );
                        mNotificationManager.notify( NOTIFICATION_BAR_INTEGER_TAG, notification );
                    }
                    
                    initTimelineUpdateIntent();

                    mTimelineUpdateIntent.putExtra( OPERATION, TIMELINE_UPDATE_OK );

                    sendTimelineUpdateBroadcast();

                    return;

                } catch ( final TwitterException e ) {
                    Log.e( LOGGER_TAG, "Twitter Error ocurred", e );
                    mainThreadHandler.post( new Runnable() {
                        public void run() {
                            Toast.makeText( TimelineService.this, String.format( "Twitter Error: %s", e.getMessage() ),
                                    Toast.LENGTH_LONG ).show();
                        }
                    } );
                } catch ( final Exception e ) {
                    Log.e( LOGGER_TAG, "Exception Occurred on updating timeline", e );
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

        initTimelineUpdateIntent();
        mTimelineUpdateIntent.putExtra( OPERATION, TIMELINE_UPDATE_ERROR );
        sendTimelineUpdateBroadcast();
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
