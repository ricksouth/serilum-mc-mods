/*
 * This is the latest source code of Death Backup.
 * Minecraft version: 1.16.5, mod version: 1.5.
 *
 * If you'd like access to the source code of previous Minecraft versions or previous mod versions, consider becoming a Github Sponsor or Patron.
 * You'll be added to a private repository which contains all versions' source of Death Backup ever released, along with some other perks.
 *
 * Github Sponsor link: https://github.com/sponsors/ricksouth
 * Patreon link: https://patreon.com/ricksouth
 *
 * Becoming a Sponsor or Patron allows me to dedicate more time to the development of mods.
 * Thanks for looking at the source code! Hope it's of some use to your project. Happy modding!
 */

package com.natamus.deathbackup.events;

import com.natamus.collective.functions.DateFunctions;
import com.natamus.collective.functions.PlayerFunctions;
import com.natamus.collective.functions.StringFunctions;
import com.natamus.deathbackup.config.ConfigHandler;
import com.natamus.deathbackup.util.Util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class DeathBackupEvent {

	@SubscribeEvent
	public void onPlayerDeath(LivingDeathEvent e) {
		Entity entity = e.getEntity();
		World world = entity.getCommandSenderWorld();
		if (world.isClientSide) {
			return;
		}
		
		if (entity instanceof PlayerEntity == false) {
			return;
		}
		
		if (world instanceof ServerWorld == false) {
			return;
		}
		
		ServerWorld serverworld = (ServerWorld)world;
		PlayerEntity player = (PlayerEntity)entity;
		String playername = player.getName().getString().toLowerCase();
		
		String gearstring = PlayerFunctions.getPlayerGearString(player);
		if (gearstring == "") {
			return;
		}
		
		String nowstring = DateFunctions.getNowInYmdhis();
		Util.writeGearStringToFile(serverworld, playername, nowstring, gearstring);
		
		if (ConfigHandler.GENERAL.sendBackupReminderMessageToThoseWithAccessOnDeath.get()) {
			if (player.hasPermissions(2)) {
				StringFunctions.sendMessage(player, ConfigHandler.GENERAL.backupReminderMessage.get(), TextFormatting.DARK_GRAY);
			}
		}
	}
}
