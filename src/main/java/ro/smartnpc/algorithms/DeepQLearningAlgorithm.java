package ro.smartnpc.algorithms;

import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerTeleportEvent;
import ro.smartnpc.SmartNPC;
import ro.smartnpc.algorithms.actions.Action;
import ro.smartnpc.algorithms.actions.movement.teleport.ActionBackward;
import ro.smartnpc.algorithms.actions.movement.teleport.ActionForward;
import ro.smartnpc.algorithms.actions.movement.teleport.ActionLeft;
import ro.smartnpc.algorithms.actions.movement.teleport.ActionRight;
import ro.smartnpc.algorithms.python.DeepQLearningProxy;
import ro.smartnpc.algorithms.states.RelativeCoordinatesState;
import ro.smartnpc.algorithms.states.State;
import ro.smartnpc.npc.EnvironmentNPC;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DeepQLearningAlgorithm implements Algorithm {
    private final DeepQLearningProxy proxy;
    private final List<Action> actions = new ArrayList<>() {
        {
            add(new ActionForward());
            add(new ActionBackward());
            add(new ActionLeft());
            add(new ActionRight());
        }
    };
    private static final double EPSILON = 0.1;  // Exploration-exploitation trade-off

    public DeepQLearningAlgorithm(int id) {
        proxy = new DeepQLearningProxy(id);

    }

    public Object getData(){
        return null;
    }

    private EnvironmentNPC environmentNPC;

    private double score = 0.0;

    @Override
    public void setEnvironmentNPC(EnvironmentNPC npc) {
        environmentNPC = npc;
    }

    private int epsilonGreedyPolicy(RelativeCoordinatesState state) {
        if (Math.random() < EPSILON) {
            return getRandomAction();
        } else {
            return proxy.getNextAction(state);
        }
    }

    private int getRandomAction() {
        return new Random().nextInt(actions.size());
    }



    private boolean reachedTarget(State state) {
        return state.isFinalState(environmentNPC);
    }

    private RelativeCoordinatesState takeAction(RelativeCoordinatesState currentState, int actionIndex) {
        Action action = actions.get(actionIndex);
        CompletableFuture<Void> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTask(SmartNPC.getInstance(),
                () -> action.execute(environmentNPC).thenRun(() -> future.complete(null))
        );

        try {
            future.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            SmartNPC.getInstance().getLogger().warning("Error while executing action " + action.getActionType().name() + " for " + environmentNPC.getName());
        } catch (TimeoutException e) {
            SmartNPC.getInstance().getLogger().warning("Timeout while executing action " + action.getActionType().name() + " for " + environmentNPC.getName());
            return null;
        }

        return processState();
    }

    private double getReward(State previousState, State state) {
        return state.getReward(previousState, environmentNPC);
    }

    private RelativeCoordinatesState processState() {
        return new RelativeCoordinatesState(environmentNPC.getNPC().getEntity().getLocation(), environmentNPC.getEnvironment().getTarget());
    }

    private double totalReward = 0.0;

    @Override
    public boolean step() {
        RelativeCoordinatesState currentState = processState();
        if (reachedTarget(currentState))
            return false;

        int action = epsilonGreedyPolicy(currentState);
        RelativeCoordinatesState nextState = takeAction(currentState, action);

        double reward = getReward(currentState, nextState);

        if (!testing)
            try {
                proxy.sendToBuffer(currentState, action, reward, nextState, nextState.isFinalState(environmentNPC));
            }catch(Exception x){
                x.printStackTrace();
            }

        totalReward += reward;

        return true;
    }

    @Override
    public void runEpisode(int numberOfSteps) {
        for (int i = 0; i < numberOfSteps; i++) {
            if (forceStopTraining)
                break;

            if (step())
                continue;

            SmartNPC.getInstance().getLogger().info("[" + environmentNPC.getName() + "] Reached target!");
            break;
        }
    }

    @Override
    public void reset() {
        totalReward = 0.0;
        CompletableFuture<Void> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTask(SmartNPC.getInstance(), () -> {
            try {
                environmentNPC.getNPC().teleport(environmentNPC.getEnvironment().getEnvironmentWorld().getWorld().getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                environmentNPC.getNPC().getNavigator().cancelNavigation();
            }catch(Exception x){
                x.printStackTrace();
            }

            future.complete(null);
        });

        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean forceStopTraining = false;

    @Override
    public void train(int numberOfEpisodes, int numberOfStepsPerEpisode) {
        forceStopTraining = false;

        double trainingScore = 0;
        for (int i = 0; i < numberOfEpisodes; i++) {
            runEpisode(numberOfStepsPerEpisode);
            if (forceStopTraining) {
                break;
            }

            double episodeReward = totalReward / numberOfStepsPerEpisode;
            SmartNPC.getInstance().getLogger().info("[" + environmentNPC.getName() + "] Reward average episode " + i + ": " + (episodeReward));
            trainingScore += episodeReward;
            reset();
        }

        score += trainingScore / numberOfEpisodes;
    }

    @Override
    public void forceStopTraining() {
        forceStopTraining = true;
        Bukkit.getScheduler().runTask(SmartNPC.getInstance(), () -> {
            environmentNPC.getNPC().teleport(environmentNPC.getEnvironment().getEnvironmentWorld().getWorld().getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            environmentNPC.getNPC().getNavigator().cancelNavigation();
        });
    }

    private boolean testing = false;

    @Override
    public void test() {
        testing = true;
        while (testing && step()) ;
        testing = false;
        reset();
    }

    public void forceStopTesting() {
        testing = false;
    }

    @Override
    public void destroy() {
        forceStopTraining();
        forceStopTesting();;

        for (Action action : actions) {
            action.destroy();
        }
        actions.clear();
    }

    @Override
    public void saveCurrentData(File whereToSave) {

    }

    @Override
    public void loadCurrentData(File fromWhereToLoad) {
        score = 0;

    }

    @Override
    public double getScore() {
        return score;
    }
}
