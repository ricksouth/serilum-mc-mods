/*
 * This is the latest source code of Player Tracking Compass.
 * Minecraft version: 1.16.5, mod version: 1.6.
 *
 * If you'd like access to the source code of previous Minecraft versions or previous mod versions, consider becoming a Github Sponsor or Patron.
 * You'll be added to a private repository which contains all versions' source of Player Tracking Compass ever released, along with some other perks.
 *
 * Github Sponsor link: https://github.com/sponsors/ricksouth
 * Patreon link: https://patreon.com/ricksouth
 *
 * Becoming a Sponsor or Patron allows me to dedicate more time to the development of mods.
 * Thanks for looking at the source code! Hope it's of some use to your project. Happy modding!
 */

package com.natamus.playertrackingcompass.network;

import java.util.function.Supplier;

import com.natamus.collective.functions.StringFunctions;
import com.natamus.playertrackingcompass.Main;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

public class RequestServerPacket {
	public RequestServerPacket() {}

	public RequestServerPacket(PacketBuffer buf) {}

	public void fromBytes(PacketBuffer buf) {}

	public void toBytes(PacketBuffer buf) {}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			BlockPos targetpos = new BlockPos(0, 0, 0);
			
			ServerPlayerEntity serverplayer = ctx.get().getSender();
			BlockPos serverplayerpos = serverplayer.getPosition();
			BlockPos comparepp = new BlockPos(serverplayerpos.getX(), 1, serverplayerpos.getZ());
			
			ServerPlayerEntity closestplayer = null;
			double closestdistance = 999999999999.0;
			
			ServerWorld world = serverplayer.getServerWorld();
			for (ServerPlayerEntity oplayer : world.getPlayers()) {
				BlockPos oplayerpos = oplayer.getPosition();
				BlockPos compareop = new BlockPos(oplayerpos.getX(), 1, oplayerpos.getZ());

				double distance = comparepp.manhattanDistance(compareop);
				if (distance < 10) {
					continue;
				}
				if (distance < closestdistance) {
					closestdistance = distance;
					closestplayer = oplayer;
				}
			}
			
			if (closestplayer != null) {
				targetpos = closestplayer.getPosition().toImmutable();
				
				StringFunctions.sendMessage(serverplayer, "The compass is pointing at " + closestplayer.getName().getString() + ".", TextFormatting.YELLOW);
			}
			else {
				StringFunctions.sendMessage(serverplayer, "Unable to redirect the compass. There are no players around or they're too close.", TextFormatting.YELLOW);
			}
			Main.network.sendTo(new PacketToClientUpdateTarget(targetpos), serverplayer.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
		});
		ctx.get().setPacketHandled(true);
	}
}
