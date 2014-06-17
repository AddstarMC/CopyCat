package au.com.addstar.copycat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class EventListener implements Listener
{
	@EventHandler
	private void onWorldLoad(WorldLoadEvent event)
	{
		CopyCatPlugin.instance.loadWorld(event.getWorld());
	}
	
	@EventHandler
	private void onWorldUnload(WorldUnloadEvent event)
	{
		CopyCatPlugin.instance.unloadWorld(event.getWorld());
	}
	
	@EventHandler
	private void onPlayerLeave(PlayerQuitEvent event)
	{
		EditSession session = EditSession.activeSessions.get(event.getPlayer());
		if(session != null)
			session.end();
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	private void onPlayerLeave(PlayerKickEvent event)
	{
		EditSession session = EditSession.activeSessions.get(event.getPlayer());
		if(session != null)
			session.end();
	}
	
	@EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
	private void onBlockPlace(BlockPlaceEvent event)
	{
		EditSession session = EditSession.activeSessions.get(event.getPlayer());
		if(session != null)
		{
			if(!session.isLocationOk(event.getBlock().getLocation()))
				event.setCancelled(true);
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
	private void onBlockBreak(BlockBreakEvent event)
	{
		EditSession session = EditSession.activeSessions.get(event.getPlayer());
		if(session != null)
		{
			if(!session.isLocationOk(event.getBlock().getLocation()))
				event.setCancelled(true);
		}
	}
}
