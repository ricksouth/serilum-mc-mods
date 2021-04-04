/*
 * This is the latest source code of Areas.
 * Minecraft version: 1.16.5, mod version: 2.2.
 *
 * If you'd like access to the source code of previous Minecraft versions or previous mod versions, consider becoming a Github Sponsor or Patron.
 * You'll be added to a private repository which contains all versions' source of Areas ever released, along with some other perks.
 *
 * Github Sponsor link: https://github.com/sponsors/ricksouth
 * Patreon link: https://patreon.com/ricksouth
 *
 * Becoming a Sponsor or Patron allows me to dedicate more time to the development of mods.
 * Thanks for looking at the source code! Hope it's of some use to your project. Happy modding!
 */

package com.natamus.areas.objects;

import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Variables {
	public static HashMap<World, HashMap<BlockPos, AreaObject>> areasperworld = new HashMap<World, HashMap<BlockPos, AreaObject>>();
	public static HashMap<World, CopyOnWriteArrayList<BlockPos>> ignoresignsperworld = new HashMap<World, CopyOnWriteArrayList<BlockPos>>();
}
