package com.newind.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class ResourceUtil {
	private static boolean isJar;
	static {
		isJar = ResourceUtil.class.getResource(ResourceUtil.class.getSimpleName() + ".class").toString().startsWith("jar:");
		System.out.println(ResourceUtil.class.getResource(ResourceUtil.class.getSimpleName() + ".class"));
	}

	public static InputStream getResource(String name) throws IOException{
		if (!isJar) {
			return new FileInputStream("./res/" + name);
		}else {
			return ResourceUtil.class.getResourceAsStream("/res/" + name);
		}
	}
}
