package main;

import java.text.SimpleDateFormat;

import javax.swing.SwingUtilities;

import gui.GUI;

public class Main {
	private static final SimpleDateFormat FORMAT = new SimpleDateFormat("YYYY/MM/dd HH:mm:ss");
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				GUI.createAndShow();
			}
		});
	}
	
	//Return current date and time as a string.
	public static String getCurrTime() {
		return FORMAT.format(new java.util.Date());
	}
}
