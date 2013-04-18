package net.battlenexus.bukkit.battleend.end;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.battlenexus.bukkit.battleend.db.SQL;
import net.battlenexus.bukkit.battleend.main.BattleEnd;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EndParasprite extends Thread implements Listener {
	private long unixtime;
	private boolean countdown;
	private BattleEnd plugin;

	public EndParasprite(BattleEnd plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	private void checkWorld() {
		plugin.log("No time set! Looking for an EnderDragon");
		final String END_NAME = Bukkit.getWorlds().get(0).getName() + "_the_end";
		final World world = Bukkit.getServer().getWorld(END_NAME);
		boolean dragon_found = false;
		try {
			for (int x = -13; x <= 13; x++) {
				for (int z = -13; z <= 13; z++) {
					if (world.isChunkLoaded(x, z))
						world.loadChunk(x, z);
					final Chunk c = world.getChunkAt(x, z);
					Entity[] list = c.getEntities();
					for (Entity e : list) {
						if (e != null && e instanceof EnderDragon) {
							dragon_found = true;
							break;
						}
					}
					if (dragon_found)
						break;
				}
				if (dragon_found)
					break;
			}
			List<Chunk> loadedChunks = new ArrayList<Chunk>(Arrays.asList(world.getLoadedChunks()));

			for (Object localObject2 = loadedChunks.iterator(); ((Iterator<Chunk>)localObject2).hasNext(); ) { Chunk c = ((Iterator<Chunk>)localObject2).next();
			if ((c.isLoaded()) && (c != null)) {
				world.unloadChunkRequest(c.getX(), c.getZ(), true);
			}
			}
			loadedChunks.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (dragon_found) {
			plugin.log("EnderDragon found.");
			plugin.log("Time will not be set..");
		}
		else {
			plugin.log("No dragon found. It must of died!");
			plugin.log("Setting reset time..");
			setNextTime(plugin.getNextRun());
		}
	}

	@Override
	public void run() {
		while (countdown) {
			if (interrupted()) {
				countdown = false;
				break;
			}
			if (plugin.getCurrentTime() >= unixtime) {
				countdown = false;
				resetEnd();
				break;
			}
			try {
				Thread.sleep(3600000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public long getNextRun() {
		return countdown ? unixtime : 0;
	}

	public void setNextTime(long nextRun) {
		if (nextRun <= 0) {
			checkWorld();
			return;
		}
		this.unixtime = nextRun;
		if (plugin.getSQL() != null) {
			final SQL sql = plugin.getSQL();
			//TODO SQL shit
		}
		if (!countdown) {
			countdown = true;
			start();
		}
	}

	private void resetEnd() {
		plugin.getServer().broadcastMessage(ChatColor.RED + "* The End is being reset. *");
		plugin.getServer().broadcastMessage(ChatColor.RED + "* Any attempts to access to that world will teleport to spawn! * ");
		final String END_NAME = Bukkit.getWorlds().get(0).getName() + "_the_end";
		final World world = Bukkit.getServer().getWorld(END_NAME);
		try {
			Player[] players = world.getPlayers().toArray(new Player[world.getPlayers().size()]);
			for (Player p : players) {
				p.getLocation().setWorld(Bukkit.getWorlds().get(0));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		world.setAutoSave(false);
		try {
			for (int x = -13; x <= 13; x++) {
				for (int z = -13; z <= 13; z++) {
					if (world.isChunkLoaded(x, z))
						world.loadChunk(x, z);
					final Chunk c = world.getChunkAt(x, z);
					Entity[] list = c.getEntities();
					for (Entity e : list) {
						if (e != null)
							e.remove();
					}

					list = null;
					boolean regenResults = false;
					try
					{
						if (!world.isChunkLoaded(x, z)) {
							world.loadChunk(x, z);
						}
						regenResults = world.regenerateChunk(x, z);
					}
					catch (NullPointerException npe)
					{
						plugin.logWarning("NPE when regenerating chunk @ " + x + ", " + z + "! Is chunk loaded?");
						regenResults = false;
					}
					catch (Exception e) {
						plugin.logWarning("Unhandled exception during reset.");
						e.printStackTrace();
						regenResults = false;
						return;
					}
					finally {
						if (!regenResults)
							plugin.logWarning("Could not regenerate chunk @ " + x + ", " + z + "!");
					}
				}
			}

			List<Chunk> loadedChunks = new ArrayList<Chunk>(Arrays.asList(world.getLoadedChunks()));

			for (Object localObject2 = loadedChunks.iterator(); ((Iterator<Chunk>)localObject2).hasNext(); ) { Chunk c = ((Iterator<Chunk>)localObject2).next();
			if ((c.isLoaded()) && (c != null)) {
				world.unloadChunkRequest(c.getX(), c.getZ(), true);
			}
			}
			loadedChunks.clear();

			world.setAutoSave(true);
		} catch (Exception e) {
			e.printStackTrace();
		}

		plugin.getServer().broadcastMessage(ChatColor.GREEN + "* The End is ready! Good luck fighting the dragon that lurks inside! *");
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void killed(EntityDeathEvent event) {
		if (countdown)
			return;
		Entity e = event.getEntity();
		if (e instanceof EnderDragon && e.getWorld().getEnvironment() == World.Environment.THE_END) {
			final Player[] players = e.getWorld().getPlayers().toArray(new Player[e.getWorld().getPlayers().size()]);
			event.setDroppedExp(plugin.getExtCount(event.getDroppedExp(), players));
			String pstring = "";
			for (int i = 0; i < players.length; i++) {
				if (i + 1 >= players.length && i != 0)
					pstring += "and " + players[i].getDisplayName();
				else
					pstring += players[i].getDisplayName() + (players.length == 1 ? " " : ", ");
			}
			plugin.getServer().broadcastMessage(pstring + ChatColor.GREEN + (players.length > 1 ? "have" : "has") + " defeated the " + ChatColor.DARK_RED + "EnderDragon!");
			setNextTime(plugin.getNextRun());
			plugin.getServer().broadcastMessage(ChatColor.GREEN + "* The End will reset in " + plugin.getDays() + " " + (plugin.getDays() == 1 ? "day" : "days") + " *");
			plugin.addPlayers(players);
		}
	}

	public void saveData() {
		EntityDeathEvent.getHandlerList().unregister(this);
		plugin.log("Saving time to SQL");
		//TODO Save SQL data
		plugin.log("Done!");
	}
}
