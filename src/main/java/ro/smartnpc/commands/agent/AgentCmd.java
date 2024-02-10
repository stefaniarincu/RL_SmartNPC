package ro.smartnpc.commands.agent;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.smartnpc.commands.CommandRoute;
import ro.smartnpc.commands.agent.routes.DeserializeRoute;
import ro.smartnpc.commands.agent.routes.LoadDataRoute;
import ro.smartnpc.commands.agent.routes.ShowStatesRoute;
import ro.smartnpc.commands.agent.routes.TestRoute;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgentCmd implements TabExecutor {

    Map<String, CommandRoute> routes = new HashMap<>() {{
        put("deserialize", new DeserializeRoute());
        put("load", new LoadDataRoute());
        put("showStates", new ShowStatesRoute());
        put("test", new TestRoute());
    }};

    private AgentCmd() {}

    private static AgentCmd instance;

    public static synchronized AgentCmd getInstance() {
        if (instance == null) {
            instance = new AgentCmd();
        }

        return instance;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            commandSender.sendMessage("§cNot enough arguments!");
            return false;
        }

        CommandRoute route = routes.getOrDefault(args[0], null);
        if (route == null) {
            commandSender.sendMessage("§cInvalid argument!");
            return false;
        }

        return route.onCommand(commandSender, args);
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> toReturn = null;
        String arg = args[0];

        if (args.length == 1)
            return routes.keySet().stream().filter(s1 -> s1.startsWith(arg)).toList();


        final CommandRoute route = routes.getOrDefault(arg, null);

        if (route != null) {
            toReturn = route.onTabComplete(commandSender, args);
        }

        return toReturn == null ? Collections.emptyList() : toReturn;
    }
}
