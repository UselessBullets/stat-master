package net.minecraft.core.achievement.stat;

import net.minecraft.core.achievement.Achievement;
import net.minecraft.core.player.Session;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class StatsContainer {
	private final StatsSyncher syncher;
	private final Map<Stat, Integer> readableStatMap;
	private final Map<Stat, Integer> workableStatMap;
	private boolean modified = false;
	public StatsContainer(Session session, File statsFolder){
		this.syncher = new StatsSyncher(session, this, statsFolder);
		readableStatMap = new HashMap<>();
		workableStatMap = new HashMap<>();
		if (syncher.unsentStatsFile.exists()) {
			addStatsToMaps(syncher.readStatsFromFile(syncher.unsentStatsFile, syncher.unsentStatsFileTemp, syncher.unsentStatsFileOld));
		}
	}
	public void addValueToStat(Stat stat, int value) {
		this.addValueToStatMap(this.workableStatMap, stat, value);
		this.addValueToStatMap(this.readableStatMap, stat, value);
		this.modified = true;
	}
	private void addValueToStatMap(Map<Stat, Integer> map, Stat stat, int value) {
		Integer integer = map.get(stat);
		int currentValue = integer != null ? integer : 0;
		map.put(stat, currentValue + value);
	}
	public boolean isAchievementUnlocked(Achievement achievement) {
		return this.readableStatMap.containsKey(achievement);
	}
	public int readStat(Stat statbase) {
		Integer integer = this.readableStatMap.get(statbase);
		return integer != null ? integer : 0;
	}
	public Map<Stat, Integer> cloneMap() {
		return new HashMap<>(this.workableStatMap);
	}
	public void addStatsToMaps(Map<Stat, Integer> map) {
		if (map == null) {
			return;
		}
		this.modified = true;
		for (Stat stat : map.keySet()) {
			this.addValueToStatMap(this.workableStatMap, stat, map.get(stat));
			this.addValueToStatMap(this.readableStatMap, stat, map.get(stat));
		}
	}
	public void syncReadableMap(Map<Stat, Integer> map) {
		if (map == null) {
			return;
		}
		for (Stat stat : map.keySet()) {
			Integer integer = this.workableStatMap.get(stat);
			int value = integer != null ? integer : 0;
			this.readableStatMap.put(stat, map.get(stat) + value);
		}
	}
	public void syncWorkableMap(Map<Stat, Integer> map) {
		if (map == null) {
			return;
		}
		this.modified = true;
		for (Stat stat : map.keySet()) {
			this.addValueToStatMap(this.workableStatMap, stat, map.get(stat));
		}
	}
	public void sync() {
		this.syncher.syncStatsFileWithMap(this.cloneMap());
	}
	public void saveIfModified() {
		if (this.modified && this.syncher.func_27420_b()) {
			this.syncher.saveStatsThread(this.cloneMap());
		}
		this.syncher.func_27425_c();
	}

}
