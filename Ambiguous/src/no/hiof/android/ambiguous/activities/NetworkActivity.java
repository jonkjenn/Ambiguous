package no.hiof.android.ambiguous.activities;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import no.hiof.android.ambiguous.Db;
import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.datasource.ConnectionDataSource;
import no.hiof.android.ambiguous.network.CloseServerSocketTask;
import no.hiof.android.ambiguous.network.CloseSocketTask;
import no.hiof.android.ambiguous.network.OpenSocketTask;
import no.hiof.android.ambiguous.network.OpenSocketTask.OpenSocketListener;
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
public class NetworkActivity extends Activity implements OpenSocketListener {
	SQLiteDatabase db;
	Handler uiHandler;
	private String address;
	private int port = 19999;
	private boolean isServer = false;
	private Socket socket;
	private ServerSocket server;
	private OpenSocketTask openSocketTask;

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

	private void closeSockets() {
		if (socket != null) {
			new CloseSocketTask().execute(socket);
		}
		if (server != null) {
			new CloseServerSocketTask().execute(server);
		}
		if(openSocketTask != null)
		{
			openSocketTask.cancel(true);
		}
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
					for (int i = 0; i < prev_ip.size(); i++) {
						interfaces.add(new String[] { "db", prev_ip.get(i) });
					}
				}

				// Need this for showing the popupmenu under it
				View serverButton = NetworkActivity.this
						.findViewById((server ? R.id.button_server
								: R.id.button_client));

				// Popumenu that shows the interface/IP to use for server or
				// client
				PopupMenu menu = new PopupMenu(NetworkActivity.this,
						serverButton);
				Menu m = menu.getMenu();
				for (int i = 0; i < interfaces.size(); i++) {
					m.add(Menu.NONE, i, i, interfaces.get(i)[0] + " "
							+ interfaces.get(i)[1]);
				}

				menu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {

						if (server) {
							NetworkActivity.this.doStartServer(interfaces
									.get(item.getItemId())[1]);
						} else {
							// if address is from db, we send false to avoid the
							// extra popup screen where u can edit the address
							NetworkActivity.this.doStartClient(
									interfaces.get(item.getItemId())[1],
									interfaces.get(item.getItemId())[0] == "db");
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
		this.address = address;
		this.isServer = true;
		startGame();
		if(true){return;}
		setStatusText("Connecting...");
		closeSockets();
		// if(network != null){network.StopSocket();}
		// network = new Network(address,true,this);
		if(openSocketTask != null){openSocketTask.cancel(true);}
		openSocketTask = new OpenSocketTask().setup(address, 19999, true);
		openSocketTask.execute(this);
		// Pretending to tell you that a connection has been made
		Toast.makeText(this, "Starting Server", Toast.LENGTH_SHORT).show();
	}

	private void doStartClient(final String address, boolean useAddress) {

		this.isServer = false;
		closeSockets();

		if (useAddress) {
			this.address = address;
			startClient(address);
			shortToast("Starting client");
		} else {
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
									ConnectionDataSource cd = new ConnectionDataSource(
											db);
									NetworkActivity.this.address = input.getText().toString();
									cd.AddConnection(NetworkActivity.this.address);
									startClient(input.getText().toString());
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

	private void shortToast(String text) {
		// Pretending to tell you that a connection has been made
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	private void startClient(String address) {
		startGame();
		if(true)return;
		if(openSocketTask != null){openSocketTask.cancel(true);}
		openSocketTask = new OpenSocketTask().setup(address, 19999, false);
		openSocketTask.execute(this);
	}

	public void showClientAddressPicker(View view) {
		startInterfacePicker(false);
	}

	/* Called when the user clicks the network action from the action bar */
	public void onActionNetworkClicked(MenuItem menuItem) {
		Intent intent = new Intent(this, NetworkActivity.class);
		startActivity(intent);
	}

	private void setStatusText(String text) {
		((TextView) findViewById(R.id.network_status)).setText(text);
	}

	@Override
	public void onOpenSocketListener(Socket socket, ServerSocket server,
			Exception exception) {
		if (exception != null) {
			shortToast("Connection failed: " + exception.getMessage());
			setStatusText("Connection failed: " + exception.getMessage());
			return;
		}
		this.socket = socket;
		this.server = server;
		closeSockets();
		shortToast("Connected");
		getActionBar().setIcon(android.R.drawable.presence_online);
		setStatusText("Connected.");
		startGame();
	}

	private void startGame() {
		Intent startGameIntent = new Intent(this, GameActivity.class);
		startGameIntent.putExtra("isNetwork", true);
		startGameIntent.putExtra("address", address);
		startGameIntent.putExtra("port", port);
		startGameIntent.putExtra("isServer", isServer);
		startActivity(startGameIntent);
	}
}
