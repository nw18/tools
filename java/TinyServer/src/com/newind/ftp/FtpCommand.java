package com.newind.ftp;

import java.security.cert.CRL;
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
	
	FtpCommand(){
		
	}
	
	private int findCmdName(String cmd,int start){
		for(int i = start; i < cmd.length(); i++){
			if(SPCRLF.indexOf(cmd.charAt(i)) >= 0)
				return i;
		}
		return -1;
	}
	
	private int findPath(String cmd,int start){
		for(int i = start; i < cmd.length();i++){
			if (CRLF.indexOf(cmd.charAt(i)) < 0) {
				return i;
			}
		}
		return -1;
	}
	
	private String response;
	private int result = 0;
	private String cmdName;
	private List<String> cmdParaList = new ArrayList<String>();
	
	public void parse(String cmd){
		while(true){
			int end = -1;
			int pos = findCmdName(cmd,0);
			if (!cmd.endsWith(CRLF) || pos < 0 || pos > 4) {
				result = -1;
				response = FtpResponse.ERR_COMMAND_FORMAT;
				break;
			}
			cmdName = cmd.substring(0, pos);
			switch (cmdName.toUpperCase()) {
			case "USER"://用户名
				end = findPath(cmd, pos + 1);
				if (end <= pos + 1) { // not null able
					result = 3;
					response = FtpResponse.ERR_COMMAND_NOT_IMPLEMENT_PARAMETER;
					break;
				}
				cmdParaList.add(cmd.substring(pos + 1, end));
				break;
			case "PASS": //密码
				end = findPath(cmd, pos + 1);
				if (end < pos + 1) { // null able
					result = 3;
					response = FtpResponse.ERR_COMMAND_NOT_IMPLEMENT_PARAMETER;
					break;
				}
				cmdParaList.add(cmd.substring(pos + 1, end));
				break;
			case "CWD": //改变工作目录
				end = findPath(cmd, pos + 1);
				if(end <= pos + 1){
					result = 3;
					response = FtpResponse.ERR_COMMAND_NOT_IMPLEMENT_PARAMETER;
					break;					
				}
				cmdParaList.add(cmd.substring(pos + 1, end));
				break;
			case "CDUP":
				cmdName = "CWD";
				cmdParaList.add("..");
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
			case "LIST": //目录列表
				break;
			case "NLST": //目录名称列表.
				break;
			case "SYST": //返回操作系统信息
				break;
			case "STAT": //返回控制连接状态或者文件信息.
				break;
			case "HELP": //这条命令我们在平常系统中得到的帮助没有什么区别，响应类型是211或214。建议在使用USER命令前使用此命令。
				break;
			case "NOOP":
				break;
			default:
				result = -2;
				response = FtpResponse.ERR_COMMAND_NOT_IMPLEMENT;
				break;
			}
			result = 0;
			break;
		}
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
	
	public void setResult(int result) {
		this.result = result;
	}
	
	public String getResponse() {
		return response;
	}
	
	public void setResponse(String response) {
		this.response = response;
	}
}
