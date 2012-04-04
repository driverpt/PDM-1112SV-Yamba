package pt.isel.pdm.Yamba;

import java.util.List;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.Twitter.Status;
import android.app.Activity;
import android.os.Bundle;

public class TimelineActivity extends Activity {
    
    private List<Status> timeline; 
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Twitter twitter = new Twitter();
        if ( timeline == null ) {
            timeline = twitter.getHomeTimeline();            
        }
    }
    
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    
    
    
}
