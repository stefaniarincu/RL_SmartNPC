package ro.smartnpc.commands.agent.routes;

import org.bukkit.command.CommandSender;
import ro.smartnpc.commands.CommandRoute;
import ro.smartnpc.environment.Environment;
import ro.smartnpc.npc.EnvironmentNPC;

import java.io.File;

public class LoadDataRoute implements CommandRoute {

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (Environment.getRunningInstance() == null) {
            sender.sendMessage("§cEnvironment not running!");
            return false;
        }

        if (args.length < 3) {
            sender.sendMessage("§cNot enough arguments! Specify file name and agent name");
            return false;
        }

        String fileName = args[1];
        File file = new File(fileName);
        if (!file.exists()) {
            sender.sendMessage("§cFile not found!");
            return false;
        }

        String agentName = args[2];
        final Environment environment = Environment.getRunningInstance();
        EnvironmentNPC agent = environment.getAgentByName(agentName);
        if (agent == null) {
            sender.sendMessage("§cAgent not found!");
            return false;
        }

        sender.sendMessage("§aLoading data...");
        agent.getAlgorithm().loadCurrentData(file);
        sender.sendMessage("§aData loaded into agent " + agentName + "!");

        return false;
    }

    @Override
    public boolean isConsoleAllowed() {
        return false;
    }

}
