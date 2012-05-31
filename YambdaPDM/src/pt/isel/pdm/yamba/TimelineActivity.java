package pt.isel.pdm.yamba;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import pt.isel.pdm.yamba.model.YambaPost;
import pt.isel.pdm.yamba.model.YambaUser;
import pt.isel.pdm.yamba.services.TimelineService;
import winterwell.jtwitter.Twitter;

public class TimelineActivity extends PreferencesEnabledActivity implements OnClickListener, OnItemClickListener,
        OnYambaTimelineChangeListener {

    public static final String  YAMBA_TIMELINE_UPDATED = "YAMBA TIMELINE UPDATED";
    
    private static final String       TERMINATOR_SHORT_TEXT_TERMINATOR = "...";
    private static final int          MAX_CHARS_NO_LIMIT               = 140;
    private static final int          DEFAULT_MAX_TWEETS               = 50;

    private ArrayList< YambaPost >    timeline                         = initTimeline();
    private boolean                   isFirstTime                      = true;

    private ListView                  view;
    private ArrayAdapter< YambaPost > adapter;
    private Button                    refreshButton;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {
            
        }
    };
    
    private IntentFilter mReceiverFilter = new IntentFilter( YAMBA_TIMELINE_UPDATED );

    
    
    private static ArrayList< YambaPost > initTimeline() {
        ArrayList< YambaPost > l = new ArrayList< YambaPost >();
        return l;
    }

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        setContentView( R.layout.timeline );
        super.onCreate( savedInstanceState );
        view = (ListView) findViewById( android.R.id.list );

        // registers the listview for the context menu
        registerForContextMenu( view );

        view.setOnItemClickListener( this );

        refreshButton = (Button) findViewById( R.id.refreshButton );
        refreshButton.setOnClickListener( this );

        YambaPDMApplication app = (YambaPDMApplication) getApplication();
        // if ( app.lastRefresh != null && !app.lastRefresh.isEnabled() ) {
        disableRefresh();
        // }

        app.lastRefresh = refreshButton;

        adapter = new TweetAdapter( this, R.layout.timeline_item, timeline );
        view.setAdapter( adapter );
        
        app.setOnYambaTimelineChangeListener( this );

        if ( isFirstTime ) {
            updateTimeline();
            isFirstTime = false;
        }
    }
    
    @Override
    public void onStart() {
        super.onStart();
        registerReceiver( mReceiver, mReceiverFilter );
    }

    private class TweetAdapter extends ArrayAdapter< YambaPost > {
        private ArrayList< YambaPost > entries;
        private Activity               activity;

        public TweetAdapter( Activity a, int textViewResourceId, ArrayList< YambaPost > entries ) {
            super( a, textViewResourceId, entries );
            this.entries = entries;
            this.activity = a;
        }

        class ViewHolder {
            public ImageView photo;
            public TextView  user;
            public TextView  date;
            public TextView  tweet;
        }

        @Override
        public View getView( int position, View convertView, ViewGroup parent ) {
            View v = convertView;
            ViewHolder holder;
            if ( v == null ) {
                LayoutInflater vi = (LayoutInflater) activity.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                v = vi.inflate( R.layout.timeline_item, null );
                holder = new ViewHolder();
                holder.photo = (ImageView) v.findViewById( R.id.photoUri );
                holder.user = (TextView) v.findViewById( R.id.user );
                holder.date = (TextView) v.findViewById( R.id.date );
                holder.tweet = (TextView) v.findViewById( R.id.tweet );
                v.setTag( holder );
            } else
                holder = (ViewHolder) v.getTag();

            final YambaPost twitterStatus = entries.get( position );
            if ( twitterStatus != null ) {
                // holder.photo.setImageBitmap(bm);
                holder.user.setText( twitterStatus.getUser().getUsername() );
                holder.date.setText( DateUtils.getRelativeTimeSpanString( twitterStatus.getDate().getTime() )
                        .toString() );
                holder.tweet.setText( twitterStatus.getTweet() );
            }
            return v;
        }
    }

    private void updateTimeline() {
        disableRefresh();
        Intent intent = new Intent( this, TimelineService.class );

        intent.putExtra( TimelineService.OPERATION, TimelineService.MSG_UPDATE_TIMELINE );
        startService( intent );
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver( mReceiver );
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
    public void onCreateContextMenu( ContextMenu menu, View v, ContextMenuInfo menuInfo ) {
        menu.setHeaderTitle( R.string.timelinectxmenu_title );
        super.onCreateContextMenu( menu, v, menuInfo );
        MenuInflater inflater = getMenuInflater();
        inflater.inflate( R.menu.timeline_context_menu, menu );
    }

    @Override
    public boolean onContextItemSelected( MenuItem item ) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        YambaPost status = (YambaPost) view.getItemAtPosition( info.position );

        if ( item.getItemId() == R.id.timelineemailctxmenu_sendemail ) {
            Intent it = new Intent( Intent.ACTION_SEND );
            it.setType( "message/rfc822" );
            it.putExtra( Intent.EXTRA_SUBJECT, getString( R.string.timelineitem_emailsubject ) );
            it.putExtra( Intent.EXTRA_TEXT, status.getDataForEmail() );
            startActivity( it );
        }
        return true;
    }

    public void onItemClick( AdapterView< ? > parent, View view, int position, long id ) {
        Intent intent = new Intent( this, DetailActivity.class );
        intent.addFlags( Intent.FLAG_ACTIVITY_SINGLE_TOP );

        YambaPost status = timeline.get( position );
        if ( status != null ) {
            intent.putExtra( YambaPost.IDENTIFIER, status );
            startActivity( intent );
        }
    }

    public void onYambaTimelineChange() {
        YambaPDMApplication app = (YambaPDMApplication) getApplication();
        List< Twitter.Status > currentTimeline = app.getCurrentTimeline();

        for ( Twitter.Status status : currentTimeline ) {
            Twitter.User twitterUser = status.getUser();

            YambaUser user = new YambaUser( twitterUser.getId(), twitterUser.getName(), twitterUser.getFriendsCount(),
                    twitterUser.getFollowersCount(), twitterUser.getStatusesCount() );

            YambaPost temp = new YambaPost( status.getId(), user, status.getCreatedAt(), status.getText() );
            timeline.add( temp );
        }

        adapter.notifyDataSetChanged();
        enableRefresh();
    }
}
