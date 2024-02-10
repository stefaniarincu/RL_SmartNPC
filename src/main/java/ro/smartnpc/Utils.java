package ro.smartnpc;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import ro.smartnpc.algorithms.states.State;

import java.io.*;
import java.util.Map;

public class Utils {

    /**
     * @return 0 - facing target <br>
     * 1 - target is on the left <br>
     * 2 - target is on the right <br>
     * 3 - target is behind <br>
     */
    public static int getFacingResult(Location source, Location target) {
        Vector playerToTarget = target.clone().subtract(source).toVector();
        Vector playerLooking = source.getDirection();
        double x1 = playerToTarget.getX();
        double z1 = playerToTarget.getZ();
        double x2 = playerLooking.getX();
        double z2 = playerLooking.getZ();
        double angle = Math.atan2(x1 * z2 - z1 * x2, x1 * x2 + z1 * z2) * 180 / Math.PI;
        if (angle >= -45 && angle < 45) {
            // forward
            return 0;
        } else if (angle >= 45 && angle < 135) {
            // left
            return 1;
        } else if (angle >= 135 && angle <= 180 || angle >= -180 && angle < -135) {
            // backward
            return 3;
        } else if (angle >= -135 && angle < -45) {
            // right
            return 2;
        }

        return -1;
    }

    public static void serialize(Map<State, Map<Integer, Double>> Q, String name) {
        String folderPath = "agents";
        File folder = new File(folderPath);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        serialize(Q, new File(folderPath + File.separator + name + ".ser"));
    }

    public static void serialize(Map<State, Map<Integer, Double>> Q, File whereToSave) {
        try {
            FileOutputStream fileOut = new FileOutputStream(whereToSave);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(Q);
            objectOut.close();
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<State, Map<Integer, Double>> deserializeQ(File fromWhere) {
        try {
            FileInputStream fileIn = new FileInputStream(fromWhere);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            Map<State, Map<Integer, Double>> deserializedQ = (Map<State, Map<Integer, Double>>) objectIn.readObject();
            objectIn.close();
            fileIn.close();
            return deserializedQ;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void deserializeAndWrite(String name) {
        try {
            FileInputStream fileIn = new FileInputStream("agents/" + name + ".ser");
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            Map<State, Map<Integer, Double>> deserializedQ = (Map<State, Map<Integer, Double>>) objectIn.readObject();

            objectIn.close();
            fileIn.close();

            PrintWriter writer = new PrintWriter("agents/" + name + ".txt");
            writer.println(deserializedQ.toString());
            writer.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
