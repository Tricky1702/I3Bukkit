package uk.org.rockthehalo.intermud3.services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import uk.org.rockthehalo.intermud3.Log;
import uk.org.rockthehalo.intermud3.LPC.LPCInt;
import uk.org.rockthehalo.intermud3.LPC.LPCMapping;
import uk.org.rockthehalo.intermud3.LPC.LPCString;

public class Services {
	private static Vector<Object> i3Services = new Vector<Object>();
	private static Vector<String> i3RouterServiceNames = new Vector<String>();

	private Services() {
	}

	/**
	 * @param service
	 *            add service to the i3Services table
	 */
	public static void addService(Object service) {
		if (!i3Services.contains(service))
			i3Services.add(service);
	}

	/**
	 * @param name
	 *            add name to the i3RouterServiceNames table
	 */
	public static void addServiceName(String name) {
		if (!i3RouterServiceNames.contains(name))
			i3RouterServiceNames.add(name);
	}

	/**
	 * Call the create method on all I3 services that have defined it.
	 */
	public static void create() {
		for (Object service : i3Services) {
			Method method = null;

			try {
				method = service.getClass().getMethod("create");
			} catch (NoSuchMethodException e) {
			} catch (SecurityException e) {
				Log.error("", e);
			}

			if (method != null) {
				try {
					method.invoke(service);
				} catch (IllegalAccessException e) {
					Log.error("", e);
				} catch (IllegalArgumentException e) {
					Log.error("", e);
				} catch (InvocationTargetException e) {
					Log.error("", e);
				}
			}
		}
	}

	/**
	 * Create all I3 services.
	 */
	public static void createServices() {
		addService(new I3Error());
		addService(new I3Startup());
		addService(new I3Mudlist());
		addService(new I3Channel());
		addService(new I3Ping());

		create();
	}

	/**
	 * Show debug info for all I3 services that have defined the debugInfo
	 * method.
	 */
	public static void debugInfo() {
		Log.debug("i3Services:           "
				+ StringUtils.join(i3Services.iterator(), ", "));
		Log.debug("i3RouterServiceNames: "
				+ StringUtils.join(i3RouterServiceNames.iterator(), ", "));

		for (Object service : i3Services) {
			Method method = null;

			try {
				method = service.getClass().getMethod("debugInfo");
			} catch (NoSuchMethodException e) {
			} catch (SecurityException e) {
				Log.error("", e);
			}

			if (method != null) {
				try {
					method.invoke(service);
				} catch (IllegalAccessException e) {
					Log.error("", e);
				} catch (IllegalArgumentException e) {
					Log.error("", e);
				} catch (InvocationTargetException e) {
					Log.error("", e);
				}
			}
		}
	}

	/**
	 * @return the services registered for I3
	 */
	public static LPCMapping getRouterServices() {
		LPCMapping mapping = new LPCMapping();

		for (String name : i3RouterServiceNames)
			mapping.put(new LPCString(name), new LPCInt(1));

		return mapping;
	}

	/**
	 * @param serviceName
	 *            the service name to search for
	 * @return the service object or null if not found
	 */
	public static Object getService(String serviceName) {
		for (Object service : i3Services)
			if (service.toString().equals(serviceName))
				return service;

		return null;
	}

	/**
	 * Call the remove method on all I3 services that have defined it.
	 */
	public static void remove() {
		for (Object service : i3Services) {
			Method method = null;

			try {
				method = service.getClass().getMethod("remove");
			} catch (NoSuchMethodException e) {
			} catch (SecurityException e) {
				Log.error("", e);
			}

			if (method != null) {
				try {
					method.invoke(service);
				} catch (IllegalAccessException e) {
					Log.error("", e);
				} catch (IllegalArgumentException e) {
					Log.error("", e);
				} catch (InvocationTargetException e) {
					Log.error("", e);
				}
			}
		}
	}

	/**
	 * Remove all I3 services.
	 */
	public static void removeServices() {
		remove();

		i3Services.clear();
		i3RouterServiceNames.clear();
	}

	/**
	 * @param name
	 *            remove name from the i3RouterServiceNames table
	 */
	public static void removeServiceName(String name) {
		i3RouterServiceNames.remove(name);
	}
}
