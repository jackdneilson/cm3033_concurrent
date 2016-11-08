package gui;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import main.Main;
import server.Server;

public class GUI extends JFrame {
	private JTextField current_time;
	private JButton start;
	private JButton stop;
	private JTextArea shares;
	private JTextArea history;
	private GridBagLayout layout;
	private GridBagConstraints constraints;
	
	private Server server;
	
	private void init() {
		Container c = getContentPane();
		setTitle("Telnet Server");
		setSize(600, 400);
		
		layout = new GridBagLayout();
		c.setLayout(layout);
		
		current_time = new JTextField(Main.getCurrTime());
		start = new JButton("Start");
		stop = new JButton("Stop");
		stop.setEnabled(false);
		shares = new JTextArea();
		shares.add(new JScrollBar());
		history = new JTextArea();
		history.add(new JScrollBar());
		constraints = new GridBagConstraints();
		
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				start.setEnabled(false);
				history.append("Starting...\n");
				server = new Server(Server.DEFAULT_PORT);
				server.setRunning(true);
				Thread server_thread = new Thread(server);
				server_thread.start();
				stop.setEnabled(true);
				history.append("Started server at " + Main.getCurrTime() + " on port " + server.getPortNumber() + "\n");
			}
		});
		
		stop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				history.append("Stopping...\n");
				stop.setEnabled(false);
				server.setRunning(false);
				start.setEnabled(true);
				history.append("Server stopped at " + Main.getCurrTime() + " on port " + server.getPortNumber() + "\n");
			}
		});
		
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 3;
		constraints.gridheight = 2;
		c.add(shares, constraints);
		
		constraints.gridy = 2;
		c.add(history, constraints);
		
		constraints.gridy = 4;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		c.add(start, constraints);
		
		constraints.gridx = 1;
		c.add(stop, constraints);
		
		constraints.gridx = 2;
		c.add(current_time, constraints);
	}
	
	//Should be called as a runnable with SwingUtilities.invokeLater()
	public static void createAndShow() {
		GUI frame = new GUI();
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		frame.init();
		frame.setVisible(true);
	}
}