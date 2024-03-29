package pt.isel.pdm.yamba.provider.contract;

import pt.isel.pdm.yamba.provider.TwitterProvider;
import android.net.Uri;
import android.provider.BaseColumns;

public class TweetContract {

    public static final String TABLE    = "TWEET";
    public static final Uri CONTENT_URI = 
        Uri.withAppendedPath(TwitterProvider.CONTENT_URI, TABLE);
    public static final String 
    _ID         = BaseColumns._ID,
    DATE        = "date",
    USER        = "user",
    TWEET       = "tweet_text",
    TIMESTAMP   = "timestamp";
}
