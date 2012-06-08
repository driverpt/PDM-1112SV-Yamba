package pt.isel.pdm.yamba.services;

import java.util.Date;
import java.util.List;

import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import winterwell.jtwitter.TwitterException;

import pt.isel.pdm.yamba.YambaPDMApplication;
import pt.isel.pdm.yamba.dataAccessLayer.TweetDataAccessLayer;
import pt.isel.pdm.yamba.dataAccessLayer.TweetToPostDataAccessLayer;
import pt.isel.pdm.yamba.model.TweetToPost;
import winterwell.jtwitter.Status;
import winterwell.jtwitter.Twitter;

public class StatusPublishingService extends ConnectivityAwareIntentService {

    private static final String   LOGGER_TAG   = "StatusPublishing";

    public static final String    SERVICE_NAME = "Status Publishing Service";

    public static final String    TWEET_MSG    = "Tweet Message";

    public static final String    OPERATION    = "pt.isel.pdm.yamba.StatusPublishingService.OPERATION";

    private Twitter               twitter;
    private Handler               mainHandler;

    private LocalBroadcastManager mLocalBcastManager;
    private Intent                mTimelineUpdatedIntent;

    public StatusPublishingService() {
        super( SERVICE_NAME );
    }

    private void initTimelineUpdatedIntent() {
        if ( mTimelineUpdatedIntent == null ) {
            mTimelineUpdatedIntent = new Intent( YambaPDMApplication.ACTION_YAMBA_TIMELINE_UPDATED );
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        setIntentRedelivery( true );
        twitter = ( ( YambaPDMApplication ) getApplication() ).getTwitter();
        mainHandler = new Handler();
        mLocalBcastManager = LocalBroadcastManager.getInstance( this );
        Log.d( LOGGER_TAG, "StatusPublishingService.onCreate()" );
    }

    @Override
    protected void onHandleIntent( Intent intent ) {
        Log.d( LOGGER_TAG, "StatusPublishingService.onHandleIntent()" );
        String message = intent.getStringExtra( TWEET_MSG );
        Log.d( LOGGER_TAG, String.format( "Tweet Message: %s", intent.getStringExtra( TWEET_MSG ) ) );
        TweetToPost tweet = null;
        try {
            if ( message != null ) {
                if ( !hasConnectivity() ) {
                    tweet = new TweetToPost( new Date(), message );
                    Log.d( LOGGER_TAG, "No Connectivity Available, storing new message in local database" );
                    storeTweetInDatabase( tweet );
                    return;
                }                
                Status status = twitter.updateStatus( message );
                TweetDataAccessLayer.insertTweet( getContentResolver(), status );
                Log.d( LOGGER_TAG, String.format( "Message Sucessfully Posted" ) );
            }
        } catch ( final Exception e ) {
            if ( tweet != null ) {
                storeTweetInDatabase( tweet );
            }
            mainHandler.post( new Runnable() {
                public void run() {
                    Toast toast = Toast.makeText( StatusPublishingService.this, e.getMessage(), Toast.LENGTH_LONG );
                    toast.show();
                }
            } );
        }
        tryRedeliverPostponed();
    }

    private void tryRedeliverPostponed() {
        Log.d( LOGGER_TAG, "Redelivering Post-poned Status" );
        List< TweetToPost > tweets = TweetToPostDataAccessLayer.getTweetsToPost( getContentResolver() );
        
        for ( TweetToPost tweet : tweets ) {
            try {
                Status tweetStatus = twitter.updateStatus( tweet.getText() );
                TweetToPostDataAccessLayer.deleteTweetToPost( getContentResolver(), tweet );
                TweetDataAccessLayer.insertTweet( getContentResolver(), tweetStatus );
            } catch ( TwitterException e ) {
                Log.e( LOGGER_TAG, String.format( "Error while trying to post Tweet Message\"%s\" with Timestamp=%s",
                        tweet.getDate().toGMTString() ) );
                continue;
            }
        }
        if ( tweets.size() != 0 ) {
            initTimelineUpdatedIntent();
            mLocalBcastManager.sendBroadcast( mTimelineUpdatedIntent );
        }
    }

    private void storeTweetInDatabase( TweetToPost tweet ) {
        TweetToPostDataAccessLayer.insertTweetToPost( getContentResolver(), tweet );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d( LOGGER_TAG, "StatusPublishingService.onDestroy()" );
    }

    @Override
    protected void onConnectivityAvailable() {
        Log.d( LOGGER_TAG, "StatusPublishingService.onConnectivityAvailable()" );
        super.onConnectivityAvailable();
    }

    @Override
    protected void onConnectivityUnavailable() {
        Log.d( LOGGER_TAG, "StatusPublishingService.onConnectivityUnavailable()" );
        super.onConnectivityUnavailable();
    }
}
