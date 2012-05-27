package pt.isel.pdm.yamba.dataAccessLayer;

import java.util.LinkedList;
import java.util.List;

import pt.isel.pdm.yamba.model.User;
import pt.isel.pdm.yamba.provider.contract.UserContract;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;

public class UserDataAccessLayer {
  
    //Get Mehtods
    public static User getUser(ContentProvider provider, String userName){
        Cursor cursor = provider.query(
                UserContract.CONTENT_URI,
                new String[]{UserContract._ID,UserContract.USERNAME,UserContract.FOLLOWERS_COUNT,UserContract.FRIENDS_COUNT,UserContract.POSTS_COUNT},
                String.format("%s = ?", UserContract.USERNAME),
                new String[]{userName},null);
        
        if(cursor != null && cursor.getCount() == 1){
            cursor.moveToNext();
            return getUserFromCursor(cursor);
        }
        return null;
    }
    public static List<User> getAllUsers(ContentProvider provider){
        Cursor cursor = provider.query(
                UserContract.CONTENT_URI,
                new String[]{UserContract._ID,UserContract.USERNAME,UserContract.FOLLOWERS_COUNT,UserContract.FRIENDS_COUNT,UserContract.POSTS_COUNT},
                null,
                null,
                null);
        if(cursor != null && cursor.getCount() > 0){
            List<User> toReturn = new LinkedList<User>();
            while (cursor.moveToNext()) {
                toReturn.add(getUserFromCursor(cursor));
            }
        }
        return null;
    }
    public static void getPhoto(ContentProvider provider, String userName){
        
    }
    
    //Insert Methods
    public static void insertUser(ContentProvider provider, User user){
        provider.insert(UserContract.CONTENT_URI, getContentValuesFromUser(user));
    }
    private static void storePhoto(ContentProvider provider){
        
    }
    
    //User aux methods
    public static User getUserFromCursor(Cursor cursor){
        long id = cursor.getLong(cursor.getColumnIndex(UserContract._ID));
        String username = cursor.getString(cursor.getColumnIndex(UserContract.USERNAME));
        int friendsCount = cursor.getInt(cursor.getColumnIndex(UserContract.FRIENDS_COUNT));
        int followersCount = cursor.getInt(cursor.getColumnIndex(UserContract.FOLLOWERS_COUNT));
        int postsCount = cursor.getInt(cursor.getColumnIndex(UserContract.POSTS_COUNT));
        
        return new User(id,username,friendsCount, followersCount,postsCount);
    }
    public static ContentValues getContentValuesFromUser(User user){
        ContentValues cv = new ContentValues();
        cv.put(UserContract._ID,             user.getId());
        cv.put(UserContract.USERNAME,        user.getUsername());
        cv.put(UserContract.FOLLOWERS_COUNT, user.getFollowersCount());
        cv.put(UserContract.FRIENDS_COUNT,   user.getFriendsCount());
        cv.put(UserContract.POSTS_COUNT,     user.getPostsCount());
        return cv;
    }
}
