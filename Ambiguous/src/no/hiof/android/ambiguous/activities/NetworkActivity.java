package no.hiof.android.ambiguous.activities;

import java.io.Closeable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import no.hiof.android.ambiguous.Db;
import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.datasource.ConnectionDataSource;
import no.hiof.android.ambiguous.network.CloseSocket;
import no.hiof.android.ambiguous.network.Network.NetworkConnectionListener;
import no.hiof.android.ambiguous.network.OpenSocket;
import no.hiof.android.ambiguous.network.OpenSocket.OpenSocketListener;
import no.hiof.android.ambiguous.network.Utility;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class NetworkActivity extends Activity implements NetworkConnectionListener, OpenSocketListener {
	SQLiteDatabase db;
	Handler uiHandler;
	Socket socket;
	ServerSocket server;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_network);
		this.db = Db.getDb(getApplicationContext()).getWritableDatabase();
		this.uiHandler = new Handler(getMainLooper());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		super.onCreateOptionsMenu(menu);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			getActionBar().setIcon(android.R.drawable.presence_offline);
		}
		return true;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		closeSockets();
	}
	
	private void closeSockets()
	{
		if(socket != null){new CloseSocket().execute((Closeable)this.socket);}
		if(server != null){new CloseSocket().execute((Closeable)this.server);}
	}
	
	private void startInterfacePicker(final boolean server) {
		Runnable run = new Runnable() {

			@Override
			public void run() {
				final List<String[]> interfaces = Utility.getInterfaces();

				// If not server, show the previous IPs stored in DB so you
				// possibly dont have to type new address.
				if (!server) {
					ConnectionDataSource cd = new ConnectionDataSource(db);
					List<String> prev_ip = cd.GetAddresses();
					for(int i=0;i<prev_ip.size();i++)
					{
						interfaces.add(new String[]{"db",prev_ip.get(i)});
					}
				}

				//Need this for showing the popupmenu under it
				View serverButton = NetworkActivity.this
						.findViewById((server?R.id.button_server:R.id.button_client));

				//Popumenu that shows the interface/IP to use for server or client
				PopupMenu menu = new PopupMenu(NetworkActivity.this,
						serverButton);
				Menu m = menu.getMenu();
				for (int i = 0; i < interfaces.size(); i++) {
					m.add(Menu.NONE, i, i, interfaces.get(i)[0] + " "
							+ interfaces.get(i)[1]);
				}

				menu.setOnMenuItemClickListener(
						new OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {

						if (server) {
							NetworkActivity.this.doStartServer(interfaces
									.get(item.getItemId())[1]);
						} else {
							//if address is from db, we send false to avoid the extra popup screen where u can edit the address
							NetworkActivity.this.doStartClient(interfaces
									.get(item.getItemId())[1],interfaces.get(item.getItemId())[0] == "db");
						}
						return true;
					}
				});
				menu.show();
			}
		};

		new Handler().post(run);
	}

	public void startServer(View view) {
		startInterfacePicker(true);
	}

	private void doStartServer(String address) {
		setStatusText("Connecting...");
		closeSockets();
		//if(network != null){network.StopSocket();}
		//network = new Network(address,true,this);
		new OpenSocket().setup(address,19999,true).execute(this);
		// Pretending to tell you that a connection has been made
		Toast.makeText(this, "Starting Server", Toast.LENGTH_SHORT).show();
	}

	private void doStartClient(final String address,boolean useAddress) {

    closeSockets();
		
	if(useAddress)
	{
		startClient(address);
		shortToast("Starting client");
	}
	else
	{
		final EditText input = new EditText(this);
		input.setText(address.substring(0, address.lastIndexOf(".") + 1));
		input.setSelection(input.getText().length());
		new AlertDialog.Builder(this)
				.setTitle("Server address")
				.setMessage("Type server IP address")
				.setView(input)
				.setPositiveButton("Connect",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								ConnectionDataSource cd = new ConnectionDataSource(db);
								cd.AddConnection(input.getText().toString());
								startClient(input.getText().toString());								
								shortToast("Starting client");
							}
						})
				.setNegativeButton("Abort",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).show();
	}

		getActionBar().setIcon(android.R.drawable.presence_online);
	}
	
	private void shortToast(String text)
	{
		// Pretending to tell you that a connection has been made
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}
	
	private void startClient(String address)
	{
		new OpenSocket().setup(address,19999,false).execute(this);
	}

	public void showClientAddressPicker(View view) {
		startInterfacePicker(false);
	}

	/* Called when the user clicks the network action from the action bar */
	public void onActionNetworkClicked(MenuItem menuItem) {
		Intent intent = new Intent(this, NetworkActivity.class);
		startActivity(intent);
	}

	@Override
	public void onConnected(String address, int port, boolean isServer) {
		uiHandler.post(new Runnable() {
			
			@Override
			public void run() {
                shortToast("Connected");
                getActionBar().setIcon(android.R.drawable.presence_online);
                setStatusText("Connected.");
			}
		});
	}
	
	private void setStatusText(String text)
	{
        ((TextView)findViewById(R.id.network_status)).setText(text);
	}

	@Override
	public void onConnectionFailed(final String reason) {
		uiHandler.post(new Runnable() {
			
			@Override
			public void run() {
                shortToast("Connection failed: " + reason);
                ((TextView)findViewById(R.id.network_status)).setText("Connection failed: " + reason);
			}
		});
	}

	@Override
	public void onDisconnected(String reason) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onListeningForConnection(String address, int port) {
		uiHandler.post(new Runnable() {
			
			@Override
			public void run() {
                setStatusText("Listening for connection");
			}
		});
	}

	@Override
	public void onTryingToConnectToServer(String address, int port) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onOpenSocketListener(Socket socket, ServerSocket server, Exception exception){
		if(exception != null){
                shortToast("Connection failed: " + exception.getMessage());
                setStatusText("Connection failed: " + exception.getMessage());
                return;
		}
		if(socket != null){this.socket = socket;}
		if(server != null){this.server = server;}
        shortToast("Connected");
        getActionBar().setIcon(android.R.drawable.presence_online);
        setStatusText("Connected.");
	}
}
