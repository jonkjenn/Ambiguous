package no.hiof.android.ambiguous.network;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class Utility {
	/**
	 * Gets a list of IPv4 addresses of the network interfaces on the computer.
	 * @return A list of network interfaces.
	 * @throws SocketException
	 */
	public static List<String[]> getInterfaces() throws SocketException {
		List<String[]> interfaces = new ArrayList<String[]>();
		//This triggers strict mode, we will allow it anyway
			Enumeration<NetworkInterface> nets = NetworkInterface
					.getNetworkInterfaces();
			for (NetworkInterface i : Collections.list(nets)) {
				String[] str = getInterfaceAddress(i);
				if (str != null) {
					interfaces.add(str);
				}
			}
		return interfaces;
	}

	/**
	 * Tries to get the IPv4 address of a network interface.
	 * @param netint The id of the network interface.
	 * @return A string array containing maximum one IP address. Null if none found.
	 * @throws SocketException
	 */
	public static String[] getInterfaceAddress(NetworkInterface netint)
			throws SocketException {
		Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
		for (InetAddress inetAddress : Collections.list(inetAddresses)) {
			//Assume is IPV4 if contains ".".
			if (inetAddress.toString().contains(String.valueOf('.'))) {
				//Clean up the address before returning it.
				return new String[] { netint.getDisplayName(),
						inetAddress.toString().replaceAll("/", "") };
			}
		}
		//No interface found.
		return null;
	}

}
