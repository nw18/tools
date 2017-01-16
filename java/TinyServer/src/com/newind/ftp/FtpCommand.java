package com.newind.ftp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FtpCommand {
	private static final String CMD_NAME = "^([a-z]|[A-Z]){3,4}";
	private static final Map<String, String> CMD_REG_MAP = new HashMap<>();
	private static final Map<String, Pattern> CMD_PATTERN_MAP = new HashMap<>();
	private static final String SP = "\\s";
	private static final String PATH_NAME = "\\S+";
	private static final String CRLF = "\\r\\n$";
	
	private static String single(String name) {
		return "^" + name + CRLF;
	}
	
	private static Pattern CMD_NAME_PATTERN = Pattern.compile(CMD_NAME);
	
	static {
		CMD_REG_MAP.put("PWD", single("PWD"));
		//Initialization the pattern map
		for(String key : CMD_REG_MAP.keySet()){
			CMD_PATTERN_MAP.put(key, Pattern.compile(CMD_REG_MAP.get(key),Pattern.CASE_INSENSITIVE));
		}
	}
	
	
	private FtpCommand(){
		
	}
	
	private String errorResponse;
	private int result = 0;
	private String cmdName;
	private List<String> cmdParaList = new ArrayList<String>();
	
	public static FtpCommand parse(String cmd){
		Matcher matcher = CMD_NAME_PATTERN.matcher(cmd);
		FtpCommand ftpCmd = new FtpCommand();
		while(true){
			if (!matcher.find()) {
				ftpCmd.result = -1;
				
				break;
			}
			
			ftpCmd.result = 0;
			break;
		}
		return ftpCmd;
	}
	
	public String getCmdName() {
		return cmdName;
	}
	
	public String[] getCmdParaList() {
		if (null == cmdParaList) {
			return null;
		}
		return cmdParaList.toArray(null);
	}
	
	public int getResult() {
		return result;
	}
}
