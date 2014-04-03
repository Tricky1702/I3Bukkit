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

public class HeartBeat extends BukkitRunnable {
	private Vector<Map<String, Object>> heartBeats = new Vector<Map<String, Object>>();
	private BukkitTask bukkitTask = null;
	private long id = 0;

	public HeartBeat() {
	}

	public void debugInfo() {
		Utils.logInfo("heartBeats: " + this.heartBeats.toString());
	}

	public void removeHeartBeat(Object owner) {
		if (owner == null || this.heartBeats.size() == 0)
			return;

		for (Map<String, Object> heartbeat : this.heartBeats) {
			if (owner == heartbeat.get("owner")) {
				this.heartBeats.remove(heartbeat);

				break;
			}
		}

		if (this.heartBeats.size() == 0)
			this.bukkitTask.cancel();
	}

	@Override
	public void run() {
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

	public void setHeartBeat(Object owner, long delay) {
		if (owner == null || delay <= 0)
			return;

		for (Map<String, Object> heartbeat : this.heartBeats)
			if (owner == heartbeat.get("owner"))
				return;

		Map<String, Object> data = new Hashtable<String, Object>();

		data.put("id", this.id++);
		data.put("owner", owner);
		data.put("currentDelay", 0);
		data.put("delay", delay);

		this.heartBeats.add(data);

		if (this.heartBeats.size() == 1)
			this.bukkitTask = runTaskTimer(Intermud3.instance, 20, 20);
	}
}
