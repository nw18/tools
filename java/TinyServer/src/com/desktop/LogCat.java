package com.desktop;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.newind.base.LogManager;

class LogCat extends JFrame{
	private static final int MAX_LENGTH = 1024;
	private static final long serialVersionUID = 1L;
	JList<String> listView;
	JScrollPane scrollPane;
	LinkedList<String> listData = new LinkedList<>();
	List<ListDataListener> listeners = new ArrayList<>();
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
		@Override
		public void publish(LogRecord record) {
			int removeCount = 0;
			int addPosition = 0;
			synchronized (listData) {
				while (listData.size() > MAX_LENGTH - 1) {
					listData.removeFirst();
					removeCount ++;
				}
				addPosition = listData.size();
				listData.add(record.getMessage());
			}
			synchronized (listeners) {
				if (removeCount > 0) {
					for(ListDataListener listener : listeners){
						listener.contentsChanged(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0, removeCount));
					}
				}
				for(ListDataListener listener : listeners){
					listener.contentsChanged(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, addPosition, addPosition + 1));
				}
			}
			listView.setSelectedIndex(addPosition);
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
	
	LogCat() {
		super("TinyServer logging");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(800, 600);
		setLayout(null);
		listView = new JList<String>();
		scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 0, getWidth(), getHeight());
		scrollPane.getViewport().add(listView);
		add(scrollPane);
		listView.setModel(listModel);
		logHandler.setLevel(Level.ALL);
		LogManager.getLogger().addHandler(logHandler);
	}
}
