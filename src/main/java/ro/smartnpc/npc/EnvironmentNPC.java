package ro.smartnpc.npc;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.EntityType;
import ro.smartnpc.algorithms.Algorithm;
import ro.smartnpc.environment.Environment;

public class EnvironmentNPC {

    NPC npc;

    private String name;

    private final Environment environment;

    private final Algorithm algorithm;

    public EnvironmentNPC(String name, Environment environment, Algorithm algorithm) {
        this.name = name;
        this.environment = environment;
        this.algorithm = algorithm;
        algorithm.setEnvironmentNPC(this);
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public NPC createAgentNpc(){
        if (this.npc != null)
            return npc;

        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, name);
        npc.spawn(environment.getEnvironmentWorld().getWorld().getSpawnLocation());
        npc.getEntity().setVelocity(npc.getEntity().getVelocity().zero());
        npc.getNavigator().getLocalParameters()
                .distanceMargin(0.5)
                .destinationTeleportMargin(0.5)
                .baseSpeed(5f)
                .stationaryTicks(2)
                .speedModifier(4f);

        this.npc = npc;
        return npc;
    }

    public String getName() {
        return name;
    }

    public NPC getNPC() {
        return npc;
    }

    public NPC getOrSpawnNPC(){
        if (npc == null)
            return createAgentNpc();
        return npc;
    }
    public Environment getEnvironment() {
        return environment;
    }

    public void destroy() {
        algorithm.destroy();
        npc.destroy();
    }
}
