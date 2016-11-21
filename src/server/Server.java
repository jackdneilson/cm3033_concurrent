package server;

import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.logging.FileHandler;

public class Server implements Runnable {
	public static final int DEFAULT_PORT = 9999;
	
	private int port_no;
	private ServerSocket server_socket;
	private ExecutorService executor;
	private volatile boolean isRunning;
	private Socket socket;
	private Semaphore mutex;
	private volatile HashMap<String, BigInteger> data;
	
	public Server(int port_no) {
		this.port_no = port_no;
		this.isRunning = false;
		this.mutex = new Semaphore(1);
		this.data = new HashMap<String, BigInteger>();
	}
	
	@Override
	//When run, starts listening on specified port and delegates incoming connections to
	//connection handlers which run in their own thread
	public void run() {
		try {
			this.server_socket = new ServerSocket(port_no);
			this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
			while (isRunning) {
				socket = server_socket.accept();
				executor.execute(new ConnectionHandler(socket, mutex, data));
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				//Waits until there are no connections before shutting down
				this.executor.shutdown();
				this.server_socket.close();
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
	}
	
	//Set to false when user presses stop button
	public void setRunning(boolean status) {
		this.isRunning = status;
	}
	
	public int getPortNumber() {
		return this.port_no;
	}
	
	public ServerSocket getSocket() {
		return this.server_socket;
	}
	
	//Should only be called after acquiring mutex
	public HashMap<String, BigInteger> getData() {
		return this.data;
	}
}