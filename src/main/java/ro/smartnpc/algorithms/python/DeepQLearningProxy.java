package ro.smartnpc.algorithms.python;

import com.fasterxml.jackson.databind.ObjectMapper;
import ro.smartnpc.SmartNPC;
import ro.smartnpc.algorithms.states.RelativeCoordinatesState;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class DeepQLearningProxy {

    private record BufferData(int id, RelativeCoordinatesState state, int actionTaken, double reward, RelativeCoordinatesState nextState, boolean done) {
    }

    private record NextActionData(int id, RelativeCoordinatesState state) {
    }
    private int id;

    public DeepQLearningProxy(int id) {
        this.id = id;
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public synchronized void init(int numberOfAgents, int numberOfActions) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("numberOfAgents", numberOfAgents);
        data.put("numberOfActions", numberOfActions);
        String jsonData = objectMapper.writeValueAsString(data);

        PythonConnection.getInstance().getOutputStream().write(jsonData.getBytes());
    }

    public synchronized void sendToBuffer(RelativeCoordinatesState state, int actionTaken, double reward, RelativeCoordinatesState nextState, boolean done) throws IOException {
        OutputStream outputStream = PythonConnection.getInstance().getOutputStream();

        outputStream.write(objectMapper.writeValueAsBytes(new BufferData(id, state, actionTaken, reward, nextState, done)));
        outputStream.write("\n".getBytes());
        outputStream.flush();
    }

    public synchronized int getNextAction(RelativeCoordinatesState state) {
        OutputStream outputStream = PythonConnection.getInstance().getOutputStream();

        try {
            outputStream.write(objectMapper.writeValueAsBytes(new NextActionData(id, state)));
            outputStream.write("\n".getBytes());
            outputStream.flush();

            String response = PythonConnection.getInstance().getInputReader().readLine();
            return Integer.parseInt(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 1;
    }
}
