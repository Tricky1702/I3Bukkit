package uk.org.rockthehalo.intermud3.services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

import uk.org.rockthehalo.intermud3.Utils;
import uk.org.rockthehalo.intermud3.LPC.LPCInt;
import uk.org.rockthehalo.intermud3.LPC.LPCMapping;
import uk.org.rockthehalo.intermud3.LPC.LPCString;

public class Services {
	private static Vector<Object> i3Services = new Vector<Object>();
	private static Vector<String> i3RouterServiceNames = new Vector<String>();

	private Services() {
	}

	public static void addServices() {
		addService(new I3Error());
		addService(new I3Startup());
		addService(new I3Mudlist());
		addService(new I3Channel());
		addService(new I3Ping());
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
	 * Execute the method create for all I3 services.
	 */
	public static void create() {
		for (Object service : i3Services) {
			Method method = null;

			try {
				method = service.getClass().getMethod("create");
			} catch (NoSuchMethodException e) {
			} catch (SecurityException e) {
			}

			if (method != null) {
				try {
					method.invoke(service);
				} catch (IllegalAccessException e) {
					Utils.logError("", e);
				} catch (IllegalArgumentException e) {
					Utils.logError("", e);
				} catch (InvocationTargetException e) {
					Utils.logError("", e);
				}
			}
		}
	}

	/**
	 * Show debug info for all I3 services.
	 */
	public static void debugInfo() {
		Utils.debug("i3Services:           " + i3Services.toString());
		Utils.debug("i3RouterServiceNames: " + i3RouterServiceNames.toString());

		for (Object service : i3Services) {
			Method method = null;

			try {
				method = service.getClass().getMethod("debugInfo");
			} catch (NoSuchMethodException e) {
			} catch (SecurityException e) {
			}

			if (method != null) {
				try {
					method.invoke(service);
				} catch (IllegalAccessException e) {
				} catch (IllegalArgumentException e) {
				} catch (InvocationTargetException e) {
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
	 * Remove all I3 services.
	 */
	public static void remove() {
		for (Object service : i3Services) {
			Method method = null;

			try {
				method = service.getClass().getMethod("remove");
			} catch (NoSuchMethodException e) {
			} catch (SecurityException e) {
			}

			if (method != null) {
				try {
					method.invoke(service);
				} catch (IllegalAccessException e) {
				} catch (IllegalArgumentException e) {
				} catch (InvocationTargetException e) {
				}
			}
		}
	}

	/**
	 * @param name
	 *            remove name from the i3RouterServiceNames table
	 */
	public static void removeServiceName(String name) {
		i3RouterServiceNames.remove(name);
	}
}
