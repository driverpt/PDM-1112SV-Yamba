package pt.isel.pdm.yamba;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import pt.isel.pdm.yamba.services.StatusPublishingService;

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
        Intent intent = new Intent(this, StatusPublishingService.class);
        intent.putExtra( StatusPublishingService.TWEET_MSG, text.getText().toString() );
        startService( intent );
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