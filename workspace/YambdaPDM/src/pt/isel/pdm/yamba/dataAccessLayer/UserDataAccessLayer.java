package pt.isel.pdm.yamba.dataAccessLayer;

import java.util.LinkedList;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;

import pt.isel.pdm.yamba.model.YambaUser;
import pt.isel.pdm.yamba.provider.contract.UserContract;

public class UserDataAccessLayer {

    public static YambaUser getUser( ContentProvider provider, String userName ) {
        Cursor cursor = provider.query( UserContract.CONTENT_URI
                                      , new String[] { UserContract._ID
                                                     , UserContract.USERNAME
                                                     , UserContract.FOLLOWERS_COUNT
                                                     , UserContract.FRIENDS_COUNT
                                                     , UserContract.POSTS_COUNT 
                                                     }
                                      , String.format( "%s = ?", UserContract.USERNAME )
                                      , new String[] { userName }
                                      , null
                );

        if ( cursor != null && cursor.getCount() == 1 ) {
            cursor.moveToNext();
            YambaUser user = getUserFromCursor( cursor );
            cursor.close();
            return user;
        }
        return null;
    }

    public static List< YambaUser > getAllUsers( ContentProvider provider ) {
        Cursor cursor = provider.query( UserContract.CONTENT_URI
                                      , new String[] { UserContract._ID
                                                     , UserContract.USERNAME
                                                     , UserContract.FOLLOWERS_COUNT
                                                     , UserContract.FRIENDS_COUNT
                                                     , UserContract.POSTS_COUNT 
                                                     }
                                      , null
                                      , null
                                      , null 
                                      );
        if ( cursor != null && cursor.getCount() > 0 ) {
            List< YambaUser > toReturn = new LinkedList< YambaUser >();
            while ( cursor.moveToNext() ) {
                toReturn.add( getUserFromCursor( cursor ) );
            }
            cursor.close();
        }
        return null;
    }

    public static void getPhoto( ContentProvider provider, String userName ) {

    }

    // Insert Methods
    public static void insertUser( ContentProvider provider, YambaUser user ) {
        provider.insert( UserContract.CONTENT_URI, getContentValuesFromUser( user ) );
    }

    private static void storePhoto( ContentProvider provider ) {

    }

    // User aux methods
    public static YambaUser getUserFromCursor( Cursor cursor ) {
        long id            = cursor.getLong  ( cursor.getColumnIndex( UserContract._ID ) );
        String username    = cursor.getString( cursor.getColumnIndex( UserContract.USERNAME ) );
        int friendsCount   = cursor.getInt   ( cursor.getColumnIndex( UserContract.FRIENDS_COUNT ) );
        int followersCount = cursor.getInt   ( cursor.getColumnIndex( UserContract.FOLLOWERS_COUNT ) );
        int postsCount     = cursor.getInt   ( cursor.getColumnIndex( UserContract.POSTS_COUNT ) );

        return new YambaUser( id, username, friendsCount, followersCount, postsCount );
    }

    public static ContentValues getContentValuesFromUser( YambaUser user ) {
        ContentValues cv = new ContentValues();
        cv.put( UserContract._ID, user.getId() );
        cv.put( UserContract.USERNAME, user.getUsername() );
        cv.put( UserContract.FOLLOWERS_COUNT, user.getFollowersCount() );
        cv.put( UserContract.FRIENDS_COUNT, user.getFriendsCount() );
        cv.put( UserContract.POSTS_COUNT, user.getPostsCount() );
        return cv;
    }
}
