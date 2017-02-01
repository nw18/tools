package com.newind;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ApplicationConfig {
	private static ApplicationConfig _config_ = null;
	private String root = ".";
	private int httpPort = 8080;
	private int ftpPort = 2121;
	private String ip = "0.0.0.0";
	private int threadCount = 64;
	private int recvBufferSize = 8 * 1024 + 1;
	private boolean isShuttingDown = false;
	private int recvTimeout = 5 * 1000;
	private int connectionTimeout = 5 * 60 * 1000;
	private String userName = "admin";
	private String passWork = "123456";
	
	public static ApplicationConfig instance() {
		if(null == _config_){
			_config_ = new ApplicationConfig();
		}
		return _config_;
	}
	
	public void load(String argv[]) throws Exception{
		for(String arg : argv){
			int pos = arg.indexOf(':');
			if (pos <= 0) {
				throw new Exception("bad parameter :\n" + arg);
			}
			String value = arg.substring(pos + 1);
			switch (arg.substring(0, pos)) {
			case "root":
				while (value.endsWith("/") || value.endsWith("\\")) {
					value = value.substring(0, value.length() - 2);
				}
				File file = new File(value);
				if (!(file.exists() && file.isDirectory())) {
					throw new Exception(value + " not exists or unaccessable.");
				}
				root = value;
				break;
			case "http_port":
				httpPort = Integer.parseInt(value);
				break;
			case "ftp_port":
				ftpPort = Integer.parseInt(value);
				break;
			case "thread":
				threadCount = Integer.parseInt(value);
				break;
			default:
				break;
			}
		}
		//remove the end /. or \. any better way?
		if(root.startsWith(".")){
			root = new File(root).getAbsolutePath();
		}
		if (root.endsWith("/.") || root.endsWith("\\.")) {
			root = root.substring(0, root.length() - 2);
		}
	}
	
	public String getRoot() {
		return root;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public String getIp() {
		return ip;
	}
	
	public int getHttpPort() {
		return httpPort;
	}
	
	public int getFtpPort() {
		return ftpPort;
	}
	
	public int getThreadCount() {
		return threadCount;
	}
	
	public int getRecvBufferSize() {
		return recvBufferSize;
	}
	
	public boolean isShuttingDown() {
		return isShuttingDown;
	}
	
	public void setShuttingDown(boolean isShuttingDown) {
		this.isShuttingDown = isShuttingDown;
	}
	
	public int getRecvTimeout() {
		return recvTimeout;
	}
	
	public int getConnectionTimeout() {
		return connectionTimeout;
	}
	
	public void setTinyLogo(byte[] tinyLogo) {
		this.tinyLogo = tinyLogo;
	}
	
	public byte[] getTinyLogo() {
		return tinyLogo;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getPassWork() {
		return passWork;
	}
	
	public void setPassWork(String passWork) {
		this.passWork = passWork;
	}
	
	//the LOGO data.
	private byte tinyLogo[] = new byte[] { 80, 75, 3, 4, 20, 0, 0, 0, 8, 0, 35, -73, 44, 74, 2, -115, 28, 23, 63, 10, 0,
			0, 62, 66, 0, 0, 8, 0, 0, 0, 108, 111, 103, 111, 46, 105, 99, 111, -19, 91, 15, 104, -108, 87, 18, 31, 115,
			-94, 18, 66, -108, 32, 65, -126, 68, 41, -27, 8, 34, 34, 34, 61, 41, -30, -122, 80, 74, 17, 41, 69, 68, 68,
			14, -111, -42, -37, -17, -37, 36, 28, -98, 39, 18, 36, -44, -120, -108, -69, 34, 37, 39, 18, -118, 72, 17,
			41, -91, -56, 113, -36, -119, -7, -66, 111, 101, -111, -26, 16, 43, -30, -91, 82, -118, -124, 82, 68, 67,
			40, 65, -92, 36, -31, 8, 38, 36, 117, -17, -9, 123, 111, -66, -8, -71, -18, -73, -39, -60, -35, 120, 27,
			-99, 48, -52, 126, -1, -34, 123, 51, 111, -34, -68, -103, 121, 19, -111, 37, -8, 107, 110, 38, 93, 47, 77,
			45, 34, -11, 34, -46, 4, -60, 45, -23, 18, 123, -33, 64, -77, -68, -122, -41, -80, -88, 33, -109, -108, 42,
			-33, -107, 106, -125, 41, -87, -110, 87, -113, -1, 101, -32, -3, 32, -80, 61, 112, 100, -125, -68, -126,
			-32, 59, 114, 59, 112, 37, 11, -2, -49, -54, 43, 8, -104, -5, 51, -28, 31, 114, -8, 17, -6, -80, 84, -54, 4,
			-23, -92, -44, -94, -81, 53, 105, 71, 86, 72, 9, 1, -13, 86, -25, -71, -46, -32, -75, -54, 42, 94, -9, -74,
			-55, 50, 47, 37, 107, 76, 95, -48, 111, 121, -70, -50, 87, -13, 61, -84, -13, -102, -24, -9, -72, -65, -97,
			-4, 27, 25, -72, -14, -122, -108, 1, 60, -116, 9, -29, 108, 70, -5, -97, -125, -98, 2, 61, -122, -79, 124,
			-56, -66, -127, 123, -127, -37, 113, 127, -83, -52, 3, 48, 111, 127, -60, -9, -25, -72, -114, -59, -14, -77,
			1, 120, -38, -96, -82, 105, -16, -74, 10, -41, 29, -6, -34, -69, -49, 124, -17, -54, -98, -112, 127, -116,
			41, 41, -59, -64, 28, 33, -96, -36, -47, 47, 112, 88, -27, -4, 43, -16, 17, 112, 16, 120, 31, 120, 3, -4,
			127, 9, -70, 27, -13, 83, 59, 71, -2, -81, -22, -8, 47, -118, -19, -21, 29, -32, 48, -5, -14, 29, -53, 43,
			126, -81, 5, -34, -42, -9, -114, -26, 124, 127, 54, 50, -1, -105, -68, -74, -46, -22, -89, 88, 29, -83, 69,
			63, -65, 71, -5, -97, -126, 78, -79, -81, 124, -120, -25, 3, -66, 99, -34, -87, 91, 8, -2, 123, 91, -91, 6,
			-17, 124, 63, 51, 6, 71, -18, 0, -33, -108, 50, 0, -6, -23, 0, -18, 7, 14, -60, -15, -81, 118, 104, 4, 120,
			106, 33, -8, 7, -81, 91, 3, -10, -25, -54, 47, 120, -9, -65, -64, 71, -64, -35, 82, 6, 64, 63, 59, -47, -10,
			57, 51, -65, -82, 60, -98, 69, 6, 67, -66, 83, -100, 7, -2, 34, -4, -21, -100, 76, 2, -81, 1, -65, 49, -21,
			-46, -111, -109, -80, -47, -53, -92, -60, -112, -26, 26, 72, 74, 63, -38, 63, 80, -124, 14, -48, 62, -100,
			47, 102, 45, -50, -105, -1, -76, 99, 116, -1, -17, -90, 47, 71, 62, -61, -4, 36, -75, 111, 15, 88, 47, 101,
			0, -107, 113, 39, -6, -6, -72, 16, -1, 58, -114, 59, -98, 35, 27, -53, -59, 63, -58, -80, 5, 120, 23, -17,
			-116, -103, 57, 73, -55, 122, -19, -105, 107, -95, 73, -54, 0, -24, -17, 19, -6, 91, -64, -73, -127, 63,
			-49, -62, -1, 120, 49, 107, -79, 0, -1, 99, -64, 15, 36, -122, 127, 124, -73, -57, -12, -31, 98, -83, -71,
			-78, 13, -2, 67, -107, -39, -109, -96, 15, 24, -25, 65, 41, 3, -96, -35, -9, 84, -73, -71, 23, 28, -101,
			-123, 127, -18, 7, 61, 126, -86, -16, 26, 40, -64, 63, -37, -72, 12, -38, 2, 122, -36, -73, 118, -18, 49,
			48, -55, 54, 77, -37, -114, -79, -7, 119, -81, 38, -91, 90, -37, -70, 108, -38, 114, -28, -126, -108, 1,
			-48, -2, 38, -6, -103, -24, -5, 26, 112, 19, -80, -33, 47, 36, 3, -41, -68, 91, 55, 79, -2, 41, -25, 73,
			-54, 123, 70, -98, -20, 59, 41, 59, -72, -65, 114, 125, -23, -3, 25, -65, 31, 124, 31, 82, -71, -35, -65,
			-102, 124, -42, 79, 44, 5, -48, 39, -11, 93, 51, 39, -100, -117, -83, -76, -75, -123, -4, 1, 51, -66, -108,
			108, -101, -91, -51, 47, -15, -34, 32, -80, 91, 44, -1, -37, -47, 102, -65, 65, -41, -84, -73, 126, 62, -89,
			-51, 5, -17, 7, 76, -52, -105, -110, 38, -3, 102, 48, 92, 35, -38, -42, 91, -64, -69, 6, 83, -78, 85, 74,
			12, -116, 47, 2, -57, -8, -91, 83, -64, -49, 97, -125, 27, 113, -3, -45, 44, -21, -96, -121, -2, -69, 44,
			18, -48, -3, 111, 4, 124, -33, 2, -2, 22, 120, -84, -112, 14, -24, -4, -59, -82, 1, 60, -33, -56, -8, 1,
			-72, 89, 42, 0, -64, -17, 102, -32, -96, -82, -127, 125, -36, 107, 64, 127, 40, 96, 3, -122, 115, 99, -106,
			124, -21, 31, -17, 124, 47, 21, 0, -16, 105, -86, 125, -50, -67, 29, -13, 69, -38, 55, -32, 113, 92, -113,
			-57, -52, 63, 109, -40, 39, 126, 42, -65, 79, -122, 103, -105, -16, -3, 67, -6, 112, 82, 33, -128, -79, -98,
			-113, -52, 45, -41, -64, -58, -48, 30, -57, -24, -64, 85, -18, -31, 49, 109, -19, -58, -77, -61, -116, -81,
			-92, -126, 108, 64, -124, -73, 100, -26, 15, -78, 20, -74, -71, 35, -42, 14, 58, -120, -101, 29, -39, 46,
			-117, 4, 104, -9, -88, -41, -54, -1, -107, 94, -8, -7, -40, 11, 26, -88, 15, 5, 100, 112, 52, -90, -71,
			-118, 3, -11, 63, 6, -108, -1, -63, -48, 118, 23, -54, 15, -32, -2, 77, 89, 36, -128, -71, -84, 6, 94, -116,
			-8, -7, -121, 77, -114, 14, 49, -119, -17, -28, -33, 11, -24, -69, -90, -53, -108, -101, 88, 104, 72, -125,
			87, -16, 127, 68, -7, -49, -86, 13, 95, -115, 123, 43, 24, -117, -58, -22, 64, 82, -70, -53, -103, -93, 93,
			40, -56, 36, -51, 26, -40, -59, 88, 83, 101, 112, -49, 119, 102, -42, 0, 125, -48, -95, -104, 53, 112, -113,
			-2, -69, 44, 2, -64, 92, 111, 10, -98, -58, 32, 83, -8, 125, -128, -9, -71, -49, -29, -70, 59, -122, -1, 41,
			-109, -61, -99, 99, -114, -12, -1, 17, -46, -114, -47, -9, 32, 98, -33, -1, 25, 62, 11, -110, -78, -63, 119,
			-14, -25, -56, 124, -41, -60, -20, -82, 84, 56, -92, -19, 26, 56, -61, 56, 85, -25, 118, 36, -93, 113, 56,
			-8, -81, -127, 60, 46, -15, 126, -116, 12, 30, -31, -7, 46, -87, 112, 0, 15, 31, 2, -57, 35, 124, -67, 23,
			62, -125, 60, 90, -14, -27, -120, 52, 127, 50, -18, -37, -104, 54, 9, 90, -94, 124, -3, 75, -117, -123, 70,
			34, -4, -97, 65, 124, 80, -91, -49, 106, -127, 95, 7, -86, 31, 17, -2, 7, 60, -41, -8, -68, -12, 33, 79, 69,
			101, 86, 105, -64, 60, 4, 115, -17, 17, -2, 111, -32, -34, -22, -16, 57, -8, 127, 39, -36, 35, 34, -4, 63,
			-10, 28, -39, 35, -117, 0, 114, -7, 7, -65, -9, -94, 126, -66, -98, -49, 103, -14, -84, -127, 107, -8, -74,
			90, 42, 28, 76, 14, -52, -115, -24, -65, -11, 5, -37, -79, 55, 84, 69, -34, 97, -82, 116, 60, -121, 127,
			-26, -84, 91, -28, 37, -6, 47, 24, 71, 13, -58, -11, 38, 99, 87, -32, 122, -4, -82, 55, 88, -28, -68, -16,
			-116, -123, 126, 111, -18, 62, -121, -21, -117, -66, -13, 116, 127, -9, 108, -50, -16, -38, 115, 54, 16,
			-74, 1, -72, 54, -109, -108, 5, 7, -61, -65, 35, 111, 0, -9, 5, 54, -73, 124, 22, 99, -20, 86, 60, -50, -3,
			-39, 32, -49, 60, 29, 121, -33, 119, -13, 32, 108, -65, -26, 41, 115, 117, -101, -9, 26, -61, -66, 96, 15,
			-105, 50, 70, -58, -77, -79, -100, 61, -112, 58, 112, 58, 40, 67, -98, -74, 24, 96, -18, 18, -3, -73, 42,
			50, -113, -101, -15, 35, -74, 92, 121, 25, 49, 62, -85, -101, 23, 31, -122, -17, -27, 124, -13, -36, -7, 11,
			101, 29, -26, -116, -14, -76, 127, -108, -11, 14, -78, -16, -2, 11, 99, -107, -67, -52, -27, 64, 71, 119,
			121, -114, -55, -85, -73, -13, -36, -114, 60, 115, 124, -13, 69, -76, -15, 118, -76, 47, 19, 27, 38, 109,
			-68, -108, 71, 6, 60, -77, -23, -31, 57, -121, 44, 48, -24, 25, 98, -113, 57, -57, -64, 58, -64, 117, -75,
			-105, -110, 6, -4, -2, -64, -60, -14, -52, 91, -27, -15, 97, -118, -64, -99, 121, -6, 90, 19, -105, 35, -93,
			13, 65, 95, -73, 88, -29, -63, 61, -77, -44, 53, 54, -77, -7, 48, -24, -9, 38, -25, -127, 53, 84, -84, 35,
			-93, -3, -10, 82, 38, -81, -71, 89, -41, -1, 73, -77, 103, -71, -49, -82, -113, 2, -7, -50, -68, 103, 30,
			106, 51, -58, 10, -24, -51, 47, -127, -75, 31, 95, 105, -114, -83, -20, -10, -47, -60, 107, -82, -103, 111,
			-98, -89, 112, -1, -6, 55, -16, 96, 70, 107, -113, -68, 54, -107, -123, -51, -45, -17, 100, -36, -17, -37,
			90, -105, -68, 49, 110, -32, -104, 90, -104, -122, 124, 125, 121, 118, -113, -7, 42, 46, 63, -112, -77, 63,
			-36, 15, 92, 51, -106, 79, 89, -25, -64, -70, 40, 41, 19, 112, -49, 99, -67, 81, -92, 111, -42, 47, -100,
			-93, 46, 68, -27, 79, 89, 104, 45, 12, -49, 99, 121, -2, -36, -94, 123, 69, -69, -42, 32, -18, 85, -3, -55,
			-101, -29, -16, -38, 102, 114, 68, 5, -21, 7, 114, 100, 49, 102, -50, -71, 93, -71, -126, -33, -5, -48, -10,
			42, 41, 3, 4, -52, -33, -80, -114, -23, 105, 78, -121, -70, -16, 3, -20, 86, 107, -88, 11, 5, -21, 79, -127,
			-59, -44, 93, 80, 54, 106, 95, 70, -118, -107, 1, 81, -49, -36, 88, 19, 112, -121, 58, -111, 46, -61, 25,
			63, -41, 109, 52, 127, -89, -70, 64, -7, 103, 104, -105, -92, -124, -7, 67, -83, 93, -103, -109, 12, 34, 99,
			-30, -36, 48, 126, -20, 97, 30, 66, 74, 4, 104, -81, 74, -49, -30, 114, -41, 54, 107, 10, -58, -51, -34,
			-104, -110, -90, 82, -28, 14, -128, -43, -102, 67, 124, 60, 87, 25, 4, 86, 14, -95, -83, -19, 79, 59, -7,
			-19, -19, 124, -63, -44, 52, 114, 79, 114, -14, -10, -53, -5, 23, -12, -4, -101, 60, -68, -48, -39, 110, 96,
			125, -52, -97, -61, 115, -2, 121, 34, -19, 67, -55, -50, -69, 3, -69, 70, 93, -29, -53, -59, -55, -33, -18,
			21, 94, -32, -104, -70, -56, 122, -48, 103, -4, -40, 98, 33, 99, 115, -55, -17, -94, -83, -21, 113, 50, 47,
			74, 31, 32, 3, -49, 41, 93, 94, -79, -73, 85, 86, 96, -65, -6, 120, -74, 53, -86, 57, -51, -2, -64, -6, -48,
			27, 124, 91, 19, -80, -86, -73, 109, 110, 121, 110, -12, 87, 103, 124, 49, 71, 6, -26, 99, 23, 116, 44, -35,
			65, 9, 125, 39, -38, 125, -83, -9, -69, 79, 62, -117, -24, -97, 58, -15, 13, -58, -48, 73, 95, -127, 123,
			36, 99, 76, 96, 67, 80, -60, -36, -24, 126, -62, -77, -44, -109, -127, -11, 73, -25, -74, 46, -24, 51, -64,
			78, -77, 22, 74, 74, 4, 90, 107, -76, -121, -75, 69, -71, -71, -100, -39, 80, 107, -77, 120, -10, 123, -98,
			-25, 127, 90, 67, -68, -59, -60, 67, 41, -87, -103, 37, 62, 111, 52, -7, 65, 71, 2, -32, 79, -76, 117, 69,
			-56, -97, 123, -28, 57, -49, 45, 125, 126, 29, -10, -70, -34, -52, 75, 56, -98, 121, -40, 109, 83, -93, -54,
			58, 54, -6, -73, -82, 28, -91, 111, -115, -33, 111, 49, -1, -64, 61, 49, -74, 62, -34, -42, -70, -79, -10,
			-4, 22, -25, -96, -112, 78, -104, -72, -76, 76, -71, 21, -22, 39, -28, -80, -106, 126, -104, -42, -88, 127,
			77, -65, 12, -3, -35, 68, -33, -73, 125, -41, -24, -21, 67, -50, 67, -111, -14, -96, -65, 127, 29, -8, 69,
			96, -3, 1, -18, 59, -36, 127, 27, -3, -44, -13, 126, 94, 96, -21, 14, -113, -87, 46, 14, -59, -55, -127,
			-71, -107, -76, 83, -2, -68, 2, 115, -99, 90, -1, -80, 35, 112, -115, 47, 124, 72, -13, 9, -52, -21, 126,
			-26, -69, 102, -97, -68, 20, -48, 38, -72, 114, -99, 49, -122, -30, 100, -52, -72, -23, -33, -35, -44, -17,
			120, -114, -40, -54, -102, 67, -6, -44, 92, -121, 57, -25, -17, -20, -117, -79, -6, -35, 92, 121, -85, -114,
			-68, 47, 47, 17, -68, -42, -103, -1, -29, 96, -98, 105, -101, -6, 85, -5, 21, -113, -104, -68, -109, 107,
			-50, 79, -66, 96, 77, 93, 96, -13, 47, 3, -102, 91, -103, 84, 62, -58, -76, -90, -3, -78, -55, 77, -39, -13,
			-122, -83, -108, 123, 36, -90, 102, 46, -22, 47, -127, -115, -37, 38, 35, 62, -30, 63, 2, 103, -31, -13, 42,
			-59, -126, -38, -6, 58, 47, 101, 98, -87, -115, -127, -11, -89, 118, 106, 110, -19, -112, -119, 51, 93, 57,
			29, -40, 57, -66, -94, 126, -1, 13, -109, -89, -78, 49, 36, 117, 108, 95, 96, -29, -46, 53, -127, -11, -35,
			-37, 77, 108, 106, 115, 22, -116, -25, -9, -89, -99, -54, -84, -67, 99, 44, -91, 103, -22, -115, -127, -85,
			53, 114, -74, -2, -106, -74, -89, -43, -40, 1, -41, -40, -116, 35, -98, -35, 111, -37, -51, 51, -85, 15,
			-69, -23, 11, -88, 63, 81, 35, -117, 12, -80, -74, 86, -8, 54, 63, -45, 104, -22, -17, 28, 115, -26, -66,
			69, 107, -15, -104, -49, -39, 20, 36, 65, -109, 70, -89, -106, -54, 43, 2, -12, 27, 52, 22, -81, 101, 126,
			115, 49, -43, -99, -66, -122, -41, -16, -94, -112, 45, 10, 38, 114, -24, 52, 48, -111, -3, -11, 78, 39, -88,
			100, 39, -66, -21, 56, 1, -102, -80, -76, 107, -99, 94, -109, 38, -78, -39, -26, -107, 19, -33, -3, 9, -12,
			32, -24, -97, 65, -41, 43, -3, -41, -14, -119, 111, 63, 90, -105, -51, -2, 109, -7, 68, -33, 71, 23, 44, 29,
			6, -3, -21, 111, 108, 31, -1, -103, -95, -74, -41, 111, -105, -124, -44, -34, 31, 86, 58, 20, 82, -27, 101,
			-88, -53, -46, -79, -82, 19, -106, -10, -123, 52, 97, -24, -24, -125, -112, -82, -77, 60, -115, -82, -76,
			-12, -16, -17, 44, -19, 72, 40, -43, -25, -99, 74, -73, -13, -67, -23, 19, -93, 59, -106, -109, -118, 24,
			58, -43, 37, 9, 14, 96, -14, -127, 36, 56, -128, -23, 78, 73, 116, -31, -125, 39, -119, -66, 68, 23, 56,
			120, -126, -113, -6, -8, 0, 47, 63, 16, 60, 56, -119, -98, 5, 23, 24, -43, -124, -32, 5, -116, 98, 90, -48,
			-62, -24, -14, -20, 19, 78, -57, 4, 80, -124, 15, 19, -8, -16, 4, 110, 46, 49, -65, 113, 115, -35, 19, -46,
			7, -72, -47, -57, 33, -11, -55, -54, 81, 51, -76, 81, -103, -74, 44, -116, -82, -20, -74, -68, 103, -121,
			-77, 69, -62, -1, 0, 80, 75, 1, 2, 31, 0, 20, 0, 0, 0, 8, 0, 35, -73, 44, 74, 2, -115, 28, 23, 63, 10, 0, 0,
			62, 66, 0, 0, 8, 0, 36, 0, 0, 0, 0, 0, 0, 0, 32, 0, 0, 0, 0, 0, 0, 0, 108, 111, 103, 111, 46, 105, 99, 111,
			10, 0, 32, 0, 0, 0, 0, 0, 1, 0, 24, 0, -30, -34, 68, 36, -28, 108, -46, 1, -46, -65, 102, 44, -28, 108, -46,
			1, -126, 45, -4, 35, -28, 108, -46, 1, 80, 75, 5, 6, 0, 0, 0, 0, 1, 0, 1, 0, 90, 0, 0, 0, 101, 10, 0, 0, 0,
			0 };

	ApplicationConfig(){
		ZipInputStream inputStream = new ZipInputStream(new ByteArrayInputStream(tinyLogo));
		try {
			//UNZIP ICON,the origin ICON too large to android..
			ZipEntry entry = inputStream.getNextEntry();
			byte[] buffer = new byte[(int)entry.getSize()];
			int len = 0;
			int read = 0;
			while(entry.getSize() > read && (len = inputStream.read(buffer,read,(int)(entry.getSize() - read))) > 0)
					read += len;
			tinyLogo = buffer;
		} catch (IOException e) {
			tinyLogo = null;
			e.printStackTrace();
		}

	}
}
