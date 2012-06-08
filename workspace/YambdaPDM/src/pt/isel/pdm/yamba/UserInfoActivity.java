package pt.isel.pdm.yamba;

import java.net.URI;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import pt.isel.pdm.yamba.services.IUserInfoService;
import pt.isel.pdm.yamba.services.UserInfoService;

public class UserInfoActivity extends Activity implements ServiceConnection {

    private static final String LOGGER_TAG = "UserInfo Activity";

    // private static int favorites;
    // private static int followers;
    // private static int posts;
    // private static String username;
    // private static Bitmap picture;

    private IUserInfoService    mRemoteInterface;
    private volatile boolean  mConnected;
    private Intent              intent;

    private TextView            screenNameView;
    private ImageView           screenImageView;
    private TextView            followersCountView;
    private TextView            favouritesCountView;
    private TextView            postCountView;
    private ProgressDialog      pd;

    private String              screenName;
    
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.userinfo );

        String title = getString( R.string.act_userinfo_pd_title );
        String message = getString( R.string.act_userinfo_pd_text );

        pd = ProgressDialog.show( this, title, message, false, true );

        // (this, R.string.act_userinfo_pd_title,
        // R.string.act_userinfo_pd_text);

        screenNameView = (TextView) findViewById( R.id.textViewScreenName );
        screenImageView = (ImageView) findViewById( R.id.imageViewProfilePicture );
        followersCountView = (TextView) findViewById( R.id.textViewFollowersCount );
        favouritesCountView = (TextView) findViewById( R.id.textViewFavoresCount );
        postCountView = (TextView) findViewById( R.id.textViewStatusesCount );
        
        screenName = getIntent().getStringExtra( UserInfoService.USER_SCREEN_NAME );        
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ( intent == null ) {
            intent = new Intent( this, UserInfoService.class );
            intent.putExtra( UserInfoService.USER_SCREEN_NAME, screenName );
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

    private class UpdateUserInfoTask extends AsyncTask< IUserInfoService, Void, Void > {

        private int       favourites;
        private int       followers;
        private int       posts;
        private String    username;
        private Bitmap    picture;

        private Exception exception;

        @Override
        protected Void doInBackground( IUserInfoService... arg0 ) {
            IUserInfoService service = arg0[0];
            try {
                URI imageURI = URI.create( mRemoteInterface.getProfileImageURI() );
                picture = BitmapFactory.decodeStream( imageURI.toURL().openStream() );

                favourites = mRemoteInterface.getFavouritesCount();
                followers = mRemoteInterface.getFollowersCount();
                posts = mRemoteInterface.getStatusesCount();
                username = mRemoteInterface.getScreenName();
            } catch ( Exception e ) {
                exception = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute( Void result ) {
            if ( exception != null ) {
                Toast.makeText( UserInfoActivity.this,
                        String.format( "Exception ocurred: %s", exception.getMessage() ), Toast.LENGTH_LONG ).show();
                Log.w( LOGGER_TAG, "Exception occurred on UpdateUserInfo", exception );
                return;
            }
            updateUi( picture, favourites, followers, posts, username );
            pd.cancel();
        }
    }

    public void onServiceConnected( ComponentName name, final IBinder service ) {
        mConnected = true;
        mRemoteInterface = IUserInfoService.Stub.asInterface( service );

        UpdateUserInfoTask task = new UpdateUserInfoTask();
        task.execute( mRemoteInterface );
    }

    private void updateUi( Bitmap imageBitmap, int favCount, int folCount, int posts, String name ) {
        screenImageView.setImageBitmap( imageBitmap );
        screenNameView.setText( name );
        followersCountView.setText( "" + folCount );
        favouritesCountView.setText( "" + favCount );
        postCountView.setText( "" + posts );
    }

    public void onServiceDisconnected( ComponentName name ) {
        resetServiceLocalData();
        Log.d( LOGGER_TAG, "Service Hangup" );
    }
}
