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

package com.natamus.collective.functions;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

public class DataFunctions {
	public static String readStringFromURL(String requestURL) {
		String data = "";
	    try (Scanner scanner = new Scanner(new URL(requestURL).openStream(), StandardCharsets.UTF_8.toString())) {
	        scanner.useDelimiter("\\A");
	        data = scanner.hasNext() ? scanner.next() : "";
	    }
	    catch(Exception ex) {}
	    
	    return data;
	}
	
	public static List<String> getInstalledModJars() {
		List<String> installedmods = new ArrayList<String>();
		
		List<ModInfo> mods = ModList.get().getMods();
		for (ModInfo modinfo : mods) {
			String filename = modinfo.getOwningFile().getFile().getFileName().replaceAll(" +\\([0-9]+\\)", "");
			installedmods.add(filename);
		}
		
		return installedmods;
	}
}
