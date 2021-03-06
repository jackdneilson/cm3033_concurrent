package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Socket;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

import javax.swing.SwingUtilities;

import gui.GUI;
import main.Main;

public class ConnectionHandler implements Runnable {
	private BufferedReader input_buf;
	private PrintWriter output_buf;
	private Socket socket;
	private Semaphore mutex;
	private HashMap<String, BigInteger> data;
	private GUI gui;
	
	public ConnectionHandler(Socket socket, Semaphore mutex, HashMap<String, BigInteger> data, GUI gui) {
		try {
			this.socket = socket;
			this.input_buf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.output_buf = new PrintWriter(socket.getOutputStream());
			this.mutex = mutex;
			this.data = data;
			this.gui = gui;
		}
		catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}
	
	//Note that this run method performs a blocking read immediately
	@Override
	public void run() {
		try {
			Scanner s;
			output_buf.write("Connected to share price quotation server at " + Inet4Address.getLocalHost() + ":" + socket.getLocalPort() + "\n\r");
			try {
				mutex.acquire();
				output_buf.write(Server.get_data_string(data) + "\r");
				mutex.release();
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}
			while (true) {
				output_buf.write("Enter an order in the format [BUY/SELL] <stock name> <stock amount> or QUIT to exit\n\r");
				output_buf.flush();
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
						throw new ParseException("Couldn't parse empty order", 0);
					}
					String temp = s.next();
					switch (temp) {
					//Checks that the order is in the form "BUY <key> <value>" and then buys the stock if the value given
					//is positive and less than the amount of current stock
					case "BUY":
						if (s.hasNext()) {
							temp = s.next();
							try {
								//Entering critical section as this code needs to read/modify the data
								mutex.acquire();
								Boolean keyFound = false;
								for (String key: data.keySet()) {
									if (temp.equals(key)) {
										keyFound = true;
										if (s.hasNext()) {
											temp = s.next();
											BigInteger numberToBuy = new BigInteger(temp);
											if (numberToBuy.compareTo(data.get(key)) <= 0 && numberToBuy.compareTo(new BigInteger("0")) == 1) {
												//Write output of operation to output buffer, then add output to history
												BigInteger currentNumber = data.get(key);
												data.put(key, currentNumber.subtract(numberToBuy));
												mutex.release();
												output_buf.write("Order Confirmed\n\n\r");
												output_buf.write(Server.get_data_string(data));
												output_buf.flush();
												SwingUtilities.invokeLater(new Runnable() {
													@Override
													public void run() {
														gui.add_to_history(numberToBuy + " stocks of " + key + " bought on " + Main.getCurrTime() + " by " +
															socket.getInetAddress() + ", port " + socket.getPort() + "\n");
													}
												});
												break;
											} else {
												s.close();
												mutex.release();
												throw new ParseException("The number of stocks to buy must be positive and less than the current number of stocks", 3);
											}
										} else {
											s.close();
											mutex.release();
											throw new ParseException("Couldn't parse incomplete order: expected number of shares", 3);
										}
									}
								}
								if (!keyFound) {
									s.close();
									mutex.release();
									throw new ParseException("The share code: " + temp + " was not found in the list of shares", 2);
								}
							} catch (InterruptedException e) {
								System.err.println(e.getMessage());
								System.exit(1);
							}
						} else {
							s.close();
							mutex.release();
							throw new ParseException("Couldn't parse incomplete order: expected stockcode", 2);
						}
						break;
					//Checks that the order is in the form "SELL <key> <value>" and then sells the stock if the value given
					//is positive
					case "SELL":
						temp = s.next();
						if (s.hasNext()) {
							temp = s.next();
							try {
								//Entering critical section as this code needs to read/modify the data
								mutex.acquire();
								for (String key: data.keySet()) {
									if (temp.equals(key)) {
										if (s.hasNext()) {
											temp = s.next();
											BigInteger numberToSell = new BigInteger(temp);
											if (numberToSell.compareTo(new BigInteger("0")) == 1) {
												BigInteger currentNumber = data.get(key);
												data.put(key, currentNumber.add(numberToSell));
												//Write output of operation to output buffer, then add output to history
												SwingUtilities.invokeLater(new Runnable() {
													@Override
													public void run() {
														gui.add_to_history(numberToSell + " stocks of " + key + " sold on " + Main.getCurrTime() + " by " +
															socket.getInetAddress() + ", port " + socket.getPort() + "\n\r");
													}
												});
												mutex.release();
												break;
											} else {
												s.close();
												mutex.release();
												throw new ParseException("The number of stocks to sell must be positive", 3);
											}
										} else {
											s.close();
											mutex.release();
											throw new ParseException("Couldn't parse incomplete order: expected number of shares", 3);
										}
									}
								}
								s.close();
								mutex.release();
								throw new ParseException("The share code: " + temp + " was not found in the list of shares", 2);
							} catch (InterruptedException e) {
								System.err.println(e.getMessage());
								System.exit(1);
							}
						} else {
							s.close();
							mutex.release();
							throw new ParseException("Couldn't parse incomplete order: expected stockcode", 2);
						}
						break;
					default:
						s.close();
						throw new ParseException("Couldn't parse order, problem with token: " + temp, 1);
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
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						gui.add_to_history("Connection from " + socket.getInetAddress() + " on port " + socket.getPort() + " dropped on " + Main.getCurrTime() + "\n\r");
					}
				});
			} catch (IOException e) {
				e.getMessage();
			}
		}
	}
}