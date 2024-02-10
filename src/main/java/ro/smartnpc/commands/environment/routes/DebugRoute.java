package ro.smartnpc.commands.environment.routes;

import org.bukkit.command.CommandSender;
import ro.smartnpc.algorithms.python.PythonConnection;
import ro.smartnpc.commands.CommandRoute;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class DebugRoute implements CommandRoute {

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {

        try {

            BufferedReader in = new BufferedReader(new InputStreamReader(PythonConnection.getInstance().getInputStream()));
            sender.sendMessage("§aConnection established! " + in.readLine());
        }catch (Exception e) {
            sender.sendMessage("§cConnection failed!");
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean isConsoleAllowed() {
        return false;
    }
}
