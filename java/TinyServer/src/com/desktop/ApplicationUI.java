package com.desktop;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import com.newind.Application;

public class ApplicationUI extends JFrame{
	private static final long serialVersionUID = 1L;
	private Application application;
	private String[] config = new String[] {
		"ip","0.0.0.0",
		"http_port","8080",
		"ftp_port","2121",
		"http_on","true",
		"ftp_on","false",
		"root","D:\\",
		"json","true",
		"user_name","admin",
		"pass_word","123456",
		"debug","true",
	};
	
	JTextField ipAddress = new JTextField("0.0.0.0"),
			httpPort = new JTextField("8080"),
			ftpPort = new JTextField("2121"),
			root = new JTextField("D:\\"),
			userName = new JTextField("admin"),
			passWord = new JTextField("123456");
	JCheckBox httpOn = new JCheckBox("http on", true), 
			ftpOn = new JCheckBox("ftp on" , false),
			json = new JCheckBox("http output in json format", true);
	
	static class Rect {
		public static int MAX_RIGHT = -1;
		public static int MAX_BOTTOM = -1;
		Rect(int x,int y,int widht,int height){
			set(x, y, widht, height);
		}
		
		int x,y,width,height;
		int right() {
			return x + width;
		}
		
		int bottom(){
			return y + height;
		}
		
		void set(int x,int y,int widht,int height){
			this.x = x;
			this.y = y;
			this.width = widht;
			this.height = height;
			if (right() > MAX_RIGHT) {
				MAX_RIGHT = right();
			}
			if (bottom() > MAX_BOTTOM) {
				MAX_BOTTOM = bottom();
			}
		}
	}
	
	static class ParameterException extends RuntimeException{
		private static final long serialVersionUID = 1L;

		ParameterException(String message) {
			super(message);
		}
	}
	
	JLabel makeLabel(String text,int x,int y){
		JLabel label = new JLabel(text);
		label.setVerticalAlignment(JLabel.CENTER);
		pLast.set(x, y, 80, 30);
		label.setBounds(x, y, 80, 30);
		return label;
	}
	
	void moveShort(JComponent component,int x,int y){
		pLast.set(x, y, 120, 30);
		component.setBounds(x, y, 120, 30);
	}
	
	void moveLong(JComponent component,int x,int y){
		pLast.set(x, y, 400, 30);
		component.setBounds(x, y, 400, 30);
	}
	
	void moveNextLine(){
		pLast.set(PADDING,pLast.bottom() + 10,0,0);
	}
	
	static final int PADDING = 10;
	
	Rect pLast = new Rect(PADDING, PADDING, 0, 0);
	
	ApplicationUI() {
		setTitle("TinyServer");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(800, 600);
		JLabel label = null;
		setLayout(null);
		//the first line
		moveNextLine();
		label = makeLabel("ip:", pLast.right() + PADDING, pLast.y);
		add(label);
		moveShort(ipAddress, pLast.right() + PADDING, pLast.y);
		add(ipAddress);
		//the second line
		moveNextLine();
		label = makeLabel("http port:", pLast.right() + PADDING, pLast.y);
		add(label);
		moveShort(httpPort, pLast.right() + PADDING, pLast.y);
		add(httpPort);
		label = makeLabel("ftp port:", pLast.right() + PADDING, pLast.y);
		add(label);
		moveShort(ftpPort, pLast.right() + PADDING, pLast.y);
		add(ftpPort);
		// the 3 line
		moveNextLine();
		moveShort(httpOn, pLast.right() + PADDING, pLast.y);
		add(httpOn);
		moveShort(ftpOn, pLast.right() + PADDING, pLast.y);
		add(ftpOn);
		//the 4 line
		moveNextLine();
		moveLong(json, pLast.right() + PADDING, pLast.y);
		add(json);
		//the 5 line
		moveNextLine();
		label = makeLabel("root path:", pLast.right() + PADDING, pLast.y);
		add(label);
		moveLong(root, pLast.right() + PADDING, pLast.y);
		add(root);
		//the 6 line
		moveNextLine();
		label = makeLabel("user:", pLast.right() + PADDING, pLast.y);
		add(label);
		moveShort(userName, pLast.right() + PADDING, pLast.y);
		add(userName);
		label = makeLabel("pass:", pLast.right() + PADDING, pLast.y);
		moveShort(passWord, pLast.right() + PADDING, pLast.y);
		add(passWord);
		add(label);
		//the 7 line
		moveNextLine();
		moveNextLine();
		JButton buttonStart = new JButton("start server");
		moveShort(buttonStart, pLast.right() + PADDING, pLast.y);
		add(buttonStart);
		setSize(Rect.MAX_RIGHT + PADDING * 2 + 20, Rect.MAX_BOTTOM + PADDING * 2 + 60);
		buttonStart.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				try {
					saveConfig();
					application.startServer(config);
				} catch (ParameterException e) {
					JOptionPane.showMessageDialog(ApplicationUI.this, e.getMessage());
					return;
				} catch (Exception e) {
					e.printStackTrace();
				}finally {
					dispose();
				}
				
			}
		});
		setLocationRelativeTo(null);
	}
	
	void saveConfig() throws ParameterException{
		//TODO save frame to configure value.
	}
	
	void setup(){
		try {
			application = new Application();
			setVisible(true);
			while(isShowing()){
				Thread.sleep(100);
			}
			application.waitServer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		ApplicationUI applicationUI = new ApplicationUI();
		applicationUI.setup();
		System.out.println("exit?");
	}
}
