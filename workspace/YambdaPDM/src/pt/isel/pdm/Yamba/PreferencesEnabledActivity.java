package pt.isel.pdm.Yamba;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

public abstract class PreferencesEnabledActivity extends Activity {
    
    /** Initialize options menu */
    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.status, m);
        return true;
    }

    /** Process Item Menu selected */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.terminate:
                finish();
                return true;
            case R.id.prefs:
                startActivity(new Intent(this, PrefsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
}
