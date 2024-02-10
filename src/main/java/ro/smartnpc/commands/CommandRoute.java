package ro.smartnpc.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface CommandRoute {

    boolean onCommand(CommandSender sender, String[] args);

    boolean isConsoleAllowed();

    default List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }
}
