package pt.isel.pdm.yamba.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class TwitterTweetsContract {

    public static final String TABLE    = "Tweet";
    public static final Uri CONTENT_URI = 
        Uri.withAppendedPath(TwitterProvider.CONTENT_URI, TABLE);
    public static final String 
    _ID         = BaseColumns._ID,
    DATE        = "sigla",
    USER        = "name",
    TWEET       = "sem",
    TIMESTAMP   = "timestamp";
}
