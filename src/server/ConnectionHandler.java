package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ConnectionHandler implements Runnable {
	private boolean isRunning;
	private Socket socket;
	private BufferedReader input_buf;
	private PrintWriter output_buf;
	
	public ConnectionHandler(Socket socket) {
		this.isRunning = false;
		try {
			this.input_buf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.output_buf = new PrintWriter(socket.getOutputStream());
		}
		catch (IOException e) {
			System.err.println("Exception while trying to open socket i/o stream:" + e.getMessage());
		}
	}
	
	@Override
	//Note that this run method performs a blocking read immediately
	public void run() {
		while(isRunning) {
			try {
				String temp = input_buf.readLine();
				output_buf.write(temp);
				output_buf.flush();
			}
			catch (IOException e) {
				System.err.println(e.getMessage());
			}
			//Close buffers to prevent memory leak after run() method is finished
			finally {
				try {
					input_buf.close();
					output_buf.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}