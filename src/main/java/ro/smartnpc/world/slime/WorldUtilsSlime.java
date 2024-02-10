package ro.smartnpc.world.slime;

import com.infernalsuite.aswm.api.SlimePlugin;
import com.infernalsuite.aswm.api.exceptions.*;
import com.infernalsuite.aswm.api.world.SlimeWorld;
import com.infernalsuite.aswm.api.world.properties.SlimeProperties;
import com.infernalsuite.aswm.api.world.properties.SlimePropertyMap;
import org.bukkit.Bukkit;
import org.bukkit.World;
import ro.smartnpc.world.WorldUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class WorldUtilsSlime implements WorldUtils {

    private final SlimePlugin slimePlugin;
    private final SlimePropertyMap properties;
    private SlimeWorld loadedEmptySlimeWorld;

    private Set<String> loadedWorldsCache = new HashSet<>();

    public WorldUtilsSlime() {
        this.slimePlugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");

        properties = new SlimePropertyMap() {{
            setValue(SlimeProperties.DIFFICULTY, "peaceful");
            setValue(SlimeProperties.WORLD_TYPE, "default_1_1");
            setValue(SlimeProperties.SPAWN_X, 0);
            setValue(SlimeProperties.SPAWN_Y, 60);
            setValue(SlimeProperties.SPAWN_Z, 0);
        }};

        try {
            loadedEmptySlimeWorld = slimePlugin.createEmptyWorld(slimePlugin.getLoader("file"), "empty", true, properties);
        } catch (WorldAlreadyExistsException | IOException e) {
            try {
                loadedEmptySlimeWorld = slimePlugin.loadWorld(slimePlugin.getLoader("file"), "empty", true, properties);
            } catch (UnknownWorldException | IOException | CorruptedWorldException | NewerFormatException | WorldLockedException ex) {
                ex.printStackTrace();
            }
        }
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
        World alreadyExistingWorld = Bukkit.getWorld(worldName);
        if (alreadyExistingWorld != null) {
            return alreadyExistingWorld;
        }

        SlimeWorld slimeWorld = loadedEmptySlimeWorld.clone(worldName);
        try {
            slimePlugin.loadWorld(slimeWorld);
            loadedWorldsCache.add(worldName);
        } catch (UnknownWorldException | WorldLockedException | IOException e) {
            e.printStackTrace();
        }

        return Bukkit.getWorld(worldName);
    }
}
