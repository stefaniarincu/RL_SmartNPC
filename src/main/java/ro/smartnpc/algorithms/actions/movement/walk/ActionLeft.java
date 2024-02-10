package ro.smartnpc.algorithms.actions.movement.walk;

import net.citizensnpcs.api.ai.event.NavigationCancelEvent;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import ro.smartnpc.SmartNPC;
import ro.smartnpc.algorithms.actions.Action;
import ro.smartnpc.algorithms.actions.ActionType;
import ro.smartnpc.algorithms.actions.movement.MovementUtil;
import ro.smartnpc.npc.EnvironmentNPC;

import java.util.concurrent.CompletableFuture;

public class ActionLeft implements Action, Listener {

    @Override
    public ActionType getActionType() {
        return ActionType.MOVE_LEFT;
    }

    public ActionLeft() {
        Bukkit.getPluginManager().registerEvents(this, SmartNPC.getInstance());
    }

    private CompletableFuture<Void> awaitingToComplete = null;
    private EnvironmentNPC envNPC = null;

    @EventHandler
    public void onNPCMove(NavigationCompleteEvent event) {
        if (envNPC == null)
            return;

        if (!event.getNPC().equals(envNPC.getNPC()))
            return;

        if (awaitingToComplete != null) {
            awaitingToComplete.complete(null);
            awaitingToComplete = null;
            envNPC = null;
        }
    }

    @EventHandler
    public void onNPCCancelMove(NavigationCancelEvent event) {
        if (envNPC == null)
            return;

        if (!event.getNPC().equals(envNPC.getNPC()))
            return;

        if (awaitingToComplete != null) {
            awaitingToComplete.complete(null);
            awaitingToComplete = null;
            envNPC = null;
        }
    }

    @Override
    public CompletableFuture<Void> execute(EnvironmentNPC envNPC) {
        NPC npc = envNPC.getNPC();
        Entity entity = npc.getEntity();
        entity.setRotation(entity.getLocation().getYaw() - 90, entity.getLocation().getPitch());

        Location target = MovementUtil.getFacingLocation(entity, 1);
        if (MovementUtil.canNavigateTo(envNPC, target)) {
            npc.getNavigator().setStraightLineTarget(target);
            awaitingToComplete = new CompletableFuture<>();
            this.envNPC = envNPC;
            return awaitingToComplete;
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void destroy() {
        if (awaitingToComplete != null) {
            awaitingToComplete.cancel(true);
            awaitingToComplete = null;
            envNPC = null;
        }

        HandlerList.unregisterAll(this);
    }
}
