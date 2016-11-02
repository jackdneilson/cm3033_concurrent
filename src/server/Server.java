package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.ParseException;

public class Server implements Runnable {
	private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
	private static final SimpleDateFormat FORMAT = new SimpleDateFormat("YYYY/MM/dd HH:mm:ss");
	private static final int DEFAULT_PORT = 9999;
	
	private int port_no;
	private ServerSocket server_socket;
	private ExecutorService executor;
	
	public Server(int port_no) {
		try {
			this.port_no = port_no;
			this.server_socket = new ServerSocket(port_no);
			this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		}
		catch (IOException e) {
			//TODO: Add ioexception code
		}
	}
	
	@Override
	public void run() {
		try {
			while (true) {
				Socket socket = server_socket.accept();
				executor.submit(new ConnectionHandler(socket));
			}
		}
		catch (IOException e) {
			//TODO: Implement ioexception logic.
		}
	}
	
	public int getPortNumber() {
		return this.port_no;
	}
	
	public static void main(String[] args) {
		try {
			//Create a new handler for the logger and add it.
			FileHandler FILE = new FileHandler(System.getProperty("user.dir") + "/telnet_server.log", true);
			LOGGER.addHandler(FILE);
			
			//Checks if a port has been specified and is an int, otherwise sets to default.
			Server server;
			if(args[0] != null) {
				try {
					server = new Server(Integer.parseInt(args[0]));
				}
				catch (ParseException e) {
					server = new Server(DEFAULT_PORT);
				}
			} else {
				server = new Server(DEFAULT_PORT);
			}
			
			LOGGER.log(Level.INFO, getDate() + " - Started server on port " + server.getPortNumber());
			server.run();
		}
		
		//If we can't find or write to the log file for whatever reason.
		catch (IOException e) {
			LOGGER.log(Level.SEVERE, getDate() + " - Could not write to log file");
			System.exit(1);
		}
	}
	
	//Return current date and time as a string.
	public static String getDate() {
		return FORMAT.format(new java.util.Date());
	}
}