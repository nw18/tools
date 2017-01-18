package com.newind.ftp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FtpCommand {
	private static final String CRLF = "\\r\\n$";
	private static final String SPCRLF = " \t\r\b";
	private static final String SP = " ";
	
	private FtpCommand(){
		
	}
	
	private int findCmdName(String cmd){
		for(int i = 0; i < cmd.length(); i++){
			if(SPCRLF.indexOf(cmd.charAt(i)) >= 0)
				return i;
		}
		return -1;
	}
	
	private String errorResponse;
	private int result = 0;
	private String cmdName;
	private List<String> cmdParaList = new ArrayList<String>();
	
	public static FtpCommand parse(String cmd){
		FtpCommand ftpCmd = new FtpCommand();
		while(true){
			int pos = ftpCmd.findCmdName(cmd);
			if (!cmd.endsWith(CRLF) || pos < 0 || pos > 4) {
				ftpCmd.result = -1;
				ftpCmd.errorResponse = FtpResponse.ERR_COMMAND_FORMAT;
				break;
			}
			ftpCmd.cmdName = cmd.substring(0, pos);
			switch (ftpCmd.cmdName.toUpperCase()) {
			case "USER"://用户名
				
				break;
			case "PASS": //密码
				break;
			case "CWD": //改变工作目录
				break;
			case "RETR": //下载文件
				break;
			case "STOR": //上传文件
			case "STOU":
				break;
			case "APPE": //附加上传文件.
				break;
			case "RNFR"://重命名 from
				break;
			case "RNTO": //重命名 to
				break;
			case "ABOR": //放弃上次的操作.
				break;
			case "DELE": //删除文件.
				break;
			case "RMD": //删除目录
				break;
			case "MKD": //创建目录
				break;
			case "PWD": //打印当前路径
				break;
			case "LIST": //
				break;
			case "NLST":
				break;
			case "":
				break;
			default:
				ftpCmd.result = -2;
				ftpCmd.errorResponse = FtpResponse.ERR_COMMAND_NOT_IMPLEMENT;
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
