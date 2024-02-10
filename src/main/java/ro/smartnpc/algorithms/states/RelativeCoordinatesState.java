package ro.smartnpc.algorithms.states;

import org.bukkit.Location;
import ro.smartnpc.Utils;
import ro.smartnpc.npc.EnvironmentNPC;

import java.io.Serializable;
import java.util.Objects;

public class RelativeCoordinatesState implements State, Serializable {

    private final int x;
    private final int y;
    private final int z;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    // 0 - facing target
    // 1 - target is on the left
    // 2 - target is on the right
    // 3 - target is behind
    private final int direction;

    public int getDirection() {
        return direction;
    }

    public RelativeCoordinatesState(Location source, Location target) {
        x = target.getBlockX() - source.getBlockX();
        y = target.getBlockY() - source.getBlockY();
        z = target.getBlockZ() - source.getBlockZ();

        direction = Utils.getFacingResult(source, target);
    }

    @Override
    public boolean isFinalState(EnvironmentNPC environmentNPC) {
        if (Math.abs(x) >= 2)
            return false;

        if (Math.abs(y) >= 2)
            return false;

        if (Math.abs(z) >= 2)
            return false;

        return true;
    }

    @Override
    public double getReward(State previousState, EnvironmentNPC environmentNPC) {
        if (!(previousState instanceof RelativeCoordinatesState previousStateCasted)) {
            throw new IllegalArgumentException("Previous state must be of type RelativeCoordinatesState");
        }

        if (isFinalState(environmentNPC)) {
            return 100;
        }

        double reward = 0.0;

        long distanceToTarget = (long) x * x + (long) z * z; //fara y momentan
        reward += -0.01 * distanceToTarget;

        long distanceToTargetPreviousState = (long) previousStateCasted.x * previousStateCasted.x + (long) previousStateCasted.z * previousStateCasted.z; //fara y momentan

        if (distanceToTargetPreviousState < distanceToTarget)
            reward -= 1;

        if (previousStateCasted.x == x && previousStateCasted.z == z)
            reward -= 5;

        return reward;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelativeCoordinatesState state = (RelativeCoordinatesState) o;
        return state.x == x &&
                state.y == y &&
                state.z == z &&
                state.direction == direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, direction);
    }

    @Override
    public String toString() {
        return "RelativeCoordinatesState{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", direction=" + direction +
                '}';
    }
}
