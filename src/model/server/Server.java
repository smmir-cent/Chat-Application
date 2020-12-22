package model.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


public class Server {

    private int port;
    //holding groups
    private HashMap<String, Set<UserThread>> groupsThreads = new HashMap<>();
    //private chats
    private ArrayList<Set<UserThread>> privateThreads = new ArrayList<>();
    //all username
    private HashMap<String, Set<Object>> allUsers = new HashMap<>();
    //all user threads
    private Set<UserThread> allUserThreads = new HashSet<>();


    public Set<UserThread> getAllUserThreads() {
        return allUserThreads;
    }

    public ArrayList<Set<UserThread>> getPrivateThreads() {
        return privateThreads;
    }

    public HashMap<String, Set<UserThread>> getGroupsThreads() {
        return groupsThreads;
    }


    public Server(int port) {
        this.port = port;
    }

    public void execute() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Server is listening on port " + port);
            //new ServerThread(this).run();
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New user connected");
                UserThread newUser = new UserThread(socket, this);
                allUserThreads.add(newUser);
                newUser.start();
//                if(scanner.hasNext()){
//                    String s = scanner.next();
//                    if(s.equals("list")){
//                        System.out.println("USERS: ");
//                        for(String s1:getAllUsers().keySet()){
//                            System.out.println(s1);
//                        }
//                        System.out.println("Groups: ");
//                        for(String s1:getGroupsThreads().keySet()){
//                            System.out.println(s1);
//                        }
//                    }
//                }
            }

        } catch (IOException ex) {
            System.out.println("Error in the server: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void addUserName(String userName, int port, InetAddress inetAddress) {
        Set<Object> client = new HashSet<>();
        client.add(port);
        client.add(inetAddress);
        allUsers.put(userName, client);
    }

    public void removeUser(String userName, UserThread aUser) {
        boolean removed = allUsers.remove(userName) != null;
        if (removed) {
            allUserThreads.remove(aUser);
            for (String s : getGroupsThreads().keySet()) {
                getGroupsThreads().get(s).remove(aUser);
            }
            privateThreads.removeIf(element -> element.contains(aUser));
            System.out.println("The user " + userName + " quited");
        }
    }

    HashMap<String, Set<Object>> getAllUsers() {
        return this.allUsers;
    }

    void addPrivateChat(UserThread userThread1, UserThread userThread2) {
        Set<UserThread> temp = new HashSet<>();
        temp.add(userThread1);
        temp.add(userThread2);
        privateThreads.add(temp);
    }

    void sendToAll(String message, UserThread excludeUser) {
        for (UserThread aUser : allUserThreads) {
            if (aUser != excludeUser) {
                aUser.sendMessage(message);
            }
        }
    }

    void sendToGroup(String group, String message, UserThread excludeUser) {
        for (UserThread aUser : groupsThreads.get(group)) {
            if (aUser != excludeUser) {
                aUser.sendMessage(message);
            }
        }
    }

    void sendFileToUser(String location, UserThread userThread) {
        userThread.sendFile(new File(location));
    }

    void sendFileToGroup(String group, String location, UserThread excludeUser) {
        for (UserThread aUser : groupsThreads.get(group)) {
            if (aUser != excludeUser) {
                aUser.sendFile(new File(location));
            }
        }
    }

    void sendToUser(String message, UserThread userThread) {
        userThread.sendMessage(message);
    }
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("port as args!");
            System.exit(0);
        }
        int port = Integer.parseInt(args[0]);
        Server server = new Server(port);
        server.execute();
    }
    void updateList() {
        StringBuilder s = new StringBuilder();
        s.append("Users");
        s.append(System.getProperty("line.separator"));

        for(String s1:getAllUsers().keySet()){
            s.append(s1);
            s.append(System.getProperty("line.separator"));
        }
        s.append("Groups:\n");
        for(String s1:getGroupsThreads().keySet()){
            s.append(s1);
            s.append(System.getProperty("line.separator"));
        }
        try(BufferedWriter writer = new BufferedWriter(new FileWriter("list.txt"))) {
            writer.write(s.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
