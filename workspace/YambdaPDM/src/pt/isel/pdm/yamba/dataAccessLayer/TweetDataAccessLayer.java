package pt.isel.pdm.yamba.dataAccessLayer;

import java.sql.Date;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import pt.isel.pdm.yamba.model.YambaPost;
import pt.isel.pdm.yamba.provider.contract.TweetContract;
import winterwell.jtwitter.Status;

public class TweetDataAccessLayer {
    
    //Get Mehtods
    public static YambaPost getTweet(ContentResolver provider, long id){
        Cursor cursor = provider.query(
                TweetContract.CONTENT_URI,
                getProjection(),
                String.format("%s = ?", TweetContract._ID),
                new String[]{id+""},null);
        
        if(cursor != null && cursor.getCount() == 1){
            cursor.moveToNext();
            YambaPost post = getTwitterStatusFromCursor(provider,cursor);
            cursor.close();
            return post;
        }
        return null;
    }
    public static List<YambaPost> getTweetsFrom(ContentResolver provider, long timestamp, int count){
        Cursor cursor = provider.query( TweetContract.CONTENT_URI
                                      , getProjection()
                                      , String.format("%s > ?", TweetContract.TIMESTAMP)
                                      , new String[]{ timestamp + "" }
                                      , "timestamp DESC"
                                      );
        
        if( cursor != null && cursor.getCount() > 0 ){
            List<YambaPost> toReturn = new LinkedList<YambaPost>();
            while (cursor.moveToNext()) {
                toReturn.add(getTwitterStatusFromCursor(provider,cursor));
            }
            cursor.close();
            return toReturn;
        }
        return null;
    }
    
    public static List<YambaPost> getLastestTweets(ContentResolver resolver, int count){
        String whereClause = "";
        if( count < 0 ) {
            whereClause = String.format("%s DESC", TweetContract.TIMESTAMP);
        }
        whereClause = String.format("%s DESC limit %d", TweetContract.TIMESTAMP, count);
        Cursor cursor = null;
        List<YambaPost> toReturn = new LinkedList<YambaPost>();
        try {
            cursor = resolver.query( TweetContract.CONTENT_URI
                                   , getProjection()
                                   , null
                                   , null
                                   , whereClause
                                   );
            if( cursor != null && cursor.getCount() > 0 ){
                while ( cursor.moveToNext() ) {
                    toReturn.add(getTwitterStatusFromCursor(resolver,cursor));
                }  
            }
        } finally {
            if( cursor != null ) {
                cursor.close();
            }
        }
        return toReturn;
    }

    public static YambaPost getLastestTweet(ContentResolver resolver){
        return getLastestTweets( resolver, 1 ).get( 0 );
    }
    
    public static List<YambaPost> getAllTweets(ContentResolver provider){
        Cursor cursor = provider.query(
                TweetContract.CONTENT_URI,
                getProjection(),
                null,
                null,
                null);
        
        if(cursor != null && cursor.getCount() > 0){
            List<YambaPost> toReturn = new LinkedList<YambaPost>();
            while (cursor.moveToNext()) {
                toReturn.add(getTwitterStatusFromCursor(provider,cursor));
            }
            cursor.close();
            return toReturn;
        }
        return null;
    }
    
    //Insert Methods
    public static void insertTweet(ContentResolver resolver, Status tweet){
        resolver.insert(TweetContract.CONTENT_URI, getContentValuesFromTwitterStatus(tweet));
    }
    //TwitterStatus aux methods
    private static String[] getProjection(){
        return new String[]{
                TweetContract._ID,
                TweetContract.USER,
                TweetContract.TWEET,
                TweetContract.DATE,
                TweetContract.TIMESTAMP
        };
    }
    private static YambaPost getTwitterStatusFromCursor(ContentResolver resolver,Cursor cursor){
        long id = cursor.getLong(cursor.getColumnIndex(TweetContract._ID));
        String username = cursor.getString(cursor.getColumnIndex(TweetContract.USER));
        String tweet = cursor.getString(cursor.getColumnIndex(TweetContract.TWEET));
        Date date = new Date( cursor.getLong(cursor.getColumnIndex(TweetContract.TIMESTAMP)));
        
        return new YambaPost(id, username, date, tweet);
    }
    private static ContentValues getContentValuesFromTwitterStatus(Status tweet){
        ContentValues cv = new ContentValues();
        cv.put(TweetContract._ID,   tweet.getId().longValue() );
        cv.put(TweetContract.USER,  tweet.getUser().getName());
        cv.put(TweetContract.TWEET, tweet.getText());
        cv.put(TweetContract.DATE,  tweet.createdAt.toLocaleString());
        cv.put(TweetContract.TIMESTAMP, tweet.createdAt.getTime() );
        return cv;
    }
}
