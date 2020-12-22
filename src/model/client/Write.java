package model.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Write extends Thread {
    private PrintWriter writer;
    private Socket socket;
    private OutputStream output;
    private String uname;

    public Write(Socket socket) {
        this.socket = socket;
        try {
            output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
        } catch (IOException ex) {
            System.out.println("Error getting output stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        String userName = scanner.next();
        writer.println(userName);
        uname = userName;
        scanner.nextLine();
        String text;
        do {
            text = scanner.nextLine();
            //todo: call function to write file in socket
            if(text.split(" ").length>2 && text.split(" ")[2].equals("-f")){

                String location = text.split(" ")[3];
                File f = new File(location);
                System.out.println(location);
                if(f.exists()){
                    System.out.println(f.getName());
                    writer.println(text+" "+f.getName()+" "+f.length());
                    //
                    sendFile(f);
                    //System.out.println("**Sent2!**");

                }else{
                    System.out.println("**File doesn't exist!**");

                }
            }else{
                writer.println(text);
            }
        } while (!text.equals("quit"));
//        try {
//            socket.close();
//        } catch (IOException ex) {
//            System.out.println("Error writing to server: " + ex.getMessage());
//        }
    }
    private void sendFile(File fileToSend){

        try {
            InputStream inputStreamData ;
            OutputStream outputStreamData ;
            inputStreamData = new BufferedInputStream(new FileInputStream(fileToSend));
            outputStreamData = new BufferedOutputStream(socket.getOutputStream());
            byte[] buffer = new byte[1024];
            int length;
            //System.out.print("before while");
            while ((length = inputStreamData.read(buffer)) > 0) {
                //System.out.print("*"+length);
                outputStreamData.write(buffer, 0, length);
            }
            outputStreamData.flush();
            //System.out.print("after while");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUname() {
        return uname;
    }
}
