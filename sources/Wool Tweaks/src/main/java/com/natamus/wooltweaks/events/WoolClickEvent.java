/*
 * This is the latest source code of Wool Tweaks.
 * Minecraft version: 1.16.5, mod version: 1.3.
 *
 * If you'd like access to the source code of previous Minecraft versions or previous mod versions, consider becoming a Github Sponsor or Patron.
 * You'll be added to a private repository which contains all versions' source of Wool Tweaks ever released, along with some other perks.
 *
 * Github Sponsor link: https://github.com/sponsors/ricksouth
 * Patreon link: https://patreon.com/ricksouth
 *
 * Becoming a Sponsor or Patron allows me to dedicate more time to the development of mods.
 * Thanks for looking at the source code! Hope it's of some use to your project. Happy modding!
 */

package com.natamus.wooltweaks.events;

import com.natamus.wooltweaks.util.Util;

import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarpetBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BedPart;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class WoolClickEvent {
	@SubscribeEvent
	public void onWoolClick(PlayerInteractEvent.RightClickBlock e) {
		World world = e.getWorld();
		if (world.isClientSide) {
			return;
		}
		
		ItemStack handstack = e.getItemStack();
		Item handitem = handstack.getItem();
		if (handitem instanceof DyeItem == false) {
			return;
		}
		
		BlockPos target = e.getPos();
		BlockState state = world.getBlockState(target);
		Block block = state.getBlock();
		
		Block newblock = null;
		if (block.getTags().contains(new ResourceLocation("wool"))) {
			newblock = Util.woolblocks.get(handitem);
		}
		else if (block instanceof BedBlock) {
			newblock = Util.bedblocks.get(handitem);
		}
		else if (block instanceof CarpetBlock) {
			newblock = Util.carpetblocks.get(handitem);
		}
		else {
			return;
		}
		
		if (newblock == null) {
			return;
		}
		
		if (block.equals(newblock)) {
			return;
		}
		
		e.setCanceled(true);
		
		BlockState newstate = newblock.defaultBlockState();
		if (block instanceof BedBlock) {
			Direction direction = state.getValue(BedBlock.FACING);
			newstate = newstate.setValue(BedBlock.FACING, direction);
			newstate = newstate.setValue(BedBlock.OCCUPIED, state.getValue(BedBlock.OCCUPIED));
			
			BedPart bedpart = state.getValue(BedBlock.PART);
			newstate = newstate.setValue(BedBlock.PART, bedpart);
			
			BlockPos othertarget = target.immutable();
			BedPart otherpart;
			if (bedpart.equals(BedPart.HEAD)) {
				otherpart = BedPart.FOOT;
				othertarget = target.relative(direction.getOpposite());
				
				world.setBlockAndUpdate(target, Blocks.AIR.defaultBlockState());
				world.setBlockAndUpdate(othertarget, Blocks.AIR.defaultBlockState());
			}
			else {
				otherpart = BedPart.HEAD;
				othertarget = target.relative(direction);
				
				world.setBlockAndUpdate(othertarget, Blocks.AIR.defaultBlockState());
				world.setBlockAndUpdate(target, Blocks.AIR.defaultBlockState());
			}
			
			world.setBlockAndUpdate(othertarget, newstate.setValue(BedBlock.PART, otherpart));
		}
		
		world.setBlockAndUpdate(target, newstate);
		
		PlayerEntity player = e.getPlayer();
		if (!player.isCreative()) {
			handstack.shrink(1);
		}
	}
}