/*
 * This is the latest source code of Difficulty Lock.
 * Minecraft version: 1.16.5, mod version: 1.2.
 *
 * If you'd like access to the source code of previous Minecraft versions or previous mod versions, consider becoming a Github Sponsor or Patron.
 * You'll be added to a private repository which contains all versions' source of Difficulty Lock ever released, along with some other perks.
 *
 * Github Sponsor link: https://github.com/sponsors/ricksouth
 * Patreon link: https://patreon.com/ricksouth
 *
 * Becoming a Sponsor or Patron allows me to dedicate more time to the development of mods.
 * Thanks for looking at the source code! Hope it's of some use to your project. Happy modding!
 */

package com.natamus.difficultylock.events;

import com.natamus.collective.functions.WorldFunctions;
import com.natamus.difficultylock.config.ConfigHandler;

import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IServerConfiguration;
import net.minecraft.world.storage.IWorldInfo;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class DifficultyLockEvent {
	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load e) {
		World world = WorldFunctions.getWorldIfInstanceOfAndNotRemote(e.getWorld());
		if (world == null) {
			return;
		}
		
		ServerWorld serverworld = (ServerWorld)world;
		IServerConfiguration serverconfiguration = serverworld.getServer().getServerConfiguration();
		
		IWorldInfo worldinfo = world.getWorldInfo();
		boolean islocked = worldinfo.isDifficultyLocked();
		if (islocked && !ConfigHandler.GENERAL.shouldChangeDifficultyWhenAlreadyLocked.get()) {
			return;
		}
		
		Difficulty currentdifficulty = worldinfo.getDifficulty();
		if (ConfigHandler.GENERAL.forcePeaceful.get()) {
			if (!currentdifficulty.equals(Difficulty.PEACEFUL)) {
				//worldinfo.setDifficulty(Difficulty.PEACEFUL);
				serverconfiguration.setDifficulty(Difficulty.PEACEFUL);
			}
		}
		else if (ConfigHandler.GENERAL.forceEasy.get()) {
			if (!currentdifficulty.equals(Difficulty.EASY)) {
				//worldinfo.setDifficulty(Difficulty.EASY);
				serverconfiguration.setDifficulty(Difficulty.EASY);
			}			
		}
		else if (ConfigHandler.GENERAL.forceNormal.get()) {
			if (!currentdifficulty.equals(Difficulty.NORMAL)) {
				//worldinfo.setDifficulty(Difficulty.NORMAL);
				serverconfiguration.setDifficulty(Difficulty.NORMAL);
			}			
		}
		else if (ConfigHandler.GENERAL.forceHard.get()) {
			if (!currentdifficulty.equals(Difficulty.HARD)) {
				//worldinfo.setDifficulty(Difficulty.HARD);
				serverconfiguration.setDifficulty(Difficulty.HARD);
			}			
		}
		
		if (ConfigHandler.GENERAL.shouldLockDifficulty.get()) {
			if (!islocked) {
				//worldinfo.setDifficultyLocked(true);
				serverconfiguration.setDifficultyLocked(true);
			}
		}
	}
}
