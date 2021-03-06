/*
 * This is the latest source code of GUI Compass.
 * Minecraft version: 1.16.5, mod version: 1.6.
 *
 * If you'd like access to the source code of previous Minecraft versions or previous mod versions, consider becoming a Github Sponsor or Patron.
 * You'll be added to a private repository which contains all versions' source of GUI Compass ever released, along with some other perks.
 *
 * Github Sponsor link: https://github.com/sponsors/ricksouth
 * Patreon link: https://patreon.com/ricksouth
 *
 * Becoming a Sponsor or Patron allows me to dedicate more time to the development of mods.
 * Thanks for looking at the source code! Hope it's of some use to your project. Happy modding!
 */

package com.natamus.guicompass.events;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.natamus.guicompass.config.ConfigHandler;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GUIEvent extends IngameGui {
	private static Minecraft mc;

	public GUIEvent(Minecraft mc){
		super(mc);
		GUIEvent.mc = mc; 
	}
	
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public void renderOverlay(RenderGameOverlayEvent.Pre e) {
		ElementType type = e.getType();
		if (type != ElementType.TEXT) {
			return;
		}
		
		if (ConfigHandler.GENERAL.mustHaveCompassInInventory.get()) {
			boolean found = false;
			PlayerInventory inv = mc.player.inventory;
			for (int n = 0; n <= 35; n++) {
				if (inv.getItem(n).getItem().equals(Items.COMPASS)) {
					found = true;
					break;
				}
			}
			if (!found) {
				return;
			}
		}
		
		String coordinates = getCoordinates();

		FontRenderer fontRender = mc.font;
		MainWindow scaled = mc.getWindow();
		int width = scaled.getGuiScaledWidth();
		
		int stringWidth = fontRender.width(coordinates);
		
		Color colour = new Color(ConfigHandler.GENERAL.RGB_R.get(), ConfigHandler.GENERAL.RGB_G.get(), ConfigHandler.GENERAL.RGB_B.get(), 255);
			
		GL11.glPushMatrix();
		
		int xcoord = 0;
		if (ConfigHandler.GENERAL.compassPositionIsLeft.get()) {
			xcoord = 5;
		}
		else if (ConfigHandler.GENERAL.compassPositionIsCenter.get()) {
			xcoord = (width/2) - (stringWidth/2);
		}
		else {
			xcoord = width - stringWidth - 5;
		}

		fontRender.draw(new MatrixStack(), coordinates, xcoord, ConfigHandler.GENERAL.compassHeightOffset.get(), colour.getRGB());
		
		GL11.glPopMatrix();
	}

	private static List<String> direction = new ArrayList<String>(Arrays.asList("S", "SW", "W", "NW", "N", "NE", "E", "SE"));
	private static String getCoordinates() {
		ClientPlayerEntity player = mc.player;
		BlockPos ppos = player.blockPosition();
		
		int yaw = (int)player.getYHeadRot();
		if (yaw < 0) {
			yaw += 360;
		}
		yaw+=22;
		yaw%=360;

		int facing = yaw/45;
		if (facing < 0) {
			facing = facing * -1;
		}
		
		return direction.get(facing) + ": " + ppos.getX() + ", " + ppos.getY() + ", " + ppos.getZ();
	}
}