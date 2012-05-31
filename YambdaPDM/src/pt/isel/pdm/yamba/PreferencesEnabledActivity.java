package pt.isel.pdm.yamba;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public abstract class PreferencesEnabledActivity extends Activity {

    protected SharedPreferences prefs;

    /** Initialize options menu */
    @Override
    public boolean onCreateOptionsMenu( Menu m ) {
        getMenuInflater().inflate( R.menu.status, m );
        return true;
    }

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        checkPreferencesInternal();
    }

    public void onResume() {
        super.onResume();
        checkPreferencesInternal();
    }

    private void checkPreferencesInternal() {
        if ( !PrefsActivity.checkPreferences( this ) ) {
            Toast t = Toast.makeText( this, R.string.fill_required_preferences, Toast.LENGTH_LONG );
            t.setDuration( 1000 );
            t.show();
            startActivity( new Intent( this, PrefsActivity.class ) );
        }
    }

    /** Process Item Menu selected */
    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        switch ( item.getItemId() ) {
            case R.id.terminate:
                finish();
                return true;
            case R.id.prefs:
                startActivity( new Intent( this, PrefsActivity.class ) );
                return true;
            case R.id.timeline: {
                Intent intent = new Intent( this, TimelineActivity.class );
                intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
                startActivity( intent );
                break;
            }
            case R.id.status: {
                Intent intent = new Intent( this, StatusActivity.class );
                intent.addFlags( Intent.FLAG_ACTIVITY_NO_HISTORY );
                startActivity( intent );
                break;
            }
            case R.id.userinfo: {
                Intent intent = new Intent( this, UserInfoActivity.class );
                intent.addFlags( Intent.FLAG_ACTIVITY_NO_HISTORY );
                startActivity( intent );
                break;
            }
        }
        return super.onOptionsItemSelected( item );
    }

}
