package no.hiof.android.ambiguous.activities;

import no.hiof.android.ambiguous.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

/**
 * The main menu that starts at startup of the app.
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        SharedPreferences sp = getSharedPreferences("no.hiof.android.ambiguous.preferences", Context.MODE_PRIVATE);
        String name = sp.getString("name", "JonAndOrAdrian");
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
    
    /** Called when the user clicks the Cards button. Starts the "gallery" activity. **/
    public void goToDeckManager(View view){
    	Intent intent = new Intent(this, CardGalleryActivity.class);
    	startActivity(intent);
    }
    
    /** Called when the user clicks the Game button, starts up a new game vs AI. **/
    public void goToGame(View view){
    	Intent intent = new Intent(this,no.hiof.android.ambiguous.activities.GameActivity.class);
    	startActivity(intent);
    }
    
    /** Called when the user clicks the network action from the action bar **/
    //TODO: Do something else with the actionbar.
    public void onActionNetworkClicked(MenuItem menuItem){
    	Intent intent = new Intent(this, NetworkActivity.class);
    	startActivity(intent);
    }
    
    /**
     * Network button, starts a new network game.
     * @param view
     */
    public void goToNetwork(View view)
    {
    	Intent intent = new Intent(this, NetworkActivity.class);
    	startActivity(intent);
    }
}
