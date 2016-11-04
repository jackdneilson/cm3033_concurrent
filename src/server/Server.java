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
	public static final int DEFAULT_PORT = 9999;
	
	private int port_no;
	private ServerSocket server_socket;
	private ExecutorService executor;
	private volatile boolean isRunning;
	
	public Server(int port_no) {
		this.port_no = port_no;
		this.isRunning = false;
	}
	
	@Override
	//When run, starts listening on specified port and delegates incoming connections to
	//connection handles which run in their own thread
	public void run() {
		try {
			this.server_socket = new ServerSocket(port_no);
			this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
			while (isRunning) {
				Socket socket = server_socket.accept();
				executor.execute(new ConnectionHandler(socket));
			}
		}
		catch (IOException e) {
			//TODO: Implement ioexception logic.
		}
	}
	
	public synchronized void setRunning(boolean status) {
		this.isRunning = status;
	}
	
	public int getPortNumber() {
		return this.port_no;
	}
}