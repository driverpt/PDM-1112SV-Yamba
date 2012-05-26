package pt.isel.pdm.yamba.provider.helper;

import pt.isel.pdm.yamba.provider.contract.TweetContract;
import pt.isel.pdm.yamba.provider.contract.UserContract;
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
        createAllTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropAllTables(db); 
        Log.d(TAG, "onUpdated");
        onCreate(db);       
    }

    //Create methods
    private void createAllTables(SQLiteDatabase db){
        createUsersTable(db);
        createTweetsTable(db);
    }
    private void createTweetsTable(SQLiteDatabase db){
        String columns =       
            TweetContract._ID           + " integer primary key, "
            + TweetContract.DATE        + " datetime not null, "
            + TweetContract.USER        + " text not null, "
            + TweetContract.TWEET       + " text "
            + TweetContract.TIMESTAMP   + " bigint";
        createTable(db,TweetContract.TABLE ,columns);
    }
    private void createUsersTable(SQLiteDatabase db){
        String columns =       
            UserContract._ID              + " integer primary key, "
            + UserContract.USERNAME         + " text not null, "
            + UserContract.FOLLOWERS_COUNT  + " int not null, "
            + UserContract.FRIENDS_COUNT    + " int not null"
            + UserContract.POSTS_COUNT      + " int not null";
        createTable(db,UserContract.TABLE ,columns);
    }
    private void createTable(SQLiteDatabase db, String tableName, String columnsDeclaration){
        String sql = "CREATE TABLE "+ tableName + "( "+ columnsDeclaration + " )";
        db.execSQL(sql);
        Log.d(TAG,"sql= "+sql);
    }

    //Drop methods
    private void dropAllTables(SQLiteDatabase db){
        dropTweetsTable(db);
        dropUsersTable(db);
    }
    private void dropTweetsTable(SQLiteDatabase db){
        dropTable(db, TweetContract.TABLE);
    }
    private void dropUsersTable(SQLiteDatabase db){
        dropTable(db, UserContract.TABLE);
    }
    private void dropTable(SQLiteDatabase db,String tableName){
        db.execSQL("DROP TABLE if exists " + tableName); 
        Log.d(TAG, "onUpdated");
    }



}
