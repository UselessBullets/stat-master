/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core.achievement.stat;

import com.b100.json.JsonParser;
import com.b100.json.element.JsonArray;
import com.b100.json.element.JsonElement;
import com.b100.json.element.JsonEntry;
import com.b100.json.element.JsonObject;
import com.b100.utils.FileUtils;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.core.MD5String;
import net.minecraft.core.achievement.Achievement;
import net.minecraft.core.player.Session;
import net.minecraft.core.world.World;
import useless.statmaster.mixins.SaveHandlerBaseAccessor;

public class StatsCounter {
	private final StatsContainer globalStatsContainer;
	private final File statsFolder;
	private final File savesFolder;
	private final Session session;
	private StatsContainer worldStatContainer;
	private String lastWorldName;
	private final Minecraft mc = Minecraft.getMinecraft(Minecraft.class);

	public StatsCounter(Session session, File mcDir) {
		this.session = session;
		statsFolder = new File(mcDir, "stats");
		savesFolder = new File(mcDir, "saves");
		FileUtils.createFolder(statsFolder);
		StatsCounter.relocateStatFiles(mcDir, statsFolder);
		this.globalStatsContainer = new StatsContainer(session, statsFolder);
	}
	public StatsContainer getWorldContainer(World world){
		if (world == null){
			return null;
		}
		if (lastWorldName != null && lastWorldName.equals(((SaveHandlerBaseAccessor)world.getSaveHandler()).getWorldDirName())){
			return worldStatContainer;
		}
		lastWorldName = ((SaveHandlerBaseAccessor)world.getSaveHandler()).getWorldDirName();
		File worldStats = new File(savesFolder, lastWorldName + "/stats");
		FileUtils.createFolder(worldStats);
		this.worldStatContainer = new StatsContainer(session, worldStats);
		return worldStatContainer;
	}

	public void addValueToStat(Stat stat, int value) {
		StatsContainer worldStats = getWorldContainer(mc.theWorld);
		if (getWorldContainer(mc.theWorld) != null){
			worldStats.addValueToStat(stat, value);
		}
		globalStatsContainer.addValueToStat(stat, value);
	}
	public Map<Stat, Integer> cloneMap() {
		return globalStatsContainer.cloneMap();
	}

	public void addStatsToMaps(Map<Stat, Integer> map) {
		StatsContainer worldStats = getWorldContainer(mc.theWorld);
		if (getWorldContainer(mc.theWorld) != null){
			worldStats.addStatsToMaps(map);
		}
		globalStatsContainer.addStatsToMaps(map);
	}
	public void syncReadableMap(Map<Stat, Integer> map) {
		StatsContainer worldStats = getWorldContainer(mc.theWorld);
		if (getWorldContainer(mc.theWorld) != null){
			worldStats.syncReadableMap(map);
		}
		globalStatsContainer.syncReadableMap(map);
	}
	public void syncWorkableMap(Map<Stat, Integer> map) {
		StatsContainer worldStats = getWorldContainer(mc.theWorld);
		if (getWorldContainer(mc.theWorld) != null){
			worldStats.syncWorkableMap(map);
		}
		globalStatsContainer.syncWorkableMap(map);
	}

	public static Map<Stat, Integer> getStatsFromFile(File file) {
		HashMap<Stat, Integer> map = new HashMap<Stat, Integer>();
		try {
			JsonParser jsonParser = new JsonParser();
			JsonObject rootObject = jsonParser.parseFileContent(file);
			JsonArray array = rootObject.getArray("stats-change");
			for (JsonElement element : array) {
				JsonObject object = element.getAsObject();
				JsonEntry entry = object.entryList().get(0);
				int i = Integer.parseInt(entry.name);
				int j = entry.value.getAsNumber().getInteger();
				Stat stat = StatList.getStat(i);
				if (stat == null) {
					System.out.println(i + " is not a valid stat");
					continue;
				}
				map.put(stat, j);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	public static String getStatFileContentString(String username, String localString, Map<Stat, Integer> map) {
		StringBuilder string1 = new StringBuilder();
		StringBuilder string2 = new StringBuilder();
		boolean flag = true;
		string1.append("{\r\n");
		if (username != null && localString != null) {
			string1.append("  \"user\":{\r\n");
			string1.append("    \"name\":\"").append(username).append("\",\r\n");
			string1.append("    \"sessionid\":\"").append(localString).append("\"\r\n");
			string1.append("  },\r\n");
		}
		string1.append("  \"stats-change\":[");
		for (Stat value : map.keySet()) {
			if (!flag) {
				string1.append("},");
			} else {
				flag = false;
			}
			string1.append("\r\n    {\"").append(value.statId).append("\":").append(map.get(value));
			string2.append(value.statGuid).append(",");
			string2.append(map.get(value)).append(",");
		}
		if (!flag) {
			string1.append("}");
		}
		MD5String md5string = new MD5String(localString);
		string1.append("\r\n  ],\r\n");
		string1.append("  \"checksum\":\"").append(md5string.getString(string2.toString())).append("\"\r\n");
		string1.append("}");
		return string1.toString();
	}
	public boolean isAchievementUnlocked(Achievement achievement) {
		StatsContainer worldStats = getWorldContainer(mc.theWorld);
		if (getWorldContainer(mc.theWorld) != null){
			return worldStats.isAchievementUnlocked(achievement);
		}
		return globalStatsContainer.isAchievementUnlocked(achievement);
	}
	public void func_27175_b() {
		// Does nothing but is called
	}
	public boolean isAchievementUnlockable(Achievement achievement) {
		return achievement.parent == null || this.isAchievementUnlocked(achievement.parent);
	}
	public int readStat(Stat statbase) {
		StatsContainer worldStats = getWorldContainer(mc.theWorld);
		if (getWorldContainer(mc.theWorld) != null){
			return worldStats.readStat(statbase);
		}
		return globalStatsContainer.readStat(statbase);
	}
	public void syncStats() {
		StatsContainer worldStats = getWorldContainer(mc.theWorld);
		if (getWorldContainer(mc.theWorld) != null){
			worldStats.sync();
		}
		globalStatsContainer.sync();
	}
	public void func_27178_d() {
		StatsContainer worldStats = getWorldContainer(mc.theWorld);
		if (getWorldContainer(mc.theWorld) != null){
			worldStats.saveIfModified();
		}
		globalStatsContainer.saveIfModified();
	}

	public static void relocateStatFiles(File minecraftFolder, File statsFolder) {
//		File[] files = minecraftFolder.listFiles();
//		Objects.requireNonNull(files);
//		for (File value : files) {
//			File file3;
//			if (!value.getName().startsWith("stats_") || !value.getName().endsWith(".dat") || (file3 = new File(statsFolder, value.getName())).exists())
//				continue;
//			System.out.println("Relocating " + value.getName());
//			value.renameTo(file3);
//		}
	}

}

