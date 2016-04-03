/**
 * NoSpawnChunks - a Bukkit plugin
 * Copyright (C) - 2014 dmulloy2
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.dmulloy2.nospawnchunks;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import lombok.Getter;
import net.dmulloy2.nospawnchunks.listeners.WorldListener;
import net.dmulloy2.nospawnchunks.util.FormatUtil;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author dmulloy2
 */

public class NoSpawnChunks extends JavaPlugin
{
	private @Getter List<String> worlds;

	private @Getter boolean keepSpawnInMemory;
	private @Getter boolean autoEnabled;
	private @Getter boolean allWorlds;
	private @Getter int interval;

	private boolean gcTask, gcUnloading;

	private final String prefix = FormatUtil.format("&3[&eNoSpawnChunks&3]&e ");

	@Override
	public void onEnable()
	{
		long start = System.currentTimeMillis();

		// Configuration
		saveDefaultConfig();
		reloadConfig();
		loadConfig();

		// Register Commands
		getCommand("nsc").setExecutor(this);

		// Register listener
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new WorldListener(this), this);

		// Deploy unload task
		if (autoEnabled)
		{
			class UnloadChunksTask extends BukkitRunnable
			{
				@Override
				public void run()
				{
					unloadChunks(false);

					if (gcTask)
						runGarbageCollector();
				}
			}

			new UnloadChunksTask().runTaskTimer(this, 60L, interval);
		}

		log("{0} has been enabled. Took {1} ms.", getDescription().getFullName(), System.currentTimeMillis() - start);
	}

	@Override
	public void onDisable()
	{
		getServer().getScheduler().cancelTasks(this);

		log("{0} has been disabled.", getDescription().getFullName());
	}

	// ---- Logging

	public void log(Level level, String string, Object... objects)
	{
		getLogger().log(level, FormatUtil.format(string, objects));
	}

	public void log(String string, Object... objects)
	{
		log(Level.INFO, string, objects);
	}

	// ---- Load and Reload

	public void reload()
	{
		reloadConfig();
		loadConfig();
	}

	private void loadConfig()
	{
		worlds = new ArrayList<>();
		for (String world : getConfig().getStringList("worlds"))
			worlds.add(world.toLowerCase());

		autoEnabled = getConfig().getBoolean("task.enabled");
		allWorlds = worlds.isEmpty() || worlds.contains("*");
		interval = getConfig().getInt("task.interval", 15) * 60 * 20;
		keepSpawnInMemory = getConfig().getBoolean("keepSpawnInMemory", false);

		gcTask = getConfig().getBoolean("garbageCollector.task");
		gcUnloading = getConfig().getBoolean("garbageCollector.unloading");
	}

	public int unloadChunks(boolean all)
	{
		long start = System.currentTimeMillis();
		log("Unloading chunks...");

		int unloadedChunks = 0;

		for (World world : getServer().getWorlds())
		{
			if (all || allWorlds || worlds.contains(world.getName().toLowerCase()))
			{
				for (Chunk chunk : world.getLoadedChunks())
				{
					if (! world.isChunkInUse(chunk.getX(), chunk.getZ()))
					{
						if (chunk.unload(true))
							unloadedChunks++;
					}
				}
			}
		}

		log("Unloaded {0} chunks. Took {1} ms.", unloadedChunks, System.currentTimeMillis() - start);
		return unloadedChunks;
	}

	public int unloadChunks(World world)
	{
		long start = System.currentTimeMillis();
		log("Unloading chunks in world {0}...", world.getName());

		int unloadedChunks = 0;

		for (Chunk chunk : world.getLoadedChunks())
		{
			if (! world.isChunkInUse(chunk.getX(), chunk.getZ()))
			{
				if (chunk.unload(true))
					unloadedChunks++;
			}
		}

		log("Unloaded {0} chunks. Took {1} ms.", unloadedChunks, System.currentTimeMillis() - start);
		return unloadedChunks;
	}

	public void unloadLater(final World world, long delay)
	{
		class UnloadLaterTask extends BukkitRunnable
		{
			@Override
			public void run()
			{
				unloadChunks(world);

				if (gcUnloading)
					runGarbageCollector();
			}
		}

		new UnloadLaterTask().runTaskLater(this, delay);
	}

	// ---- Garbage Collection

	public void runGarbageCollector()
	{
		long freeMemoryStart = Runtime.getRuntime().freeMemory();

		System.gc();

		long freeMemoryEnd = Runtime.getRuntime().freeMemory();
		long diff = (freeMemoryEnd - freeMemoryStart) / 1024 / 1024;
		if (diff > 0)
			log("Freed {0} mb!", diff);
	}

	// ---- Command Handling

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		onCommand(sender, args);
		return true;
	}

	private void onCommand(CommandSender sender, String[] args)
	{
		if (args.length == 0 || args[0].equalsIgnoreCase("version"))
		{
			sender.sendMessage(prefix + FormatUtil.format("&bNoSpawnChunks &ev&b{0} &eby &bdmulloy2", getDescription().getVersion()));
			return;
		}

		if (args[0].equalsIgnoreCase("unload") || args[0].equalsIgnoreCase("unloadchunks"))
		{
			if (! sender.hasPermission("nospawnchunks.unloadchunks"))
			{
				sender.sendMessage(prefix + FormatUtil.format("&4You do not have permission to do this!"));
				return;
			}

			unloadChunks(sender, args);
			return;
		}

		if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl"))
		{
			if (! sender.hasPermission("nospawnchunks.reload"))
			{
				sender.sendMessage(prefix + FormatUtil.format("&4You do not have permission to do this!"));
				return;
			}

			sender.sendMessage(prefix + FormatUtil.format("&eReloading..."));

			reload();

			sender.sendMessage(prefix + FormatUtil.format("&eNoSpawnChunks has been reloaded!"));
			return;
		}

		sender.sendMessage(prefix + FormatUtil.format("&4Unknown command!"));
		return;
	}

	private void unloadChunks(CommandSender sender, String[] args)
	{
		boolean all = false;
		if (args.length > 0)
			all = args[0].equalsIgnoreCase("all");

		World world = null;
		if (args.length > 0)
			world = getServer().getWorld(args[0]);

		sender.sendMessage(prefix + FormatUtil.format("&eUnloading chunks..."));
		log("Unloading chunks...");

		int unloaded = 0;

		if (world != null)
			unloaded = unloadChunks(world);
		else
			unloaded = unloadChunks(all);

		sender.sendMessage(prefix + FormatUtil.format("&b{0} &echunks unloaded!", unloaded));

		if (gcUnloading)
			runGarbageCollector();
	}
}