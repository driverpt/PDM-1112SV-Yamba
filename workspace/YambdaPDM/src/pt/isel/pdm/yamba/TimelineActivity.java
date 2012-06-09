package pt.isel.pdm.yamba;

import java.util.Date;

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
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
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import pt.isel.pdm.yamba.model.YambaPost;
import pt.isel.pdm.yamba.provider.contract.TweetContract;
import pt.isel.pdm.yamba.provider.helper.EmailHelper;
import pt.isel.pdm.yamba.services.TimelineService;

public class TimelineActivity extends PreferencesEnabledActivity implements OnClickListener, OnItemClickListener {

    private static final String LOGGER_TAG                       = "Timeline Activity";

    private static final String TERMINATOR_SHORT_TEXT_TERMINATOR = "...";
    private static final int    MAX_CHARS_NO_LIMIT               = -1;
    private static final int    MAX_TWEETS_NO_LIMIT              = -1;

    private boolean             isFirstTime                      = true;

    private ListView            view;
    private CursorAdapter       mCursorAdapter;
    private Button              refreshButton;

    private BroadcastReceiver   mReceiver;

    private YambaPDMApplication app;

    private class TimelineUpdatedBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive( Context context, Intent intent ) {
            Log.d( LOGGER_TAG, "TimelineUpdatedBroadcastReceiver.onReceive() called" );
            int operationResult = intent.getIntExtra( TimelineService.OPERATION, TimelineService.INVALID_OPERATION );
            if ( operationResult == TimelineService.TIMELINE_UPDATE_OK ) {
                updateCursor();
            } else {
                enableRefresh();
            }
        }
    }

    private IntentFilter mReceiverFilter = new IntentFilter( YambaPDMApplication.ACTION_YAMBA_TIMELINE_UPDATED );

    public TimelineActivity() {
        mReceiver = new TimelineUpdatedBroadcastReceiver();
    }

    // TODO: Create a class just to Handle Widget Actions. SRP ( Single Responsability Principle )
    private void configureWidget( Intent intent ) {
        Log.d( LOGGER_TAG, String.format("Widget Intent =%s", intent) );
        if ( AppWidgetManager.ACTION_APPWIDGET_CONFIGURE.equals( intent.getAction() ) ) {

            /*
             * Intent update = new Intent(this,WidgetProvider.class);
             * update.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
             * sendBroadcast(update);
             */

            AlertDialog.Builder builder = new AlertDialog.Builder( this );
            builder.setMessage( "Widget Installed" )
                   .setPositiveButton( "OK", new DialogInterface.OnClickListener() {
                       public void onClick( DialogInterface dialog, int id ) {
                           TimelineActivity.this.finish();
                       }
                   });
            AlertDialog alert = builder.create();
            int widgetId = intent.getIntExtra( AppWidgetManager.EXTRA_APPWIDGET_ID, -1 );
            Intent result = new Intent();
            result.putExtra( AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId );
            setResult( RESULT_OK, result );
            alert.show();
        }
    }

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        setContentView( R.layout.timeline );

        super.onCreate( savedInstanceState );

        configureWidget( getIntent() );

        view = (ListView) findViewById( android.R.id.list );

        // registers the listview for the context menu
        registerForContextMenu( view );

        app = (YambaPDMApplication) getApplication();

        view.setOnItemClickListener( this );

        refreshButton = (Button) findViewById( R.id.refreshButton );
        refreshButton.setOnClickListener( this );
        
        // if ( app.lastRefresh != null && !app.lastRefresh.isEnabled() ) {
        disableRefresh();
        // }

        app.lastRefresh = refreshButton;

        mCursorAdapter = new TweetCursorAdapter( this, null );

        view.setAdapter( mCursorAdapter );

        registerReceiver( mReceiver, mReceiverFilter );

        if ( isFirstTime ) {
            updateCursor();
            isFirstTime = false;
        }
    }

    private void updateCursor() {
        TimelineCursorUpdateTask task = new TimelineCursorUpdateTask();
        task.execute();
    }

    private class TweetCursorAdapter extends ResourceCursorAdapter {

        public TweetCursorAdapter( Context context, Cursor c ) {
            super( context, R.layout.timeline_item, c );
        }

        class ViewHolder {
            public ImageView photo;
            public TextView  user;
            public TextView  date;
            public TextView  tweet;
        }

        @Override
        public void bindView( View view, Context context, Cursor cursor ) {
            ImageView photoView = (ImageView) view.findViewById( R.id.photoUri );
            TextView userView = (TextView) view.findViewById( R.id.user );
            TextView tweetView = (TextView) view.findViewById( R.id.tweet );
            TextView dateView = (TextView) view.findViewById( R.id.date );

            ViewHolder holder = new ViewHolder();
            holder.photo = photoView;
            holder.user = userView;
            holder.date = dateView;
            holder.tweet = tweetView;

            view.setTag( holder );

            String screenName = cursor.getString( cursor.getColumnIndex( TweetContract.USER ) );
            long timestamp = cursor.getLong( cursor.getColumnIndex( TweetContract.TIMESTAMP ) );
            String tweet = cursor.getString( cursor.getColumnIndex( TweetContract.TWEET ) );
            
            SharedPreferences prefs = app.getSharedPreferences();
            
            Integer maxChars = Integer.parseInt( prefs.getString( PrefsActivity.KEY_MAX_PRESENTED_CHARS, String.valueOf(MAX_CHARS_NO_LIMIT) ) );
            if ( maxChars != MAX_CHARS_NO_LIMIT ) {
                if( tweet.length() > maxChars ) {
                    tweet = tweet.substring( 0, maxChars ) + TERMINATOR_SHORT_TEXT_TERMINATOR;
                }
            }
            
            dateView.setText( DateUtils.getRelativeTimeSpanString( timestamp ).toString() );
            tweetView.setText( tweet );
            userView.setText( screenName );
        }
    }

    private class TimelineCursorUpdateTask extends AsyncTask< Void, Void, Cursor > {

        @Override
        protected Cursor doInBackground( Void... params ) {
            SharedPreferences prefs = app.getSharedPreferences();
            Integer maxTweets = Integer.parseInt( prefs.getString( PrefsActivity.KEY_MAX_PRESENTED_TWEETS, String.valueOf(MAX_TWEETS_NO_LIMIT) ) );
            
            String orderByClause = "timestamp DESC".concat( ( maxTweets != MAX_TWEETS_NO_LIMIT ) ? String.format(" LIMIT %d", maxTweets ) : "" ) ;
            
            Cursor mNewCursor = getContentResolver()
                    .query( TweetContract.CONTENT_URI,
                            new String[] { TweetContract._ID, TweetContract.TIMESTAMP, TweetContract.TWEET,
                                    TweetContract.USER }, null, null, orderByClause );
            return mNewCursor;
        }

        @Override
        protected void onPostExecute( Cursor result ) {
            if ( result != null ) {
                mCursorAdapter.changeCursor( result );
                mCursorAdapter.notifyDataSetChanged();
            }
            enableRefresh();
            super.onPostExecute( result );
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
        Log.d( LOGGER_TAG, "onStop() called" );
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver( mReceiver );
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

        Cursor cursor = (Cursor) view.getItemAtPosition( info.position );

        // YambaPost status = ( YambaPost ) view.getItemAtPosition( info.position );

        long id = cursor.getLong( cursor.getColumnIndex( TweetContract._ID ) );
        String screenName = cursor.getString( cursor.getColumnIndex( TweetContract.USER ) );
        Date tweetDate = new Date( cursor.getLong( cursor.getColumnIndex( TweetContract.TIMESTAMP ) ) );
        String tweet = cursor.getString( cursor.getColumnIndex( TweetContract.TWEET ) );

        YambaPost status = new YambaPost( id, screenName, tweetDate, tweet );

        if ( item.getItemId() == R.id.timelineemailctxmenu_sendemail ) {
          Intent it = EmailHelper.getEmailIntent(getString(R.string.timelineitem_emailsubject) , status);
          startActivity( it );
        }
        return true;
    }

    public void onItemClick( AdapterView< ? > parent, View view, int position, long id ) {
        Intent intent = new Intent( this, DetailActivity.class );
        intent.addFlags( Intent.FLAG_ACTIVITY_SINGLE_TOP );

        Cursor cursor = (Cursor) parent.getItemAtPosition( position );

        long _id = cursor.getLong( cursor.getColumnIndex( TweetContract._ID ) );
        String screenName = cursor.getString( cursor.getColumnIndex( TweetContract.USER ) );
        Date tweetDate = new Date( cursor.getLong( cursor.getColumnIndex( TweetContract.TIMESTAMP ) ) );
        String tweet = cursor.getString( cursor.getColumnIndex( TweetContract.TWEET ) );

        YambaPost status = new YambaPost( _id, screenName, tweetDate, tweet );

        // YambaPost status = timeline.get( position );
        if ( status != null ) {
            intent.putExtra( YambaPost.IDENTIFIER, status );
            startActivity( intent );
        }
    }
    
}
