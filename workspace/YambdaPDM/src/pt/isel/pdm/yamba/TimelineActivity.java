package pt.isel.pdm.yamba;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import pt.isel.pdm.yamba.model.TwitterStatus;
import pt.isel.pdm.yamba.model.User;
import pt.isel.pdm.yamba.services.TimelineService;
import winterwell.jtwitter.Twitter;

public class TimelineActivity extends PreferencesEnabledActivity implements OnClickListener, OnItemClickListener,
OnYambaTimelineChangeListener {

    private static final String   TERMINATOR_SHORT_TEXT_TERMINATOR = "...";
    private static final int      MAX_CHARS_NO_LIMIT               = 140;
    private static final int      DEFAULT_MAX_TWEETS               = 50;

    private List< TwitterStatus > timeline                         = initTimeline();
    private boolean               isFirstTime                      = true;

    private ListView              view;
    private SimpleAdapter         adapter;
    private Button                refreshButton;

    private static List< TwitterStatus > initTimeline() {
        List< TwitterStatus > l = new ArrayList< TwitterStatus >();
        return l;
    }

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        setContentView( R.layout.timeline );
        super.onCreate( savedInstanceState );
        view = (ListView) findViewById( android.R.id.list );
        
        // registers the listview for the context menu
        registerForContextMenu(view);  
        
        view.setOnItemClickListener( this );

        refreshButton = (Button) findViewById( R.id.refreshButton );
        refreshButton.setOnClickListener( this );

        YambaPDMApplication app = (YambaPDMApplication) getApplication();
        //if ( app.lastRefresh != null && !app.lastRefresh.isEnabled() ) {
         disableRefresh();
        //}

        app.lastRefresh = refreshButton;

        String[] from = { TwitterStatus.KEY_USER, TwitterStatus.KEY_TIMESTAMP, TwitterStatus.KEY_TWEET };
        int[] to = { R.id.user, R.id.date, R.id.tweet};
        adapter = new TweetAdapter( this, timeline, R.layout.timeline_item, from, to );
        view.setAdapter( adapter );

        app.setOnYambaTimelineChangeListener( this );

        if( isFirstTime ) {
            updateTimeline();
            isFirstTime = false;
        }
    }

    private class TweetAdapter extends SimpleAdapter {

        public TweetAdapter( Context context, List< TwitterStatus > data, int resource, String[] from, int[] to ) {
            super( context, data, resource, from, to );
        }

        @Override
        public void setViewText( TextView v, String text ) {
            switch ( v.getId() ) {
            case R.id.tweet: {
                SharedPreferences prefs = ((YambaPDMApplication) getApplication()).getSharedPreferences();
                String strPrefs = prefs.getString( PrefsActivity.KEY_MAX_PRESENTED_CHARS,
                        String.valueOf( MAX_CHARS_NO_LIMIT ) );
                if ( !strPrefs.isEmpty() ) {
                    int max_chars = Integer.parseInt( strPrefs );
                    if ( max_chars < text.length() ) {
                        text = text.substring( 0, max_chars ).concat( TERMINATOR_SHORT_TEXT_TERMINATOR );
                    }
                }
                v.setText( text );
                break;
            }
            case R.id.date: {
                long tweetTimeStamp = Long.parseLong( text );
                String relativeTime = DateUtils.getRelativeTimeSpanString( tweetTimeStamp ).toString();
                v.setText( relativeTime );
                break;
            }
            default: {
                super.setViewText( v, text );
                break;
            }
            }
        }
    }

    private void updateTimeline() {
    	disableRefresh();
        Intent intent = new Intent( this, TimelineService.class );

        intent.putExtra( TimelineService.OPERATION, TimelineService.MSG_UPDATE_TIMELINE );
        startService( intent );
    }

    @Override
    protected void onDestroy() {
        YambaPDMApplication app = (YambaPDMApplication) getApplication();
        app.setOnYambaTimelineChangeListener( null );
        super.onDestroy();
    }

    public void onClick( View v ) {
        updateTimeline();
    }

    private void disableRefresh() {
        refreshButton.setEnabled( false );
    }

    private void enableRefresh() {
        Button refresh = ((YambaPDMApplication) getApplication()).lastRefresh;
        refresh.setEnabled( true );
    }

    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	menu.setHeaderTitle(R.string.timelinectxmenu_title);
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.timeline_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
    	TwitterStatus status = (TwitterStatus)view.getItemAtPosition(info.position);
    	

    	if (item.getItemId() == R.id.timelineemailctxmenu_sendemail)
    	{
    		Intent it = new Intent(Intent.ACTION_SEND);
    		it.setType("message/rfc822"); 
            it.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.timelineitem_emailsubject));
            it.putExtra(Intent.EXTRA_TEXT, status.getDataForEmail());
            startActivity(it);
    	}
    	return true;
    }
    
    public void onItemClick( AdapterView< ? > parent, View view, int position, long id ) {
        Intent intent = new Intent( this, DetailActivity.class );
        intent.addFlags( Intent.FLAG_ACTIVITY_SINGLE_TOP );

        TwitterStatus status = timeline.get(position);
        if (status != null)
        {
            intent.putExtra( TwitterStatus.KEY_TWEET,           status.getTweet() );
            intent.putExtra( TwitterStatus.KEY_DATE,            status.getDate() );
            intent.putExtra( TwitterStatus.KEY_USER,            status.getUser().getUsername() );
            intent.putExtra( TwitterStatus.KEY_ID,              status.getId() );
//            intent.putExtra( TwitterStatus.KEY_PHOTO_URI,       status.getPhotoUri() );
//            intent.putExtra( TwitterStatus.KEY_FOLLOWERS_COUNT, status.getFollowersCount() );
//            intent.putExtra( TwitterStatus.KEY_FRIENDS_COUNT,   status.getFriendsCount() );
//            intent.putExtra( TwitterStatus.KEY_POSTS_COUNT,     status.getPostsCount() );
            startActivity(intent);
        }
    }

    
    public void onYambaTimelineChange() {
        YambaPDMApplication app = (YambaPDMApplication) getApplication();
        List< Twitter.Status > currentTimeline = app.getCurrentTimeline();

        Hashtable<String,Bitmap> bmTable = new Hashtable<String, Bitmap>();

        for ( Twitter.Status status : currentTimeline ) {   
            winterwell.jtwitter.Twitter.User twitterUser = status.getUser();
            User user = new User(
                    twitterUser.getId(), 
                    twitterUser.getName(), 
                    twitterUser.getFriendsCount(),
                    twitterUser.getFollowersCount(),
                    twitterUser.getStatusesCount());
            
            TwitterStatus temp = new TwitterStatus( 
                    status.getId(), 
                    user,
                    status.getCreatedAt(),
                    status.getText() 
//                    status.getUser().getProfileImageUrl(),
//                    status.getUser().getFriendsCount(),
//                    status.getUser().getFollowersCount(),
//                    status.getUser().getStatusesCount()
                    );
            timeline.add(temp);
        }

        adapter.notifyDataSetChanged();
        enableRefresh();
    }
}
