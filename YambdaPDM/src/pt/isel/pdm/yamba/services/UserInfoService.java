package pt.isel.pdm.yamba.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import pt.isel.pdm.yamba.YambaPDMApplication;
import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.Twitter.User;

public class UserInfoService extends Service {

    private static final String  LOGGER_TAG = "UserInfo Service";

    private final UserInfoBinder mBinder    = new UserInfoBinder();

    public class UserInfoBinder extends IUserInfoService.Stub {

        public String getProfileImageURI() throws RemoteException {
            return getUserInfo().getProfileImageUrl().toString();
        }

        public String getScreenName() throws RemoteException {
            return getUserInfo().getScreenName();
        }

        public int getStatusesCount() throws RemoteException {
            return getUserInfo().getStatusesCount();
        }

        public int getFollowersCount() throws RemoteException {
            return getUserInfo().getFollowersCount();
        }

        public int getFavouritesCount() throws RemoteException {
            return getUserInfo().getFavoritesCount();
        }
    }

    @Override
    public IBinder onBind( Intent arg0 ) {
        Log.d( LOGGER_TAG, "onBind() called" );
        return mBinder;
    }

    public User getUserInfo() {
        YambaPDMApplication app = ( YambaPDMApplication ) getApplication();
        Twitter twitter = app.getTwitter();
        String screenName = twitter.getScreenName();
        User user = twitter.getUser( screenName );
        return user;
    }

    @Override
    public boolean onUnbind( Intent intent ) {
        Log.d( LOGGER_TAG, "onUnbind() called" );
        return super.onUnbind( intent );
    }

}
