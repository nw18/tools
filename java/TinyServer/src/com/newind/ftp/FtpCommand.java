package com.newind.ftp;

import java.util.ArrayList;
import java.util.List;

public class FtpCommand {
	private static final int MAX_CMD_LENGTH = 4;
	private static final char[] TERMINATOR_LIST = new char[] {' ','\r','\n'};
	private FtpCommand(){
		
	}
	private int result = 0;
	private String cmdName;
	private List<String> cmdParaList = new ArrayList<String>();
	
	public static FtpCommand parse(String cmd){
		FtpCommand ftpCmd = new FtpCommand();
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
