package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

import org.omg.CORBA.SystemException;

import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.ParseException;

public class ConnectionHandler implements Runnable {
	private BufferedReader input_buf;
	private PrintWriter output_buf;
	private Socket socket;
	private Semaphore mutex;
	private HashMap<String, BigInteger> data;
	
	public ConnectionHandler(Socket socket, Semaphore mutex, HashMap<String, BigInteger> data) {
		try {
			this.socket = socket;
			this.input_buf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.output_buf = new PrintWriter(socket.getOutputStream());
			this.mutex = mutex;
			this.data = data;
		}
		catch (IOException e) {
			System.err.println("Exception while trying to open socket i/o stream:" + e.getMessage());
		}
	}
	
	@Override
	//Note that this run method performs a blocking read immediately
	public void run() {
		try {
			Scanner s;
			output_buf.write("Connected to share price quotation server at " + Inet4Address.getLocalHost() + ":" + socket.getLocalPort() + "\n");
			output_buf.flush();
			while (true) {
				String input = input_buf.readLine();
				if (input.equals("QUIT")) {
					output_buf.write("Goodbye\n");
					output_buf.flush();
					break;
				}
				s = new Scanner(input);
				s.useDelimiter(" ");
				try {
					if (!s.hasNext()) {
						s.close();
						throw new ParseException("Couldn't parse empty order");
					}
					//TODO: Add buy/sell logic, implement error checking
					String temp = s.next();
					switch (temp) {
					//Checks that the order is in the form "BUY <key> <value>" and then buys the stock if the value given
					//is positive and less than the amount of current stock
					case "BUY":
						if (s.hasNext()) {
							temp = s.next();
							try {
								mutex.acquire();
								for (String key: data.keySet()) {
									if (temp.equals(key)) {
										if (s.hasNext()) {
											temp = s.next();
											BigInteger numberToBuy = new BigInteger(temp);
											if (numberToBuy.compareTo(data.get(key)) == -1 && numberToBuy.compareTo(new BigInteger("0")) == 1) {
												BigInteger currentNumber = data.get(key);
												data.put(key, currentNumber.subtract(numberToBuy));
												mutex.release();
												break;
											} else {
												s.close();
												mutex.release();
												throw new ParseException("The number of stocks to buy must be positive and less than the current number of stocks");
											}
										} else {
											s.close();
											mutex.release();
											throw new ParseException("Couldn't parse incomplete order: expected number of shares");
										}
									}
								}
								s.close();
								mutex.release();
								throw new ParseException("The share code: " + temp + " was not found in the list of shares");
							} catch (InterruptedException e) {
								System.err.println(e.getMessage());
								System.exit(1);
							}
						} else {
							s.close();
							mutex.release();
							throw new ParseException("Couldn't parse incomplete order: expected stockcode");
						}
						break;
					//TODO: Write comment
					case "SELL":
						temp = s.next();
						break;
					default:
						s.close();
						throw new ParseException("Couldn't parse order, problem with token: " + temp);
					}
				} catch (ParseException e) {
					output_buf.println(e.getMessage());
					output_buf.flush();
				}
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		} catch (NullPointerException e) {
			System.out.println("Stream closed prematurely by user");
		}
		//Close buffers to prevent memory leak after run() method is finished
		finally {
			try {
				socket.close();
				input_buf.close();
				output_buf.close();
			} catch (IOException e) {
				e.getMessage();
			}
		}
	}
}