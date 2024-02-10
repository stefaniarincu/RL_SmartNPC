package ro.smartnpc.commands.agent.routes;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ro.smartnpc.SmartNPC;
import ro.smartnpc.algorithms.states.RelativeCoordinatesState;
import ro.smartnpc.algorithms.states.State;
import ro.smartnpc.commands.CommandRoute;
import ro.smartnpc.environment.Environment;
import ro.smartnpc.npc.EnvironmentNPC;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ShowStatesRoute implements CommandRoute {
    //x increase -> moves to the left
    //x decrease -> moves to the right
    //z increase -> moves forward
    //z decrease -> moves backward

    //actionTaken = 0 -> move forward
    //actionTaken = 1 -> move backward
    //actionTaken = 2 -> turn left
    //actionTaken = 3 -> turn right

    //direction = 0 -> facing target
    //direction = 1 -> target is on the left
    //direction = 2 -> target is on the right
    //direction = 3 -> target is behind
    private static int[] getParticleDirectionData(int direction, int actionTaken) {
        if (direction == 0) {
            if (actionTaken == 0) {
                return new int[]{0, 0, 1};
            }
            else if (actionTaken == 1) {
                return new int[]{0, 0, -1};
            }
            else if (actionTaken == 2) {
                return new int[]{-1, 0, 0};
            }
            else if (actionTaken == 3) {
                return new int[]{1, 0, 0};
            }
        }
        else if (direction == 1) {
            if (actionTaken == 0) {
                return new int[]{1, 0, 0};
            }
            else if (actionTaken == 1) {
                return new int[]{-1, 0, 0};
            }
            else if (actionTaken == 2) {
                return new int[]{0, 0, 1};
            }
            else if (actionTaken == 3) {
                return new int[]{0, 0, -1};
            }
        }
        else if (direction == 2) {
            if (actionTaken == 0) {
                return new int[]{-1, 0, 0};
            }
            else if (actionTaken == 1) {
                return new int[]{1, 0, 0};
            }
            else if (actionTaken == 2) {
                return new int[]{0, 0, -1};
            }
            else if (actionTaken == 3) {
                return new int[]{0, 0, 1};
            }
        }
        else if (direction == 3) {
            if (actionTaken == 0) {
                return new int[]{0, 0, -1};
            }
            else if (actionTaken == 1) {
                return new int[]{0, 0, 1};
            }
            else if (actionTaken == 2) {
                return new int[]{1, 0, 0};
            }
            else if (actionTaken == 3) {
                return new int[]{-1, 0, 0};
            }
        }

        return new int[]{0, 0, 0};
    }

    static class LocationData {
        public float x;
        public float y;
        public float z;

        private int direction;

        public int[] particleDirection;

        public LocationData(float x, float y, float z, int direction, int actionTaken) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.direction = direction;
            this.particleDirection = getParticleDirectionData(direction, actionTaken);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LocationData that = (LocationData) o;
            return Float.compare(that.x, x) == 0 && Float.compare(that.y, y) == 0 && Float.compare(that.z, z) == 0 && direction == that.direction;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z, direction);
        }
    }

    BukkitTask task;

    private void spawnParticles(Map<State, Map<Integer, Double>> Q) {
        if (task != null) {
            task.cancel();
        }

        Location target = Environment.getRunningInstance().getTarget();
        List<LocationData> locations = Q.entrySet().stream().map(stateAndActionsPair -> {
            State state = stateAndActionsPair.getKey();
            int actionTaken = stateAndActionsPair.getValue().entrySet().stream().max(Map.Entry.comparingByValue()).orElseThrow().getKey();
            if (state instanceof RelativeCoordinatesState relativeCoordinatesState) {
                return new LocationData(
                        target.getBlockX() - relativeCoordinatesState.getX(),
                        target.getBlockY() - relativeCoordinatesState.getY(),
                        target.getBlockZ() - relativeCoordinatesState.getZ(),
                        relativeCoordinatesState.getDirection(),
                        actionTaken
                );
            }

            return null;
        }).distinct().toList();


        World world = Environment.getRunningInstance().getEnvironmentWorld().getWorld();
        task = new BukkitRunnable() {
            @Override
            public void run() {
                for (LocationData location : locations) {
                    world.spawnParticle(Particle.FIREWORKS_SPARK, location.x, location.y+0.2, location.z, 0, location.particleDirection[0], location.particleDirection[1], location.particleDirection[2], 0.1F);
                }
            }
        }.runTaskTimer(SmartNPC.getInstance(), 0, 40);
    }

    private void stopParticles() {
        if (task != null) {
            task.cancel();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (Environment.getRunningInstance() == null) {
            sender.sendMessage("§cEnvironment not running!");
            return false;
        }

        if (args.length < 2) {
            sender.sendMessage("§cNot enough arguments! Specify agent name");
            return false;
        }

        if (args[1].equals("stop")) {
            stopParticles();
            sender.sendMessage("§aStopped showing states!");
            return false;
        }

        String agentName = args[1];
        final Environment environment = Environment.getRunningInstance();
        EnvironmentNPC agent = environment.getAgentByName(agentName);
        if (agent == null) {
            sender.sendMessage("§cAgent not found!");
            return false;
        }

        sender.sendMessage("§aShowing states...");
        Map<State, Map<Integer, Double>> Q = (Map<State, Map<Integer, Double>>) agent.getAlgorithm().getData();
        spawnParticles(Q);
        return false;
    }

    @Override
    public boolean isConsoleAllowed() {
        return false;
    }

}
