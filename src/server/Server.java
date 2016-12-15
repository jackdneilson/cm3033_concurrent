package server;

import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import javax.swing.SwingUtilities;

import gui.GUI;
import main.Main;

public class Server implements Runnable {
	public static final int DEFAULT_PORT = 9999;
	
	private int port_no;
	private ServerSocket server_socket;
	private ExecutorService executor;
	private volatile boolean is_running;
	private Semaphore mutex;
	private GUI gui;
	//BigInteger used to represent stocks to deal with large numbers of stocks, and because
	//you cannot have < 1 stock
	private volatile HashMap<String, BigInteger> data;
	
	public Server(int port_no, GUI gui) {
		this.port_no = port_no;
		this.is_running = false;
		this.mutex = new Semaphore(1);
		this.gui = gui;
		this.data = new HashMap<String, BigInteger>();
		initialise_data();
	}
	
	@Override
	//When run, starts listening on specified port and delegates incoming connections to
	//connection handlers which run in their own thread
	public void run() {
		try {
			this.server_socket = new ServerSocket(port_no);
			this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
			while (is_running) {
				Socket socket = server_socket.accept();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						gui.add_to_history("New connection from " + socket.getInetAddress() + " on port " + socket.getPort() + " on " + Main.getCurrTime() + "\n");
					}
				});
				executor.execute(new ConnectionHandler(socket, mutex, data, gui));
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				//Continues to serve current connections after server has been stopped
				this.executor.shutdown();
				this.server_socket.close();
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
	}
	
	public void set_running(boolean status) {
		this.is_running = status;
	}
	
	public int get_port_number() {
		return this.port_no;
	}
	
	public ServerSocket get_socket() {
		return this.server_socket;
	}
	
	//Should only be called after acquiring mutex
	public HashMap<String, BigInteger> get_data() {
		return this.data;
	}
	
	private void initialise_data() {
		data.put("SKY", new BigInteger("7777"));
		data.put("VOD", new BigInteger("1234"));
		data.put("TSCO", new BigInteger("2356"));
		data.put("BP", new BigInteger("4015"));
	}
	
	//Returns a string representation of the data
	public static String get_data_string(HashMap<String, BigInteger> data) {
		StringBuilder retval = new StringBuilder();
		for (String key: data.keySet()) {
			retval.append(key);
			retval.append(": ");
			retval.append(data.get(key));
			retval.append("\n\r");
		}
		return retval.toString();
	}
	
	public Semaphore get_semaphore() {
		return this.mutex;
	}
}