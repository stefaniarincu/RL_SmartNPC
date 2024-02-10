package ro.smartnpc.commands.world.routes;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ro.smartnpc.SmartNPC;
import ro.smartnpc.commands.CommandRoute;

import java.util.ArrayList;
import java.util.List;

public class TpRoute implements CommandRoute {
    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cNot enough arguments!");
            return false;
        }

        World world = Bukkit.getWorld(args[1]);
        if (world == null) {
            sender.sendMessage("§cWorld not found!");
            return false;
        }

        Player player = (Player) sender;
        player.teleport(world.getSpawnLocation());
        sender.sendMessage("§aTeleported to world!");
        return false;
    }

    @Override
    public boolean isConsoleAllowed() {
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>(SmartNPC.getInstance().getWorldUtils().getLoadedWorldsCache());
    }
}
