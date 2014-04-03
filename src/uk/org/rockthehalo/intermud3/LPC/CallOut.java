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
	private BukkitTask bukkitTask = null;
	private long id = 0;

	public CallOut() {
	}

	public void debugInfo() {
		Utils.logInfo("callOuts: " + this.callOuts.toString());
	}

	public long callOut(Object owner, String func, long delay) {
		return callOut(owner, func, delay, null);
	}

	public long callOut(Object owner, String func, long delay, Object[] args) {
		if (owner == null || func == null || func.isEmpty() || delay <= 0)
			return -1;

		Map<String, Object> data = new Hashtable<String, Object>();

		long id = this.id++;

		data.put("id", id);
		data.put("owner", owner);
		data.put("currentDelay", 0);
		data.put("delay", delay * 20);
		data.put("func", func);

		if (args != null)
			data.put("args", args);

		this.callOuts.add(data);

		if (this.callOuts.size() == 1)
			this.bukkitTask = runTaskTimer(Intermud3.instance, 1, 1);

		return id;
	}

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

	public void removeCallOuts(Object owner) {
		if (owner == null || this.callOuts.size() == 0)
			return;

		Vector<Map<String, Object>> callouts = new Vector<Map<String, Object>>();

		for (Map<String, Object> callout : this.callOuts)
			if (owner == callout.get("owner"))
				callouts.add(callout);

		for (Map<String, Object> callout : callouts)
			this.callOuts.remove(callout);

		if (this.callOuts.size() == 0)
			this.bukkitTask.cancel();
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

			if (this.callOuts.size() == 0)
				this.bukkitTask.cancel();
		}
	}
}
