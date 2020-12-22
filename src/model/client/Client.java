package model.client;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    private String hostname;
    private int port;
    public Client(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }
    public void execute() {
        try {
            Socket socket = new Socket(hostname, port);
            System.out.println("Connected to the server");
            Read read = new Read(socket);
            read.start();
            Write write = new Write(socket);
            write.start();
            while(read.getUname()==null){
                read.setUname(write.getUname());
            }
        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O Error: " + ex.getMessage());
        }

    }
}

class Client_Main {
    public static void main(String[] args) {
        if (args.length < 2) return;
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
        Client client = new Client(hostname, port);
        client.execute();
    }
}
