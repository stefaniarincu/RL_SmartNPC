package ro.smartnpc.algorithms.states;

import ro.smartnpc.npc.EnvironmentNPC;

public interface State {

    boolean isFinalState(EnvironmentNPC environmentNPC);

    double getReward(State previousState, EnvironmentNPC environmentNPC);
}
