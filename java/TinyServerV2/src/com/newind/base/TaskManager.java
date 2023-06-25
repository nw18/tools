package com.newind.base;

import java.util.Timer;

public class TaskManager {
	private static Timer defaultTimer;
	public static Timer getDefaultTimer(){
		if (null == defaultTimer) {
			defaultTimer = new Timer(true);
		}
		return defaultTimer;
	}
}
