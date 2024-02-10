package ro.smartnpc.algorithms.actions.movement.teleport;

import net.citizensnpcs.api.ai.event.NavigationCancelEvent;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.event.NPCTeleportEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import ro.smartnpc.SmartNPC;
import ro.smartnpc.algorithms.actions.Action;
import ro.smartnpc.algorithms.actions.ActionType;
import ro.smartnpc.algorithms.actions.movement.MovementUtil;
import ro.smartnpc.npc.EnvironmentNPC;

import java.util.concurrent.CompletableFuture;

public class ActionRight implements Action{

    @Override
    public ActionType getActionType() {
        return ActionType.MOVE_RIGHT;
    }

    public ActionRight() {

    }

    @Override
    public CompletableFuture<Void> execute(EnvironmentNPC envNPC) {
        NPC npc = envNPC.getNPC();
        Entity entity = npc.getEntity();
        entity.setRotation(entity.getLocation().getYaw() + 90, entity.getLocation().getPitch());

        Location target = MovementUtil.getFacingLocation(entity, 1);
        if (MovementUtil.canNavigateTo(envNPC, target)) {
            npc.teleport(target, PlayerTeleportEvent.TeleportCause.PLUGIN);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void destroy() {

    }
}
