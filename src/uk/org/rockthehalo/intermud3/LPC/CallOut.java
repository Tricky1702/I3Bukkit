package uk.org.rockthehalo.intermud3.LPC;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import uk.org.rockthehalo.intermud3.Intermud3;
import uk.org.rockthehalo.intermud3.Log;
import uk.org.rockthehalo.intermud3.services.ServiceType;

public class CallOut extends BukkitRunnable {
	private Vector<Map<String, Object>> callOuts = new Vector<Map<String, Object>>();
	private Vector<Map<String, Object>> heartBeats = new Vector<Map<String, Object>>();
	private BukkitTask bukkitTask = null;
	private long id = 0;

	public CallOut() {
		this.bukkitTask = runTaskTimer(Intermud3.instance, 1, 1);
	}

	public void debugInfo() {
		List<LPCArray> list = new ArrayList<LPCArray>();

		for (Map<String, Object> callout : this.callOuts) {
			LPCArray data = new LPCArray();

			data.add(callout.get("id"));
			data.add(ServiceType.getServiceName(callout.get("owner")));
			data.add(callout.get("currentDelay"));
			data.add(callout.get("delay"));
			data.add(callout.get("func"));
			data.add(callout.get("args"));

			list.add(data);
		}

		Log.debug("callOuts:   " + StringUtils.join(list, ", "));

		list.clear();

		for (Map<String, Object> heartbeat : this.heartBeats) {
			LPCArray data = new LPCArray();

			data.add(heartbeat.get("id"));
			data.add(ServiceType.getServiceName(heartbeat.get("owner")));
			data.add(heartbeat.get("currentDelay"));
			data.add(heartbeat.get("delay"));

			list.add(data);
		}

		Log.debug("heartBeats: " + StringUtils.join(list, ", "));
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
	public long addCallOut(Object owner, String func, long delay) {
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
	public long addCallOut(Object owner, String func, long delay, Object[] args) {
		if (owner == null || func == null || func.isEmpty() || delay <= 0)
			return -1;

		Map<String, Object> data = new ConcurrentHashMap<String, Object>();

		long id = this.id++;

		data.put("id", id);
		data.put("owner", owner);
		data.put("currentDelay", 0L);
		data.put("delay", delay * 20L);
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
	public void addHeartBeat(Object owner, long delay) {
		if (owner == null || delay <= 0)
			return;

		for (Map<String, Object> heartbeat : this.heartBeats)
			if (owner == heartbeat.get("owner"))
				return;

		Map<String, Object> data = new ConcurrentHashMap<String, Object>();

		data.put("id", this.id++);
		data.put("owner", owner);
		data.put("currentDelay", 0L);
		data.put("delay", delay * 20L);

		this.heartBeats.add(data);
	}

	public void remove() {
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
	public void removeCallOut(int id) {
		if (id < 0 || this.callOuts.isEmpty())
			return;

		for (Map<String, Object> callout : this.callOuts)
			if (id == (Integer) callout.get("id")) {
				this.callOuts.remove(callout);

				break;
			}

		if (this.callOuts.isEmpty())
			this.bukkitTask.cancel();
	}

	/**
	 * Remove callouts by class owner.
	 * 
	 * @param owner
	 *            the class owner
	 */
	public void removeCallOuts(Object owner) {
		if (owner == null || this.callOuts.isEmpty())
			return;

		Vector<Map<String, Object>> callouts = new Vector<Map<String, Object>>();

		for (Map<String, Object> callout : this.callOuts)
			if (owner == callout.get("owner"))
				callouts.add(callout);

		for (Map<String, Object> callout : callouts)
			this.callOuts.remove(callout);
	}

	/**
	 * Remove heatbeats by class owner.
	 * 
	 * @param owner
	 *            the class owner
	 */
	public void removeHeartBeat(Object owner) {
		if (owner == null || this.heartBeats.isEmpty())
			return;

		for (Map<String, Object> heartbeat : this.heartBeats) {
			if (owner == heartbeat.get("owner")) {
				this.heartBeats.remove(heartbeat);

				break;
			}
		}
	}

	@Override
	public void run() {
		if (!this.callOuts.isEmpty()) {
			Vector<Map<String, Object>> callouts = new Vector<Map<String, Object>>();

			for (Map<String, Object> callout : this.callOuts) {
				long currentDelay = (Long) callout.get("currentDelay") + 1;

				if (currentDelay >= (Long) callout.get("delay")) {
					callouts.add(callout);
				} else {
					int index = this.callOuts.indexOf(callout);

					callout.put("currentDelay", currentDelay);
					this.callOuts.set(index, callout);
				}
			}

			for (Map<String, Object> callout : callouts) {
				Object owner = callout.get("owner");
				Method method = null;

				try {
					method = owner.getClass().getMethod(
							(String) callout.get("func"));
				} catch (NoSuchMethodException e) {
				} catch (SecurityException e) {
				}

				if (method != null) {
					try {
						Object[] args = (Object[]) callout.get("args");

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
			Vector<Map<String, Object>> heartbeats = new Vector<Map<String, Object>>();

			for (Map<String, Object> heartbeat : this.heartBeats) {
				long currentDelay = (Long) heartbeat.get("currentDelay") + 1;
				int index = this.heartBeats.indexOf(heartbeat);

				if (currentDelay >= (Long) heartbeat.get("delay")) {
					currentDelay = 0;
					heartbeats.add(heartbeat);
				}

				heartbeat.put("currentDelay", currentDelay);
				this.heartBeats.set(index, heartbeat);
			}

			for (Map<String, Object> heartbeat : heartbeats) {
				Object owner = heartbeat.get("owner");
				Method method = null;

				try {
					method = owner.getClass().getMethod("heartBeat");
				} catch (NoSuchMethodException e) {
				} catch (SecurityException e) {
				}

				if (method != null) {
					try {
						method.invoke(owner);
					} catch (IllegalAccessException e) {
					} catch (IllegalArgumentException e) {
					} catch (InvocationTargetException e) {
					}
				}
			}
		}
	}
}
