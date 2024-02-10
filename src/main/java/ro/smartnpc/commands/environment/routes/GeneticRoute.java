package ro.smartnpc.commands.environment.routes;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import ro.smartnpc.SmartNPC;
import ro.smartnpc.algorithms.genetic.TrainGenetic;
import ro.smartnpc.commands.CommandRoute;
import ro.smartnpc.environment.Environment;

public class GeneticRoute implements CommandRoute {

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (Environment.getRunningInstance() == null) {
            sender.sendMessage("§cEnvironment not running!");
            return false;
        }

        if (args.length < 4) {
            sender.sendMessage("§cNot enough arguments! Specify number of iterations and episodes and steps");
            return false;
        }

        int iterations;
        try{
            iterations = Integer.parseInt(args[1]);
        }catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid number of iterations!");
            return false;
        }

        int episodes;
        try{
            episodes = Integer.parseInt(args[2]);
        }catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid number of episodes!");
            return false;
        }

        int steps;
        try{
            steps = Integer.parseInt(args[3]);
        }catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid number of steps!");
            return false;
        }

        TrainGenetic trainGenetic = TrainGenetic.trainGenetic(iterations, episodes, steps);
        if (trainGenetic != null && !trainGenetic.isRunning()) {
            Bukkit.getScheduler().runTaskAsynchronously(SmartNPC.getInstance(), trainGenetic::startTraining);
            sender.sendMessage("§aGenetic training started!");
        }
        else
            sender.sendMessage("§cTraining already started!");

        return false;
    }

    @Override
    public boolean isConsoleAllowed() {
        return false;
    }

}
