package ro.smartnpc.commands.agent.routes;

import org.bukkit.command.CommandSender;
import ro.smartnpc.Utils;
import ro.smartnpc.commands.CommandRoute;

public class DeserializeRoute implements CommandRoute {
    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cNot enough arguments! Specify the agent name");
            return false;
        }

        Utils.deserializeAndWrite(args[1]);
        sender.sendMessage("§aDeserialized and wrote to file "+args[1]+"!");

        return false;
    }

    @Override
    public boolean isConsoleAllowed() {
        return true;
    }
}
