package ro.smartnpc.algorithms.actions;

import ro.smartnpc.npc.EnvironmentNPC;

import java.util.concurrent.CompletableFuture;

public interface Action {

    ActionType getActionType();

    CompletableFuture<Void> execute(EnvironmentNPC npc);

    void destroy();

    default void executeContinuous(EnvironmentNPC npc, double... args) {
        execute(npc);
    }
}
