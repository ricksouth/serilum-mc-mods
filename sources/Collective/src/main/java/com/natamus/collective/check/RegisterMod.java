/*
 * This is the latest source code of Collective.
 * Minecraft version: 1.16.5, mod version: 2.27.
 *
 * If you'd like access to the source code of previous Minecraft versions or previous mod versions, consider becoming a Github Sponsor or Patron.
 * You'll be added to a private repository which contains all versions' source of Collective ever released, along with some other perks.
 *
 * Github Sponsor link: https://github.com/sponsors/ricksouth
 * Patreon link: https://patreon.com/ricksouth
 *
 * Becoming a Sponsor or Patron allows me to dedicate more time to the development of mods.
 * Thanks for looking at the source code! Hope it's of some use to your project. Happy modding!
 */

package com.natamus.collective.check;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.natamus.collective.config.ConfigHandler;
import com.natamus.collective.functions.DataFunctions;
import com.natamus.collective.functions.StringFunctions;
import com.natamus.collective.functions.WorldFunctions;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class RegisterMod {
	private static List<String> jarlist = new ArrayList<String>();
	private static HashMap<String, String> jartoname = new HashMap<String, String>();
	public static boolean shouldDoCheck = true;
	
	public static void register(String modname, String modid, String modversion, String forgeversion) {
		String jarname = modid + "_" + forgeversion.replaceAll("\\[", "").replaceAll("\\]","") + "-" + modversion + ".jar";
		if (modid.equalsIgnoreCase("collective")) {
			jarname = jarname.replaceAll("_", "-");
		}
		
		jarlist.add(jarname);
		jartoname.put(jarname, modname);
	}
	
	public static void initialProcess() {
		if (!ConfigHandler.COLLECTIVE.enableAntiRepostingCheck.get()) {
			shouldDoCheck = false;
		}
		else if (!checkAlternative()) {
			shouldDoCheck = false;
		}
	}
	
	public static void joinWorldProcess(World world, PlayerEntity player) {
		if (world instanceof ServerWorld == false) {
			return;
		}
		
		List<String> wrongmodnames = checkIfAllJarsExist();
		if (wrongmodnames.size() > 0) {
			if (processPreJoinWorldCheck(world)) {
				String s = "";
				if (wrongmodnames.size() > 1) {
					s = "s";
				}
				
				String projecturl = "https://curseforge.com/members/serilum/projects";
				StringFunctions.sendMessage(player, "Mod" + s + " from incorrect sources:", TextFormatting.RED, projecturl);
				for (String wrongmodname : wrongmodnames) {
					StringFunctions.sendMessage(player, " " + wrongmodname, TextFormatting.YELLOW, projecturl);
				}
				
				StringFunctions.sendMessage(player, "You are receiving this message because you are using some of Serilum's mods, but probably haven't downloaded them from the original source. Unofficial sources can contain malicious software, supply no income for developers and host outdated versions.", TextFormatting.RED, projecturl);
				StringFunctions.sendMessage(player, "Serilum's mod downloads are only officially available at:", TextFormatting.DARK_GREEN, projecturl);
				StringFunctions.sendMessage(player, " https://curseforge.com/members/serilum/projects (click)", TextFormatting.YELLOW, projecturl);
				StringFunctions.sendMessage(player, "You won't see this message again in this instance. Thank you for reading.", TextFormatting.DARK_GREEN, projecturl);
				StringFunctions.sendMessage(player, "-Rick (Serilum)", TextFormatting.YELLOW, projecturl);
				
				processPostJoinWorldCheck(world);
			}
		}
		
		shouldDoCheck = false;
	}
	
	private static boolean processPreJoinWorldCheck(World world) {
		String checkfilepath = WorldFunctions.getWorldPath((ServerWorld)world) + File.separator + "data" + File.separator + "collective" + File.separator + "checked.txt";
		File checkfile = new File(checkfilepath);
		if (checkfile.exists()) {
			shouldDoCheck = false;
		}
		else if (!checkAlternative()) {
			shouldDoCheck = false;
		}
		
		return shouldDoCheck;
	}
	
	private static void processPostJoinWorldCheck(World world) {
		shouldDoCheck = false;

		String worlddatapath = WorldFunctions.getWorldPath((ServerWorld)world) + File.separator + "data" + File.separator + "collective";
		File dir = new File(worlddatapath);
		dir.mkdirs();
		
		try {
			PrintWriter writer = new PrintWriter(worlddatapath + File.separator + "checked.txt", "UTF-8");
			writer.println("# Please check out https://stopmodreposts.org/ for more information on why this feature exists.");
			writer.println("checked=true");
			writer.close();
		} catch (Exception e) { }
		
		String alternativecheckpath = System.getProperty("user.dir") + File.separator + "data" + File.separator + "collective";
		File alternativedir = new File(alternativecheckpath);
		alternativedir.mkdirs();
		
		try {
			PrintWriter writer = new PrintWriter(alternativecheckpath + File.separator + "checked.txt", "UTF-8");
			writer.println("# Please check out https://stopmodreposts.org/ for more information on why this feature exists.");
			writer.println("checked=true");
			writer.close();
		} catch (Exception e) { }
	}
	
	private static List<String> checkIfAllJarsExist() {
		List<String> installedmods = DataFunctions.getInstalledModJars();
		
		List<String> wrongmodnames = new ArrayList<String>();
		for (String jarname : jarlist) {
			if (!installedmods.contains(jarname) && jartoname.containsKey(jarname)) {
				wrongmodnames.add(jartoname.get(jarname));
			}
		}
		
		if (wrongmodnames.size() > 0) {
			Collections.sort(wrongmodnames);
		}
		return wrongmodnames;
	}
	
	private static boolean checkAlternative() {
		String alternativecheckfilepath = System.getProperty("user.dir") + File.separator + "data" + File.separator + "collective" + File.separator + "checked.txt";
		File alternativecheckfile = new File(alternativecheckfilepath);
		if (alternativecheckfile.exists()) {
			return false;
		}
		return true;
	}
}
