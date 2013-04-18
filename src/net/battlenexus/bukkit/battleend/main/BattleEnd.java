package net.battlenexus.bukkit.battleend.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;
import net.battlenexus.bukkit.battleend.db.SQL;
import net.battlenexus.bukkit.battleend.end.EndParasprite;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class BattleEnd extends JavaPlugin
{
	private static final Logger log = Logger.getLogger("Minecraft");
	private SQL sql;
	private int days;
	private boolean allowrepeatorbs;
	private ArrayList<String> names = new ArrayList<String>();
	private EndParasprite parasprite;

	public void onLoad()
	{
		saveDefaultConfig();
	}

	public void onEnable()
	{
		long nextunixtime = 0L;
		days = getConfig().getInt("battleend.time-to-reset");
		allowrepeatorbs = getConfig().getBoolean("battleend.allowexprepeat");
		File f = new File(getDataFolder(), "cache.bak");
		if (f.exists()) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(f.getAbsoluteFile()));
				nextunixtime = Long.parseLong(in.readLine());
				in.close();
				f.delete();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (allowrepeatorbs) {
			File ff = new File(getDataFolder(), "pcache.bak");
			if (ff.exists()) {
				try {
					BufferedReader in = new BufferedReader(new FileReader(f.getAbsoluteFile()));
					String line;
					while ((line = in.readLine()) != null) {
						names.add(line);
					}
					in.close();
					f.delete();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		log("The End Reset set to " + days + " days.");
		log("Next reset @" + nextunixtime + "(Unix Time)");
		log("Brewing up a parasprite");
		parasprite = new EndParasprite(this);
		log("Setting time");
		parasprite.setNextTime(nextunixtime);
		log("All set!");
	}

	public void log(String message) {
		log.info("[BattleEnd] " + message);
	}

	public void onDisable()
	{
		if (parasprite != null) {
			log("Clearing cache..");
			File cache = new File(getDataFolder(), "cache.bak");
			if (cache.exists()) {
				if (parasprite.getNextRun() != 0)
					cache.delete();
				else
					cache.deleteOnExit();
			}
			if (parasprite.getNextRun() != 0) {
				log("Saving time..");
				FileWriter write = null;
				try {
					write = new FileWriter(cache);
					write.write("" + parasprite.getNextRun());
					write.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
				finally {
					if (write != null) {
						try {
							write.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
			log("Saving SQL data..");
			parasprite.saveData();
		}
		if (allowrepeatorbs) {
			log("Saving player cache..");
			File cache = new File(getDataFolder(), "pcache.bak");
			if (cache.exists())
					cache.delete();
			FileWriter write = null;
			try {
				write = new FileWriter(cache);
				for (String s : names) {
					write.write(s + "\n");
				}
				write.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				if (write != null) {
					try {
						write.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		log("All set!");
	}

	public SQL getSQL()
	{
		return sql;
	}

	public void logWarning(String string) {
		log.warning("[BattleEnd] " + string);
	}

	public long getNextRun() {
		return getCurrentTime() + (days * 86400L);
	}

	public int getDays() {
		return days;
	}
	
	public long getCurrentTime() {
		return System.currentTimeMillis() / 1000L;
	}
	
	public int getExtCount(int currentcount, Player[] players) {
		if (allowrepeatorbs)
			return currentcount;
		int i = 0;
		for (Player p : players) {
			if (names.contains(p.getName()))
				i++;
		}
		if (i >= players.length)
			return 0;
		return currentcount;
	}
	
	public void addPlayers(Player[] players) {
		for (Player p : players) {
			if (!names.contains(p.getName()))
				names.add(p.getName());
		}
	}
}