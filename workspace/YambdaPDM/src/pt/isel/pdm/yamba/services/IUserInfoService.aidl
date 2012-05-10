package pt.isel.pdm.yamba.services;

interface IUserInfoService {
    String getProfileImageURI();
    String getScreenName();
    int    getStatusesCount();
    int    getFollowersCount();
    int    getFavouritesCount();
}