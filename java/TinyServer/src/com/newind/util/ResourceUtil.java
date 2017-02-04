package com.newind.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.newind.ApplicationConfig;

public class ResourceUtil {
	public static InputStream getResource(String name) throws IOException{
		ApplicationConfig config = ApplicationConfig.instance();
		if (config.isDebug()) {
			return new FileInputStream("./res/" + name);
		}else {
			return config.getClass().getResourceAsStream("/res/" + name);
		}
	}
}
