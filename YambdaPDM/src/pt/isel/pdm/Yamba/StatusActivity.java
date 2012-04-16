package pt.isel.pdm.Yamba;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class StatusActivity extends PreferencesEnabledActivity implements OnClickListener {

    private static final String TAG = "PDM";
    private Button              submit;
    private EditText            text;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        submit = (Button) findViewById(R.id.buttonUpdate);
        submit.setOnClickListener(this);
        YambaPDMApplication app = (YambaPDMApplication) getApplication();
        if (app.lastSubmit != null && !app.lastSubmit.isEnabled()) {
            disableSubmit();
        }
        app.lastSubmit = submit;
        text = (EditText) findViewById(R.id.editText);
    }

    /** Called by submit button */
    public void onClick(View v) {
        Log.d(TAG, "onClick");
        disableSubmit();

        if(PrefsActivity.checkPreferences(this)){
            // Update status and enable submit in background
            new UpdateStatusTask().execute(text.getText().toString());
        }
        else{
            Toast t = Toast.makeText( this, R.string.fill_required_preferences,Toast.LENGTH_LONG);
            t.setDuration(1000);
            t.show();
        }
    }

    // UTILITIES

    /** Task to update Status and enable submit button (in background) */
    private class UpdateStatusTask extends AsyncTask<String, Void, Void> {
        private static final long  TOTAL_TM = 5000; // Elapsed time.
        private volatile Exception error    = null;

        protected Void doInBackground(String... params) {
            try {
                long startTm = System.currentTimeMillis();
                YambaPDMApplication yamba = ( YambaPDMApplication ) getApplication();
                yamba.getTwitter().updateStatus(params[0]);
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

    /** Enable submit button of last activity */
    private void enableSubmit() {
        Button submit = ((YambaPDMApplication) getApplication()).lastSubmit;
        submit.setEnabled(true);
        submit.setText(R.string.buttonUpdate);
    }

    /** Disable submit button of this activity */
    private void disableSubmit() {
        submit.setEnabled(false);
        submit.setText(R.string.buttonBusy);
    }
}