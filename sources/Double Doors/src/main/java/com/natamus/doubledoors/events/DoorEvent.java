/*
 * This is the latest source code of Double Doors.
 * Minecraft version: 1.16.5, mod version: 2.4.
 *
 * If you'd like access to the source code of previous Minecraft versions or previous mod versions, consider becoming a Github Sponsor or Patron.
 * You'll be added to a private repository which contains all versions' source of Double Doors ever released, along with some other perks.
 *
 * Github Sponsor link: https://github.com/sponsors/ricksouth
 * Patreon link: https://patreon.com/ricksouth
 *
 * Becoming a Sponsor or Patron allows me to dedicate more time to the development of mods.
 * Thanks for looking at the source code! Hope it's of some use to your project. Happy modding!
 */

package com.natamus.doubledoors.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.natamus.collective.functions.WorldFunctions;
import com.natamus.doubledoors.config.ConfigHandler;
import com.natamus.doubledoors.util.Util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.block.StoneButtonBlock;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.block.WoodButtonBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class DoorEvent {
	private static List<BlockPos> prevpoweredpos = new ArrayList<BlockPos>();
	private static HashMap<BlockPos, Integer> prevbuttonpos = new HashMap<BlockPos, Integer>();
	
	@SubscribeEvent
	public void onNeighbourNotice(BlockEvent.NeighborNotifyEvent e) {
		World world = WorldFunctions.getWorldIfInstanceOfAndNotRemote(e.getWorld());
		if (world == null) {
			return;
		}
		
		BooleanProperty proppowered = BlockStateProperties.POWERED;
		BlockPos pos = e.getPos().immutable();
		BlockState state = e.getState();
		Block block = state.getBlock();
		
		if (block instanceof PressurePlateBlock == false) {
			if (block instanceof StoneButtonBlock == false && block instanceof WoodButtonBlock == false) {
				return;
			}
			else {
				if (prevbuttonpos.containsKey(pos)) {
					prevbuttonpos.remove(pos);
				}
				else {
					prevbuttonpos.put(pos, 1);
					return;
				}
				
				if (!state.getValue(proppowered)) {
					if (!prevpoweredpos.contains(pos)) {
						return;
					}
					prevpoweredpos.remove(pos);
				}
			}
		}
		else {
			if (!state.getValue(proppowered)) {
				if (!prevpoweredpos.contains(pos)) {
					return;
				}
			}
		}
		
		boolean playsound = true;
		boolean stateprop = state.getValue(proppowered);
		
		Iterator<BlockPos> blocksaround = BlockPos.betweenClosedStream(pos.getX()-1, pos.getY(), pos.getZ()-1, pos.getX()+1, pos.getY()+1, pos.getZ()+1).iterator();
		
		BlockPos doorpos = null;
		while (blocksaround.hasNext()) {
			BlockPos npos = blocksaround.next().immutable();
			BlockState ostate = world.getBlockState(npos);
			if (Util.isDoorBlock(ostate)) {
				doorpos = npos;
				break;
			}
		}
		
		if (doorpos != null) {
			if (processDoor(world, doorpos, world.getBlockState(doorpos), stateprop, playsound)) {
				if (stateprop) {
					prevpoweredpos.add(pos);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onDoorClick(PlayerInteractEvent.RightClickBlock e) {
		World world = e.getWorld();
		if (world.isClientSide && e.getHand().equals(Hand.MAIN_HAND)) {
			return;
		}
		
		PlayerEntity player = e.getPlayer();
		if (player.isShiftKeyDown()) {
			return;
		}
		
		BlockPos cpos = e.getPos();
		BlockState clickstate = world.getBlockState(cpos);

		if (!Util.isDoorBlock(clickstate)) {
			return;
		}
		if (clickstate.getMaterial().equals(Material.METAL)) {
			return;
		}
		
		if (processDoor(world, cpos, clickstate, null, true)) {
			e.setUseBlock(Result.DENY);
			e.setCanceled(true);		
		}
	}
	
	private boolean processDoor(World world, BlockPos pos, BlockState state, Boolean isopen, Boolean playsound) {
		Block block = state.getBlock();
		if (block instanceof DoorBlock) {
			if (state.getValue(DoorBlock.HALF).equals(DoubleBlockHalf.UPPER)) {
				pos = pos.below().immutable();
				state = world.getBlockState(pos);
			}
		}
		
		if (isopen == null) {
			isopen = !state.getValue(BlockStateProperties.OPEN);
		}
		
		int yoffset = 0;
		if (block instanceof DoorBlock == false) {
			yoffset = 1;
		}
		
		Iterator<BlockPos> blocksaround = BlockPos.betweenClosedStream(pos.getX()-1, pos.getY()-1, pos.getZ()-1, pos.getX()+1, pos.getY()+yoffset, pos.getZ()+1).iterator();
		while (blocksaround.hasNext()) {
			BlockPos bpa = blocksaround.next();
			if (bpa.equals(pos)) {
				continue;
			}
			BlockState ostate = world.getBlockState(bpa);
			Block oblock = ostate.getBlock();
			if (Util.isDoorBlock(ostate)) {
				if (oblock.getRegistryName().equals(block.getRegistryName())) {
					if (oblock instanceof DoorBlock) {
						if (!ConfigHandler.GENERAL.enableDoors.get()) {
							continue;
						}
						
						DoorBlock door = (DoorBlock)oblock;
						if (state.getValue(DoorBlock.HINGE).equals(ostate.getValue(DoorBlock.HINGE))) {
							continue;
						}
						
						if (playsound) {
							door.setOpen(world, state, pos, isopen); // toggleDoor
						}
						else {
							world.setBlock(pos, state.setValue(DoorBlock.OPEN, Boolean.valueOf(isopen)), 10);
						}
						world.setBlock(bpa, ostate.setValue(DoorBlock.OPEN, Boolean.valueOf(isopen)), 10);
						return true;
					}
					else if (oblock instanceof TrapDoorBlock) {
						if (!ConfigHandler.GENERAL.enableTrapdoors.get()) {
							continue;
						}
						
						if (playsound) {
							if (isopen) {
								int i = ostate.getMaterial() == Material.METAL ? 1037 : 1007;
								world.levelEvent(null, i, pos, 0);
							} else {
								int j = ostate.getMaterial() == Material.METAL ? 1036 : 1013;
								world.levelEvent(null, j, pos, 0);
							}
						}

						world.setBlock(pos, state.setValue(BlockStateProperties.OPEN, Boolean.valueOf(isopen)), 10);
						world.setBlock(bpa, ostate.setValue(BlockStateProperties.OPEN, Boolean.valueOf(isopen)), 10);
						return true;
					}
					else if (oblock instanceof FenceGateBlock) {
						if (!ConfigHandler.GENERAL.enableFenceGates.get()) {
							continue;
						}
						
						world.setBlock(pos, state.setValue(DoorBlock.OPEN, Boolean.valueOf(isopen)), 10);
						world.setBlock(bpa, ostate.setValue(DoorBlock.OPEN, Boolean.valueOf(isopen)), 10);
						return true;
					}
				}
			}
		}
		
		return false;
	}
}