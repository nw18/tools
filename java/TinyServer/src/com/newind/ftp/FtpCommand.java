package com.newind.ftp;

import java.util.ArrayList;
import java.util.List;

public class FtpCommand {
	private static final String CRLF = "\r\n";
	private static final String SPCRLF = " \t\r\b\n";
	public static final int OK = 0;
	public static final int ERR = -1;
	public static final int ERR_FORMAT = -2;
	public static final int ERR_PARAM = -3;
	
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
				result = ERR_FORMAT;
				response = FtpResponse.ERR_COMMAND_FORMAT;
				break;
			}
			cmdName = cmd.substring(0, pos);
			int start = pos + 1;
			switch (cmdName.toUpperCase()) {
			case "USER"://用户名
				end = findPath(cmd, start);
				if (end <= start) { // not null able
					result = ERR_PARAM;
					response = FtpResponse.ERR_COMMAND_PARAMETERS;
					break;
				}
				cmdParaList.add(cmd.substring(start, end));
				break;
			case "PASS": //密码
				end = findPath(cmd, start);
				if (end < start) { // null able
					result = ERR_PARAM;
					response = FtpResponse.ERR_COMMAND_PARAMETERS;
					break;
				}
				cmdParaList.add(cmd.substring(start, end));
				break;
			case "CWD": //改变工作目录
				end = findPath(cmd, start);
				if(end <= start){
					result = ERR_PARAM;
					response = FtpResponse.ERR_COMMAND_PARAMETERS;
					break;					
				}
				cmdParaList.add(cmd.substring(start, end));
				break;
			case "CDUP":
				cmdName = "CWD";
				cmdParaList.add("..");
				break;
			case "RETR": //下载文件
				end = findPath(cmd, start);
				if (end <= start) {
					result = ERR_PARAM;
					response = FtpResponse.ERR_COMMAND_PARAMETERS;
				}
				cmdParaList.add(cmd.substring(start,end));
				break;
			case "STOR": //上传文件
			case "STOU":
				end = findPath(cmd, start);
				if (end <= start) {
					result = ERR_PARAM;
					response = FtpResponse.ERR_COMMAND_PARAMETERS;
				}
				cmdParaList.add(cmd.substring(start,end));
				break;
			case "APPE": //附加上传文件.
				end = findPath(cmd, start);
				if (end <= start) {
					result = ERR_PARAM;
					response = FtpResponse.ERR_COMMAND_PARAMETERS;
				}
				cmdParaList.add(cmd.substring(start,end));
				break;
			case "RNFR"://重命名 from
				end = findPath(cmd, start);
				if (end <= start) {
					result = ERR_PARAM;
					response = FtpResponse.ERR_COMMAND_PARAMETERS;
				}
				cmdParaList.add(cmd.substring(start,end));
				break;
			case "RNTO": //重命名 to
				end = findPath(cmd, start);
				if (end <= start) {
					result = ERR_PARAM;
					response = FtpResponse.ERR_COMMAND_PARAMETERS;
				}
				cmdParaList.add(cmd.substring(start,end));
				break;
			case "ABOR": //放弃上次的操作.
				//TODO 
				break;
			case "DELE": //删除文件.
				end = findPath(cmd, start);
				if (end <= start) {
					result = ERR_PARAM;
					response = FtpResponse.ERR_COMMAND_PARAMETERS;
				}
				cmdParaList.add(cmd.substring(start,end));
				break;
			case "RMD": //删除目录
				end = findPath(cmd, start);
				if (end <= start) {
					result = ERR_PARAM;
					response = FtpResponse.ERR_COMMAND_PARAMETERS;
				}
				cmdParaList.add(cmd.substring(start,end));
				break;
			case "MKD": //创建目录
				end = findPath(cmd, start);
				if (end <= start) {
					result = ERR_PARAM;
					response = FtpResponse.ERR_COMMAND_PARAMETERS;
				}
				cmdParaList.add(cmd.substring(start,end));
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
	
	public String getParam(int index){
		return cmdParaList.get(index);
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
