/*
 * This is the latest source code of Healing Campfire.
 * Minecraft version: 1.16.5, mod version: 3.3.
 *
 * If you'd like access to the source code of previous Minecraft versions or previous mod versions, consider becoming a Github Sponsor or Patron.
 * You'll be added to a private repository which contains all versions' source of Healing Campfire ever released, along with some other perks.
 *
 * Github Sponsor link: https://github.com/sponsors/ricksouth
 * Patreon link: https://patreon.com/ricksouth
 *
 * Becoming a Sponsor or Patron allows me to dedicate more time to the development of mods.
 * Thanks for looking at the source code! Hope it's of some use to your project. Happy modding!
 */

package com.natamus.healingcampfire.events;

import java.util.List;

import com.natamus.collective.functions.FABFunctions;
import com.natamus.healingcampfire.config.ConfigHandler;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class CampfireEvent {
	@SubscribeEvent
	public void playerTickEvent(PlayerTickEvent e) {
		PlayerEntity player = e.player;
		World world = player.getCommandSenderWorld();
		if (world.isClientSide || !e.phase.equals(Phase.START)) {
			return;
		}
		
		if (player.tickCount % ConfigHandler.GENERAL.checkForCampfireDelayInTicks.get() != 0) {
			return;
		}
		
		List<BlockPos> nearbycampfires = FABFunctions.getAllTileEntityPositionsNearbyEntity(TileEntityType.CAMPFIRE, ConfigHandler.GENERAL.healingRadius.get(), world, player);
		if (nearbycampfires.size() == 0) {
			return;
		}
		
		EffectInstance effectinstance;
		if (ConfigHandler.GENERAL.hideEffectParticles.get()) {
			effectinstance = new EffectInstance(Effects.REGENERATION.getEffect(), ConfigHandler.GENERAL.effectDurationSeconds.get()*20, ConfigHandler.GENERAL.regenerationLevel.get()-1, true, false);
		}
		else {
			effectinstance = new EffectInstance(Effects.REGENERATION.getEffect(), ConfigHandler.GENERAL.effectDurationSeconds.get()*20, ConfigHandler.GENERAL.regenerationLevel.get()-1);
		}
		
		BlockPos campfire = null;
		for (BlockPos nearbycampfire : nearbycampfires) {
			BlockState campfirestate = world.getBlockState(nearbycampfire);
			Block block = campfirestate.getBlock();
			
			if (!ConfigHandler.GENERAL.enableEffectForNormalCampfires.get()) {
				if (block.equals(Blocks.CAMPFIRE)) {
					continue;
				}
			}
			if (!ConfigHandler.GENERAL.enableEffectForSoulCampfires.get()) {
				if (block.equals(Blocks.SOUL_CAMPFIRE)) {
					continue;
				}
			}
			
			if (ConfigHandler.GENERAL.campfireMustBeLit.get()) {
				Boolean islit = campfirestate.getValue(CampfireBlock.LIT);
				if (!islit) {
					continue;
				}
			}
			if (ConfigHandler.GENERAL.campfireMustBeSignalling.get()) {
				Boolean issignalling = campfirestate.getValue(CampfireBlock.SIGNAL_FIRE);
				if (!issignalling) {
					continue;
				}
			}
			
			campfire = nearbycampfire.immutable();
			break;
		}
		
		if (campfire == null) {
			return;
		}
		
		BlockPos ppos = player.blockPosition();
		double r = (double)ConfigHandler.GENERAL.healingRadius.get();
		if (ppos.closerThan(campfire, r)) {
			boolean addeffect = true;
			EffectInstance currentregen = player.getEffect(effectinstance.getEffect());
			if (currentregen != null) {
				int currentduration = currentregen.getDuration();
				if (currentduration > (ConfigHandler.GENERAL.effectDurationSeconds.get()*10)) {
					addeffect = false;
				}
			}
			
			if (addeffect) {
				player.addEffect(effectinstance);
			}
		}
		if (ConfigHandler.GENERAL.healPassiveMobs.get()) {
			for (Entity entity : world.getEntities(player, new AxisAlignedBB(campfire.getX()-r, campfire.getY()-r, campfire.getZ()-r, campfire.getX()+r, campfire.getY()+r, campfire.getZ()+r))) {
				if (entity instanceof LivingEntity && (entity instanceof PlayerEntity == false) && !entity.getType().getCategory().equals(EntityClassification.MONSTER)) {
					LivingEntity le = (LivingEntity)entity;
					
					boolean addeffect = true;
					EffectInstance currentregen = le.getEffect(effectinstance.getEffect());
					if (currentregen != null) {
						int currentduration = currentregen.getDuration();
						if (currentduration > (ConfigHandler.GENERAL.effectDurationSeconds.get()*10)) {
							addeffect = false;
						}
					}
					
					if (addeffect) {
						le.addEffect(effectinstance);
					}
				}
			}
		}
	}
}