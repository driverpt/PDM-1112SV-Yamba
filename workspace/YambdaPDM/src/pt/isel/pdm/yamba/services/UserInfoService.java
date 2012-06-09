package pt.isel.pdm.yamba.services;

import pt.isel.pdm.yamba.YambaPDMApplication;
import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.Twitter_Users;
import winterwell.jtwitter.User;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class UserInfoService extends Service {

    private static final String  LOGGER_TAG = "UserInfo Service";
    
    public static final String USER_SCREEN_NAME = "pt.isel.pdm.yamba.USER_SCREEN_NAME";

    private final UserInfoBinder mBinder    = new UserInfoBinder();
   
    private User user;
    private YambaPDMApplication app;
    
    public class UserInfoBinder extends IUserInfoService.Stub {

        public String getProfileImageURI() throws RemoteException {
            return user.getProfileImageUrl().toString();
        }

        public String getScreenName() throws RemoteException {
            return user.getScreenName();
        }

        public int getStatusesCount() throws RemoteException {
            return user.getStatusesCount();
        }

        public int getFollowersCount() throws RemoteException {
            return user.getFollowersCount();
        }

        public int getFavouritesCount() throws RemoteException {
            return user.getFavoritesCount();
        }
    }
   
    @Override
    public void onCreate() {
        app = ( YambaPDMApplication ) getApplication();
        super.onCreate();
    }
    
    @Override
    public IBinder onBind( Intent arg0 ) {
        Log.d( LOGGER_TAG, "onBind() called" );
        String screenName = arg0.getStringExtra( USER_SCREEN_NAME );
        Log.d( LOGGER_TAG, String.format("Screen Name=%s", screenName ) );
        Twitter twitter = app.getTwitter();
        Twitter_Users users = twitter.users();
        user = users.getUser( screenName );
        return mBinder;
    }
    
    @Override
    public boolean onUnbind( Intent intent ) {
        Log.d( LOGGER_TAG, "onUnbind() called" );
        return super.onUnbind( intent );
    }

}
