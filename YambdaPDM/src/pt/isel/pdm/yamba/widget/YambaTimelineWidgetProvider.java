package pt.isel.pdm.yamba.widget;

import java.util.concurrent.ExecutionException;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.RemoteViews;

import pt.isel.pdm.yamba.R;
import pt.isel.pdm.yamba.TimelineActivity;
import pt.isel.pdm.yamba.YambaPDMApplication;
import pt.isel.pdm.yamba.dataAccessLayer.TweetDataAccessLayer;
import pt.isel.pdm.yamba.model.YambaPost;
import pt.isel.pdm.yamba.services.TimelineService;
import winterwell.jtwitter.Twitter;

public class YambaTimelineWidgetProvider extends AppWidgetProvider {

    ConnectivityManager cm = null;
    NetworkInfo         ni = null;
    PendingIntent       pi = null;

    private PendingIntent getPendingIntent( Context ctx ) {
        if ( pi == null ) {
            Intent it = new Intent( ctx, TimelineActivity.class );
            pi = PendingIntent.getActivity( ctx, 0, it, 0 );
        }
        return pi;
    }

    @Override
    public void onEnabled( Context context ) {
        Log.d("PDM", String.format("onEnabled() called, Context=%s", context ));
        super.onEnabled( context );
    }
    
    @Override
    public void onDisabled( Context context ) {
        Log.d("PDM", String.format("onDisabled() called, Context=%s", context ));
        super.onDisabled( context );
    }
    
    @Override
    public void onUpdate( final Context ctx, AppWidgetManager mgr, int[] widgetIds ) { 
        AsyncTask<Void, Void, YambaPost> task = new AsyncTask< Void, Void, YambaPost >() {

            @Override
            protected YambaPost doInBackground( Void... params ) {
                return TweetDataAccessLayer.getLastestTweet( ctx.getContentResolver() );
            }
            
        }.execute();
        
        YambaPDMApplication app = (YambaPDMApplication) ctx.getApplicationContext();
        Twitter twitter = app.getTwitter();
               
        String ids = "";
        for ( int id : widgetIds )
            ids += " " + id;
        Log.v( "PDM", String.format("onUpdate Ids=%s", ids ) );
        
        YambaPost lastestTweet = null;
        try {
            lastestTweet = task.get();
        } catch ( InterruptedException e ) { Log.d("PDM", "Task Interrupted", e ); } 
          catch ( ExecutionException e ) { Log.d("PDM", "Task Exception Occurred", e ); }
        
        if( lastestTweet == null ) {
            Log.d("PDM", "Nothing to update");
         }
        
        for ( int id : widgetIds ) {
            //TweetDataAccessLayer.getTweetsFrom( provider, timestamp, count )
            
            RemoteViews remoteViews = new RemoteViews( ctx.getPackageName(), R.layout.widget );
            remoteViews.setTextViewText( R.id.header_text_item, twitter.getScreenName() );
            remoteViews.setTextViewText( R.id.widgetTextUser, lastestTweet.getUser() );
            remoteViews.setTextViewText( R.id.widgetTextTweet, lastestTweet.getTweet() );
            remoteViews.setTextViewText( R.id.widgetTextPostedAt, DateUtils.getRelativeTimeSpanString( lastestTweet.getDate().getTime() ) );
            remoteViews.setOnClickPendingIntent( R.id.widgetItem, getPendingIntent( ctx ) );
            mgr.updateAppWidget( id, remoteViews );
        }
    }


    @Override
    public void onReceive( Context ctx, Intent intent ) {
        Log.v( "PDM", intent.getAction() );
        if ( intent.getAction().equals( TimelineService.TIMELINE_UPDATE_OK ) ) {
            AppWidgetManager widgetManager = AppWidgetManager.getInstance( ctx );
            ComponentName componentName = new ComponentName( ctx, TimelineActivity.class );
            Log.v( "PDM", "Timeline" );
            onUpdate( ctx, widgetManager, widgetManager.getAppWidgetIds( componentName ) );
        }

        super.onReceive( ctx, intent );
    }
}
