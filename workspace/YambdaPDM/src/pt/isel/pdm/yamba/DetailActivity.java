package pt.isel.pdm.yamba;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import pt.isel.pdm.yamba.model.YambaPost;

public class DetailActivity extends Activity {

    private TextView  tweetView;
    private TextView  userView;
    private TextView  dateView;
    private TextView  idView;
    private TextView  friendsView;
    private TextView  followersView;
    private TextView  postsView;
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
        friendsView = (TextView) findViewById( R.id.friendsView );
        followersView = (TextView) findViewById( R.id.followersView );
        postsView = (TextView) findViewById( R.id.postsView );
        userImage = (ImageView) findViewById( R.id.tweetDetailUserImage );

        updateUi( getIntent() );
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

        String username = tStatus.getUser().getUsername();
        int friendsCount = tStatus.getUser().getFriendsCount();
        int followersCount = tStatus.getUser().getFollowersCount();
        int postsCount = tStatus.getUser().getPostsCount();

        DateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm" );

        idView.setText( getString( R.string.detail_message_id, tweetId ) );
        tweetView.setText( tweet );
        userView.setText( username );
        dateView.setText( dateFormat.format( tweetDate ) );
        friendsView.setText( "" + friendsCount );
        followersView.setText( "" + followersCount );
        postsView.setText( "" + postsCount );        
    }
}
