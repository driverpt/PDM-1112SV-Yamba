package pt.isel.pdm.yamba.services;

import android.app.IntentService;
import android.content.Intent;
import android.net.NetworkInfo;
import android.util.Log;

import pt.isel.pdm.yamba.ConnectivityAwareBroadcastReceiver;
import pt.isel.pdm.yamba.YambaPDMApplication;

public abstract class ConnectivityAwareIntentService extends IntentService {

    private static final String LOGGER_TAG       = "ConnectivityAware Service";

    protected ConnectivityAwareBroadcastReceiver mConnectivityReceiver;

    protected ConnectivityAwareIntentService( String name ) {
        super( name );
        mConnectivityReceiver = new ConnectivityAwareBroadcastReceiver() {
            
            @Override
            public void onPostConnectivityChange() {
                if ( hasConnectivity() ) {
                    onConnectivityAvailable();
                } else {
                    onConnectivityUnavailable();
                }
                
            }
            
            @Override
            public boolean onCheckConnectivityChange( NetworkInfo networkInfo ) {
                return ( networkInfo != null && networkInfo.isConnected() );
            }
        };
    }


    @Override
    public void onCreate() {
        super.onCreate();

        Intent intent = registerReceiver( mConnectivityReceiver, ( (YambaPDMApplication) getApplication() ).getConnectivityIntentFilter() );
        Log.d( LOGGER_TAG, String.format( "Receiver Registered, Intent=%s", intent ) );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver( mConnectivityReceiver );
    }
    
    protected boolean hasConnectivity() {
        return mConnectivityReceiver.hasConnectivity();
    }

    protected void onConnectivityAvailable() {
        Log.d( LOGGER_TAG, "onConnectivityAvailable() called" );
    }

    protected void onConnectivityUnavailable() {
        Log.d( LOGGER_TAG, "onConnectivityUnavailable() called" );
    }
}
