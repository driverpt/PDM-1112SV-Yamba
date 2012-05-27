package pt.isel.pdm.yamba.model;

import java.util.Date;
import java.util.TreeMap;

@SuppressWarnings("serial")
public class TwitterStatus extends TreeMap< String, Object > {

    public static final String KEY_ID              = "pdm_id";
    public static final String KEY_DATE            = "pdm_date";
    public static final String KEY_TIMESTAMP       = "pdm_timestamp";
    public static final String KEY_USER            = "pdm_user";
    public static final String KEY_TWEET           = "pdm_tweet";
    //public static final String KEY_PHOTO_URI       = "pdm_photoUri";
    //public static final String KEY_FRIENDS_COUNT   = "pdm_friendsCount";
    //public static final String KEY_FOLLOWERS_COUNT = "pdm_followersCount";
    //public static final String KEY_POSTS_COUNT     = "pdm_postsCount";

    public TwitterStatus( long id, User user, Date date, String tweet){//, URI photoUri, int friendsCount, int followersCount, int postsCount) {
        putData(id, user, date, tweet);//, photoUri,friendsCount, followersCount, postsCount);
    }

    private void putData(long id, User user, Date date, String tweet){ //URI pictureUri, int friendsCount, int followersCount, int postsCount) {
        put( KEY_ID, id );
        put( KEY_DATE, date );
        put( KEY_USER, user );
        put( KEY_TWEET, tweet );
        put(KEY_TIMESTAMP, date.getTime());
        //if (pictureUri != null)
        //    put(KEY_PHOTO_URI, pictureUri);
        //put( KEY_FRIENDS_COUNT, friendsCount );
        //put( KEY_FOLLOWERS_COUNT, followersCount );
        //put( KEY_POSTS_COUNT, postsCount );

    }

    public long getId() {
        return ( ( Long ) get( KEY_ID ) ).longValue();
    }
    public Date getDate() {
        return ( Date ) get( KEY_DATE );
    }
    public User getUser() {
        return ( User ) get( KEY_USER );
    }
    public String getTweet() {
        return ( String ) get( KEY_TWEET );
    }
//    public URI getPhotoUri(){
//        return (URI)get(KEY_PHOTO_URI);
//    }
//    public int getFriendsCount() {
//        return ( Integer ) get( KEY_FRIENDS_COUNT );
//    }
//    public int getFollowersCount() {
//        return ( Integer ) get( KEY_FOLLOWERS_COUNT );
//    }
//    public int getPostsCount() {
//        return ( Integer ) get( KEY_POSTS_COUNT );
//    }

	public String getDataForEmail() {
		return String.format("user: %s\ndate: %s\ntweet: %s\n", getUser(), getDate().toString(), getTweet()).toString();
	}





}
