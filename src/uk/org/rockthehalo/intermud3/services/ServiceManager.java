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
	private static <T> void addService(final ServiceType serviceType, final T service) {
		serviceType.setService(service);
	}

	/**
	 * Call the create method on all I3 services that have defined it.
	 */
	public static void create() {
		for (final ServiceType s : ServiceType.values()) {
			final Object service = s.getService();
			Method method = null;

			if (service != null) {
				Method[] methods = {};

				try {
					methods = service.getClass().getMethods();
				} catch (SecurityException sE) {
					Log.error("ServiceManager.create::Security exception in getting methods of " + service, sE);
				}

				for (Method m : methods) {
					if (m.getName().equals("create")) {
						method = m;

						break;
					}
				}
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
		addService(ServiceType.I3UCACHE, new I3UCache());

		create();
	}

	/**
	 * Show debug info for all I3 services that have defined the debugInfo
	 * method.
	 */
	public static void debugInfo() {
		final List<String> serviceList = new ArrayList<String>(ServiceType.values().length);
		final List<String> routerServiceList = new ArrayList<String>(ServiceType.values().length);

		for (final ServiceType s : ServiceType.values()) {
			serviceList.add(s.getName());

			if (s.isVisibleOnRouter())
				routerServiceList.add(s.getName());
		}

		Log.debug("Services:       " + StringUtils.join(serviceList, ", "));
		Log.debug("RouterServices: " + StringUtils.join(routerServiceList, ", "));

		for (final ServiceType s : ServiceType.values()) {
			final Object service = s.getService();
			Method method = null;

			if (service != null) {
				Method[] methods = {};

				try {
					methods = service.getClass().getMethods();
				} catch (SecurityException sE) {
					Log.error("ServiceManager.debuygInfo::Security exception in getting methods of " + service, sE);
				}

				for (Method m : methods) {
					if (m.getName().equals("debugInfo")) {
						method = m;

						break;
					}
				}
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
		final LPCMapping mapping = new LPCMapping(ServiceType.size());

		for (final ServiceType service : ServiceType.values())
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
	public static <T> T getService(final String serviceName) {
		return (T) ServiceType.getNamedService(serviceName).getService();
	}

	/**
	 * Call the remove method on all I3 services that have defined it.
	 */
	public static void remove() {
		for (final ServiceType s : ServiceType.values()) {
			final Object service = s.getService();
			Method method = null;

			if (service != null) {
				Method[] methods = {};

				try {
					methods = service.getClass().getMethods();
				} catch (SecurityException sE) {
					Log.error("ServiceManager.remove::Security exception in getting methods of " + service, sE);
				}

				for (Method m : methods) {
					if (m.getName().equals("remove")) {
						method = m;

						break;
					}
				}
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

			// Remove reference.
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
