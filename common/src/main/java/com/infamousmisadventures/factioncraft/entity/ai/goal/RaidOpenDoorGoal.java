package com.infamousmisadventures.factioncraft.entity.ai.goal;


import com.infamousmisadventures.factioncraft.entity.data.MobRaiderData;
import com.infamousmisadventures.factioncraft.entity.data.holder.IMobRaiderDataHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

public class RaidOpenDoorGoal extends OpenDoorGoal {
    public RaidOpenDoorGoal(Mob p_i51284_2_) {
        super(p_i51284_2_, false);
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean canUse() {
        MobRaiderData raiderCapability = ((IMobRaiderDataHolder) this.mob).getOrCreateMobRaiderData();
        return raiderCapability.hasActiveRaid() && (super.canUse() || this.canUseSameSpot());
    }

    private boolean canUseSameSpot() {
        if (!GoalUtils.hasGroundPathNavigation(this.mob)) {
            return false;
        } else {
            GroundPathNavigation groundpathnavigation = (GroundPathNavigation)this.mob.getNavigation();
            Path path = groundpathnavigation.getPath();
            if (path != null && !path.isDone() && groundpathnavigation.canOpenDoors()) {
                for(int i = 0; i < Math.min(path.getNextNodeIndex() + 2, path.getNodeCount()); ++i) {
                    Node node = path.getNode(i);
                    this.doorPos = new BlockPos(node.x, node.y + 1, node.z);
                    if (!(this.mob.distanceToSqr((double)this.doorPos.getX(), this.mob.getY(), (double)this.doorPos.getZ()) > 2.25D)) {
                        this.hasDoor = DoorBlock.isWoodenDoor(this.mob.level(), this.doorPos);
                        if (this.hasDoor) {
                            return true;
                        }
                    }
                }
            }

            this.doorPos = this.mob.blockPosition().above();
            this.hasDoor = DoorBlock.isWoodenDoor(this.mob.level(), this.doorPos);
            return this.hasDoor;
        }
    }

    @Override
    public void stop() {
    }
}
