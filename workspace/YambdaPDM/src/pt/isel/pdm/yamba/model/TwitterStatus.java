package pt.isel.pdm.yamba.model;


import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class TwitterStatus implements Parcelable{
    
    public static final String IDENTIFIER = "TWITTER.STATUS_PARCELABLE";    
    private long _id;
    private User _user;
    private Date _date;
    private String _tweet;
    
    public TwitterStatus( long id, User user, Date date, String tweet){//, URI photoUri, int friendsCount, int followersCount, int postsCount) {
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
    public User getUser() {
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
        dest.writeString(_date.toString());
        dest.writeString(_tweet);
        dest.writeParcelable(_user, 0);
    }
    public static final Parcelable.Creator<TwitterStatus> CREATOR = new Parcelable.Creator<TwitterStatus>() {
        public TwitterStatus createFromParcel(Parcel in) {
            return new TwitterStatus(in);
        }

        public TwitterStatus[] newArray(int size) {
            return new TwitterStatus[size];
        }
    };
    private TwitterStatus(Parcel in) {
        _id = in.readLong();
        _date = new Date(in.readString());
        _tweet = in.readString();
        _user = in.readParcelable( User.class.getClassLoader());
    }
    public String getDataForEmail() {
        return String.format("user: %s\ndate: %s\ntweet: %s\n", getUser(), getDate().toString(), getTweet()).toString();
    }
}
