package uk.org.rockthehalo.intermud3.services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import uk.org.rockthehalo.intermud3.Log;
import uk.org.rockthehalo.intermud3.LPC.LPCInt;
import uk.org.rockthehalo.intermud3.LPC.LPCMapping;
import uk.org.rockthehalo.intermud3.LPC.LPCString;

public class ServiceManager {
	private ServiceManager() {
	}

	/**
	 * @param serviceType
	 * @param service
	 */
	private static <T> void addService(ServiceType serviceType, T service) {
		serviceType.setService(service);
	}

	/**
	 * Call the create method on all I3 services that have defined it.
	 */
	public static void create() {
		for (ServiceType s : ServiceType.values()) {
			Object service = s.getService();
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
		addService(ServiceType.I3ERROR, new I3Error());
		addService(ServiceType.I3STARTUP, new I3Startup());
		addService(ServiceType.I3MUDLIST, new I3Mudlist());
		addService(ServiceType.I3CHANNEL, new I3Channel());
		addService(ServiceType.I3PING, new I3Ping());

		create();
	}

	/**
	 * Show debug info for all I3 services that have defined the debugInfo
	 * method.
	 */
	public static void debugInfo() {
		List<String> serviceList = new ArrayList<String>(
				ServiceType.values().length);
		List<String> routerServiceList = new ArrayList<String>(
				ServiceType.values().length);

		for (ServiceType s : ServiceType.values()) {
			serviceList.add(s.getName());

			if (s.isVisibleOnRouter())
				routerServiceList.add(s.getName());
		}

		Log.debug("Services:       " + StringUtils.join(serviceList, ", "));
		Log.debug("RouterServices: "
				+ StringUtils.join(routerServiceList, ", "));

		for (ServiceType s : ServiceType.values()) {
			Object service = s.getService();
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

		for (ServiceType service : ServiceType.values())
			if (service.isVisibleOnRouter())
				mapping.put(new LPCString(service.getName()), new LPCInt(1));

		return mapping;
	}

	/**
	 * @param serviceName
	 *            the service name to search for
	 * @return the service object or null if not found
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getService(String serviceName) {
		return (T) ServiceType.getNamedService(serviceName).getService();
	}

	/**
	 * Call the remove method on all I3 services that have defined it.
	 */
	public static void remove() {
		for (ServiceType s : ServiceType.values()) {
			Object service = s.getService();
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

			s.setService(null);
		}
	}

	/**
	 * Remove all I3 services.
	 */
	public static void removeServices() {
		remove();
	}
}
