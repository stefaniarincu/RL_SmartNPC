package ro.smartnpc.world.classic;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import ro.smartnpc.world.WorldUtils;

import java.util.HashSet;
import java.util.Set;

public class WorldUtilsClassic implements WorldUtils {

    private Set<String> loadedWorldsCache = new HashSet<>();

    private World createWorld(String worldName) {
        WorldCreator wc = new WorldCreator(worldName);
        wc.generator(new EmptyChunkGenerator());
        wc.generateStructures(false);
        wc.type(WorldType.FLAT);
        return wc.createWorld();
    }

    private World loadWorldIntern(String worldName) {
        World alreadyExistingWorld = Bukkit.getWorld(worldName);
        if (alreadyExistingWorld != null) {
            return alreadyExistingWorld;
        }

        WorldCreator w = new WorldCreator(worldName);
        w.type(WorldType.FLAT);
        w.environment(World.Environment.NORMAL);
        return Bukkit.getServer().createWorld(w);
    }

    @Override
    public Set<String> getLoadedWorldsCache() {
        return loadedWorldsCache;
    }

    @Override
    public void deleteWorld(String worldName) {

    }

    @Override
    public World loadWorld(String worldName) {
        loadedWorldsCache.add(worldName);

        World loadAttempt = loadWorldIntern(worldName);
        if (loadAttempt != null) {
            return loadAttempt;
        }

        return createWorld(worldName);
    }
}
