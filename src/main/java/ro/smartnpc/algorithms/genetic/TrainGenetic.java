package ro.smartnpc.algorithms.genetic;

import ro.smartnpc.SmartNPC;
import ro.smartnpc.environment.Environment;
import ro.smartnpc.npc.EnvironmentNPC;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public class TrainGenetic {
    private static final String GLOBAL_FOLDER_NAME = "genetic";
    private static final File GLOBAL_FOLDER;
    static {
        SmartNPC.getInstance().getLogger().info("Creating genetic folder...");
        File file = new File(GLOBAL_FOLDER_NAME);
        if (!file.exists())
            file.mkdir();
        GLOBAL_FOLDER = file;
        SmartNPC.getInstance().getLogger().info("Genetic folder created!");
    }

    private static TrainGenetic instance;

    public static TrainGenetic getInstance() {
        return instance;
    }

    private int numberOfIterations;
    private int numberOfEpisodes;
    private int numberOfStepsPerEpisode;


    private TrainGenetic(int numberOfIterations, int numberOfEpisodes, int numberOfStepsPerEpisode) {
        instance = this;
        this.numberOfIterations = numberOfIterations;
        this.numberOfEpisodes = numberOfEpisodes;
        this.numberOfStepsPerEpisode = numberOfStepsPerEpisode;
    }

    private String folderForThisGenetic = null;

    private void generateFolder() {
        if (folderForThisGenetic == null) {
            try {
                folderForThisGenetic = String.valueOf(System.currentTimeMillis()/1000);
                Path path = Files.createDirectory(Path.of(GLOBAL_FOLDER_NAME, folderForThisGenetic));
                SmartNPC.getInstance().getLogger().info("Folder for this genetic: " + path);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private File saveBestModel(int iteration) {
        Environment environment = Environment.getRunningInstance();
        File bestModelForIteration = new File(GLOBAL_FOLDER, folderForThisGenetic + File.separator + ("BestModel"+iteration) + ".ser");

        EnvironmentNPC bestAgent = environment.getAgents()
                .stream()
                .max(Comparator.comparingDouble(agent -> agent.getAlgorithm().getScore()))
                .orElse(null);

        SmartNPC.getInstance().getLogger().info("Best agent for iteration " + iteration + " is " + bestAgent.getName() + " with score " + bestAgent.getAlgorithm().getScore());

        bestAgent.getAlgorithm().saveCurrentData(bestModelForIteration);

        return bestModelForIteration;
    }

    private void selectAndApplyBest(int iterationIndex) {
        File bestModelForIteration = saveBestModel(iterationIndex);

        Environment environment = Environment.getRunningInstance();
        for (EnvironmentNPC agent : environment.getAgents()) {
            agent.getAlgorithm().loadCurrentData(bestModelForIteration);
        }
    }

    private void showCurrentScores() {
        Environment environment = Environment.getRunningInstance();
        for (EnvironmentNPC agent : environment.getAgents()) {
            SmartNPC.getInstance().getLogger().info(agent.getName() + " has score " + agent.getAlgorithm().getScore());
        }
    }

    public void startTraining() {
        Environment environment = Environment.getRunningInstance();

        SmartNPC.getInstance().getLogger().info("Starting genetic training...");
        generateFolder();

        int iteration = 0;
        while (iteration < numberOfIterations) {
            SmartNPC.getInstance().getLogger().info("[Genetic "+folderForThisGenetic+"] Iteration " + iteration + " started!");
            environment.train(numberOfEpisodes, numberOfStepsPerEpisode);

            showCurrentScores();
            selectAndApplyBest(iteration);

            SmartNPC.getInstance().getLogger().info("[Genetic "+folderForThisGenetic+"] Iteration " + iteration + " ended!");
            iteration++;
        }

        SmartNPC.getInstance().getLogger().info("Genetic training ended!");
        folderForThisGenetic = null;
    }

    public static TrainGenetic trainGenetic(int numberOfIterations, int numberOfEpisodes, int numberOfStepsPerEpisode) {
        if (instance == null) {
            instance = new TrainGenetic(numberOfIterations, numberOfEpisodes, numberOfStepsPerEpisode);
        }

        return instance;
    }

    public boolean isRunning() {
        return folderForThisGenetic != null;
    }
}
