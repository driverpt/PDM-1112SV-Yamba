package pt.isel.pdm.yamba;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public abstract class ConnectivityAwareBroadcastReceiver extends BroadcastReceiver {
    
    private volatile boolean mHasConnectivity = false;
    
    @Override
    public void onReceive( Context context, Intent intent ) {
        ConnectivityManager mConnectivityManager = ( ConnectivityManager ) context.getSystemService( Activity.CONNECTIVITY_SERVICE );
        boolean hasNetwork = !intent.getBooleanExtra( ConnectivityManager.EXTRA_NO_CONNECTIVITY, false );
        boolean newNetworkState = false;
        if ( hasNetwork ) {
            NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
            newNetworkState = onCheckConnectivityChange( networkInfo ); 
        }
        
        if ( newNetworkState != mHasConnectivity ) {
            mHasConnectivity = newNetworkState;
            onPostConnectivityChange();
        }
    }
    
    protected boolean onCheckConnectivityChange( NetworkInfo networkInfo ) {
        return ( networkInfo != null && networkInfo.isConnected() );
    }
    
    public abstract void onPostConnectivityChange();
    
    public final boolean hasConnectivity() {
        return mHasConnectivity;
    }
}