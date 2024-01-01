package net.minecraft.core.achievement.stat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import net.minecraft.core.player.Session;
import useless.statmaster.StatsContainer;

public class StatsSyncher {
	private volatile boolean busy = false;
	private volatile Map<Stat, Integer> statsMap = null;
	private final StatsContainer statsCounter;
	public final File unsentStatsFile;
	public final File statsFile;
	public final File unsentStatsFileTemp;
	public final File statsFileTemp;
	public final File unsentStatsFileOld;
	public final File statsFileOld;
	public final Session session;
	private int int1 = 0;

	public StatsSyncher(Session session, StatsContainer statsCounter, File statsDir) {
		this.unsentStatsFile = new File(statsDir, "stats_" + session.username.toLowerCase() + "_unsent.dat");
		this.statsFile = new File(statsDir, "stats_" + session.username.toLowerCase() + ".dat");
		this.unsentStatsFileOld = new File(statsDir, "stats_" + session.username.toLowerCase() + "_unsent.old");
		this.statsFileOld = new File(statsDir, "stats_" + session.username.toLowerCase() + ".old");
		this.unsentStatsFileTemp = new File(statsDir, "stats_" + session.username.toLowerCase() + "_unsent.tmp");
		this.statsFileTemp = new File(statsDir, "stats_" + session.username.toLowerCase() + ".tmp");
		if (!session.username.toLowerCase().equals(session.username)) {
			this.renameFile(statsDir, "stats_" + session.username + "_unsent.dat", this.unsentStatsFile);
			this.renameFile(statsDir, "stats_" + session.username + ".dat", this.statsFile);
			this.renameFile(statsDir, "stats_" + session.username + "_unsent.old", this.unsentStatsFileOld);
			this.renameFile(statsDir, "stats_" + session.username + ".old", this.statsFileOld);
			this.renameFile(statsDir, "stats_" + session.username + "_unsent.tmp", this.unsentStatsFileTemp);
			this.renameFile(statsDir, "stats_" + session.username + ".tmp", this.statsFileTemp);
		}
		this.statsCounter = statsCounter;
		this.session = session;
		this.readStatsThread();
	}

	private void renameFile(File file, String s, File file1) {
		File file2 = new File(file, s);
		if (file2.exists() && !file2.isDirectory() && !file1.exists()) {
			file2.renameTo(file1);
		}
	}

	public Map<Stat, Integer> readStatsFromFile(File statsFile, File tempStatsFile, File oldStatsFile) {
		if (statsFile.exists()) {
			return this.readStatsFromFile(statsFile);
		}
		if (oldStatsFile.exists()) {
			return this.readStatsFromFile(oldStatsFile);
		}
		if (tempStatsFile.exists()) {
			return this.readStatsFromFile(tempStatsFile);
		}
		return null;
	}

	/*
	 * WARNING - Removed try catching itself - possible behaviour change.
	 */
	private Map<Stat, Integer> readStatsFromFile(File statsFile) {
        try {
            return StatsCounter.getStatsFromFile(statsFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
        return null;
	}

	private void saveStats(Map<Stat, Integer> map, File statsFile, File tempStatsFile, File oldStatsFile) throws IOException {
		try (PrintWriter writer = new PrintWriter(new FileWriter(tempStatsFile, false))){
			writer.print(StatsCounter.getStatFileContentString(this.session.username, "local", map));
		}
		if (oldStatsFile.exists()) {
			oldStatsFile.delete();
		}
		if (statsFile.exists()) {
			statsFile.renameTo(oldStatsFile);
		}
		tempStatsFile.renameTo(statsFile);
	}

	public void readStatsThread() {
		if (this.busy) {
			throw new IllegalStateException("Can't get stats from server while StatsSyncher is busy!");
		}
		this.int1 = 100;
		this.busy = true;
		new Thread(() -> {
			try {
				if (this.statsMap != null) {
					this.saveStats(this.statsMap, this.statsFile, this.statsFileTemp, this.statsFileOld);
				} else if (this.statsFile.exists()) {
					this.statsMap = this.readStatsFromFile(this.statsFile, this.statsFileTemp, this.statsFileOld);
				}
			} catch (Exception exception) {
				exception.printStackTrace();
			} finally {
				this.busy = false;
			}
		}).start();
	}

	public void saveStatsThread(Map<Stat, Integer> map) {
		if (this.busy) {
			throw new IllegalStateException("Can't save stats while StatsSyncher is busy!");
		}
		this.int1 = 100;
		this.busy = true;
		new Thread(() -> {
			try {
				this.saveStats(map, this.unsentStatsFile, this.unsentStatsFileTemp, this.unsentStatsFileOld);
			} catch (Exception exception) {
				exception.printStackTrace();
			} finally {
				this.busy = false;
			}
		}).start();
	}

	/*
	 * WARNING - Removed try catching itself - possible behaviour change.
	 */
	public void syncStatsFileWithMap(Map<Stat, Integer> map) {
		int i = 30;
		while (this.busy && --i > 0) {
			try {
				Thread.sleep(100L);
			} catch (InterruptedException interruptedexception) {
				interruptedexception.printStackTrace();
			}
		}
		this.busy = true;
		try {
			this.saveStats(map, this.unsentStatsFile, this.unsentStatsFileTemp, this.unsentStatsFileOld);
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			this.busy = false;
		}
	}

	public boolean func_27420_b() {
		return this.int1 <= 0 && !this.busy;
	}

	public void func_27425_c() {
		// Does nothing maps are always null
		if (this.int1 > 0) {
			--this.int1;
		}
		if (this.statsMap != null) {
			this.statsCounter.syncReadableMap(this.statsMap);
			this.statsMap = null;
		}
	}
}
