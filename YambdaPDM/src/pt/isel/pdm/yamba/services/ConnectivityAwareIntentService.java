package pt.isel.pdm.yamba.services;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public abstract class ConnectivityAwareIntentService extends IntentService {

    private volatile boolean    mHasConnectivity = false;
    private ConnectivityManager mConnManager;
    protected BroadcastReceiver mConnectivityReceiver;

    private class ConnectivityAwareBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive( Context context, Intent intent ) {
            boolean hasNetwork = !intent.getBooleanExtra( ConnectivityManager.EXTRA_NO_CONNECTIVITY, true );
            if ( hasNetwork ) {
                updateNetworkInfo();
                onConnectivityAvailable();
            } else {
                mHasConnectivity = false;
            }
        }
    }

    private final IntentFilter mConnectivityReceiverFilter = new IntentFilter( ConnectivityManager.CONNECTIVITY_ACTION );

    protected ConnectivityAwareIntentService( String name ) {
        super( name );
    }

    protected boolean hasConnectivity() {
        return mHasConnectivity;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mConnManager = (ConnectivityManager) getSystemService( Context.CONNECTIVITY_SERVICE );
        mConnectivityReceiver = new ConnectivityAwareBroadcastReceiver();

        updateNetworkInfo();
        
        registerReceiver( mConnectivityReceiver, mConnectivityReceiverFilter );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver( mConnectivityReceiver );
    }

    private void updateNetworkInfo() {
        NetworkInfo networkInfo = mConnManager.getActiveNetworkInfo();
        mHasConnectivity = (networkInfo != null 
                         && networkInfo.getType() == ConnectivityManager.TYPE_WIFI
                         && networkInfo.isConnected()
                         );
    }

    protected void onConnectivityAvailable() {
    }

    protected void onConnectivityUnavailable() {
    }
}