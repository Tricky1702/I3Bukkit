package uk.org.rockthehalo.intermud3.services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

import uk.org.rockthehalo.intermud3.Intermud3;
import uk.org.rockthehalo.intermud3.LPC.LPCInt;
import uk.org.rockthehalo.intermud3.LPC.LPCMapping;
import uk.org.rockthehalo.intermud3.LPC.LPCString;

public class Services {
	private static Vector<Object> services;
	private static Vector<String> routerServices;

	public Services() {
		services = new Vector<Object>();
		routerServices = new Vector<String>();

		new I3Error();
		new Startup();
		new Mudlist();
		new I3Channel();
		new Ping();
	}

	/**
	 * @param service
	 *            add service to the services table
	 */
	public static void addService(Object service) {
		if (!services.contains(service))
			services.add(service);
	}

	/**
	 * @param serviceName
	 *            add serviceName to the routerServices table
	 */
	public static void addServiceName(String serviceName) {
		if (!routerServices.contains(serviceName))
			routerServices.add(serviceName);
	}

	/**
	 * Execute the method create for all I3 services.
	 */
	public static void create() {
		for (Object obj : services) {
			Method method = null;

			try {
				method = obj.getClass().getMethod("create");
			} catch (NoSuchMethodException e) {
			} catch (SecurityException e) {
			}

			if (method != null) {
				try {
					method.invoke(obj);
				} catch (IllegalAccessException e) {
					Intermud3.instance.logError("", e);
				} catch (IllegalArgumentException e) {
					Intermud3.instance.logError("", e);
				} catch (InvocationTargetException e) {
					Intermud3.instance.logError("", e);
				}
			}
		}
	}

	/**
	 * Show debug info for all I3 services.
	 */
	public static void debugInfo() {
		Intermud3.instance.logInfo("services:       " + services.toString());
		Intermud3.instance.logInfo("routerServices: "
				+ routerServices.toString());

		for (Object obj : services) {
			Method method = null;

			try {
				method = obj.getClass().getMethod("debugInfo");
			} catch (NoSuchMethodException e) {
				method = null;
			} catch (SecurityException e) {
				method = null;
			}

			if (method != null) {
				try {
					method.invoke(obj);
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

		for (String name : routerServices)
			mapping.put(new LPCString(name), new LPCInt(1));

		return mapping;
	}

	/**
	 * @param name
	 *            the service name to search for
	 * @return the service object or null if not found
	 */
	public static Object getService(String name) {
		for (Object obj : services)
			if (obj.toString().equals(name))
				return obj;

		return null;
	}

	/**
	 * Remove all I3 services.
	 */
	public static void remove() {
		for (Object obj : services) {
			Method method = null;

			try {
				method = obj.getClass().getMethod("remove");
			} catch (NoSuchMethodException e) {
				method = null;
			} catch (SecurityException e) {
				method = null;
			}

			if (method != null) {
				try {
					method.invoke(obj);
				} catch (IllegalAccessException e) {
				} catch (IllegalArgumentException e) {
				} catch (InvocationTargetException e) {
				}
			}
		}
	}

	/**
	 * @param serviceName
	 *            remove serviceName from the routerServices table
	 */
	public static void removeServiceName(String serviceName) {
		routerServices.remove(serviceName);
	}
}
