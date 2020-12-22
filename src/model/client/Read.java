package model.client;



import java.io.*;
import java.net.Socket;

public class Read extends Thread {
    private BufferedReader reader;
    private Socket socket;
    private InputStream input;
    private String uname;

    public Read(Socket socket) {
        this.socket = socket;
        try {
            input = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));
        } catch (IOException ex) {
            System.out.println("Error getting input stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            try {
                String response = reader.readLine();
                //todo: call function to read file in socket
                String[] arr = response.split(" ");
                if (arr[0].equals("**FILE**")) {
                    StringBuilder s = new StringBuilder();
                    s.append(arr[1], 0, arr[1].lastIndexOf('\\'));
                    String temp = '\\' + uname + "-";
                    s.append(temp);
                    s.append(arr[1].substring(arr[1].lastIndexOf('\\')+1));
                    //s.append(arr[1].substring(arr[1].lastIndexOf('\\')+temp.length()));

                    //System.out.println(s);

                    receiveFile(s.toString() , Integer.parseInt(arr[2]));
                    System.out.println("\nFile received!");
                } else {
                    System.out.println("\n" + response);
                }
            } catch (Exception ex) {
//                System.out.println("Error reading from server: " + ex.getMessage());
//                ex.printStackTrace();
                break;
            }
        }
    }

    private void receiveFile(String fileName , int length) {

        try {
            InputStream inputStreamData;
            OutputStream outputStreamData;
            System.out.println(fileName);
            outputStreamData = new FileOutputStream(fileName);
            inputStreamData = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            byte[] buffer = new byte[1024];
            int i;
            //System.out.println("before while");
            System.out.println(length);
            for(int number = 0;number<length;number+=i){
                i = inputStreamData.read(buffer);
                //System.out.println("***"+i);
                outputStreamData.write(buffer, 0, i);
            }
            //System.out.println("after while");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getUname() {
        return uname;
    }
}
