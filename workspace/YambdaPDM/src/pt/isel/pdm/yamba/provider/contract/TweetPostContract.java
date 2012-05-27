package pt.isel.pdm.yamba.provider.contract;

import pt.isel.pdm.yamba.provider.TwitterProvider;
import android.net.Uri;
import android.provider.BaseColumns;

public class TweetPostContract {
    
    public static final String TABLE    = "TweetToPost";
    public static final Uri CONTENT_URI = 
        Uri.withAppendedPath(TwitterProvider.CONTENT_URI, TABLE);
    public static final String 
    _ID         = BaseColumns._ID,
    DATE        = "date",
    TWEET       = "tweet_text";
}
