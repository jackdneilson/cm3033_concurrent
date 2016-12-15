package gui;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;

import main.Main;
import server.Server;

@SuppressWarnings("serial")
public class GUI extends JFrame {
	private JTextField curr_time;
	private JButton start;
	private JButton stop;
	private JTextArea shares;
	private JTextArea history;
	private JScrollPane scroll;
	private GridBagLayout layout;
	private Timer updateTime;
	private Timer update_data;
	
	private Thread server_thread;
	private Server server;
	
	//Initialise GUI objects, add action listeners and start timers
	private void init() {
		Container c = getContentPane();
		setTitle("Telnet Server");
		setSize(600, 500);
		
		layout = new GridBagLayout();
		c.setLayout(layout);
		
		curr_time = new JTextField(Main.getCurrTime());
		start = new JButton("Start");
		stop = new JButton("Stop");
		stop.setEnabled(false);
		shares = new JTextArea();
		shares.setEditable(false);
		shares.setPreferredSize(getMinimumSize());
		history = new JTextArea();
		history.setEditable(false);
		scroll = new JScrollPane(history, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setPreferredSize(getMinimumSize());
		
		updateTime = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				curr_time.setText(Main.getCurrTime());
			}
		});
		updateTime.start();
		
		update_data = new Timer(100, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Semaphore mutex = server.get_semaphore();
				try {
					mutex.acquire();
					shares.setText(Server.get_data_string(server.get_data()));
					mutex.release();
				} catch (InterruptedException exception) {
					System.err.println(exception.getMessage());
				}
			}
		});
		
		//Passes reference to gui to enable server and connection handlers to update history field
		start.addActionListener(new StartButtonActionListener(this) {
			@Override
			public void actionPerformed(ActionEvent e) {
				start.setEnabled(false);
				history.append("Starting...\n");
				server = new Server(Server.DEFAULT_PORT, getGUI());
				server.set_running(true);
				server_thread = new Thread(server);
				server_thread.start();
				update_data.start();
				stop.setEnabled(true);
				history.append("Started server on " + Main.getCurrTime() + " on port " + server.get_port_number() + "\n");
			}
		});
		
		stop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stop.setEnabled(false);
				history.append("Stopping...\n");
				server.set_running(false);
				update_data.stop();
				shares.setText("");
				try {
					server.get_socket().close();
				} catch (IOException ioexception) {
					System.err.println(ioexception.getMessage());
				}
				
				start.setEnabled(true);
				history.append("Server stopped on " + Main.getCurrTime() + " on port " + server.get_port_number() + "\n");
			}
		});
		
		GridBagConstraints sharesConstraints = new GridBagConstraints();
		GridBagConstraints scrollConstraints = new GridBagConstraints();
		GridBagConstraints startConstraints = new GridBagConstraints();
		GridBagConstraints stopConstraints = new GridBagConstraints();
		GridBagConstraints currentTimeConstraints = new GridBagConstraints();
		
		sharesConstraints.gridx = 0;
		sharesConstraints.gridy = 0;
		sharesConstraints.gridwidth = 3;
		sharesConstraints.gridheight = 2;
		sharesConstraints.fill = GridBagConstraints.BOTH;
		sharesConstraints.weightx = 1.0;
		sharesConstraints.weighty = 1.0;
		c.add(shares, sharesConstraints);
		
		startConstraints.gridx = 0;
		startConstraints.gridy = 2;
		startConstraints.gridwidth = 1;
		startConstraints.gridheight = 1;
		startConstraints.fill = GridBagConstraints.HORIZONTAL;
		startConstraints.weightx = 1.0;
		startConstraints.weighty = 0;
		c.add(start, startConstraints);
		
		stopConstraints.gridx = 1;
		stopConstraints.gridy = 2;
		stopConstraints.gridwidth = 1;
		stopConstraints.gridheight = 1;
		stopConstraints.fill = GridBagConstraints.HORIZONTAL;
		stopConstraints.weightx = 1.0;
		stopConstraints.weighty = 0;
		c.add(stop, stopConstraints);
		
		currentTimeConstraints.gridx = 2;
		currentTimeConstraints.gridy = 2;
		currentTimeConstraints.gridwidth = 1;
		currentTimeConstraints.gridheight = 1;
		currentTimeConstraints.fill = GridBagConstraints.NONE;
		currentTimeConstraints.weightx = 1.0;
		currentTimeConstraints.weighty = 0;
		currentTimeConstraints.anchor = GridBagConstraints.EAST;
		c.add(curr_time, currentTimeConstraints);
		
		scrollConstraints.gridx = 0;
		scrollConstraints.gridy = 3;
		scrollConstraints.gridwidth = 3;
		scrollConstraints.gridheight = 2;
		scrollConstraints.fill = GridBagConstraints.BOTH;
		scrollConstraints.weightx = 1.0;
		scrollConstraints.weighty = 1.0;
		c.add(scroll, scrollConstraints);
	}
	
	//Should only be called after mutex lock is acquired
	public void add_to_history(String toAdd) {
		history.append(toAdd);
	}
	
	//Should be called as a runnable with SwingUtilities.invokeLater()
	public static void createAndShow() {
		GUI frame = new GUI();
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		frame.init();
		frame.setVisible(true);
	}
	
	public class StartButtonActionListener implements ActionListener {
		private GUI gui;
		
		public StartButtonActionListener(GUI gui) {
			this.gui = gui;
		}
		
		public GUI getGUI() {
			return this.gui;
		}
		
		//This should always be overridden
		@Override
		public void actionPerformed(ActionEvent e) {
		}
	}
}