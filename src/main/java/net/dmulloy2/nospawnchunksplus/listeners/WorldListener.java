/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.nospawnchunksplus.listeners;

import lombok.AllArgsConstructor;
import net.dmulloy2.nospawnchunksplus.NoSpawnChunksPlus;

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
	private final NoSpawnChunksPlus plugin;

	@EventHandler(priority = EventPriority.MONITOR)
	public void onWorldInit(WorldInitEvent event)
	{
		World world = event.getWorld();
		world.setKeepSpawnInMemory( false );
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerChangedWorld(PlayerChangedWorldEvent event)
	{
		World world = event.getFrom();
		if ( world.getPlayers().isEmpty() )
			plugin.unloadAutomatically( world, 20L );
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		if ( player == null )
			return;

		World world = player.getWorld();
		if ( world == null )
			return;

		if ( world.getPlayers().isEmpty() )
			plugin.unloadAutomatically( world, 20L );
	}
}