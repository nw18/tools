package com.newind.util;

public class TextUtil {
	public static boolean equal(String a,String b){
		if (a == null) {
			return b == null;
		}
		return a.equals(b);
	}
}
