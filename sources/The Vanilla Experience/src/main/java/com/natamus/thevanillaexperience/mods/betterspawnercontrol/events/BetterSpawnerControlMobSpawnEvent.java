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

package com.natamus.thevanillaexperience.mods.betterspawnercontrol.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.natamus.collective.functions.BlockPosFunctions;
import com.natamus.collective.functions.WorldFunctions;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.TorchBlock;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.spawner.AbstractSpawner;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class BetterSpawnerControlMobSpawnEvent {
	final static List<Block> torchblocks = new ArrayList<Block>(Arrays.asList(Blocks.TORCH, Blocks.WALL_TORCH, Blocks.SOUL_TORCH, Blocks.SOUL_WALL_TORCH));
	
	@SubscribeEvent
	public void onMobSpawn(LivingSpawnEvent.CheckSpawn e) {
		World world = WorldFunctions.getWorldIfInstanceOfAndNotRemote(e.getWorld());
		if (world == null) {
			return;
		}

		AbstractSpawner msbl = e.getSpawner();
		if (msbl != null) {
			BlockPos spos = msbl.getPos();
			
			Boolean alltorches = true;
			for (BlockPos ap : BlockPosFunctions.getBlocksAround(spos, false)) {
				Block block = world.getBlockState(ap).getBlock();
				if (block instanceof TorchBlock == false && block instanceof WallTorchBlock == false) {
					alltorches = false;
					break;
				}
			}
			
			if (alltorches) {
				e.setResult(Result.DENY);
			}
		}
	}
}
