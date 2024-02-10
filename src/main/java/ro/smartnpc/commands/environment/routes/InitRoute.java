package ro.smartnpc.commands.environment.routes;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ro.smartnpc.SmartNPC;
import ro.smartnpc.commands.CommandRoute;
import ro.smartnpc.environment.Environment;
import ro.smartnpc.environment.EnvironmentWorld;
import ro.smartnpc.map.Schematic;

public class InitRoute implements CommandRoute {
    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (Environment.getRunningInstance() != null) {
            sender.sendMessage("§cEnvironment already running! If you want to reset, unload it first!");
            return false;
        }

        if (args.length < 2) {
            sender.sendMessage("§cNot enough arguments! Specify number of agents");
            return false;
        }

        if (args[1].equals("deep")) {
            Player player = (Player) sender;
            sender.sendMessage("§aLoading world settings...");
            EnvironmentWorld environmentWorld = new EnvironmentWorld("movement", Schematic.MOVEMENT_ARENA1);
            final Environment environment = new Environment(environmentWorld, 1, true);

            sender.sendMessage("§aLoading world...");
            environment.init().whenComplete((world, throwable) -> {
                if (throwable != null) {
                    throwable.printStackTrace();
                }

                environment.setTarget(environmentWorld.getWorld().getSpawnLocation().clone().add(0, 0, 36));

                sender.sendMessage("§aTeleporting to world...");

                Bukkit.getScheduler().runTask(SmartNPC.getInstance(), () -> {
                    player.teleport(world.getSpawnLocation());
                });

                sender.sendMessage("§aWorld loaded!");
            });
            return false;
        }

        int agents;
        try{
            agents = Integer.parseInt(args[1]);
        }catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid argument!");
            return false;
        }

        Player player = (Player) sender;
        sender.sendMessage("§aLoading world settings...");
        EnvironmentWorld environmentWorld = new EnvironmentWorld("movement", Schematic.MOVEMENT_ARENA1);
        final Environment environment = new Environment(environmentWorld, agents, false);

        sender.sendMessage("§aLoading world...");
        environment.init().whenComplete((world, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
            }

            environment.setTarget(environmentWorld.getWorld().getSpawnLocation().clone().add(0, 0, 36));

            sender.sendMessage("§aTeleporting to world...");

            Bukkit.getScheduler().runTask(SmartNPC.getInstance(), () -> {
                player.teleport(world.getSpawnLocation());
            });

            sender.sendMessage("§aWorld loaded!");
        });

        return false;
    }

    @Override
    public boolean isConsoleAllowed() {
        return false;
    }
}
