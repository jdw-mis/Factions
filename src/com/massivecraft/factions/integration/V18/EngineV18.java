package com.massivecraft.factions.integration.V18;

import com.massivecraft.factions.Factions;
import com.massivecraft.factions.engine.EngineFlagExplosion;
import com.massivecraft.massivecore.Engine;
import com.massivecraft.massivecore.MassivePlugin;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockExplodeEvent;

import java.util.Collection;

public class EngineV18 extends Engine
{
	// -------------------------------------------- //
	// INSTANCE & CONSTRUCT
	// -------------------------------------------- //

	private static EngineV18 i = new EngineV18();
	public static EngineV18 get() { return i; }

	@Override
	public MassivePlugin getActivePlugin()
	{
		return Factions.get();
	}

	// -------------------------------------------- //
	// LISTENER
	// -------------------------------------------- //
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void blockExplosion(BlockExplodeEvent event)
	{
		Location location = event.getBlock().getLocation();
		Cancellable cancellable = event;
		Collection<Block> blocks = event.blockList();
		
		EngineFlagExplosion.get().blockExplosion(location, cancellable, blocks);
	}
	
}
