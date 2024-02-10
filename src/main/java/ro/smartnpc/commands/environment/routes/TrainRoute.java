package ro.smartnpc.commands.environment.routes;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import ro.smartnpc.SmartNPC;
import ro.smartnpc.commands.CommandRoute;
import ro.smartnpc.environment.Environment;

public class TrainRoute implements CommandRoute {
    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (Environment.getRunningInstance() == null) {
            sender.sendMessage("§cEnvironment not running!");
            return false;
        }

        if (args.length < 2) {
            sender.sendMessage("§cNot enough arguments! Specify number of episodes and steps");
            return false;
        }

        if (args[1].equals("stop")) {
            sender.sendMessage("§aIf training is running, it will be stopped.");
            final Environment environment = Environment.getRunningInstance();
            environment.forceStopTraining();
            return false;
        }

        if (args.length < 3) {
            sender.sendMessage("§cNot enough arguments! Specify number of episodes and steps");
            return false;
        }

        int episodes;
        try{
            episodes = Integer.parseInt(args[1]);
        }catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid argument!");
            return false;
        }

        int steps;
        try{
            steps = Integer.parseInt(args[2]);
        }catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid argument!");
            return false;
        }

        sender.sendMessage("§aRunning training with "+ episodes + " episodes and " + steps + " steps...");

        final Environment environment = Environment.getRunningInstance();

        Bukkit.getScheduler().runTaskAsynchronously(SmartNPC.getInstance(), () -> {
            environment.train(episodes, steps);
            environment.saveAgentsToFolder("agents");
        });

        return false;
    }

    @Override
    public boolean isConsoleAllowed() {
        return false;
    }
}
