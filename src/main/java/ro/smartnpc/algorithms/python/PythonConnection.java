package ro.smartnpc.algorithms.python;

import ro.smartnpc.SmartNPC;

import java.io.*;
import java.net.Socket;

public class PythonConnection {

    private static PythonConnection instance = null;

    private final static String SERVER_HOST = "127.0.0.1";
    private final static int SERVER_PORT = 25567;

    public static synchronized PythonConnection getInstance() {
        if (instance == null) {
            instance = new PythonConnection();
        }
        return instance;
    }

    private Socket socket;

    private BufferedReader inputReader;
    private OutputStream outputStream;

    private PythonConnection() {

    }

    public boolean connect() {
        try {
            if (socket == null || socket.isClosed()) {
                socket = new Socket(SERVER_HOST, SERVER_PORT);

                inputReader = new BufferedReader(new InputStreamReader(PythonConnection.getInstance().getInputStream()));
                outputStream = socket.getOutputStream();

                socket.setSoTimeout(7000);
            }
            return true;
        } catch (Exception e){
            e.printStackTrace();
            SmartNPC.getInstance().getLogger().warning("Failed to connect to python server");
        }
        return false;
    }

    public void reconnect() {
        closeConnection();
        connect();
    }

    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }

    public BufferedReader getInputReader() {
        return inputReader;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
