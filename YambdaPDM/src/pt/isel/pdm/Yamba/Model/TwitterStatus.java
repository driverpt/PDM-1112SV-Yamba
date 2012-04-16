package pt.isel.pdm.Yamba.Model;

import java.util.Date;
import java.util.TreeMap;

@SuppressWarnings( "serial" )
public class TwitterStatus extends TreeMap< String, Object > {
    public static final String KEY_ID        = "pdm_id";
    public static final String KEY_DATE      = "pdm_date";
    public static final String KEY_TIMESTAMP = "pdm_timestamp";
    public static final String KEY_USER      = "pdm_user";
    public static final String KEY_TWEET     = "pdm_tweet";

    public TwitterStatus( long id, String user, Date date, String tweet ) {
        put( KEY_ID, id );
        put( KEY_DATE, date );
        put( KEY_USER, user );
        put( KEY_TWEET, tweet );
        put( KEY_TIMESTAMP, date.getTime() );
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
}
