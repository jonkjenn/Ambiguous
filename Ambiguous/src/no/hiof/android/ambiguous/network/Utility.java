package no.hiof.android.ambiguous.network;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class Utility {
	public static List<String[]> getInterfaces() {
		List<String[]> interfaces = new ArrayList<String[]>();
		try {
			Enumeration<NetworkInterface> nets = NetworkInterface
					.getNetworkInterfaces();
			for (NetworkInterface i : Collections.list(nets)) {
				String[] str = getInterfaceInformation(i);
				if (str != null) {
					interfaces.add(str);
				}
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return interfaces;
	}

	public static String[] getInterfaceInformation(NetworkInterface netint)
			throws SocketException {
		// Log.d("Display name: ", netint.getDisplayName());
		// Log.d("Name: ", netint.getName());
		Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
		for (InetAddress inetAddress : Collections.list(inetAddresses)) {
			// Log.d("InetAddress: ", inetAddress.toString());
			if (inetAddress.toString().contains(String.valueOf('.'))) {
				return new String[] { netint.getDisplayName(),
						inetAddress.toString().replaceAll("/", "") };
			}
		}
		return null;
	}

}
