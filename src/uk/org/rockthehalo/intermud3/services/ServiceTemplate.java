package uk.org.rockthehalo.intermud3.services;

import uk.org.rockthehalo.intermud3.LPC.Packet;

public abstract class ServiceTemplate {
	private String serviceName = null;

	/**
	 * @return the serviceName
	 */
	public String getServiceName() {
		return this.serviceName;
	}

	/**
	 * @param serviceName
	 *            the serviceName to set
	 */
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	/**
	 * @return the serviceName
	 */
	@Override
	public String toString() {
		if (this.serviceName != null)
			return this.serviceName;

		return super.toString();
	}

	public abstract void create();

	public abstract void remove();

	/**
	 * Service reply handler.
	 * 
	 * @param packet
	 *            the reply packet
	 */
	public abstract void replyHandler(Packet packet);

	/**
	 * Service request handler.
	 * 
	 * @param packet
	 *            the request packet
	 */
	public abstract void reqHandler(Packet packet);
}
