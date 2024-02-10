package ro.smartnpc.world;

import org.bukkit.World;

import java.util.Set;

public interface WorldUtils {

    Set<String> getLoadedWorldsCache();

    void deleteWorld(String worldName);

    World loadWorld(String worldName);
}
