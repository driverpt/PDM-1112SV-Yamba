package pt.isel.pdm.yamba;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import pt.isel.pdm.yamba.services.IUserInfoService;
import pt.isel.pdm.yamba.services.UserInfoService;

public class UserInfoActivity extends Activity implements ServiceConnection {

    private static final String LOGGER_TAG = "UserInfo Activity";

    private IUserInfoService    mRemoteInterface;
    private volatile boolean    mConnected;
    private Intent              intent;

    private TextView            screenNameView;
    private ImageView           screenImageView;
    private TextView            followersCountView;
    private TextView            favouritesCountView;
    private TextView            postCountView;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.userinfo );

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

    private void updateUi() {
        try {
            URI imageURI = URI.create( mRemoteInterface.getProfileImageURI() );
            int favouritesCount = mRemoteInterface.getFavouritesCount();
            int followersCount = mRemoteInterface.getFollowersCount();
            int postCount = mRemoteInterface.getStatusesCount();
            String screenName = mRemoteInterface.getScreenName();

            Bitmap imageBitmap = BitmapFactory.decodeStream( imageURI.toURL().openStream() );
            screenImageView.setImageBitmap( imageBitmap );
            screenNameView.setText( screenName );
            followersCountView.setText( "" + followersCount );
            favouritesCountView.setText( "" + favouritesCount );
            postCountView.setText( "" + postCount );

        } catch ( RemoteException e ) {
            Log.d( LOGGER_TAG, "Remote Exception Ocurred", e );
        } catch ( MalformedURLException e ) {
            Log.d( LOGGER_TAG, "MalformedURL Exception Ocurred", e );
        } catch ( IOException e ) {
            Log.d( LOGGER_TAG, "IO Exception Ocurred", e );
        }
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

    public void onServiceConnected( ComponentName name, IBinder service ) {
        mConnected = true;
        mRemoteInterface = IUserInfoService.Stub.asInterface( service );
        updateUi();
        Log.d( LOGGER_TAG, "Service Connected" );
    }

    public void onServiceDisconnected( ComponentName name ) {
        resetServiceLocalData();
        Log.d( LOGGER_TAG, "Service Hangup" );
    }
}
