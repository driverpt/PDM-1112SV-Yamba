package pt.isel.pdm.yamba;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import pt.isel.pdm.yamba.services.UserInfoService;

import pt.isel.pdm.yamba.model.YambaPost;

public class DetailActivity extends Activity implements OnClickListener {

    private TextView  tweetView;
    private TextView  userView;
    private TextView  dateView;
    private TextView  idView;

    private ImageView userImage;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.detail );

        Log.d( "PDM", "DetailActivity.onCreate" );

        idView = (TextView) findViewById( R.id.idTextView );
        tweetView = (TextView) findViewById( R.id.tweetTextView );
        userView = (TextView) findViewById( R.id.userTextView );
        dateView = (TextView) findViewById( R.id.dateTextView );

        userImage = (ImageView) findViewById( R.id.tweetDetailUserImage );

        updateUi( getIntent() );
        
        userImage.setOnClickListener( this );
    }

    @Override
    protected void onNewIntent( Intent intent ) {
        updateUi( intent );
    }
    
    private void updateUi( Intent intent ) {
        Bundle extras = intent.getExtras();

        // User user = extras.getParcelable(User.IDENTIFIER);
        YambaPost tStatus = extras.getParcelable( YambaPost.IDENTIFIER );

        long tweetId = tStatus.getId();
        String tweet = tStatus.getTweet();
        Date tweetDate = tStatus.getDate();

        String username = tStatus.getUser();

        DateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm" );

        idView.setText( getString( R.string.detail_message_id, tweetId ) );
        tweetView.setText( tweet );
        userView.setText( username );
        dateView.setText( dateFormat.format( tweetDate ) );
       
    }

    @Override
    public void onClick( View v ) {
        String screenName = userView.getText().toString();
        Intent intent = new Intent( this, UserInfoActivity.class );
        intent.putExtra( UserInfoService.USER_SCREEN_NAME, screenName );
        startActivity( intent );
    }
}
