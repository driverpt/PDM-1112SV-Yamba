package pt.isel.pdm.yamba.model;

public class User {

    private long _id;
    private String _username;
    private int _friendsCount;
    private int _followersCount;
    private int _postsCount;
    
    public User(long id, String username, int friendsCount, int followersCount, int postsCount){
        _username = username;
        _friendsCount = friendsCount;
        _followersCount = followersCount;
        _postsCount = postsCount;
    }
    
    public long getId(){
        return _id;
    }
    public String getUsername(){
        return _username;
    }
    public int getFriendsCount(){
        return _friendsCount;
    }
    public int getFollowersCount(){
        return _followersCount;
    }
    public int getPostsCount(){
        return _postsCount;
    }    
    
}
