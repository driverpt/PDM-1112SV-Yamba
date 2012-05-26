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
    
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, "UC", TWITTER_ALL);
        uriMatcher.addURI(AUTHORITY, "UC/#", TWITTER_ID);
    }
    @Override
    public boolean onCreate() {
        Log.v("PDM","LeicProvider: onCreate()");
        thp = new TwitterHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        selection = makeSelection(uri, selection);
        SQLiteDatabase db = thp.getReadableDatabase();
        return db.query(TwitterTweetsContract.TABLE, projection, selection, selectionArgs, null, null, sortOrder);
    }

    private String makeSelection(Uri uri, String sel) {
        switch (uriMatcher.match(uri)) {
        case TWITTER_ID:    return TwitterTweetsContract._ID + "=" + uri.getLastPathSegment();
        default:            return sel;
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = thp.getWritableDatabase();
        try {
            selection = makeSelection(uri, selection);
            return db.delete(TwitterTweetsContract.TABLE, selection, selectionArgs);
        } finally { db.close(); }
    }

    private static final String MIME_TWEETS_ALL = "vnd.android.cursor.dir/vnd.isel.pdm.Tweet";
    private static final String MIME_TWEETS_ONE = "vnd.android.cursor.item/vnd.isel.pdm.Tweet";
    @Override
    public String getType(Uri uri) {
        return uriMatcher.match(uri)==TWITTER_ALL ? MIME_TWEETS_ALL : MIME_TWEETS_ONE;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = thp.getWritableDatabase();
        try {
            long row = db.insert(TwitterTweetsContract.TABLE, null, values);
            return (row==-1) ? null : ContentUris.withAppendedId(uri, row);
        } finally { db.close(); }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        SQLiteDatabase db = thp.getWritableDatabase();
        try {
            selection = makeSelection(uri, selection);
            return db.update(TwitterTweetsContract.TABLE, values, selection, selectionArgs);
        } finally { db.close(); }
    }

}
