package uk.org.rockthehalo.intermud3.services;

import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

public class Services {
	private static Vector<Runnable> threadList;
	private static Hashtable<Object, String> threadTable;
	private static Hashtable<String, Integer> services;

	public static Startup startup;
	public static I3Error i3Error;
	public static Ping ping;

	public Services() {
		threadList = new Vector<Runnable>();
		threadTable = new Hashtable<Object, String>();
		services = new Hashtable<String, Integer>();
		startup = new Startup();
		i3Error = new I3Error();
		ping = new Ping();
	}

	/**
	 * @param services
	 *            the services to set
	 */
	public void setServices(Hashtable<String, Integer> services) {
		Services.services = services;
	}

	/**
	 * @return the services registered for I3
	 */
	public Hashtable<String, Integer> getServices() {
		return services;
	}

	/**
	 * 
	 * @param name
	 *            add a service name to the table
	 */
	public static void addService(Runnable threadObject, String serviceName) {
		services.put(serviceName, 1);
		threadTable.put(threadObject, "I3-" + serviceName);
	}

	/**
	 * 
	 */
	public static void startHeartBeats() {
		Set<?> keys = threadTable.keySet();

		for (Object key : keys) {
			String threadName = threadTable.get(key);
			Thread thread = new Thread(null, (Runnable) key, threadName);

			threadList.add(thread);
			thread.start();
		}
	}

	/**
	 * 
	 */
	public static void stopHeartBeats() {
		for (Object thread : threadList)
			((Thread) thread).interrupt();

		threadList.clear();
	}
}
