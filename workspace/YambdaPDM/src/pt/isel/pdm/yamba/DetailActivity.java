package pt.isel.pdm.yamba;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import pt.isel.pdm.yamba.model.TwitterStatus;

public class DetailActivity extends Activity {

    private TextView tweetView;
    private TextView userView;
    private TextView dateView;
    private TextView idView;
    
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.detail );
        
        Log.d("PDM", "DetailActivity.onCreate");
        
        idView    = ( TextView ) findViewById( R.id.idTextView );
        tweetView = ( TextView ) findViewById( R.id.tweetTextView );
        userView  = ( TextView ) findViewById( R.id.userTextView );
        dateView  = ( TextView ) findViewById( R.id.dateTextView );
        
        // HotFix for calling onNewIntent
        // @see: http://charlesvilla.com/post/2011/07/30/Android-Top-Tip-2-SingleTop-Launch.aspx
        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent( Intent intent ) {
        setIntent(intent);
        Bundle extras = intent.getExtras();
        
        String tweet   = extras.getString( TwitterStatus.KEY_TWEET );
        Date tweetDate = ( Date ) extras.get( TwitterStatus.KEY_DATE );
        String user    = extras.getString( TwitterStatus.KEY_USER );
        long tweetId    = extras.getLong( TwitterStatus.KEY_ID );
        
        DateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm" );
        
        idView.setText( getString( R.string.detail_message_id, tweetId ) );
        tweetView.setText( tweet );
        userView.setText( user );
        dateView.setText( dateFormat.format( tweetDate ) );
        
    }
}
