package pt.isel.pdm.Yamba;

import winterwell.jtwitter.Twitter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class StatusActivity extends PreferencesEnabledActivity implements OnClickListener, OnSharedPreferenceChangeListener {
    
    private static final String TAG = "PDM";
    private Button              submit;
    private EditText            text;
    private Twitter             twitter;
    private SharedPreferences   prefs;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        submit = (Button) findViewById(R.id.buttonUpdate);
        submit.setOnClickListener(this);
        App app = (App) getApplication();
        if (app.lastSubmit != null && !app.lastSubmit.isEnabled()) {
            disableSubmit();
        }
        app.lastSubmit = submit;
        text = (EditText) findViewById(R.id.editText);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    /** Called by submit button */
    public void onClick(View v) {
        Log.d(TAG, "onClick");
        disableSubmit();
        // Update status and enable submit in background
        new UpdateStatusTask().execute(text.getText().toString());
    }

    /** Invalidates the twitter when changing preferences */
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        Log.d(TAG, "onPrefsChanged");
        twitter = null;
    }

    // UTILITIES

    /** Task to update Status and enable submit button (in background) */
    private class UpdateStatusTask extends AsyncTask<String, Void, Void> {
        private static final long  TOTAL_TM = 5000; // Elapsed time.
        private volatile Exception error    = null;

        protected Void doInBackground(String... params) {
            try {
                long startTm = System.currentTimeMillis();
                getTwitter().updateStatus(params[0]);
                long elapsedTm = System.currentTimeMillis() - startTm;
                if (elapsedTm < TOTAL_TM) // Provides a minimum duration
                    Thread.sleep(TOTAL_TM - elapsedTm);
                Log.d(TAG, "Submited. Elapsed time=" + elapsedTm + ", text=" + params[0]);
            } catch (Exception ex) {
                error = ex;
            }
            return null;
        }

        protected void onPostExecute(Void res) {
            Log.d(TAG, "onPostExecute");
            if (error != null)
                showToast(getString(R.string.failMessage, error));
            enableSubmit();
        }
    }

    /** Displays a Toast with long length duration */
    private void showToast(String txt) {
        Toast.makeText(StatusActivity.this, txt, Toast.LENGTH_LONG).show();
    }

    /** Return the twitter object using shared preferences */
    private Twitter getTwitter() {
        if (twitter == null) {
            Log.d(TAG, "new Twitter");
            String user = prefs.getString("user", "pdmstudent");
            String pass = prefs.getString("pass", "");
            String url = prefs.getString("url", "");
            twitter = new Twitter(user, pass);
            twitter.setAPIRootUrl(url);
        }
        return twitter;
    }

    /** Enable submit button of last activity */
    private void enableSubmit() {
        Button submit = ((App) getApplication()).lastSubmit;
        submit.setEnabled(true);
        submit.setText(R.string.buttonUpdate);
    }

    /** Disable submit button of this activity */
    private void disableSubmit() {
        submit.setEnabled(false);
        submit.setText(R.string.buttonBusy);
    }
}