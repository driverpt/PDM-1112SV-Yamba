package pt.isel.pdm.yamba.services;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

public class WifiTimelineAutoUpdaterService extends Service {
	
	private final String LOG_CAT = "WIFI_TIMELINE_SERVICE";
	private static volatile boolean connectionAvailable = false;
	private static volatile boolean wifiAvailable = false;
	
	public static WifiTimelineAutoUpdaterService serviceInstance;

	IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

	public WifiTimelineAutoUpdaterService() {
		super();
	}
	
	BroadcastReceiver rec = new BroadcastReceiver() {
		// Verifies connectivity changes
		// also needs to validate if we have WIFI or only 3G!
		@Override
		public void onReceive(Context context, Intent intent) {
			try
			{
				connectionAvailable = intent.getBooleanExtra(ConnectivityManager.CONNECTIVITY_ACTION,false);
				ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
				wifiAvailable = connManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI && wifiManager.isWifiEnabled();
			}
			catch (Exception ex){
				Log.d(LOG_CAT, ex.getMessage());
			}
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
        Log.d(LOG_CAT, "onCreate()" );
        serviceInstance = this;
		registerReceiver(rec, filter);
	}

	@Override
	public void onDestroy() {
        Log.d(LOG_CAT, "onDestroy()" );
        unregisterReceiver(rec);
		super.onDestroy();
	}

	private void updateTimeline() {
		// TODO Auto-generated method stub
	}
}
