package model.server;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;


public class UserThread extends Thread {
    private Socket socket;
    private Server server;
    private PrintWriter writer;
    private String uname;


    public UserThread(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    public void run() {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
//            System.out.println(socket.getInetAddress().getHostAddress());
//            System.out.println(socket.getLocalAddress().getHostAddress());
//            System.out.println(socket.getLocalPort());
            printUsers();
            this.sendMessage("**Enter Your Username!**");
            String userName = reader.readLine();
            while (server.getAllUsers().containsKey(userName)) {
                this.sendMessage("**you can't pick this username,choose another**");
                userName = reader.readLine();
            }
            server.addUserName(userName, socket.getPort(), socket.getInetAddress());
            uname = userName;
            server.updateList();
            String serverMessage = "**" + uname + " connected!**";
            server.sendToAll(serverMessage, this);
            String clientMessage;
            do {
                clientMessage = reader.readLine();
                String[] command = clientMessage.split(" ");
                switch (command[0]) {
                    case "create":
                        if (command.length>1 && server.getAllUsers().containsKey(command[1])) {
                            UserThread des = userValidation(command[1]);
                            if (des != null) {
                                server.addPrivateChat(this, des);
                                des.sendMessage("**Chat created with " + this.uname + "**");
                                this.sendMessage("**Chat created with " + des.uname + "**");
                            }
                        } else if (command.length>1 && server.getGroupsThreads().containsKey(command[1])) {
                            this.sendMessage("**group exists!**");
                        } else if (command.length>1 && !server.getGroupsThreads().containsKey(command[1]) && command.length > 2) {
                            HashSet<UserThread> temp = new HashSet<>();
                            temp.add(this);
                            this.sendMessage("**" + command[1] + " group created!**");
                            for (int i = 2; i < command.length; i++) {
                                UserThread des = userValidation(command[i]);
                                if (des != null) {
                                    temp.add(des);
                                    des.sendMessage("**" + uname + " added you to " + command[1] + " group!**");
                                } else {
                                    this.sendMessage(command[i] + " doesn't exist.");
                                }
                            }
                            server.getGroupsThreads().put(command[1], temp);
                            server.updateList();
                        } else {
                            this.sendMessage("**you must add members to group!**");
                        }
                        break;
                    case "send":
                        if (command.length>1 && server.getGroupsThreads().containsKey(command[1])) {
                            if (server.getGroupsThreads().get(command[1]).contains(this)) {
                                if (command[2].equals("-f")) {
                                    String temp = uname + "to" + command[1] + "-"+new Random().nextInt(100)+"-" + command[4];
                                    receiveFile( ".\\serverFiles\\"+temp , Integer.parseInt(command[5]));
                                    server.sendToGroup(command[1],"**FILE** "+ ".\\clientFiles\\" + uname+temp + " " + Integer.parseInt(command[5]),this);
                                    server.sendFileToGroup(command[1],".\\serverFiles\\"+temp ,this);
                                    this.sendMessage("**Sent!**");
                                } else {
                                    this.sendMessage("**Sent!**");
                                    server.sendToGroup(command[1], "In " + command[1] + " group >> " + uname + " :" + clientMessage.substring(clientMessage.indexOf(" ", clientMessage.indexOf(" ") + 1)), this);
                                }
                            } else {
                                this.sendMessage("**you aren't member of " + command[1] + " group!**");
                            }
                        } else if (command.length>1 && server.getAllUsers().containsKey(command[1])) {
                            UserThread userThread = userValidation(command[1]);
                            boolean val = false;
                            for (Set<UserThread> p : server.getPrivateThreads()) {
                                if (p.contains(this) && p.contains(userThread)) {
                                    if (command[2].equals("-f")) {
                                        String temp = uname + "to" + command[1] + "-"+new Random().nextInt(100)+"-" + command[4];
                                        receiveFile( ".\\serverFiles\\"+temp , Integer.parseInt(command[5]));
                                        server.sendToUser("**FILE** "+ ".\\clientFiles\\"+ temp + " " + Integer.parseInt(command[5]), userThread);
                                        server.sendFileToUser(".\\serverFiles\\"+temp , userThread);
                                        this.sendMessage("**Sent!**");
                                        //todo
                                    } else {
                                        this.sendMessage("**Sent!**");
                                        server.sendToUser(">> " + uname + " :" + clientMessage.substring(clientMessage.indexOf(" ", clientMessage.indexOf(" ") + 1)), userThread);
                                    }
                                    val = true;
                                    break;
                                }
                            }
                            if (!val) {
                                this.sendMessage("**can't send message before creating private chat!**");
                            }
                        } else {
                            this.sendMessage("**user/group not found!**");
                        }
                        break;
                    case "list":
                        if (command.length == 2) {
                            if (!server.getGroupsThreads().containsKey(command[1])) {
                                this.sendMessage("**" + command[1] + "group doesn't exists!**");
                            } else {
                                StringBuilder list = new StringBuilder("User(s) in " + command[1] + ":");
                                for (UserThread userThread : server.getGroupsThreads().get(command[1])) {
                                    list.append(" ").append(userThread.uname);
                                }
                                this.sendMessage(list.toString());
                            }
                        } else {
                            StringBuilder list = new StringBuilder("User(s):");
                            for (String s : server.getAllUsers().keySet()) {
                                list.append(" ").append(s);
                            }
                            list.append(" | Group(s):");
                            for (String s : server.getGroupsThreads().keySet()) {
                                list.append(" ").append(s);
                            }
                            this.sendMessage(list.toString());
                        }
                        break;
                    case "change":
                        if (command.length>1 && server.getAllUsers().containsKey(command[1])) {
                            this.sendMessage("**you can't pick this username,choose another**");
                            break;
                        } else if(command.length>1){
                            Set<Object> obj = server.getAllUsers().remove(uname);
                            server.sendToAll("**" + uname + " changed to " + command[1] + "!**", this);
                            sendMessage("**You are " + command[1] + "**");
                            uname = command[1];
                            server.getAllUsers().put(uname, obj);
                        }
                        break;
                    case "leave":
                        if (command.length>1 && !server.getGroupsThreads().containsKey(command[1])) {
                            this.sendMessage("**" + command[1] + "group doesn't exists!**");
                        } else if(command.length>1) {
                            UserThread leaving = null;
                            for (UserThread userThread : server.getGroupsThreads().get(command[1])) {
                                if (userThread.uname.equals(uname)) {
                                    leaving = userThread;
                                    break;
                                }
                            }
                            if (leaving != null) {
                                server.getGroupsThreads().get(command[1]).remove(leaving);
                                server.sendToAll("**" + uname + " left the " + command[1] + " group!**", this);
                            } else {
                                this.sendMessage("**You aren't member of " + command[1] + "group**");
                            }
                        }
                        break;
                    case "getlist":
                        File file =new File("list.txt");
                        sendFile(file);
                        String temp = "list.txt";
                        this.sendMessage("**FILE** "+ ".\\clientFiles\\"+ temp + " " + file.length());
                        server.sendFileToUser(temp , this);
                        break ;
                    case "quit":
                        server.removeUser(uname, this);
                        server.sendToAll("**" + uname + " has quited.**", this);
                        server.updateList();
                        socket.close();
                        break ;
                    default:
                        this.sendMessage("Wrong Command");
                        this.sendMessage("Commands: send / send -f / list / getlist / leave / change / ");
                        break;
                }
            } while (!clientMessage.equals("quit"));



        } catch (IOException ex) {
            System.out.println("Error in UserThread: " + ex.getMessage());
            server.removeUser(uname, this);
            server.sendToAll("**" + uname + " has quited.**", this);
            server.updateList();
            try {
                socket.close();
            } catch (IOException e) {
                //e.printStackTrace();
            }

            ex.printStackTrace();
        }
    }

    private void printUsers() {
        if (!server.getAllUsers().isEmpty()) {
            writer.println("Connected users: " + server.getAllUsers().keySet());
        } else {
            writer.println("No other users connected");
        }
    }

    public void sendMessage(String message) {
        writer.println(message);
    }

    private UserThread userValidation(String name) {
        for (UserThread u : server.getAllUserThreads()) {
            if (u.uname.equals(name))
                return u;
        }
        return null;
    }
    private void receiveFile(String fileName , int length) {
        try {
            InputStream inputStreamData;
            OutputStream outputStreamData;
            outputStreamData = new FileOutputStream(fileName);
            inputStreamData = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            byte[] buffer = new byte[1024];
            int i;
            for(int number = 0;number<length;number+=i){
                i = inputStreamData.read(buffer);
                outputStreamData.write(buffer, 0, i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendFile(File fileToSend){
        try {
            InputStream inputStreamData;
            OutputStream outputStreamData;
            inputStreamData = new BufferedInputStream(new FileInputStream(fileToSend));
            outputStreamData = new BufferedOutputStream(socket.getOutputStream());
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStreamData.read(buffer)) > 0) {
                outputStreamData.write(buffer, 0, length);
            }
            outputStreamData.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

