package ro.smartnpc.environment;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import ro.smartnpc.SmartNPC;
import ro.smartnpc.algorithms.DeepQLearningAlgorithm;
import ro.smartnpc.algorithms.QLearningAlgorithm;
import ro.smartnpc.npc.EnvironmentNPC;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Environment {

    private static Environment instance;

    public static Environment getRunningInstance() {
        return instance;
    }

    private Location target;
    private EnvironmentWorld environmentWorld;
    private List<EnvironmentNPC> agents;

    public Environment(EnvironmentWorld environmentWorld, int numberOfAgents, boolean deepLearning) {
        instance = this;
        this.environmentWorld = environmentWorld;
        agents = new ArrayList<>(numberOfAgents);
        registerAgents(numberOfAgents, deepLearning);
    }

    public void registerAgents(int numberOfAgents, boolean deep) {
        if (deep) {
            //hard coding 1 agent since the socket can't handle more than 1 agent currently
            agents.add(new EnvironmentNPC("Agent0", this, new DeepQLearningAlgorithm(0)));
            return;
        }

        for (int i = 0; i < numberOfAgents; i++)
            agents.add(new EnvironmentNPC("Agent"+i, this, new QLearningAlgorithm()));
    }

    public void unload() {
        for (EnvironmentNPC agent : agents) {
            agent.destroy();
        }
        environmentWorld.unloadWorld();
        instance = null;
    }

    public CompletableFuture<World> init(){
        environmentWorld.loadWorldAndMap();

        final CompletableFuture<World> toReturn = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskAsynchronously(SmartNPC.getInstance(), () -> {
            try {
                while (!environmentWorld.isMapLoaded()) {
                    Thread.sleep(300);
                }

                Bukkit.getScheduler().runTask(SmartNPC.getInstance(), () -> {
                    for (EnvironmentNPC agent : agents) {
                        agent.getOrSpawnNPC();
                    }
                });

                toReturn.complete(environmentWorld.getWorld());
            }catch(InterruptedException x) {
                toReturn.completeExceptionally(x);
            }
        });

        return toReturn;
    }

    public void reset(){
        for (EnvironmentNPC agent : agents) {
            agent.getAlgorithm().reset();
        }
    }

    public void step(){
        for (EnvironmentNPC agent : agents) {
            agent.getAlgorithm().step();
        }
    }

    public void train(int numberOfEpisodes, int numberOfSteps) {
//        for (EnvironmentNPC agent : agents) {
//            Bukkit.getScheduler().runTaskAsynchronously(SmartNPC.getInstance(), () -> {
//                agent.getAlgorithm().train(numberOfEpisodes, numberOfSteps);
//            });
//        }

        List<CompletableFuture<Void>> completableFutures = new ArrayList<>();

        for (EnvironmentNPC agent : agents) {
            CompletableFuture<Void> future = new CompletableFuture<>();

            Bukkit.getScheduler().runTaskAsynchronously(SmartNPC.getInstance(), () -> {
                agent.getAlgorithm().train(numberOfEpisodes, numberOfSteps);
                future.complete(null);
            });

            completableFutures.add(future);
        }

        CompletableFuture<Void> allOf = CompletableFuture.allOf(
                completableFutures.toArray(new CompletableFuture[0])
        );

        allOf.join();
    }

    public void forceStopTraining() {
        for (EnvironmentNPC agent : agents) {
            Bukkit.getScheduler().runTaskAsynchronously(SmartNPC.getInstance(), () -> {
                agent.getAlgorithm().forceStopTraining();
            });
        }
    }

    public void saveAgentsToFolder(String folderName) {
        File folder = new File(folderName);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        for (EnvironmentNPC agent : agents) {
            agent.getAlgorithm().saveCurrentData(new File(folder, agent.getName() + ".ser"));
        }
    }

    public Location getTarget() {
        return target;
    }

    public void setTarget(Location target) {
        this.target = target;
    }

    public EnvironmentWorld getEnvironmentWorld() {
        return environmentWorld;
    }

    public List<EnvironmentNPC> getAgents() {
        return agents;
    }

    public EnvironmentNPC getAgentByName(String name) {
        for (EnvironmentNPC agent : agents) {
            if (agent.getName().equals(name))
                return agent;
        }
        return null;
    }
}
