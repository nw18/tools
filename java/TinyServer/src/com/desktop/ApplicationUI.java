package com.desktop;

import javax.swing.*;

//import com.newind.Application;

public class ApplicationUI {
	public static void main(String[] args) {
		//final Application application = new Application();
		JFrame frame = new JFrame("TinyServer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
