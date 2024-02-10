package ro.smartnpc.listeners;

import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.ai.event.NavigationReplaceEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MoveListener implements Listener {

    @EventHandler
    public void onMove(NavigationCompleteEvent event) {
        Bukkit.broadcastMessage("NPC MOVED");
    }

    @EventHandler
    public void onMove(NavigationReplaceEvent event) {
        Bukkit.broadcastMessage("NPC REPLACED MOVEMENT");
    }

}
