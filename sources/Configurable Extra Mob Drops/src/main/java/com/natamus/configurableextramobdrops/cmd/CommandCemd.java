/*
 * This is the latest source code of Configurable Extra Mob Drops.
 * Minecraft version: 1.16.5, mod version: 1.4.
 *
 * If you'd like access to the source code of previous Minecraft versions or previous mod versions, consider becoming a Github Sponsor or Patron.
 * You'll be added to a private repository which contains all versions' source of Configurable Extra Mob Drops ever released, along with some other perks.
 *
 * Github Sponsor link: https://github.com/sponsors/ricksouth
 * Patreon link: https://patreon.com/ricksouth
 *
 * Becoming a Sponsor or Patron allows me to dedicate more time to the development of mods.
 * Thanks for looking at the source code! Hope it's of some use to your project. Happy modding!
 */

package com.natamus.configurableextramobdrops.cmd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.natamus.collective.functions.StringFunctions;
import com.natamus.configurableextramobdrops.util.Util;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TextFormatting;

public class CommandCemd {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
    	dispatcher.register(Commands.literal("cemd").requires((iCommandSender) -> iCommandSender.hasPermissionLevel(2))
			.executes((command) -> {
				CommandSource source = command.getSource();
				
				showUsage(source);
				return 1;
			})
			.then(Commands.literal("usage")
			.executes((command) -> {
				CommandSource source = command.getSource();
				
				showUsage(source);
				return 1;
			}))
			.then(Commands.literal("list")
			.executes((command) -> {
				CommandSource source = command.getSource();
				
				ArrayList<String> mobnames = new ArrayList<String>();
				for (EntityType<?> et : Util.mobdrops.keySet()) {
					String[] nspl = et.getRegistryName().toString().toLowerCase().split(":");
					if (nspl.length < 2) {
						continue;
					}
					
					mobnames.add(nspl[1]);
				}
				
				Collections.sort(mobnames);
				
				String output = "";
				for (String mobname : mobnames) {
					if (output != "") {
						output += ", ";
					}
					
					output += mobname;
				}
				
				output += ".";
				
				StringFunctions.sendMessage(source, "Available entity names:", TextFormatting.DARK_GREEN, true);
				StringFunctions.sendMessage(source, output, TextFormatting.YELLOW);
				StringFunctions.sendMessage(source, "To add a drop: /cemd addhand <entity-name>", TextFormatting.DARK_GRAY);
				return 1;
			}))
			.then(Commands.literal("reload")
			.executes((command) -> {
				CommandSource source = command.getSource();
				
		    	try {
					Util.loadMobConfigFile();
				} catch (Exception ex) {
					StringFunctions.sendMessage(source, "Something went wrong while reloading the mob drop config file.", TextFormatting.RED);
					ex.printStackTrace();
					return 0;
				}
		    	
		    	StringFunctions.sendMessage(source, "Successfully loaded the mob drop config file.", TextFormatting.DARK_GREEN);
				return 1;
			}))
			.then(Commands.literal("addhand")
			.then(Commands.argument("entity-name", StringArgumentType.word())
			.executes((command) -> {
				return processAddhand(command, 1.0);
			})))
			.then(Commands.literal("addhand")
			.then(Commands.argument("entity-name", StringArgumentType.word())
			.then(Commands.argument("drop-chance", DoubleArgumentType.doubleArg())
			.executes((command) -> {
				CommandSource source = command.getSource();
				
				double chance = DoubleArgumentType.getDouble(command, "drop-chance");
				if (chance < 0 || chance > 1.0) {
					StringFunctions.sendMessage(source, "The chance has to be in between 0 and 1.0.", TextFormatting.RED);
					return 0;
				}
				
				return processAddhand(command, chance);
			}))))
			.then(Commands.literal("cleardrops")
			.then(Commands.argument("entity-name", StringArgumentType.word())
			.executes((command) -> {
				CommandSource source = command.getSource();

				String entityname = StringArgumentType.getString(command, "entity-name").toLowerCase().trim();
				EntityType<?> entitytype = null;
				
				
				for (EntityType<?> et : Util.mobdrops.keySet()) {
					String registrystring = et.getRegistryName().toString();
					if (!registrystring.contains(":")) {
						continue;
					}

					if (registrystring.split(":")[1].equalsIgnoreCase(entityname)) {
						entitytype = et;
						break;
					}
				}
				
				if (entitytype == null) {
					StringFunctions.sendMessage(source, "Unable to find an entity with the name '" + entityname + "'.", TextFormatting.RED);
					showList(source);
					return 0;
				}
				
				if (!Util.mobdrops.containsKey(entitytype)) {
					StringFunctions.sendMessage(source, "Unable to find an entity with the name '" + entityname + "' in the drop hashmap.", TextFormatting.RED);
					showList(source);
					return 0;					
				}
				
				Util.mobdrops.put(entitytype, new CopyOnWriteArrayList<ItemStack>());
				
				try {
					if (!Util.writeDropsMapToFile()) {
						StringFunctions.sendMessage(source, "!Something went wrong while writing the new config.", TextFormatting.RED);
					}
				} catch (Exception ex) {
					StringFunctions.sendMessage(source, "Something went wrong while writing the new config.", TextFormatting.RED);
					ex.printStackTrace();
				}
				
				StringFunctions.sendMessage(source, "Successfully cleared all drops for the entity '" + entitytype.getName().getString() + "'.", TextFormatting.DARK_GREEN);
				return 1;
			})))
		);
    }
    
    private static int processAddhand(CommandContext<CommandSource> command, double dropchance) {
    	CommandSource source = command.getSource();
    	
    	PlayerEntity player;
		try {
			player = source.asPlayer();
		}
		catch (CommandSyntaxException ex) {
			StringFunctions.sendMessage(source, "This command can only be executed as a player in-game.", TextFormatting.RED);
			return 1;
		}
		
		String entityname = StringArgumentType.getString(command, "entity-name").toLowerCase().trim();
		EntityType<?> entitytype = null;
		
		for (EntityType<?> et : Util.mobdrops.keySet()) {
			String registryname = et.getRegistryName().toString().toLowerCase();
			if (!registryname.contains(":")) {
				continue;
			}
			
			if (registryname.split(":")[1].equalsIgnoreCase(entityname)) {
				entitytype = et;
				break;
			}
		}
		
		if (entitytype == null) {
			StringFunctions.sendMessage(source, "Unable to find an entity with the name '" + entityname + "'.", TextFormatting.RED);
			showList(source);
			return 0;
		}
		
		if (!Util.mobdrops.containsKey(entitytype)) {
			StringFunctions.sendMessage(source, "Unable to find an entity with the name '" + entityname + "' in the drop hashmap.", TextFormatting.RED);
			showList(source);
			return 0;					
		}
		
		ItemStack hand = player.getHeldItemMainhand();
		if (hand.isEmpty()) {
			StringFunctions.sendMessage(source, "Your hand is empty! Unable to add drop.", TextFormatting.RED);
			return 0;
		}
		
		ItemStack toadd = hand.copy();
		CompoundNBT nbt = toadd.getOrCreateTag();
		nbt.putDouble("dropchance", dropchance);
		toadd.setTag(nbt);
		
		Util.mobdrops.get(entitytype).add(toadd.copy());
		
		try {
			if (!Util.writeDropsMapToFile()) {
				StringFunctions.sendMessage(source, "!Something went wrong while writing the new config.", TextFormatting.RED);
			}
		} catch (Exception ex) {
			StringFunctions.sendMessage(source, "Something went wrong while writing the new config.", TextFormatting.RED);
			ex.printStackTrace();
		}
		
		StringFunctions.sendMessage(source, "Successfully added '" + toadd.getCount() + " " + toadd.getDisplayName().getString().toLowerCase() + "' as a drop for the entity '" + entitytype.getName().getString() + "' with a drop chance of '" + dropchance + "'.", TextFormatting.DARK_GREEN);
		return 1;
    }
    
    private static void showUsage(CommandSource source) {
		StringFunctions.sendMessage(source, "Configurable Extra Mob Drops Usage:", TextFormatting.DARK_GREEN, true);
		StringFunctions.sendMessage(source, " /cemd usage", TextFormatting.DARK_GREEN);
		StringFunctions.sendMessage(source, "  Show this message.", TextFormatting.DARK_GRAY);
		StringFunctions.sendMessage(source, " /cemd list", TextFormatting.DARK_GREEN);
		StringFunctions.sendMessage(source, "  Lists available entities to add drops to.", TextFormatting.DARK_GRAY);
		StringFunctions.sendMessage(source, " /cemd reload", TextFormatting.DARK_GREEN);
		StringFunctions.sendMessage(source, "  Reloads the config file.", TextFormatting.DARK_GRAY);
		StringFunctions.sendMessage(source, " /cemd addhand <entity-name>", TextFormatting.DARK_GREEN);
		StringFunctions.sendMessage(source, "  Add your hand to the entity's drops with a 100% chance.", TextFormatting.DARK_GRAY);
		StringFunctions.sendMessage(source, " /cemd addhand <entity-name> <drop-chance>", TextFormatting.DARK_GREEN);
		StringFunctions.sendMessage(source, "  Add your hand to the entity's drops with drop-chance in between 0 and 1.0.", TextFormatting.DARK_GRAY);
		StringFunctions.sendMessage(source, " /cemd cleardrops <entity-name>", TextFormatting.DARK_GREEN);
		StringFunctions.sendMessage(source, "  Clears all drops of the specified entity.", TextFormatting.DARK_GRAY);
    }
    
    private static void showList(CommandSource source) {
		StringFunctions.sendMessage(source, " /cemd list", TextFormatting.DARK_GREEN);
		StringFunctions.sendMessage(source, "  Lists available entities to add drops to.", TextFormatting.DARK_GRAY);
    }
}
