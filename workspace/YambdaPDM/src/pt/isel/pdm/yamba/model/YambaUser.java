package pt.isel.pdm.yamba.model;

import android.os.Parcel;
import android.os.Parcelable;

public class YambaUser implements Parcelable {
    public static final String IDENTIFIER = "USER_PARCELABLE";
    private long               _id;
    private String             _username;
    private int                _friendsCount;
    private int                _followersCount;
    private int                _postsCount;

    public YambaUser( long id, String username, int friendsCount, int followersCount, int postsCount ) {
        _username = username;
        _friendsCount = friendsCount;
        _followersCount = followersCount;
        _postsCount = postsCount;
    }

    public long getId() {
        return _id;
    }

    public String getUsername() {
        return _username;
    }

    public int getFriendsCount() {
        return _friendsCount;
    }

    public int getFollowersCount() {
        return _followersCount;
    }

    public int getPostsCount() {
        return _postsCount;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel( Parcel dest, int flags ) {
        dest.writeLong( _id );
        dest.writeString( _username );
        dest.writeInt( _friendsCount );
        dest.writeInt( _followersCount );
        dest.writeInt( _postsCount );
    }

    public static final Parcelable.Creator< YambaUser > CREATOR = new Parcelable.Creator< YambaUser >() {
                                                                    public YambaUser createFromParcel( Parcel in ) {
                                                                        return new YambaUser( in );
                                                                    }

                                                                    public YambaUser[] newArray( int size ) {
                                                                        return new YambaUser[ size ];
                                                                    }
                                                                };

    private YambaUser( Parcel in ) {
        _id = in.readLong();
        _username = in.readString();
        _friendsCount = in.readInt();
        _followersCount = in.readInt();
        _postsCount = in.readInt();
    }

}
