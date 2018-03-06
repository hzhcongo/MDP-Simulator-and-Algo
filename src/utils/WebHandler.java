package utils;

import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.io.*;

public class WebHandler {
	static Thread sent;
	static Thread receive;
	static Socket socket;
	private static final Object lock = new Object();
	public static final int PORT = 2323;
	public static final String HOSTNAME = "192.168.4.2";
	public static BufferedWriter stdOut;
	public static BufferedReader stdIn;
    
	public static void openConnection(){
		try {
			socket = new Socket(HOSTNAME,PORT);
			BufferedWriter stdOut = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			BufferedReader stdIn =new BufferedReader(new InputStreamReader(socket.getInputStream()));

			System.out.print("Buffer initialized");
//			while(true){ 
//				//Uncomment the method below for reading string from RPI
//				readMessageFromRPI(stdIn);}
//				// Method below for sending string to RPI
//				//for(int i=0;i<5;i++){
//				//   	sendMessageToRPI("DSFF "+i,stdOut);
//				//         } 
//				
//				//sendMessageToRPI("Bye! To RPi", stdOut);
//	
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	}

	public static String readMessageFromRPI (){
		String message;
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.print("Reading Message From RPI........");
			}
		});
		try {
			Thread.sleep(200);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while(true){
			if(Thread.holdsLock(lock)==false)
			{  
				message="";                 
				try {
					message = stdIn.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
					System.out.println("[MessageHandler]getString: " + message);
				thread.start();
				break;
			}
		}
		return message;
	}

	public static void sendMessageToRPI (String message){

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.print("Sending Message To RPI........");
			}
		});
		while(true){
			if(Thread.holdsLock(lock)==false)
			{                  
				try {
					stdOut.write(message);
					socket.setTcpNoDelay(true);
					stdOut.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}             
				System.out.println("Message sent: "+message); 
				thread.start(); break;
			}
		}
	}
}