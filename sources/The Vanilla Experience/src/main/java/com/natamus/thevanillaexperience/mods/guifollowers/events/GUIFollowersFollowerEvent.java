/*
 * This is the latest source code of The Vanilla Experience.
 * Minecraft version: 1.16.5, mod version: 1.2.
 *
 * If you'd like access to the source code of previous Minecraft versions or previous mod versions, consider becoming a Github Sponsor or Patron.
 * You'll be added to a private repository which contains all versions' source of The Vanilla Experience ever released, along with some other perks.
 *
 * Github Sponsor link: https://github.com/sponsors/ricksouth
 * Patreon link: https://patreon.com/ricksouth
 *
 * Becoming a Sponsor or Patron allows me to dedicate more time to the development of mods.
 * Thanks for looking at the source code! Hope it's of some use to your project. Happy modding!
 */

package com.natamus.thevanillaexperience.mods.guifollowers.events;

import java.util.ArrayList;
import java.util.List;

import com.natamus.thevanillaexperience.mods.guifollowers.config.GUIFollowersConfigHandler;
import com.natamus.thevanillaexperience.mods.guifollowers.util.GUIFollowersVariables;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class GUIFollowersFollowerEvent {
	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent e) {
		if (!e.phase.equals(Phase.START)) {
			return;
		}
		
		PlayerEntity player = e.player;
		if (player == null) {
			return;
		}
		
		if (player.tickCount % (20* GUIFollowersConfigHandler.GENERAL.timeBetweenChecksInSeconds.get()) != 0) {
			return;
		}
		
		World world = player.getCommandSenderWorld();
		if (world == null) {
			return;
		}

		int dc = GUIFollowersConfigHandler.GENERAL.distanceToCheckForFollowersAround.get(); // distancecheck
		if (dc <= 0) {
			return;
		}
		
		Vector3d pvec = player.position();
		List<Entity> entitiesaround = world.getEntities(player, new AxisAlignedBB(pvec.x-dc, pvec.y-dc, pvec.z-dc, pvec.x+dc, pvec.y+dc, pvec.z+dc));
		for (Entity ea : entitiesaround) {
			if (ea instanceof TameableEntity == false) {
				continue;
			}
			
			TameableEntity te = (TameableEntity)ea;
			if (!te.isTame()) {
				continue;
			}
			
			if (!te.isOwnedBy(player)) {
				continue;
			}
			
			if (te.isInSittingPose()) {
				continue;
			}
			
			if (!GUIFollowersVariables.activefollowers.contains(ea)) {
				GUIFollowersVariables.activefollowers.add(ea);
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerLogout(PlayerLoggedOutEvent e) {
		GUIFollowersVariables.activefollowers = new ArrayList<Entity>();
	}
	
	@SubscribeEvent
	public void onKey(InputEvent.KeyInputEvent e) {
		if (e.getAction() != 1) {
			return;
		}

		if (e.getKey() == GUIFollowersVariables.clearlist_hotkey.getKey().getValue()) {
			GUIFollowersVariables.activefollowers = new ArrayList<Entity>();
		}
	}
}
