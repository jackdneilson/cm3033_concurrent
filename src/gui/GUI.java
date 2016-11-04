package gui;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
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
	
	private Server server;
	
	private void init() {
		Container c = getContentPane();
		setTitle("Telnet Server");
		setSize(600, 400);
		
		current_time = new JTextField(Main.getDate());
		start = new JButton("Start");
		stop = new JButton("Stop");
		stop.setEnabled(false);
		shares = new JTextArea();
		history = new JTextArea();
		
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				start.setEnabled(false);
				history.append("Starting...\n");
				server = new Server(Server.DEFAULT_PORT);
				server.setRunning(true);
				stop.setEnabled(true);
				SwingUtilities.invokeLater(server);
				history.append("Started server on " + Main.getDate() + " on port " + server.getPortNumber() + "\n");
			}
		});
		
		stop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stop.setEnabled(false);
				history.append("Stopping...\n");
				server.setRunning(false);
			}
		});
	}
	
	//Should be called as a runnable with SwingUtilities.invokeLater()
	public static void createAndShow() {
		GUI frame = new GUI();
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		frame.init();
		frame.setVisible(true);
	}
}