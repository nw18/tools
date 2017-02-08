package com.newind.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputUtil {
	public static boolean isIp(String ipAddress) {
		String ip = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";
		Pattern pattern = Pattern.compile(ip);
		Matcher matcher = pattern.matcher(ipAddress);
		if(!matcher.matches()){
			return false;
		}
		for(String string : ipAddress.split(".")){
			if (Integer.parseInt(string) > 255) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean isPort(String port){
		String portReg = "\\d+";
		Pattern pattern = Pattern.compile(portReg);
		Matcher matcher = pattern.matcher(port);
		return matcher.matches() && Integer.parseInt(port) < 65536;
	}
	
	public static boolean isUserName(String name){
		String unReg = "\\w+";
		Pattern pattern = Pattern.compile(unReg);
		Matcher matcher = pattern.matcher(name);
		return matcher.matches();
	}
	
	public static List<String> getAllIP(){
		List<String> ipList = new ArrayList<>();
		try {
			Enumeration<NetworkInterface> interfaces=null;
			interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {  
				NetworkInterface ni = interfaces.nextElement(); 
				Enumeration<InetAddress> addresss = ni.getInetAddresses();
				while(addresss.hasMoreElements())
				{
					InetAddress nextElement = addresss.nextElement();
					if (Inet4Address.class.isInstance(nextElement)) {
						String hostAddress = nextElement.getHostAddress();
						ipList.add(hostAddress);
					}
				}
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ipList; 
	}
	
	public static class ParameterException extends RuntimeException{
		private static final long serialVersionUID = 1L;
		public ParameterException(String message) {
			super(message);
		}
	}
}
