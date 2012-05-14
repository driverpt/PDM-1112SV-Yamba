package pt.isel.pdm.yamba;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import pt.isel.pdm.yamba.services.IUserInfoService;
import pt.isel.pdm.yamba.services.UserInfoService;

public class UserInfoActivity extends Activity implements ServiceConnection {

	private static final String LOGGER_TAG = "UserInfo Activity";
	
	private static int favorites;
	private static int followers;
	private static int posts;
	private static String username;
	private static Bitmap picture;
	
	
	private IUserInfoService    mRemoteInterface;
	private volatile boolean    mConnected;
	private Intent              intent;

	private TextView            screenNameView;
	private ImageView           screenImageView;
	private TextView            followersCountView;
	private TextView            favouritesCountView;
	private TextView            postCountView;
	private ProgressDialog      pd;

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.userinfo );
		
		String title = getString(R.string.act_userinfo_pd_title);
		String message = getString(R.string.act_userinfo_pd_text);
		
		pd = ProgressDialog.show(this, title, message, false, true);
		
		//(this, R.string.act_userinfo_pd_title, R.string.act_userinfo_pd_text);

		screenNameView = ( TextView ) findViewById( R.id.textViewScreenName );
		screenImageView = ( ImageView ) findViewById( R.id.imageViewProfilePicture );
		followersCountView = ( TextView ) findViewById( R.id.textViewFollowersCount );
		favouritesCountView = ( TextView ) findViewById( R.id.textViewFavoresCount );
		postCountView = ( TextView ) findViewById( R.id.textViewStatusesCount );
	}

	@Override
	protected void onResume() {
		super.onResume();
		if ( intent == null ) {
			intent = new Intent( this, UserInfoService.class );
		}

		bindService( intent, this, Context.BIND_AUTO_CREATE );
		Log.d( LOGGER_TAG, "onResume() called" );
	}


	private void resetServiceLocalData() {
		mConnected = false;
		mRemoteInterface = null;
	}

	@Override
	protected void onStop() {
		unbindService( this );
		super.onStop();
		Log.d( LOGGER_TAG, "onStop() called" );
	}

	public void onServiceConnected( ComponentName name, final IBinder service ) {
		mConnected = true;

		Thread t = new Thread() {

			public void run() {
				mRemoteInterface = IUserInfoService.Stub.asInterface( service );
				try {
					URI imageURI =  URI.create(mRemoteInterface.getProfileImageURI());
					picture = BitmapFactory.decodeStream( imageURI.toURL().openStream());
					
					favorites = mRemoteInterface.getFavouritesCount();
					followers = mRemoteInterface.getFollowersCount();
					posts = mRemoteInterface.getStatusesCount();
					username = mRemoteInterface.getScreenName();
					
					/*
					//Too much of a hassle like this.... class variables FTW!!
					
					Bundle data = new Bundle();
					
					data.putInt(KEY_FAVORITES, mRemoteInterface.getFavouritesCount());
					data.putInt(KEY_FOLLOWERS, mRemoteInterface.getFollowersCount());
					data.putInt(KEY_POSTS, mRemoteInterface.getStatusesCount());
					
					data.putString(KEY_NAME, mRemoteInterface.getScreenName());
					
					Message msg = new Message();
					
					msg.setData(data);
					handler.sendEmptyMessage(msg);
					*/
					handler.sendEmptyMessage(0);
					
					
				} catch (RemoteException e) {
					Log.d( LOGGER_TAG, "Remote Exception ocurred", e);
				} catch ( MalformedURLException e ) {
					Log.d( LOGGER_TAG, "MalformedURL Exception Ocurred", e );
				} catch ( IOException e ) {
					Log.d( LOGGER_TAG, "IO Exception Ocurred", e );
				}

				Log.d( LOGGER_TAG, "Service Connected" );

			}
		};
		t.start();
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			updateUi();
		}


	};
	
	private void updateUi() {
		screenImageView.setImageBitmap(picture);
		screenNameView.setText(username);
		followersCountView.setText( "" + followers);
		favouritesCountView.setText( "" + favorites);
		postCountView.setText( "" + posts);
		pd.cancel();
	}
	
	private void updateUi(Bitmap imageBitmap, int favCount, int folCount, int posts, String name) {
		screenImageView.setImageBitmap( imageBitmap );
		screenNameView.setText(name);
		followersCountView.setText( "" + folCount);
		favouritesCountView.setText( "" + favCount);
		postCountView.setText( "" + posts);

		// closes the progress dialog
		pd.cancel();

	}



	public void onServiceDisconnected( ComponentName name ) {
		resetServiceLocalData();
		Log.d( LOGGER_TAG, "Service Hangup" );
	}
}
