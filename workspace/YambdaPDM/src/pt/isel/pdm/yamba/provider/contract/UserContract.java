package pt.isel.pdm.yamba.provider.contract;

import pt.isel.pdm.yamba.provider.TwitterProvider;
import android.net.Uri;
import android.provider.BaseColumns;

public class UserContract{

    public static final String TABLE    = "USER";
    public static final Uri CONTENT_URI = 
        Uri.withAppendedPath(TwitterProvider.CONTENT_URI, TABLE);
    public static final String 
    _ID             = BaseColumns._ID,
    USERNAME        = "username",
    FRIENDS_COUNT   = "friends_count",
    FOLLOWERS_COUNT = "followers_count",
    POSTS_COUNT     = "posts_count";
    
}
