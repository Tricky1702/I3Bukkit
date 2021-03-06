package uk.org.rockthehalo.intermud3.services;

import uk.org.rockthehalo.intermud3.Packet;

public abstract class ServiceTemplate {
	/**
	 * Service reply handler.
	 * 
	 * @param packet
	 *            the reply packet
	 */
	public abstract void replyHandler(final Packet packet);

	/**
	 * Service request handler.
	 * 
	 * @param packet
	 *            the request packet
	 */
	public abstract void reqHandler(final Packet packet);
}
