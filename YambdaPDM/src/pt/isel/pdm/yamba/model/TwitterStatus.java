package pt.isel.pdm.yamba.model;

import java.net.URI;
import java.util.Date;
import java.util.TreeMap;

import android.net.Uri;

@SuppressWarnings( "serial" )
public class TwitterStatus extends TreeMap< String, Object > {
    public static final String KEY_ID        = "pdm_id";
    public static final String KEY_DATE      = "pdm_date";
    public static final String KEY_TIMESTAMP = "pdm_timestamp";
    public static final String KEY_USER      = "pdm_user";
    public static final String KEY_TWEET     = "pdm_tweet";
    public static final String KEY_PHOTO_URI = "pdm_photoUri";

    public TwitterStatus( long id, String user, Date date, String tweet, URI photoUri) {
        putData(id, user, date, tweet, photoUri);
    }

    private void putData(long id, String user, Date date, String tweet, URI pictureUri) {
        put( KEY_ID, id );
        put( KEY_DATE, date );
        put( KEY_USER, user );
        put( KEY_TWEET, tweet );
        put( KEY_TIMESTAMP, date.getTime());
        if (pictureUri != null)
            put(KEY_PHOTO_URI, pictureUri);
    }

    public long getId() {
        return ( ( Long ) get( KEY_ID ) ).longValue();
    }

    public Date getDate() {
        return ( Date ) get( KEY_DATE );
    }

    public String getUser() {
        return ( String ) get( KEY_USER );
    }

    public String getTweet() {
        return ( String ) get( KEY_TWEET );
    }

    public URI getPhotoUri()
    {
        return (URI)get(KEY_PHOTO_URI);
    }
}
