package com.massivecraft.factions.engine;

import com.massivecraft.factions.AccessStatus;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.TerritoryAccess;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MConf;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.factions.util.AsciiMap;
import com.massivecraft.massivecore.Engine;
import com.massivecraft.massivecore.mixin.MixinTitle;
import com.massivecraft.massivecore.ps.PS;
import com.massivecraft.massivecore.util.MUtil;
import com.massivecraft.massivecore.util.Txt;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Collections;

public class EngineMoveChunk extends Engine
{
	// -------------------------------------------- //
	// INSTANCE & CONSTRUCT
	// -------------------------------------------- //

	private static EngineMoveChunk i = new EngineMoveChunk();
	public static EngineMoveChunk get() { return i; }

	// -------------------------------------------- //
	// MOVE CHUNK: DETECT
	// -------------------------------------------- //

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void moveChunkDetect(PlayerMoveEvent event)
	{
		// If the player is moving from one chunk to another ...
		if (MUtil.isSameChunk(event)&&isSameHeight(event.getFrom().getBlockY(),event.getTo().getBlockY())) return;
		Player player = event.getPlayer();
		if (MUtil.isntPlayer(player)) return;

		// ... gather info on the player and the move ...
		MPlayer mplayer = MPlayer.get(player);

		PS chunkFrom = PS.valueOf(event.getFrom());
		PS chunkTo = PS.valueOf(event.getTo());

		// ... send info onwards and try auto-claiming.
		if(MUtil.isSameChunk(event))
		{
			sendAutoMapUpdate(mplayer, player);
			tryAutoClaim(mplayer, chunkTo);
		}
		sendFactionTerritoryInfo(mplayer, player, chunkFrom, chunkTo);
		sendTerritoryAccessMessage(mplayer, chunkFrom, chunkTo);
	}
	
	//240/16
	private boolean isSameHeight(int yF, int yT)
	{
		if(!(yF >= 240 ^ yT >= 240)&&!(yF <= 16 ^ yT <= 16))
			return true;
		return false;
	}

	// -------------------------------------------- //
	// MOVE CHUNK: SEND CHUNK INFO
	// -------------------------------------------- //
	
	private static void sendAutoMapUpdate(MPlayer mplayer, Player player)
	{
		if (!mplayer.isMapAutoUpdating()) return;
		AsciiMap map = new AsciiMap(mplayer, player, false);
		mplayer.message(map.render());
	}
	
	private static void sendFactionTerritoryInfo(MPlayer mplayer, Player player, PS chunkFrom, PS chunkTo)
	{
		Faction factionFrom = BoardColl.get().getFactionAt(chunkFrom);
		Faction factionTo = BoardColl.get().getFactionAt(chunkTo);
		
		if(chunkFrom.getBlockY(true) >= 240 || chunkFrom.getBlockY(true) <= 16)
			factionFrom = Faction.get( Factions.ID_NONE );
		if(chunkTo.getBlockY(true) >= 240 || chunkTo.getBlockY(true) <= 16)
			factionTo = Faction.get( Factions.ID_NONE );
		
		if (factionFrom == factionTo) return;
		
		if (mplayer.isTerritoryInfoTitles())
		{
			String titleMain = parseTerritoryInfo(MConf.get().territoryInfoTitlesMain, mplayer, factionTo);
			String titleSub = parseTerritoryInfo(MConf.get().territoryInfoTitlesSub, mplayer, factionTo);
			int ticksIn = MConf.get().territoryInfoTitlesTicksIn;
			int ticksStay = MConf.get().territoryInfoTitlesTicksStay;
			int ticksOut = MConf.get().territoryInfoTitleTicksOut;
			MixinTitle.get().sendTitleMessage(player, ticksIn, ticksStay, ticksOut, titleMain, titleSub);
		}
		else
		{
			String message = parseTerritoryInfo(MConf.get().territoryInfoChat, mplayer, factionTo);
			player.sendMessage(message);
		}
	}
	
	private static String parseTerritoryInfo(String string, MPlayer mplayer, Faction faction)
	{
		if (string == null) throw new NullPointerException("string");
		if (faction == null) throw new NullPointerException("faction");
		
		string = Txt.parse(string);
		string = string.replace("{name}", faction.getName());
		string = string.replace("{relcolor}", faction.getColorTo(mplayer).toString());
		string = string.replace("{desc}", faction.getDescriptionDesc());
		
		return string;
	}
	
	private static void sendTerritoryAccessMessage(MPlayer mplayer, PS chunkFrom, PS chunkTo)
	{
		// Get TerritoryAccess for from & to chunks
		TerritoryAccess accessFrom = BoardColl.get().getTerritoryAccessAt(chunkFrom);
		TerritoryAccess accessTo = BoardColl.get().getTerritoryAccessAt(chunkTo);
		
		// See if the status has changed
		AccessStatus statusFrom = accessFrom.getTerritoryAccess(mplayer, chunkFrom);
		AccessStatus statusTo = accessTo.getTerritoryAccess(mplayer, chunkTo);
		if (statusFrom == statusTo) return;
		
		// Inform
		mplayer.message(statusTo.getStatusMessage());
	}

	// -------------------------------------------- //
	// MOVE CHUNK: TRY AUTO CLAIM
	// -------------------------------------------- //

	private static void tryAutoClaim(MPlayer mplayer, PS chunkTo)
	{
		// If the player is auto claiming ...
		Faction autoClaimFaction = mplayer.getAutoClaimFaction();
		if (autoClaimFaction == null) return;

		// ... try claim.
		mplayer.tryClaim(autoClaimFaction, Collections.singletonList(chunkTo));
	}

}
