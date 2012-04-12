package pt.isel.pdm.Yamba;

import java.util.LinkedList;
import java.util.List;

import android.view.Menu;
import android.view.MenuItem;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

public class MainMenuActivity extends Activity {

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     * 
     * Example Taken from => http://stackoverflow.com/questions/3535958/how-to-setup-a-main-menu-layout-in-android
     */

    private static final int VIEW_TWEETS = 0;
    private static final int NEW_TWEET   = 1;

    private List<Integer> menuItems;
    
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.main_menu );
        
        menuItems = new LinkedList< Integer >();
        menuItems.add( R.string.timeline );
        menuItems.add( R.string.new_tweet );
        
        GridView grid = ( GridView ) findViewById( R.id.grid );
        grid.setAdapter( new MainMenuAdapter() );
        grid.setOnItemClickListener( new OnItemClickListener() {

            public void onItemClick( AdapterView< ? > parent, View v, int position, long id ) {
                switch ( position ) {
                case VIEW_TWEETS: {
                    Intent intent = new Intent( MainMenuActivity.this, TimelineActivity.class );
                    intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
                    startActivity( intent );
                    break;
                }
                case NEW_TWEET: {
                    Intent intent = new Intent( MainMenuActivity.this, StatusActivity.class );
                    intent.addFlags( Intent.FLAG_ACTIVITY_NO_HISTORY );
                    startActivity( intent );
                    break;
                }
                }
            }
        } );
    }

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
    
    private class MainMenuAdapter extends BaseAdapter {
        public int getCount() {
            return menuItems.size();
        }

        public Object getItem( int position ) {
            return menuItems.get( position );
        }

        public long getItemId( int position ) {
            return position;
        }

        public View getView( int position, View convertView, ViewGroup parent ) {
            TextView textView;
            
            if ( convertView == null ) {
                textView = new TextView( getApplicationContext() );
                textView.setGravity( Gravity.CENTER );
            } else {
                textView = ( TextView ) convertView;
            }
            textView.setText( menuItems.get( position ) );
            return textView;
        }
    }
}
