package uk.org.rockthehalo.intermud3.services;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public enum ServiceType {
	UNKNOWN("unknown"), I3CHANNEL("channel"), I3ERROR("error"), I3MUDLIST("mudlist"), I3PING("ping"), I3STARTUP("startup"), I3UCACHE(
			"ucache");

	private static final Map<String, ServiceType> nameToService = Collections
			.synchronizedMap(new LinkedHashMap<String, ServiceType>(values().length));
	private static final Map<Object, String> serviceToName = Collections.synchronizedMap(new LinkedHashMap<Object, String>(
			values().length));

	private String name = null;
	private Object service = null;
	private boolean visibleOnRouter = false;

	private ServiceType(final String name) {
		this(name, null, false);
	}

	private ServiceType(final String name, final Object service, final boolean visibleOnRouter) {
		this.name = name;
		this.service = service;
		this.visibleOnRouter = visibleOnRouter;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	public static ServiceType getNamedService(final String name) {
		return nameToService.get(name);
	}

	/**
	 * @return the service
	 */
	@SuppressWarnings("unchecked")
	public <T> T getService() {
		return (T) service;
	}

	public static String getServiceName(final Object service) {
		final String name = serviceToName.get(service);

		if (name == null)
			return service.toString();

		return name;
	}

	/**
	 * @return the visibleOnRouter
	 */
	public boolean isVisibleOnRouter() {
		return visibleOnRouter;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
		nameToService.put(name, this);
	}

	/**
	 * @param service
	 *            the service to set
	 */
	public <T> void setService(final T service) {
		this.service = service;
		serviceToName.put(service, this.name);
	}

	/**
	 * @param visibleOnRouter
	 *            the visibleOnRouter to set
	 */
	public void setVisibleOnRouter(final boolean visibleOnRouter) {
		this.visibleOnRouter = visibleOnRouter;
	}

	public static int size() {
		return values().length;
	}

	static {
		for (final ServiceType st : values()) {
			nameToService.put(st.name, st);

			if (st.service != null)
				serviceToName.put(st.service, st.name);
		}
	}
}
