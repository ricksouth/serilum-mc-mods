/*
 * This is the latest source code of Better Conduit Placement.
 * Minecraft version: 1.16.5, mod version: 1.3.
 *
 * If you'd like access to the source code of previous Minecraft versions or previous mod versions, consider becoming a Github Sponsor or Patron.
 * You'll be added to a private repository which contains all versions' source of Better Conduit Placement ever released, along with some other perks.
 *
 * Github Sponsor link: https://github.com/sponsors/ricksouth
 * Patreon link: https://patreon.com/ricksouth
 *
 * Becoming a Sponsor or Patron allows me to dedicate more time to the development of mods.
 * Thanks for looking at the source code! Hope it's of some use to your project. Happy modding!
 */

package com.natamus.betterconduitplacement.events;

import com.natamus.betterconduitplacement.config.ConfigHandler;
import com.natamus.betterconduitplacement.util.Util;
import com.natamus.collective.functions.BlockFunctions;
import com.natamus.collective.functions.WorldFunctions;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ConduitEvent {
	@SubscribeEvent
	public void onWaterClick(PlayerInteractEvent.RightClickItem e) {
		World world = e.getWorld();
		if (world.isClientSide) {
			return;
		}
		
		ItemStack itemstack = e.getItemStack();
		if (!itemstack.getItem().equals(Items.CONDUIT)) {
			return;
		}
		
		PlayerEntity player = e.getPlayer();
		Vector3d look = player.getLookAngle();
		float distance = 2.0F;
		double dx = player.getX() + (look.x * distance);
		double dy = player.getY() + (look.y * distance) + 2;
		double dz = player.getZ() + (look.z * distance);
		
		BlockPos frontpos = new BlockPos(dx, dy, dz);
		
		if (!world.getBlockState(frontpos).getBlock().equals(Blocks.WATER)) {
			return;
		}
		
		if (!player.isCreative()) {
			itemstack.shrink(1);
		}
		
		world.setBlockAndUpdate(frontpos, Blocks.CONDUIT.defaultBlockState());
	}
	
	@SubscribeEvent
	public void onConduitClick(PlayerInteractEvent.RightClickBlock e) {
		World world = e.getWorld();
		if (world.isClientSide) {
			return;
		}
		
		BlockPos cpos = e.getPos();
		if (!world.getBlockState(cpos).getBlock().equals(Blocks.CONDUIT)) {
			return;
		}
		
		ItemStack hand = e.getItemStack();
		if (!BlockFunctions.isOneOfBlocks(Util.conduitblocks, hand)) {
			return;
		}
		
		boolean set = false;
		PlayerEntity player = e.getPlayer();
		while (hand.getCount() > 0) {
			BlockPos nextpos = Util.getNextLocation(world, cpos);
			if (nextpos == null) {
				break;
			}
			
			Block block = world.getBlockState(nextpos).getBlock();
			if (ConfigHandler.GENERAL.dropReplacedBlockTopConduit.get()) {
				if (!block.equals(Blocks.AIR) && !block.equals(Blocks.WATER)) {
					ItemEntity ei = new ItemEntity(world, cpos.getX(), cpos.getY()+1, cpos.getZ(), new ItemStack(block, 1));
					world.addFreshEntity(ei);
				}
			}
			
			if (!player.isCreative()) {
				hand.shrink(1);
			}
			
			world.setBlockAndUpdate(nextpos, Block.byItem(hand.getItem()).defaultBlockState());
			
			set = true;
			if (!player.isShiftKeyDown()) {
				break;
			}
		}
		
		if (set) {
			e.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public void onBlockBreak(BlockEvent.BreakEvent e) {
		World world = WorldFunctions.getWorldIfInstanceOfAndNotRemote(e.getWorld());
		if (world == null) {
			return;
		}
		
		if (!ConfigHandler.GENERAL.breakConduitBlocks.get()) {
			return;
		}
		
		BlockPos bpos = e.getPos();
		if (!world.getBlockState(bpos).getBlock().equals(Blocks.CONDUIT)) {
			return;
		}
		
		Util.destroyAllConduitBlocks(world, bpos);
	}
}
