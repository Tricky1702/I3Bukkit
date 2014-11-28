package uk.org.rockthehalo.intermud3.LPC;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import uk.org.rockthehalo.intermud3.Intermud3;
import uk.org.rockthehalo.intermud3.Log;
import uk.org.rockthehalo.intermud3.Utils;
import uk.org.rockthehalo.intermud3.services.ServiceType;

public class CallOut extends BukkitRunnable {
	private final BukkitTask bukkitTask;
	private final Vector<Map<String, Object>> callOuts = new Vector<Map<String, Object>>(32);
	private final Vector<Map<String, Object>> heartBeats = new Vector<Map<String, Object>>(16);

	private long id = 0L;

	public CallOut() {
		this.bukkitTask = runTaskTimer(Intermud3.plugin, 2L, 2L);
	}

	public void debugInfo() {
		if (!this.callOuts.isEmpty()) {
			final List<String> list = new ArrayList<String>(this.callOuts.size());

			for (final Map<String, Object> callout : this.callOuts) {
				final LPCArray data = new LPCArray();

				data.add(callout.get("id"));
				data.add(ServiceType.getServiceName(callout.get("owner")));
				data.add(callout.get("currentDelay"));
				data.add(callout.get("delay"));
				data.add(callout.get("func"));
				data.add(callout.get("args"));

				list.add(Utils.toMudMode(data));
			}

			Log.debug("callOuts: " + StringUtils.join(list, ", "));
		}

		if (!this.heartBeats.isEmpty()) {
			final List<String> list = new ArrayList<String>(this.heartBeats.size());

			for (final Map<String, Object> heartbeat : this.heartBeats) {
				final LPCArray data = new LPCArray();

				data.add(heartbeat.get("id"));
				data.add(ServiceType.getServiceName(heartbeat.get("owner")));
				data.add(heartbeat.get("currentDelay"));
				data.add(heartbeat.get("delay"));

				list.add(Utils.toMudMode(data));
			}

			Log.debug("heartBeats: " + StringUtils.join(list, ", "));
		}
	}

	/**
	 * @param owner
	 *            the class owner
	 * @param func
	 *            the method to call
	 * @param delay
	 *            the delay in seconds to wait
	 * @return the callout ID
	 */
	public long addCallOut(final Object owner, final String func, final long delay) {
		return addCallOut(owner, func, delay, null);
	}

	/**
	 * @param owner
	 *            the class owner
	 * @param func
	 *            the method to call
	 * @param delay
	 *            the delay in seconds to wait
	 * @param args
	 *            the arguments to pass to the method
	 * @return the callout ID
	 */
	public long addCallOut(final Object owner, final String func, final long delay, final Object[] args) {
		if (owner == null || func == null || func.isEmpty() || delay <= 0)
			return -1;

		final Map<String, Object> data = Collections.synchronizedMap(new HashMap<String, Object>(6));
		final long id = this.id++;

		data.put("id", id);
		data.put("owner", owner);
		data.put("currentDelay", 0L);
		data.put("delay", delay * 10L);
		data.put("func", func);

		if (args != null)
			data.put("args", args);

		this.callOuts.add(data);

		return id;
	}

	/**
	 * @param owner
	 *            the class owner
	 * @param delay
	 *            the delay in seconds between heartbeats
	 */
	public void addHeartBeat(final Object owner, final long delay) {
		if (owner == null || delay <= 0)
			return;

		for (final Map<String, Object> heartbeat : this.heartBeats)
			if (owner == heartbeat.get("owner"))
				return;

		final Map<String, Object> data = Collections.synchronizedMap(new HashMap<String, Object>(4));

		data.put("id", this.id++);
		data.put("owner", owner);
		data.put("currentDelay", 0L);
		data.put("delay", delay * 10L);

		this.heartBeats.add(data);
	}

	public void remove() {
		// Stop the task timer.
		this.bukkitTask.cancel();

		// Clear out all lists.
		removeAllCallOuts();
		removeAllHeartBeats();
	}

	/**
	 * Remove all the callouts.
	 */
	private void removeAllCallOuts() {
		this.callOuts.clear();
	}

	/**
	 * Remove all the callouts.
	 */
	private void removeAllHeartBeats() {
		this.heartBeats.clear();
	}

	/**
	 * Remove callout by callout ID.
	 * 
	 * @param id
	 *            the callout ID
	 */
	public void removeCallOut(final long id) {
		if (id < 0 || id >= this.id || this.callOuts.isEmpty())
			return;

		for (final Map<String, Object> callout : this.callOuts)
			if (id == (long) callout.get("id")) {
				this.callOuts.remove(callout);

				return;
			}
	}

	/**
	 * Remove callouts by class owner.
	 * 
	 * @param owner
	 *            the class owner
	 */
	public void removeCallOuts(final Object owner) {
		if (owner == null || this.callOuts.isEmpty())
			return;

		final Vector<Map<String, Object>> callouts = new Vector<Map<String, Object>>(this.callOuts.size());

		for (final Map<String, Object> callout : this.callOuts)
			if (owner == callout.get("owner"))
				callouts.add(callout);

		for (final Map<String, Object> callout : callouts)
			this.callOuts.remove(callout);
	}

	/**
	 * Remove heatbeats by class owner.
	 * 
	 * @param owner
	 *            the class owner
	 */
	public void removeHeartBeat(final Object owner) {
		if (owner == null || this.heartBeats.isEmpty())
			return;

		for (final Map<String, Object> heartbeat : this.heartBeats) {
			if (owner == heartbeat.get("owner")) {
				this.heartBeats.remove(heartbeat);

				return;
			}
		}
	}

	@Override
	public void run() {
		if (!this.callOuts.isEmpty()) {
			final Vector<Map<String, Object>> callouts = new Vector<Map<String, Object>>(this.callOuts.size());

			for (final Map<String, Object> callout : this.callOuts) {
				final long currentDelay = (long) callout.get("currentDelay") + 1;

				if (currentDelay >= (long) callout.get("delay")) {
					callouts.add(callout);
				} else {
					final int index = this.callOuts.indexOf(callout);

					callout.put("currentDelay", currentDelay);
					this.callOuts.set(index, callout);
				}
			}

			for (final Map<String, Object> callout : callouts) {
				final Object owner = callout.get("owner");
				Method[] methods = {};
				Method method = null;

				try {
					methods = owner.getClass().getMethods();
				} catch (SecurityException sE) {
					Log.error("Callout.run::Security exception in callout section getting methods of " + owner, sE);
				}

				for (Method m : methods) {
					if (m.getName().equals(callout.get("func"))) {
						method = m;

						break;
					}
				}

				if (method != null) {
					try {
						final Object[] args = (Object[]) callout.get("args");

						if (args == null)
							method.invoke(owner);
						else
							method.invoke(owner, args);
					} catch (IllegalAccessException e) {
						Log.error("", e);
					} catch (IllegalArgumentException e) {
						Log.error("", e);
					} catch (InvocationTargetException e) {
						Log.error("", e);
					}
				}

				this.callOuts.remove(callout);
			}
		}

		if (!this.heartBeats.isEmpty()) {
			final Vector<Map<String, Object>> heartbeats = new Vector<Map<String, Object>>(this.heartBeats.size());

			for (final Map<String, Object> heartbeat : this.heartBeats) {
				long currentDelay = (long) heartbeat.get("currentDelay") + 1;

				if (currentDelay >= (long) heartbeat.get("delay")) {
					currentDelay = 0;
					heartbeats.add(heartbeat);
				}

				final int index = this.heartBeats.indexOf(heartbeat);

				heartbeat.put("currentDelay", currentDelay);
				this.heartBeats.set(index, heartbeat);
			}

			for (final Map<String, Object> heartbeat : heartbeats) {
				final Object owner = heartbeat.get("owner");
				Method[] methods = {};
				Method method = null;

				try {
					methods = owner.getClass().getMethods();
				} catch (SecurityException sE) {
					Log.error("Callout.run::Security exception in heartbeat section getting methods of " + owner, sE);
				}

				for (Method m : methods) {
					if (m.getName().equals("heartBeat")) {
						method = m;

						break;
					}
				}

				if (method != null) {
					try {
						method.invoke(owner);
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
	}
}
