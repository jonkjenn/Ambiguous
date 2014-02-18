package no.hiof.android.ambiguous.activities;

import no.hiof.android.ambiguous.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
    
    /* Called when the user clicks the Cards button */
    public void goToDeckManager(View view){
    	Intent intent = new Intent(this, DeckManagerActivity.class);
    	startActivity(intent);
    }
    
    /* Called when the user clicks the Game button */
    public void goToGame(View view){
    	Intent intent = new Intent(this,no.hiof.android.ambiguous.activities.GameActivity.class);
    	startActivity(intent);
    }
    
    /* Called when the user clicks the network action from the action bar */
    public void onActionNetworkClicked(MenuItem menuItem){
    	Intent intent = new Intent(this, NetworkActivity.class);
    	startActivity(intent);
    }
    
    /*public void goToNetwork(View view)
    {
    	Intent intent = new Intent(this, NetworkActivity.class);
    	startActivity(intent);
    }*/
}
