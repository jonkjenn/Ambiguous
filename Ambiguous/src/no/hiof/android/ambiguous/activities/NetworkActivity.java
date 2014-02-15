package no.hiof.android.ambiguous.activities;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.network.Client;
import no.hiof.android.ambiguous.network.Server;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;

public class NetworkActivity extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_network);
	}

	private static List<String[]> getInterfaces()
	{
		List<String[]> interfaces = new ArrayList<String[]>();
		try {
			Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
			for(NetworkInterface i : Collections.list(nets))
			{
				String[] str = getInterfaceInformation(i);
				if(str != null)
				{
                    interfaces.add(str);
				}
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return interfaces;
	}
	
    private static String[] getInterfaceInformation(NetworkInterface netint) throws SocketException {
        //Log.d("Display name: ", netint.getDisplayName());
        //Log.d("Name: ", netint.getName());
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            //Log.d("InetAddress: ", inetAddress.toString());
        	if(inetAddress.toString().length() < 15){return new String[]{netint.getDisplayName(), inetAddress.toString().replaceAll("/","")};}
        }
        return null;
     }
    
    private void PickInterface()
    {
    	Runnable run = new Runnable() {
			
			@Override
			public void run() {
                final List<String[]> interfaces = getInterfaces();
                
                View serverButton = NetworkActivity.this.findViewById(R.id.button_server);

                PopupMenu menu = new PopupMenu(NetworkActivity.this,serverButton);
                Menu m = menu.getMenu();
                for(int i=0;i<interfaces.size();i++)
                {
                	m.add(Menu.NONE,i,i,interfaces.get(i)[0] + " " + interfaces.get(i)[1]);
                }
                menu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						NetworkActivity.this.doStartServer(interfaces.get(item.getItemId())[1]);
						return false;
					}
				});
                menu.show();
			}
		};
		
		new Handler().post(run);
    }
	
	public void startServer(View view)
	{
		PickInterface();
		//Server s = new Server();
	}

	private void doStartServer(String address)
	{
		Server s = new Server(address);		
	}
	
	public void startClient(View view)
	{
		Client c = new Client();		
	}

}
