package au.com.addstar.copycat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
}
