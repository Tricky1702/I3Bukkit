package uk.org.rockthehalo.intermud3.LPC;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.bukkit.scheduler.BukkitRunnable;

import uk.org.rockthehalo.intermud3.Intermud3;

public class CallOut extends BukkitRunnable {
	private Vector<Map<String, Object>> callOuts;
	private Vector<Map<String, Object>> heartBeats;

	public static CallOut instance;

	public CallOut() {
		instance = this;

		this.callOuts = new Vector<Map<String, Object>>();
		this.heartBeats = new Vector<Map<String, Object>>();
		this.runTaskTimer(Intermud3.instance, 20, 20);
	}

	public void debugInfo() {
		Intermud3.instance.logInfo("callOuts:   " + this.callOuts.toString());
		Intermud3.instance.logInfo("heartBeats: " + this.heartBeats.toString());
	}

	public int callOut(Object owner, String func, long delay) {
		return callOut(owner, func, delay, null);
	}

	public int callOut(Object owner, String func, long delay, Object[] args) {
		if (owner == null || func == null || func.isEmpty() || delay <= 0)
			return -1;

		Map<String, Object> data = new Hashtable<String, Object>();

		data.put("owner", owner);
		data.put("currentDelay", 0L);
		data.put("delay", delay);
		data.put("func", func);

		if (args != null)
			data.put("args", args);

		this.callOuts.add(data);

		return this.callOuts.size() - 1;
	}

	public void removeCallOut(int i) {
		if (i < this.callOuts.size())
			this.callOuts.remove(i);
	}

	public void removeCallOuts(Object owner) {
		if (owner == null)
			return;

		Vector<Object> callouts = new Vector<Object>();

		for (Object obj : this.callOuts)
			if (owner == ((Map<?, ?>) obj).get("owner"))
				callouts.add(obj);

		for (Object obj : callouts)
			this.callOuts.remove(obj);
	}

	public void removeHeartBeat(Object owner) {
		if (owner == null)
			return;

		int i = 0;

		for (Object obj : this.heartBeats) {
			if (owner == ((Map<?, ?>) obj).get("owner"))
				break;

			i++;
		}

		if (i < this.heartBeats.size())
			this.heartBeats.remove(i);
	}

	@Override
	public void run() {
		@SuppressWarnings("unchecked")
		Vector<Map<String, Object>> callOutsCopy = (Vector<Map<String, Object>>) this.callOuts
				.clone();
		@SuppressWarnings("unchecked")
		Vector<Map<String, Object>> heartBeatsCopy = (Vector<Map<String, Object>>) this.heartBeats
				.clone();
		int i = 0;

		for (Object obj : callOutsCopy) {
			@SuppressWarnings("unchecked")
			Map<String, Object> data = (Map<String, Object>) obj;
			Object owner = data.get("owner");
			long currentDelay = (Long) data.get("currentDelay");
			long delay = (Long) data.get("delay");

			currentDelay++;

			if (currentDelay >= delay) {
				Method method = null;

				try {
					method = owner.getClass().getMethod(
							(String) data.get("func"));
				} catch (NoSuchMethodException e) {
				} catch (SecurityException e) {
				}

				if (method != null) {
					try {
						Object[] args = (Object[]) data.get("args");

						if (args == null)
							method.invoke(owner);
						else
							method.invoke(owner, args);

					} catch (IllegalAccessException e) {
						Intermud3.instance.logError("", e);
					} catch (IllegalArgumentException e) {
						Intermud3.instance.logError("", e);
					} catch (InvocationTargetException e) {
						Intermud3.instance.logError("", e);
					}
				}

				this.callOuts.remove(data);
			} else {
				data.put("currentDelay", currentDelay);
				this.callOuts.set(i, data);
			}

			i++;
		}

		i = 0;

		for (Object obj : heartBeatsCopy) {
			@SuppressWarnings("unchecked")
			Map<String, Object> data = (Map<String, Object>) obj;
			Object owner = data.get("owner");
			long currentDelay = (Long) data.get("currentDelay");
			long delay = (Long) data.get("delay");

			currentDelay++;

			if (currentDelay >= delay) {
				currentDelay = 0;

				Method method = null;

				try {
					method = owner.getClass().getMethod("heartBeat");
				} catch (NoSuchMethodException e) {
					method = null;
				} catch (SecurityException e) {
					method = null;
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

			data.put("currentDelay", currentDelay);
			this.heartBeats.set(i, data);
			i++;
		}
	}

	public void setHeartBeat(Object owner, long delay) {
		if (owner == null || delay <= 0)
			return;

		for (Object obj : this.heartBeats) {
			@SuppressWarnings("unchecked")
			Map<String, Object> data = (Map<String, Object>) obj;

			if (owner == data.get("owner"))
				return;
		}

		Map<String, Object> data = new Hashtable<String, Object>();

		data.put("owner", owner);
		data.put("currentDelay", 0L);
		data.put("delay", delay);
		this.heartBeats.add(data);
	}
}
