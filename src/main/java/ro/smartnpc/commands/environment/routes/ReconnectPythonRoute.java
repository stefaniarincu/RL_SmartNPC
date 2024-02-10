package ro.smartnpc.commands.environment.routes;

import org.bukkit.command.CommandSender;
import ro.smartnpc.algorithms.python.PythonConnection;
import ro.smartnpc.commands.CommandRoute;

public class ReconnectPythonRoute implements CommandRoute {

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {

        PythonConnection.getInstance().reconnect();
        sender.sendMessage("§aReconnection attempted!");

        return false;
    }

    @Override
    public boolean isConsoleAllowed() {
        return false;
    }

}
