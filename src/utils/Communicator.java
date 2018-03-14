package utils;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * To communicate with the different parts of the system via RPi
 */

public class Communicator {

    public static final String EX_START = "EX_START";       // Android to PC
    public static final String FP_START = "FP_START";       // Android to PC
    public static final String MAP_STRINGS = "MAP";         // PC to Android
    public static final String BOT_POS = "BOT_POS";         // PC to Android
    public static final String BOT_START = "BOT_START";     // PC to Arduino
    public static final String INSTRUCTIONS = "INSTR";      // PC to Arduino
    public static final String SENSOR_DATA = "SDATA";       // Arduino to PC

    private static Communicator communicator = null;
    private static Socket connectionSocket = null;

    public BufferedWriter writer;
    public BufferedReader reader;
	
    private Communicator() {
    }

    public static Communicator getCommMgr() {
        if (communicator == null) {
            communicator = new Communicator();
        }
        return communicator;
    }

    public void openConnection() {
        System.out.println("Opening connection...");

        try {
            String HOST = "192.168.4.1";
            int PORT = 2323;
            connectionSocket = new Socket(HOST, PORT);
            connectionSocket.setTcpNoDelay(true);

            writer = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(connectionSocket.getOutputStream())));
            reader = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

            System.out.println("openConnection(): " + " success");

            return;
        } catch (UnknownHostException e) {
            System.out.println("UnknownHostException at openConnection()");
        } catch (IOException e) {
            System.out.println("IOException at openConnection()");
        } catch (Exception e) {
            System.out.println("Exception at openConnection()");
            System.out.println(e.toString());
        }

        System.out.println("Failed to connect");
    }

    public void closeConnection() {
        System.out.println("Closing connection");

        try {
            reader.close();

            if (connectionSocket != null) {
                connectionSocket.close();
                connectionSocket = null;
            }
            System.out.println("Connection closed");
        } catch (IOException e) {
            System.out.println("IOException at closeConnection()");
        } catch (NullPointerException e) {
            System.out.println("NullPointerException at closeConnection()");
        } catch (Exception e) {
            System.out.println("Exception at closeConnection()");
            System.out.println(e.toString());
        }
    }

    public boolean isConnected() {
        return connectionSocket.isConnected();
    }

    public void sendMsg(String msg, String msgType) {
        System.out.print("Sending message: ");
        try {
            writer.write(msg);
            writer.flush();
            System.out.println(msg);
        } catch (IOException e) {
            System.out.println("IOException at sendMsg()");
        } catch (Exception e) {
            System.out.println("Exception at sendMsg()");
            System.out.println(e.toString());
        }
    }

    public String recvMsg() {
        System.out.print("Recieving message: ");
        try {
            StringBuilder sb = new StringBuilder();
            String input = reader.readLine();
            
            sb.append(input);
            if(sb != null) System.out.println(sb.toString());
            return sb.toString();
        } catch (IOException e) {
            System.out.println("IOException at recvMsg()");
        } catch (NullPointerException e) {
            System.out.println("NullPointerException at recvMsg()");
        } catch (Exception e) {
            System.out.println("Exception at recvMsg(): " + e.toString());
        }

        return null;
    }
}