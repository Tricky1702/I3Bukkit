package uk.org.rockthehalo.intermud3.LPC;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import uk.org.rockthehalo.intermud3.Intermud3;
import uk.org.rockthehalo.intermud3.Utils;

public class CallOut extends BukkitRunnable {
	private Vector<Map<String, Object>> callOuts = new Vector<Map<String, Object>>();
	private Vector<Map<String, Object>> heartBeats = new Vector<Map<String, Object>>();
	private BukkitTask bukkitTask = null;
	private long id = 0;

	public CallOut() {
		this.bukkitTask = runTaskTimer(Intermud3.instance, 1, 1);
	}

	public void debugInfo() {
		Utils.debug("callOuts:   " + this.callOuts.toString());
		Utils.debug("heartBeats: " + this.heartBeats.toString());
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

		Map<String, Object> data = new Hashtable<String, Object>();

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

		Map<String, Object> data = new Hashtable<String, Object>();

		data.put("id", this.id++);
		data.put("owner", owner);
		data.put("currentDelay", 0L);
		data.put("delay", delay * 20L);

		this.heartBeats.add(data);
	}

	/**
	 * Remove all the callouts.
	 */
	public void removeAllCallOuts() {
		this.callOuts.clear();
	}

	/**
	 * Remove all the callouts.
	 */
	public void removeAllHeartBeats() {
		this.heartBeats.clear();
	}

	/**
	 * Remove callout by callout ID.
	 * 
	 * @param id
	 *            the callout ID
	 */
	public void removeCallOut(int id) {
		if (id < 0 || this.callOuts.size() == 0)
			return;

		for (Map<String, Object> callout : this.callOuts)
			if (id == (Integer) callout.get("id")) {
				this.callOuts.remove(callout);

				break;
			}

		if (this.callOuts.size() == 0)
			this.bukkitTask.cancel();
	}

	/**
	 * Remove callouts by class owner.
	 * 
	 * @param owner
	 *            the class owner
	 */
	public void removeCallOut(Object owner) {
		if (owner == null || this.callOuts.size() == 0)
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
		if (owner == null || this.heartBeats.size() == 0)
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
		if (this.callOuts.size() > 0) {
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
						Utils.logError("", e);
					} catch (IllegalArgumentException e) {
						Utils.logError("", e);
					} catch (InvocationTargetException e) {
						Utils.logError("", e);
					}
				}

				this.callOuts.remove(callout);
			}
		}

		if (this.heartBeats.size() > 0) {
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
