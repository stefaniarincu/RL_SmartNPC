package ro.smartnpc.algorithms;

import ro.smartnpc.npc.EnvironmentNPC;

import java.io.File;

public interface Algorithm {

    void setEnvironmentNPC(EnvironmentNPC npc);

    /**
     * @return true if step was successful, false if the algorithm should stop
     */
    boolean step();

    void reset();

    void test();

    void forceStopTesting();

    void runEpisode(int numberOfSteps);

    void train(int numberOfEpisodes, int numberOfStepsPerEpisode);

    void forceStopTraining();

    void saveCurrentData(File whereToSave);

    void loadCurrentData(File fromWhereToLoad);

    double getScore();

    Object getData();

    void destroy();
}
