/*
 * This is the latest source code of Giant Spawn.
 * Minecraft version: 1.16.5, mod version: 2.5.
 *
 * If you'd like access to the source code of previous Minecraft versions or previous mod versions, consider becoming a Github Sponsor or Patron.
 * You'll be added to a private repository which contains all versions' source of Giant Spawn ever released, along with some other perks.
 *
 * Github Sponsor link: https://github.com/sponsors/ricksouth
 * Patreon link: https://patreon.com/ricksouth
 *
 * Becoming a Sponsor or Patron allows me to dedicate more time to the development of mods.
 * Thanks for looking at the source code! Hope it's of some use to your project. Happy modding!
 */

package com.natamus.giantspawn.ai;

import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.monster.GiantEntity;

public class GiantAttackGoal extends MeleeAttackGoal {
	   private int field_234037_i_; // attackTick
	   private final GiantEntity giant;
	   private int raiseArmTicks;

	   public GiantAttackGoal(GiantEntity giantIn, double speedIn, boolean longMemoryIn) {
	      super(giantIn, speedIn, longMemoryIn);
	      this.giant = giantIn;
	   }

	   /**
	    * Execute a one shot task or start executing a continuous task
	    */
	   public void startExecuting() {
	      super.startExecuting();
	      this.raiseArmTicks = 0;
	   }

	   /**
	    * Reset the task's internal state. Called when this task is interrupted by another one
	    */
	   public void resetTask() {
	      super.resetTask();
	      this.giant.setAggroed(false);
	   }

	   /**
	    * Keep ticking a continuous task that has already been started
	    */
	   public void tick() {
	      super.tick();
	      ++this.raiseArmTicks;
	      if (this.raiseArmTicks >= 5 && this.field_234037_i_ < 10) {  // attackTick
	         this.giant.setAggroed(true);
	      } else {
	         this.giant.setAggroed(false);
	      }

	   }
}
