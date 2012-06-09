package pt.isel.pdm.yamba.model;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class YambaPost implements Parcelable{
    
    public static final String IDENTIFIER = "TWITTER.STATUS_PARCELABLE";    
    private long _id;
    private String _user;
    private Date _date;
    private String _tweet;
    
    public YambaPost( long id, String user, Date date, String tweet){//, URI photoUri, int friendsCount, int followersCount, int postsCount) {
        _id = id;
        _user = user;
        _date = date;
        _tweet = tweet;
    }

    public long getId() {
        return _id;
    }
    public Date getDate() {
        return _date;
    }
    public String getUser() {
        return _user;
    }
    public String getTweet() {
        return _tweet;
    }
    
    // **********************************************
    //  for implementing Parcelable
    // **********************************************
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(_id);
        dest.writeLong(_date.getTime());
        dest.writeString(_tweet);
        dest.writeString(_user); // Twitter.User is not Parcelable
    }
    public static final Parcelable.Creator<YambaPost> CREATOR = new Parcelable.Creator<YambaPost>() {
        public YambaPost createFromParcel(Parcel in) {
            return new YambaPost(in);
        }

        public YambaPost[] newArray(int size) {
            return new YambaPost[size];
        }
    };
    private YambaPost(Parcel in) {
        _id = in.readLong();
        _date = new Date(in.readLong());
        _tweet = in.readString();
        _user = in.readString();
    }
    public String getDataForEmail() {
        return String.format("user: %s\ndate: %s\ntweet: %s\n", getUser(), getDate().toString(), getTweet()).toString();
    }
}
