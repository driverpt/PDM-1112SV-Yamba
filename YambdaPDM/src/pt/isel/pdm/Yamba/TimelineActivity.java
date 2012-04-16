package pt.isel.pdm.Yamba;

import java.util.ArrayList;
import java.util.List;

import pt.isel.pdm.Yamba.Model.TwitterStatus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import winterwell.jtwitter.Twitter;

public class TimelineActivity extends PreferencesEnabledActivity implements
        OnClickListener, OnItemClickListener {

    private static final String TERMINATOR_SHORT_TEXT_TERMINATOR = "...";

    private static final int MAX_CHARS_NO_LIMIT = 140;

    private static final int DEFAULT_MAX_TWEETS = 50;

    private List<TwitterStatus> timeline = initTimeline();
    private boolean isFirstTime = true;

    private ListView view;
    private SimpleAdapter adapter;
    private Button refreshButton;

    private static List<TwitterStatus> initTimeline() {
        List<TwitterStatus> l = new ArrayList<TwitterStatus>();
        return l;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.timeline);
        super.onCreate(savedInstanceState);
        view = (ListView) findViewById(android.R.id.list);
        view.setOnItemClickListener(this);

        refreshButton = (Button) findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(this);

        YambaPDMApplication app = (YambaPDMApplication) getApplication();
        if (app.lastRefresh != null && !app.lastRefresh.isEnabled()) {
            disableRefresh();
        }

        app.lastRefresh = refreshButton;

        String[] from = { TwitterStatus.KEY_USER, TwitterStatus.KEY_TIMESTAMP,
                TwitterStatus.KEY_TWEET };
        int[] to = { R.id.user, R.id.date, R.id.tweet };
        adapter = new TweetAdapter(this, timeline, R.layout.timeline_item,
                from, to);
        view.setAdapter(adapter);

        if (isFirstTime && PrefsActivity.checkPreferences(this)) {
            disableRefresh();
            updateTimeline();
            isFirstTime = false;
        }
    }

    private class TweetAdapter extends SimpleAdapter {

        public TweetAdapter(Context context, List<TwitterStatus> data,
                int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

        @Override
        public void setViewText(TextView v, String text) {
            switch (v.getId()) {
            case R.id.tweet: {
                SharedPreferences prefs = ((YambaPDMApplication) getApplication()).getSharedPreferences();
                String strPrefs = prefs.getString(PrefsActivity.KEY_MAX_PRESENTED_CHARS,String.valueOf(MAX_CHARS_NO_LIMIT));
                if(!strPrefs.isEmpty()){
                    int max_chars = Integer.parseInt(strPrefs);
                    if(max_chars < text.length()){
                        text = text.substring(0,max_chars).concat(TERMINATOR_SHORT_TEXT_TERMINATOR);
                    }
                }
                v.setText(text);
                break;
            }
            case R.id.date: {
                long tweetTimeStamp = Long.parseLong(text);
                String relativeTime = DateUtils.getRelativeTimeSpanString(
                        tweetTimeStamp).toString();
                v.setText(relativeTime);
                break;
            }
            default: {
                super.setViewText(v, text);
                break;
            }
            }
        }
    }

    private class UpdateTimelineTask extends AsyncTask<Void, Void, Void> {
        YambaPDMApplication app = (YambaPDMApplication) getApplication();

        private volatile Exception exception;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                SharedPreferences prefs = ((YambaPDMApplication) getApplication()).getSharedPreferences();
                String strPrefs = prefs.getString(PrefsActivity.KEY_MAX_PRESENTED_TWEETS,String.valueOf(DEFAULT_MAX_TWEETS));
                
                int max_tweets;
                if(!strPrefs.isEmpty()){
                    max_tweets = Integer.parseInt(strPrefs);
                }
                else{
                    max_tweets = DEFAULT_MAX_TWEETS;
                }
                app.getTwitter().setCount(max_tweets);
                List<Twitter.Status> twitterStatus = app.getTwitter().getPublicTimeline();
                for(Twitter.Status status : twitterStatus){
                    TwitterStatus temp = new TwitterStatus(status.getId(),
                            status.getUser().getName(), status.getCreatedAt(),
                            status.getText());
                    timeline.add(temp);
                }
            } catch (Exception e) {
                exception = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (exception != null) {
                Toast.makeText(TimelineActivity.this, exception.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
            adapter.notifyDataSetChanged();
            Toast.makeText(
                    TimelineActivity.this,
                    String.format(getString(R.string.fetched_elements),
                            timeline.size()), Toast.LENGTH_LONG).show();
            enableRefresh();
        }

    }

    private void updateTimeline() {
        if (PrefsActivity.checkPreferences(this)) {
            UpdateTimelineTask task = new UpdateTimelineTask();
            timeline.clear();
            task.execute();
        } else {
            Toast t = Toast.makeText(this, R.string.fill_required_preferences,
                    Toast.LENGTH_LONG);
            t.setDuration(1000);
            t.show();
            enableRefresh();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onClick(View v) {
        disableRefresh();
        updateTimeline();
    }

    private void disableRefresh() {
        refreshButton.setEnabled(false);
    }

    private void enableRefresh() {
        Button refresh = ((YambaPDMApplication) getApplication()).lastRefresh;
        refresh.setEnabled(true);
    }

    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        intent.putExtra(TwitterStatus.KEY_TWEET, timeline.get(position)
                .getTweet());
        intent.putExtra(TwitterStatus.KEY_DATE, timeline.get(position)
                .getDate());
        intent.putExtra(TwitterStatus.KEY_USER, timeline.get(position)
                .getUser());
        intent.putExtra(TwitterStatus.KEY_ID, timeline.get(position).getId());

        startActivity(intent);
    }
}
