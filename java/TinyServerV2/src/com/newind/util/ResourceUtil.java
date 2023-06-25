package com.newind.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


public class ResourceUtil {
	private static boolean isJar;
	static {
		URL url = ResourceUtil.class.getResource("/res/logo.png");
		isJar = url != null && url.toString().startsWith("jar:");
	}

	public static InputStream getResource(String name) throws IOException{
		if (!isJar) {
			return new FileInputStream("./res/" + name);
		}else {
			return ResourceUtil.class.getResourceAsStream("/res/" + name);
		}
	}
}
