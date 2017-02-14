package com.desktop;

import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import javax.swing.*;
import com.newind.Application;
import com.newind.util.InputUtil;
import com.newind.util.InputUtil.ParameterException;
import com.newind.util.TextUtil;

public class ApplicationUI extends JFrame{
	private static final long serialVersionUID = 1L;
	private static final String LAST_CONFIG = "./TinyServer.conf";
	private Application application;
	private String[] config = new String[] {
		"ip","0.0.0.0",
		"http_port","8080",
		"ftp_port","2121",
		"http_on","true",
		"ftp_on","false",
		"writable","false",
		"root","D:\\",
		"json_mode","true",
		"user_name","admin",
		"pass_word","123456",
		"thread_count","64",
	};
	
	JTextField ipAddress,httpPort,ftpPort,root,userName,passWord,threadCount;
	JCheckBox httpOn, ftpOn,writable,jsonMode;
	
	void initFrame() {
		ipAddress = new JTextField(getValue("ip"));
		httpPort = new JTextField(getValue("http_port"));
		ftpPort = new JTextField(getValue("ftp_port"));
		root = new JTextField(getValue("root"));
		userName = new JTextField(getValue("user_name"));
		passWord = new JTextField(getValue("pass_word"));
		threadCount = new JTextField(getValue("thread_count"));
		httpOn = new JCheckBox("http on", Boolean.parseBoolean(getValue("http_on")));
		ftpOn = new JCheckBox("ftp on", Boolean.parseBoolean(getValue("ftp_on")));
		writable = new JCheckBox("ftp writable",Boolean.parseBoolean(getValue("writable")));
		jsonMode = new JCheckBox("http output in json format", Boolean.parseBoolean(getValue("json_mode")));
	}
	
	void saveLast(){
		FileOutputStream outputStream = null;
		try{
			outputStream = new FileOutputStream(LAST_CONFIG);
			for(int i = 0; i < config.length; i++){
				outputStream.write(config[i].getBytes());
				outputStream.write("\r\n".getBytes());
			}
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	void loadLast(){
		FileInputStream inputStream = null;
		BufferedReader bufferedReader = null;
		String[] lastConfig = new String[config.length];
		int i = 0;
		try {
			File lastFile = new File(LAST_CONFIG);
			if(!(lastFile.exists() && lastFile.isFile() && lastFile.canRead())){
				return;
			}
			inputStream = new FileInputStream(lastFile);
			bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			while(i < lastConfig.length){
				String line = bufferedReader.readLine();
				if (TextUtil.isEmpty(line) || (i % 2 == 0 && !TextUtil.equal(line, config[i]))) {
					break;
				}
				lastConfig[i++] = line;
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}finally {
			if (inputStream != null) {
				try { inputStream.close(); } catch (IOException e) { }
			}
		}
		if (i == lastConfig.length) {
			for(i = 1; i < config.length; i+=1){
				config[i] = lastConfig[i];
			}
		}
	}
	
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
	
	JLabel makeLabel(String text,int x,int y){
		JLabel label = new JLabel(text);
		label.setVerticalAlignment(JLabel.CENTER);
		pLast.set(x, y, 80, 30);
		label.setBounds(x, y, 80, 30);
		return label;
	}
	
	void moveHalfShort(JComponent component,int x,int y){
		pLast.set(x, y, 60, 30);
		component.setBounds(x, y, 60, 30);
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
		//setUndecorated(true);
		//getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
		setTitle("TinyServer");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JLabel label = null;
		setContentPane(new Panel());
		setLayout(null);
		loadLast();
		initFrame();
		//the first line
		moveNextLine();
		label = makeLabel("ip:", pLast.right() + PADDING, pLast.y);
		add(label);
		moveShort(ipAddress, pLast.right() + PADDING, pLast.y);
		add(ipAddress);
		final JButton choseIP = new JButton("...");
		moveHalfShort(choseIP, pLast.right() + PADDING, pLast.y);
		add(choseIP);
		choseIP.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JPopupMenu menu = new JPopupMenu();
				List<String> ipList = InputUtil.getAllIP();
				for(final String ip : ipList){
					menu.add(ip).addActionListener(new ActionListener() {
						String ipStr = ip;
						@Override
						public void actionPerformed(ActionEvent arg0) {
							ipAddress.setText(ipStr);
						}
					});;
				}
				menu.show(ApplicationUI.this, choseIP.getX() + choseIP.getWidth(), choseIP.getY() + choseIP.getHeight());
			}
		});
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
		moveShort(writable, pLast.right() + PADDING, pLast.y);
		add(writable);
		//the 4 line
		moveNextLine();
		moveLong(jsonMode, pLast.right() + PADDING, pLast.y);
		add(jsonMode);
		//the 5 line
		moveNextLine();
		label = makeLabel("root path:", pLast.right() + PADDING, pLast.y);
		add(label);
		moveLong(root, pLast.right() + PADDING, pLast.y);
		add(root);
		JButton choseFile = new JButton("...");
		moveHalfShort(choseFile, pLast.right() + PADDING, pLast.y);
		add(choseFile);
		//the 6 line
		moveNextLine();
		label = makeLabel("user:", pLast.right() + PADDING, pLast.y);
		add(label);
		moveShort(userName, pLast.right() + PADDING, pLast.y);
		add(userName);
		label = makeLabel("pass:", pLast.right() + PADDING, pLast.y);
		add(label);
		moveShort(passWord, pLast.right() + PADDING, pLast.y);
		add(passWord);
		label = makeLabel("thread:", pLast.right() + PADDING, pLast.y);
		add(label);
		moveShort(threadCount, pLast.right() + PADDING, pLast.y);
		add(threadCount);
		//the 7 line
		moveNextLine();
		moveNextLine();
		JButton buttonStart = new JButton("start server");
		moveShort(buttonStart, pLast.right() + PADDING, pLast.y);
		add(buttonStart);
		setSize(Rect.MAX_RIGHT + PADDING * 2 + 20, Rect.MAX_BOTTOM + PADDING * 2 + 50);
		buttonStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				try {
					saveConfig();
					application.startServer(config);
					saveLast();
				} catch (ParameterException e) {
					JOptionPane.showMessageDialog(ApplicationUI.this, e.getMessage());
					return;
				} catch (Exception e) {
					e.printStackTrace();
				}
				dispose();
				
			}
		});
		choseFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser chooser = new JFileChooser(root.getText());
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int res = chooser.showOpenDialog(ApplicationUI.this);
				if (res == JFileChooser.APPROVE_OPTION) {
					root.setText(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		setLocationRelativeTo(null);
	}
	
	void saveConfig() throws ParameterException{
		String ip = ipAddress.getText();
		if (!InputUtil.isIp(ip)) {
			ipAddress.selectAll();
			throw new ParameterException("bad ipv4 address.");
		}
		updateValue("ip", ip);
		String http_port = httpPort.getText();
		if (!InputUtil.isPort(http_port)) {
			httpPort.selectAll();
			throw new ParameterException("bad http port.");
		}
		updateValue("http_port", http_port);
		String ftp_port = ftpPort.getText();
		if (!InputUtil.isPort(ftp_port)) {
			ftpPort.selectAll();
			throw new ParameterException("bad ftp port.");
		}
		updateValue("ftp_port", ftp_port);
		File rootFile = new File(root.getText());
		if (!rootFile.exists() || !rootFile.isDirectory() || !TextUtil.equal(rootFile.getAbsolutePath(), root.getText())) {
			root.selectAll();
			throw new ParameterException("bad root path.");
		}
		updateValue("root", rootFile.getAbsolutePath());
		String user_name = userName.getText();
		if (!InputUtil.isUserName(user_name)) {
			userName.selectAll();
			throw new ParameterException("bad user name.");
		}
		updateValue("user_name", user_name);
		String pass_word = passWord.getText();
		if (!InputUtil.isUserName(pass_word)) {
			passWord.selectAll();
			throw new ParameterException("bad pass word.");
		}
		String thread_count = threadCount.getText();
		if (!InputUtil.isPort(thread_count) || Integer.parseInt(thread_count) < 4 || Integer.parseInt(thread_count) > 4096) {
			threadCount.selectAll();
			throw new ParameterException("thead count [4~4096]");
		}
		updateValue("thread_count", thread_count);
		updateValue("pass_word", pass_word);
		updateValue("http_on",String.valueOf(httpOn.isSelected()));
		updateValue("ftp_on",String.valueOf(ftpOn.isSelected()));
		updateValue("writable", String.valueOf(writable.isSelected()));
		updateValue("json_mode", String.valueOf(jsonMode.isSelected()));
	}
	
	void updateValue(String key,String value) throws ParameterException{
		System.out.println(key + ":" + value);
		for(int i = 0; i < config.length; i += 2){
			if (TextUtil.equal(key, config[i])) {
				config[i+1] = value;
				return;
			}
		}
		throw new ParameterException("Can't find " + key);
	}
	
	String getValue(String key) throws ParameterException{
		for(int i = 0; i < config.length; i += 2){
			if (TextUtil.equal(key, config[i])) {
				return config[i+1];
			}
		}
		throw new ParameterException("Can't find " + key);
	}
	
	void setup(){
		try {
			LogCat logCat = new LogCat();
			application = new Application();
			setVisible(true);
			while(isShowing()){
				Thread.sleep(100);
			}
			if (!application.isRunning()) {
				return;
			}
			setVisible(false);
			logCat.setVisible(true);
			while(logCat.isShowing()){
				Thread.sleep(100);
			}
			application.closeServer();
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
