package pt.isel.pdm.yamba.provider;

import pt.isel.pdm.yamba.provider.contract.TweetContract;
import pt.isel.pdm.yamba.provider.contract.TweetPostContract;
import pt.isel.pdm.yamba.provider.contract.UserContract;
import pt.isel.pdm.yamba.provider.helper.TwitterHelper;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class TwitterProvider  extends ContentProvider{
    private TwitterHelper thp;


    public static final String AUTHORITY = "pt.isel.pdm.yamba.providers";
    public static final Uri CONTENT_URI = 
        Uri.parse(ContentResolver.SCHEME_CONTENT+"://"+AUTHORITY);

    private static final int TWEET_ALL = 1;
    private static final int TWEET_ID  = 2;
    private static final int USER_ALL  = 3;
    private static final int USER_ID   = 4;
    private static final int TWEET_TO_POST_ALL = 5;
    private static final int TWEET_TO_POST_ID = 6;
    
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, "TWEET", TWEET_ALL);
        uriMatcher.addURI(AUTHORITY, "TWEET/#", TWEET_ID);
        uriMatcher.addURI(AUTHORITY, "USER", USER_ID);
        uriMatcher.addURI(AUTHORITY, "USER/#", USER_ID);
        uriMatcher.addURI(AUTHORITY, "TWEET_TO_POST", TWEET_TO_POST_ALL);
        uriMatcher.addURI(AUTHORITY, "TWEET_TO_POST/#", TWEET_TO_POST_ID);
    }
    @Override
    public boolean onCreate() {
        Log.v("PDM","TwitterProvider: onCreate()");
        thp = new TwitterHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        SelectionParameters args = makeSelection(uri, selection);
        SQLiteDatabase db = thp.getReadableDatabase();
        return db.query(args.getTable(), projection, args.getSelection(), selectionArgs, null, null, sortOrder);
    }
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = thp.getWritableDatabase();
        try {
            String table = getTable(uri);
            long row = db.insert(table, null, values);
            return (row==-1) ? null : ContentUris.withAppendedId(uri, row);
        } finally { db.close(); }
    }
    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        SQLiteDatabase db = thp.getWritableDatabase();
        try {
            SelectionParameters args = makeSelection(uri, selection);
            return db.update(args.getTable(), values, args.getSelection(), selectionArgs);
        } finally { db.close(); }
    }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = thp.getWritableDatabase();
        try {
            SelectionParameters args = makeSelection(uri, selection);
            return db.delete(args.getTable(), args.getSelection(), selectionArgs);
        } finally { db.close(); }
    }

    private class SelectionParameters{
        public String _table;
        public String _selection;

        public SelectionParameters(String table, String selection){
            _table = table;
            _selection = selection;
        }

        public String getSelection(){ return _selection; }
        public String getTable(){ return _table; }
    }
    private SelectionParameters makeSelection(Uri uri, String sel) {
        switch (uriMatcher.match(uri)) {
        case TWEET_ALL: 
            return new SelectionParameters(TweetContract.TABLE,sel);
        case TWEET_ID: 
            return new SelectionParameters(TweetContract.TABLE,TweetContract._ID + "=" + uri.getLastPathSegment());
        case USER_ALL: 
            return new SelectionParameters(UserContract.TABLE,sel);
        case USER_ID:    
            return new SelectionParameters(UserContract.TABLE,UserContract._ID + "=" + uri.getLastPathSegment());
        case TWEET_TO_POST_ALL: 
            return new SelectionParameters(TweetPostContract.TABLE,sel);
        case TWEET_TO_POST_ID:    
            return new SelectionParameters(TweetPostContract.TABLE,TweetPostContract._ID + "=" + uri.getLastPathSegment());
        default:  
            return null;
        }
    }
    private String getTable(Uri uri){
        switch (uriMatcher.match(uri)) {
        case TWEET_ALL: 
        case TWEET_ID: 
            return TweetContract.TABLE;
        case USER_ALL: 
        case USER_ID:    
            return UserContract.TABLE;
        case TWEET_TO_POST_ALL: 
        case TWEET_TO_POST_ID:    
            return TweetPostContract.TABLE;
        default:  
            return null;
        }
    }
    
    private static final String MIME_BASE_DIR = "vnd.android.cursor.dir/vnd.pt.isel.pdm.yamba";
    private static final String MIME_BASE_ITEM = "vnd.android.cursor.item/vnd.pt.isel.pdm.yamba";
    private static final String MIME_TWEET_ALL = MIME_BASE_DIR +  ".tweet";
    private static final String MIME_TWEET_ONE = MIME_BASE_ITEM + ".tweet";
    private static final String MIME_USER_ALL = MIME_BASE_DIR +  ".user";
    private static final String MIME_USER_ONE = MIME_BASE_ITEM + ".user";
    private static final String MIME_TWEET_TO_POST_ALL = MIME_BASE_DIR +  ".tweetToPost";
    private static final String MIME_TWEET_TO_POST_ONE = MIME_BASE_ITEM + ".tweetToPost";
    @Override
    public String getType(Uri uri) {
        int match = uriMatcher.match(uri);
        switch (match)
        {
        case TWEET_ALL:
            return MIME_TWEET_ALL;
        case TWEET_ID:
            return MIME_TWEET_ONE;
        case USER_ALL:
            return MIME_USER_ALL;
        case USER_ID:
            return MIME_USER_ONE;
        case TWEET_TO_POST_ALL:
            return MIME_TWEET_TO_POST_ALL;
        case TWEET_TO_POST_ID:
            return MIME_TWEET_TO_POST_ONE;
        default:
            return null;
        }
    }
}
