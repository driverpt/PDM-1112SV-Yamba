package pt.isel.pdm.yamba.dataAccessLayer;

import java.sql.Date;
import java.util.LinkedList;
import java.util.List;

import pt.isel.pdm.yamba.model.TweetToPost;
import pt.isel.pdm.yamba.provider.contract.TweetPostContract;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;

public class TweetToPostDataAccessLayer {

    //GetMethods
    public static List<TweetToPost> getTweetsToPost(ContentProvider provider){
        Cursor cursor = provider.query(
                TweetPostContract.CONTENT_URI,
                getProjection(),
                null,
                null,
                null);
        
        if(cursor != null && cursor.getCount() > 0){
            List<TweetToPost> toReturn = new LinkedList<TweetToPost>();
            while (cursor.moveToNext()) {
                toReturn.add(getTweetToPostFromCursor(cursor));
            }
            return toReturn;
        }
        return null;
    }
    
    //Insert Methods
    public static void insertTweetToPost(ContentProvider provider, TweetToPost tweet){
        provider.insert(TweetPostContract.CONTENT_URI, getContentValuesFromTweetToPost(tweet));
    }
    
    //Aux methods
    private static String[] getProjection() {
        return new String[]{TweetPostContract.DATE,TweetPostContract.TWEET };
    }
    
    public static TweetToPost getTweetToPostFromCursor(Cursor cursor){
        Date date = Date.valueOf(cursor.getString(cursor.getColumnIndex(TweetPostContract.DATE)));
        String text = cursor.getString(cursor.getColumnIndex(TweetPostContract.TWEET));
        return new TweetToPost(date,text);
    }
    public static ContentValues getContentValuesFromTweetToPost(TweetToPost tweet){
        ContentValues cv = new ContentValues();
        cv.put(TweetPostContract.DATE,  tweet.getDate().toLocaleString());
        cv.put(TweetPostContract.TWEET, tweet.getText());
        return cv;
    }
}
