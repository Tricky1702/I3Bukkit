package uk.org.rockthehalo.intermud3.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum ServiceType {
	I3CHANNEL("channel"), I3ERROR("error"), I3MUDLIST("mudlist"), I3PING("ping"), I3STARTUP(
			"startup"), I3UCACHE("ucache");

	private static Map<String, ServiceType> nameToService = null;
	private static Map<Object, String> serviceToName = null;
	private String name = null;
	private Object service = null;
	private boolean visibleOnRouter = false;

	private ServiceType(String name) {
		setName(name);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	public static ServiceType getNamedService(String name) {
		if (ServiceType.nameToService == null) {
			ServiceType.initMapping();
		}

		return ServiceType.nameToService.get(name);
	}

	/**
	 * @return the service
	 */
	@SuppressWarnings("unchecked")
	public <T> T getService() {
		return (T) service;
	}

	public static String getServiceName(Object service) {
		if (ServiceType.serviceToName == null) {
			ServiceType.initMapping();
		}

		String name = ServiceType.serviceToName.get(service);

		if (name == null)
			return service.toString();

		return name;
	}

	private static void initMapping() {
		ServiceType.nameToService = new ConcurrentHashMap<String, ServiceType>(
				ServiceType.values().length);
		ServiceType.serviceToName = new ConcurrentHashMap<Object, String>(
				ServiceType.values().length);

		for (ServiceType st : ServiceType.values()) {
			ServiceType.nameToService.put(st.name, st);

			if (st.service != null)
				ServiceType.serviceToName.put(st.service, st.name);
		}
	}

	/**
	 * @return the visibleOnRouter
	 */
	public boolean isVisibleOnRouter() {
		return visibleOnRouter;
	}

	/**
	 * @param service
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param service
	 *            the service to set
	 */
	public <T> void setService(T service) {
		this.service = service;
	}

	/**
	 * @param visibleOnRouter
	 *            the visibleOnRouter to set
	 */
	public void setVisibleOnRouter(boolean visibleOnRouter) {
		this.visibleOnRouter = visibleOnRouter;
	}

	public static int size() {
		return ServiceType.values().length;
	}
}
