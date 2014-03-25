package no.hiof.android.ambiguous.activities;

import java.net.SocketException;
import java.util.List;

import no.hiof.android.ambiguous.Db;
import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.datasource.ConnectionDataSource;
import no.hiof.android.ambiguous.network.Utility;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Toast;

/**
 * For selecting IP to host from or listen to and starting a network game.
 */
public class NetworkActivity extends Activity {
	SQLiteDatabase db;
	private String address;
	private int port = 19999;
	private boolean isServer = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_network);
		this.db = Db.getDb(getApplicationContext()).getWritableDatabase();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		return true;
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	/**
	 * When running as server we need to pick the interface we should listen for
	 * incoming network connections on.
	 * 
	 * @param isServer
	 * @throws SocketException 
	 */
	private void startInterfacePicker(final boolean isServer) throws SocketException {
				final List<String[]> interfaces = Utility.getInterfaces();

				// If not server, show the previous IPs stored in DB so you
				// possibly do not have to type new address.
				if (!isServer) {
					ConnectionDataSource cd = new ConnectionDataSource(db);
					List<String> prev_ip = cd.GetAddresses();
					for (int i = 0; i < prev_ip.size(); i++) {
						interfaces.add(new String[] { "db", prev_ip.get(i) });
					}
				}

				// Need this for showing the popupmenu under it
				View serverButton = NetworkActivity.this
						.findViewById((isServer ? R.id.button_server
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

				//When click on a interface/ip to use.
				menu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {

						if (isServer) {
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

	//Start server button
	public void startServer(View view) {
		try {
			startInterfacePicker(true);
		} catch (SocketException e) {
			showPickerException(e);
		}
	}

	private void doStartServer(String address) {
        shortToast(getResources().getString(R.string.network_server_start_toast));
		this.address = address;
		this.isServer = true;
		startGame();
	}

	/**
	 * Dialog for editing the server address you want to connect to.
	 * @param address The partial or complete address. If partial we want to present it to the user so they don't have to type the whole address. We guess that server is on same subnet. 
	 * @param useAddress If the address is already correct. address edit step is then skipped.
	 */
	private void doStartClient(final String address, boolean useAddress) {

		if (useAddress) {
			this.address = address;
			startGame();
			shortToast(getResources().getString(R.string.network_client_start_toast));
		} else {
			final EditText input = new EditText(this);
			input.setText(address.substring(0, address.lastIndexOf(".") + 1));
			input.setSelection(input.getText().length());
			new AlertDialog.Builder(this)
					.setTitle(R.string.server_address_editor_title)
					.setMessage(R.string.server_address_editor_message)
					.setView(input)
					.setPositiveButton(R.string.connect,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									//Add the new server ip to the database for future usage.
									ConnectionDataSource cd = new ConnectionDataSource(
											db);
									NetworkActivity.this.address = input
											.getText().toString();
									cd.AddConnection(NetworkActivity.this.address);
									startGame();
								}
							})
					.setNegativeButton(R.string.abort,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
								}
							}).show();
		}
	}

	/**
	 * Display a short duration Toast message.
	 * @param text
	 */
	private void shortToast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Client button click
	 * @param view
	 */
	public void showClientAddressPicker(View view) {
		try {
			startInterfacePicker(false);
		} catch (SocketException e) {
			showPickerException(e);
		}
	}
	
	/**
	 * Displays a dialog box with an error message for the network interface picker.
	 * @param e
	 */
	private void showPickerException(SocketException e)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.network_picker_exception_title)
		.setMessage(e.getMessage())
		.setPositiveButton(R.string.close, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		})
		.show();
	}

	/**
	 * Starts GameActivity with the chosen network settings.
	 */
	private void startGame() {
		Intent startGameIntent = new Intent(this, GameActivity.class);
		startGameIntent.putExtra("isNetwork", true);
		startGameIntent.putExtra("address", address);
		startGameIntent.putExtra("port", port);
		startGameIntent.putExtra("isServer", isServer);
		startActivity(startGameIntent);
	}
}
