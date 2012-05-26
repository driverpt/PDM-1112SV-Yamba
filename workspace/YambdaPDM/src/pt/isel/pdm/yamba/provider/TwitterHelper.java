package pt.isel.pdm.yamba.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TwitterHelper extends SQLiteOpenHelper{

    static final String TAG = "PDM";
    static final String DB_NAME = "twitter";
    static final int DB_VERSION = 1;

    public TwitterHelper(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String columns =       
            TwitterTweetsContract._ID           + " integer primary key, "
            + TwitterTweetsContract.DATE        + " datetime not null, "
            + TwitterTweetsContract.USER        + " text not null, "
            + TwitterTweetsContract.TWEET       + " text "
            + TwitterTweetsContract.TIMESTAMP   + " bigint";
        String sql = "CREATE TABLE "+ TwitterTweetsContract.TABLE + "( "+ columns + " )";
        db.execSQL(sql);
        Log.d(TAG,"sql= "+sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE if exists " + TwitterTweetsContract.TABLE); 
        Log.d(TAG, "onUpdated");
        onCreate(db);       
    }

}
