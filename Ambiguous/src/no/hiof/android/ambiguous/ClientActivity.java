package no.hiof.android.ambiguous;

import no.hiof.android.ambiguous.network.Client;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.Menu;
import android.widget.TextView;

public class ClientActivity extends Activity {
	private Client client;
	private String address;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_client);
		address = getIntent().getStringExtra("address");	
		client = new Client(address, new Handler(getMainLooper(),new Callback() {
			
			@Override
			public boolean handleMessage(Message msg) {
				switch(Client.ClientStates.values()[msg.what])
				{
				case CONNECTED:
					showMessage(msg.obj.toString());
				}
				return false;
			}
		}));
		
		client.Connect();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.client, menu);
		return true;
	}

	private void showMessage(String message)
	{
		TextView view = (TextView)findViewById(R.id.serverMessage);		
		view.setText(message);
	}

}
