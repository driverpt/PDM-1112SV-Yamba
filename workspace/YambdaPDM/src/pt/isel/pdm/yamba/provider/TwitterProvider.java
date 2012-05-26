package pt.isel.pdm.yamba.provider;

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

    private static final int TWITTER_ALL = 1;
    private static final int TWITTER_ID = 2;
    private static final int USER_ALL = 3;
    private static final int USER_ID = 4;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, "TWEET", TWITTER_ALL);
        uriMatcher.addURI(AUTHORITY, "TWEET/#", TWITTER_ID);
        uriMatcher.addURI(AUTHORITY, "USER", USER_ID);
        uriMatcher.addURI(AUTHORITY, "USER/#", USER_ID);
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
        selection = makeSelection(uri, selection);
        SQLiteDatabase db = thp.getReadableDatabase();
        return db.query(TweetContract.TABLE, projection, selection, selectionArgs, null, null, sortOrder);
    }

    private String makeSelection(Uri uri, String sel) {
        switch (uriMatcher.match(uri)) {
        case TWITTER_ID: return TweetContract._ID + "=" + uri.getLastPathSegment();
        case USER_ID:    return UserContract._ID + "=" + uri.getLastPathSegment();
        default:         return sel;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = thp.getWritableDatabase();
        try {
            long row = db.insert(TweetContract.TABLE, null, values);
            return (row==-1) ? null : ContentUris.withAppendedId(uri, row);
        } finally { db.close(); }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        SQLiteDatabase db = thp.getWritableDatabase();
        try {
            selection = makeSelection(uri, selection);
            return db.update(TweetContract.TABLE, values, selection, selectionArgs);
        } finally { db.close(); }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = thp.getWritableDatabase();
        try {
            selection = makeSelection(uri, selection);
            return db.delete(TweetContract.TABLE, selection, selectionArgs);
        } finally { db.close(); }
    }

    private static final String MIME_BASE_DIR = "vnd.android.cursor.dir/vnd.pt.isel.pdm.yamba";
    private static final String MIME_BASE_ITEM = "vnd.android.cursor.item/vnd.pt.isel.pdm.yamba";

    private static final String MIME_TWEET_ALL = MIME_BASE_DIR +  ".tweet";
    private static final String MIME_TWEET_ONE = MIME_BASE_ITEM + ".tweet";
    private static final String MIME_USER_ALL = MIME_BASE_DIR +  ".user";
    private static final String MIME_USER_ONE = MIME_BASE_ITEM + ".user";

    @Override
    public String getType(Uri uri) {
        int match = uriMatcher.match(uri);
        switch (match)
        {
        case TWITTER_ALL:
            return MIME_TWEET_ALL;
        case TWITTER_ID:
            return MIME_TWEET_ONE;
        case USER_ALL:
            return MIME_USER_ALL;
        case USER_ID:
            return MIME_USER_ONE;
        default:
            return null;
        }
    }
}
