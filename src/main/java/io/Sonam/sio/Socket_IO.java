package io.Sonam.sio;

import io.socket.client.IO;
import io.socket.client.Socket;

public class Socket_IO {

    private Socket socket;

    public Socket_IO(String websocketURL) {
        try {
            socket = IO.socket(websocketURL);
            System.out.println("Socket.IO connected.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("SOCKET IO FAILED TO STARTUP");
        }
    }

    public Socket getSocket() {
        return socket;
    }

}
