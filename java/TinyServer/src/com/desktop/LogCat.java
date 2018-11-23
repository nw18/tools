package com.desktop;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.Timer;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.newind.base.LogManager;

class LogCat extends JFrame{
	private static final int MAX_LENGTH = 1024;
	private static final long serialVersionUID = 1L;
	JList<String> listView;
	JScrollPane scrollPane;
	LinkedList<String> listData = new LinkedList<>();
	int removeCount = 0;
	int addingCount = 0;
	LinkedList<String> listCache = new LinkedList<>();
	List<ListDataListener> listeners = new ArrayList<>();
	Timer refreshTimer;
	ListModel<String> listModel = new ListModel<String>() {
		@Override
		public void addListDataListener(ListDataListener l) {
			synchronized (listeners) {
				if (!listeners.contains(l)) {
					listeners.add(l);					
				}
			}
		}

		@Override
		public String getElementAt(int index) {
			synchronized (listData) {				
				return listData.get(index);
			}
		}

		@Override
		public int getSize() {
			synchronized (listData) {				
				return listData.size();
			}
		}

		@Override
		public void removeListDataListener(ListDataListener l) {
			synchronized (listeners) {
				listeners.remove(l);
			}
		}
	};
	
	Handler logHandler = new Handler() {
		Date date = new Date(0);
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS",Locale.US);
		@Override
		public void publish(LogRecord record) {
			synchronized (listCache) {
				while (listCache.size() > MAX_LENGTH - 1) {
					listCache.removeFirst();
					removeCount ++;
				}
				date.setTime(record.getMillis());
				listCache.add(String.format("%06d %s %s",record.getThreadID(),dateFormat.format(date),record.getMessage()));
				addingCount++;
			}
		}
		
		@Override
		public void flush() {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void close() throws SecurityException {
			// TODO Auto-generated method stub
			
		}
	};
	
	LogCat(Component theQCode) {
		//setUndecorated(true);
		//getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
		setTitle("TinyServer logging");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(800, 600);
		setLayout(null);
		setLocationRelativeTo(null);
		listView = new JList<>();
		scrollPane = new JScrollPane();
		scrollPane.getViewport().add(listView);
		if(theQCode != null) {
			listView.add(theQCode);
			theQCode.setBounds(600,0,theQCode.getWidth(),theQCode.getHeight());
		}
		setContentPane(scrollPane);
		listView.setModel(listModel);
		logHandler.setLevel(Level.ALL);
		LogManager.getLogger().addHandler(logHandler);
		refreshTimer = new Timer(40, new ActionListener() {
			boolean hasNewData = false;
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(hasNewData){
					scrollEnd(); //scroll with a delay period.
				}
				if(syncData()){
					hasNewData = true;
				}else {
					hasNewData = false;
				}
			}
		});
		addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent arg0) {
			}
			
			@Override
			public void windowIconified(WindowEvent arg0) {
			}
			
			@Override
			public void windowDeiconified(WindowEvent arg0) {
			}
			
			@Override
			public void windowDeactivated(WindowEvent arg0) {
			}
			
			@Override
			public void windowClosing(WindowEvent arg0) {
			}
			
			@Override
			public void windowClosed(WindowEvent arg0) {
				refreshTimer.stop(); //other event not use.
			}
			
			@Override
			public void windowActivated(WindowEvent arg0) {
			}
		});
	}
	
	@Override
	public void setVisible(boolean arg0) {
		if (arg0) {
			refreshTimer.start();
		}else {
			refreshTimer.stop();
		}
		super.setVisible(arg0);
	}
	
	boolean syncData(){
		int removeCount = 0;
		int addingCount = 0;
		synchronized (listData) {
			synchronized (listCache) {
				removeCount = this.removeCount;
				addingCount = this.addingCount;
				if(removeCount == 0 && addingCount == 0){
					return false;
				}
				listData.clear();
				listData.addAll(listCache);
				this.removeCount = 0;
				this.addingCount = 0;
			}
			//
			if(addingCount > listData.size()){
				addingCount = listData.size();
			}
			synchronized (listeners) {
				if (removeCount > 0) {
					for(ListDataListener listener : listeners){
						listener.contentsChanged(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0, removeCount - 1));
					}
				}
				if(addingCount > 0){
					for(ListDataListener listener : listeners){
						listener.contentsChanged(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED,listData.size() - addingCount, listData.size() - 1));
					}
				}
			}
			if(listData.size() > 0){
				listView.setSelectedIndex(listData.size() - 1);
			}
		}
		return true;
	}
	
	void scrollEnd(){
		JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
		if (scrollBar.getValue() != scrollBar.getMaximum()) {
			scrollBar.setValue(scrollBar.getMaximum());
		}
	}
}
