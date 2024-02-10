package ro.smartnpc.commands.agent.routes;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import ro.smartnpc.SmartNPC;
import ro.smartnpc.commands.CommandRoute;
import ro.smartnpc.environment.Environment;
import ro.smartnpc.npc.EnvironmentNPC;

public class TestRoute implements CommandRoute {
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

        EnvironmentNPC environmentNPC = Environment.getRunningInstance().getAgentByName(args[1]);
        if (environmentNPC == null){
            sender.sendMessage("§cAgent not found!");
            return false;
        }

        if (args.length > 2 && args[2].equals("stop")){
            environmentNPC.getAlgorithm().forceStopTesting();
            sender.sendMessage("§aStopped testing!");
            return false;
        }

        sender.sendMessage("§aRunning test for agent "+args[1]+"!");
        sender.sendMessage("§aTo stop the agent run /smartnpc_env test <agent> stop");

        Bukkit.getScheduler().runTaskAsynchronously(SmartNPC.getInstance(), () -> {
            environmentNPC.getAlgorithm().test();
        });

        return false;
    }

    @Override
    public boolean isConsoleAllowed() {
        return false;
    }
}
