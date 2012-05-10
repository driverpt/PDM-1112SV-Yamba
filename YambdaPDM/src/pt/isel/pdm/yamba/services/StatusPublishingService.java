package pt.isel.pdm.yamba.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import pt.isel.pdm.yamba.YambaPDMApplication;
import winterwell.jtwitter.Twitter;

public class StatusPublishingService extends IntentService {

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
        try {
            twitter.updateStatus( message );
            Log.d( LOGGER_TAG, String.format( "Message Sucessefully Posted" ) );
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
}
