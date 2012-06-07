package pt.isel.pdm.yamba.services;

import java.util.Date;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import pt.isel.pdm.yamba.dataAccessLayer.TweetDataAccessLayer;

import winterwell.jtwitter.Twitter.Status;

import pt.isel.pdm.yamba.YambaPDMApplication;
import pt.isel.pdm.yamba.dataAccessLayer.TweetToPostDataAccessLayer;
import pt.isel.pdm.yamba.model.TweetToPost;
import winterwell.jtwitter.Twitter;

public class StatusPublishingService extends ConnectivityAwareIntentService {

    private static final String LOGGER_TAG   = "StatusPublishing";

    public static final String  SERVICE_NAME = "Status Publishing Service";

    public static final String  TWEET_MSG    = "Tweet Message";

    private Twitter             twitter;
    private Handler             mainHandler;
  
    public StatusPublishingService() {
        super( SERVICE_NAME );
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setIntentRedelivery( true );
        twitter = ((YambaPDMApplication) getApplication()).getTwitter();
        mainHandler = new Handler();
        Log.d( LOGGER_TAG, "StatusPublishingService.onCreate()" );      
    }

    @Override
    protected void onHandleIntent( Intent intent ) {
        Log.d( LOGGER_TAG, "StatusPublishingService.onHandleIntent()" );
        String message = intent.getStringExtra( TWEET_MSG );
        Log.d( LOGGER_TAG, String.format( "Tweet Message: %s", intent.getStringExtra( TWEET_MSG ) ) );
        if ( !hasConnectivity() ) {
            Log.d( LOGGER_TAG, "No Wifi Connectivity Available, storing new message in local database" );
            TweetToPost tweet = new TweetToPost( new Date(), message );
            TweetToPostDataAccessLayer.insertTweetToPost( getContentResolver(), tweet );
            return;
        }
        
        try {
            Status status = twitter.updateStatus( message );
            TweetDataAccessLayer.insertTweet( getContentResolver(), status );
            Log.d( LOGGER_TAG, String.format( "Message Sucessfully Posted" ) );
            
        } catch ( final Exception e ) {
            mainHandler.post( new Runnable() {
                public void run() {
                    Toast toast = Toast.makeText( StatusPublishingService.this, e.getMessage(), Toast.LENGTH_LONG );
                    toast.show();
                }
            } );
        }
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
