package ro.smartnpc.algorithms.actions.movement;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import ro.smartnpc.npc.EnvironmentNPC;

public class MovementUtil {

    public static boolean canNavigateTo(EnvironmentNPC envNPC, Location target) {
        if (target.getBlock().isSolid())
            return false;

        if (target.getBlock().getRelative(BlockFace.UP).isSolid())
            return false;

        return true;
    }

    public static Location getFacingLocation(Entity entity, double distance) {
        Location target = entity.getLocation().clone().add(entity.getFacing().getDirection().normalize().multiply(distance));
        target.setY(entity.getLocation().getY());
        return target;
    }
}
