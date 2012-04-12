package pt.isel.pdm.Yamba;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import winterwell.jtwitter.Twitter;

public class TimelineActivity extends Activity implements OnClickListener {

    private List< TwitterStatus > timeline = initTimeline();

    private ListView view;
    private SimpleAdapter adapter;
    private Button refreshButton;
    
    private static List< TwitterStatus > initTimeline() {
        List< TwitterStatus > l = new ArrayList< TwitterStatus >();
        return l;
    }
    
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        setContentView( R.layout.timeline );
        super.onCreate( savedInstanceState );
        view = ( ListView ) findViewById( android.R.id.list );
        
        refreshButton = ( Button ) findViewById( R.id.button1 );
        refreshButton.setOnClickListener( this );
        
        App app = (App) getApplication();
        if (app.lastRefresh != null && !app.lastRefresh.isEnabled()) {
            disableRefresh();
        }
        
        app.lastRefresh = refreshButton;
        
        String[] from = { TwitterStatus.KEY_USER, TwitterStatus.KEY_DATE, TwitterStatus.KEY_TWEET };
        int[] to = { R.id.user, R.id.date, R.id.tweet };        
        adapter = new SimpleAdapter( this, timeline, R.layout.timeline_item, from, to );
        view.setAdapter( adapter );
        
        updateTimeline();
    }
    
    private class UpdateTimelineTask extends AsyncTask< Void, Void, Void > {

        @Override
        protected Void doInBackground( Void ... params ) {
            List<Twitter.Status> twitterStatus = ((App) getApplication()).getTwitter().getPublicTimeline();
            
            for( Twitter.Status status : twitterStatus ) {
                TwitterStatus temp = new TwitterStatus( status.getId(), status.getUser().getName(), status.getCreatedAt(), status.getText() );
                timeline.add( temp );
            }
            return null;
        }
        
        @Override
        protected void onPostExecute( Void result ) {
            adapter.notifyDataSetChanged();
            enableRefresh();
        }
        
    }
    
    private void updateTimeline() {
        UpdateTimelineTask task = new UpdateTimelineTask();
        timeline.clear();
        task.execute();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onClick( View v ) {
        disableRefresh();
        updateTimeline();
    }
    
    private void disableRefresh() {
        refreshButton.setEnabled( false );
    }
    
    private void enableRefresh() {
        Button refresh = ((App) getApplication()).lastRefresh;
        refresh.setEnabled(true);        
        
    }    
}
