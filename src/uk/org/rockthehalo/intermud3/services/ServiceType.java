package uk.org.rockthehalo.intermud3.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum ServiceType {
	I3CHANNEL("channel"), I3ERROR("error"), I3MUDLIST("mudlist"), I3PING("ping"), I3STARTUP(
			"startup");

	private static Map<String, ServiceType> nameToService = null;
	private static Map<Object, String> serviceToName = null;
	private Object service = null;
	private String name = null;
	private boolean visibleOnRouter = false;

	private ServiceType(String name) {
		this.name = name;
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

		for (ServiceType service : ServiceType.values()) {
			ServiceType.nameToService.put(service.name, service);
			ServiceType.serviceToName.put(service.service, service.name);
		}
	}

	/**
	 * @return the service
	 */
	@SuppressWarnings("unchecked")
	public <T> T getService() {
		return (T) service;
	}

	/**
	 * @param service
	 *            the service to set
	 */
	public <T> void setService(T service) {
		this.service = service;
	}

	/**
	 * @return the visibleOnRouter
	 */
	public boolean isVisibleOnRouter() {
		return visibleOnRouter;
	}

	/**
	 * @param visibleOnRouter
	 *            the visibleOnRouter to set
	 */
	public void setVisibleOnRouter(boolean visibleOnRouter) {
		this.visibleOnRouter = visibleOnRouter;
	}
}
