package ro.smartnpc.commands.world.routes;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ro.smartnpc.SmartNPC;
import ro.smartnpc.commands.CommandRoute;
import ro.smartnpc.map.Schematic;
import ro.smartnpc.map.SchematicUtils;

import java.util.Arrays;
import java.util.List;

public class LoadSchematicRoute implements CommandRoute {

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cNot enough arguments!");
            return false;
        }

        Player player = (Player) sender;

        Bukkit.getScheduler().runTaskAsynchronously(SmartNPC.getInstance(), () -> {
            Schematic schematic;
            try {
                schematic = Schematic.valueOf(args[1].toUpperCase());
            }catch(IllegalArgumentException x){
                player.sendMessage("§cSchematic not found!");
                return;
            }

            if(SchematicUtils.loadSchematic(schematic, player.getLocation())) {
                player.sendMessage("§aSchematic loaded!");
            }else{
                player.sendMessage("§cSchematic not loaded!");
            }
        });

        return false;
    }

    @Override
    public boolean isConsoleAllowed() {
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return Arrays.stream(Schematic.values()).map(Enum::name).toList();
    }

}
