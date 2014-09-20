/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.nospawnchunks.listeners;

import lombok.AllArgsConstructor;
import net.dmulloy2.nospawnchunks.NoSpawnChunks;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldInitEvent;

/**
 * @author dmulloy2
 */

@AllArgsConstructor
public class WorldListener implements Listener
{
	private final NoSpawnChunks plugin;

	@EventHandler(priority = EventPriority.MONITOR)
	public void onWorldInit(WorldInitEvent event)
	{
		World world = event.getWorld();
		if (plugin.isAllWorlds() || plugin.getWorlds().contains(world.getName().toLowerCase()))
			world.setKeepSpawnInMemory(plugin.isKeepSpawnInMemory());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerChangedWorld(PlayerChangedWorldEvent event)
	{
		World world = event.getFrom();
		if (plugin.isAllWorlds() || plugin.getWorlds().contains(world.getName().toLowerCase()))
		{
			if (world.getPlayers().isEmpty())
				plugin.unloadLater(world, 20L);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		if (player == null)
			return;

		World world = player.getWorld();
		if (world == null)
			return;

		if (plugin.isAllWorlds() || plugin.getWorlds().contains(world.getName().toLowerCase()))
		{
			if (world.getPlayers().isEmpty())
				plugin.unloadLater(world, 20L);
		}
	}
}