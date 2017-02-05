package com.newind.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class ResourceUtil {
	private static boolean isJar = false;
	static {
		isJar = ResourceUtil.class.getResource("").getFile().startsWith("jar:");
	}
	public static InputStream getResource(String name) throws IOException{
		if (!isJar) {
			return new FileInputStream("./res/" + name);
		}else {
			return ResourceUtil.class.getResourceAsStream("/res/" + name);
		}
	}
}
