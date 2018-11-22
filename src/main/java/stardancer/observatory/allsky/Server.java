package stardancer.observatory.allsky;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(Server.class);
    ServerSocket serverSocket;
    Settings settings;

    public Server(Settings settings) {
        this.settings = settings;

        try {
            serverSocket = new ServerSocket(settings.getIntSettingFor(Settings.ALL_SKY_CAMERA_SERVER_PORT));
        } catch (IOException i) {

        }

        LOGGER.debug("CREATING SERVER OBJECT");
    }


    public void run() {
        LOGGER.debug("Server - Starting server thread! Waiting for incoming connections!");
        while(true) {
            try {
                Socket socket = serverSocket.accept();
                LOGGER.debug("Got a new client! " + socket.getInetAddress());
                ServerRunner serverRunner = new ServerRunner(settings, socket);
                Thread serverRunnerThread = new Thread(serverRunner);
                serverRunnerThread.start();
            } catch (IOException i) {

            }

        }
    }

    private class ServerRunner implements Runnable {

        private Socket inputSocket;
        private Settings settings;

        public ServerRunner(Settings settings, Socket socket) {
            this.settings = settings;
            this.inputSocket = socket;
        }

        public void run() {
            LOGGER.debug("ServerRunner - Starting up new communication server!");
            try (
                PrintWriter out = new PrintWriter(inputSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(inputSocket.getInputStream())))
            {
                String input = in.readLine();
                parseInput(input);
                String outputLine = input;
                out.println(outputLine);

            } catch (IOException i) {

            }
        }

        private void parseInput(String input) {
            if (input.contains(",")) {
                String[] split = input.split(",");
                settings.setSettingFor(split[0], split[1]);
            }
        }
    }
}







