package pt.isel.pdm.Yamba;

import winterwell.jtwitter.Twitter;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.widget.Button;

public class App extends Application {
    public Button             lastSubmit;
    private Twitter           twitter;
    private SharedPreferences prefs;

    @Override
    public void onCreate() {
        initSharedPrefs();
        prefs.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                initTwitter();
            }
        });
        super.onCreate();
    }

    public Twitter getTwitter() {
        if (twitter == null) {
            initTwitter();
        }
        return twitter;
    }

    public SharedPreferences getSharedPreferences() {
        return prefs;
    }

    private void initSharedPrefs() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    private void initTwitter() {
        String username = prefs.getString("PDM_USERNAME", "");
        String password = prefs.getString("PDM_PASSWORD", "");
        String url = prefs.getString("PDM_URL", "");
        twitter = new Twitter(username, password);
        twitter.setAPIRootUrl(url);
    }
}
