package com.newind.ftp;

import java.util.ArrayList;
import java.util.List;

public class FtpCommand {
	private static final String CRLF = "\r\n";
	private static final String SPCRLF = " \t\b\r\n";
	public static final int OK = 0;
	public static final int ERR = -1;
	public static final int ERR_FORMAT = -2;
	public static final int ERR_PARAM = -3;
	
	FtpCommand(){
		
	}
	
	private int findCmdName(String cmd,int start){
		for(int i = start; i < cmd.length(); i++){
			if(SPCRLF.indexOf(cmd.charAt(i)) >= 0){
				return i;
			}
		}
		return -1;
	}
	
	private int findPath(String cmd,int start){
		for(int i = start; i < cmd.length();i++){
			if (CRLF.indexOf(cmd.charAt(i)) >= 0) {
				return i;
			}
		}
		return -1;
	}
	
	private int findParam(String cmd,int start){
		for(int i = start; i < cmd.length(); i++){
			if(SPCRLF.indexOf(cmd.charAt(i)) >= 0){
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
					break;
				}
				cmdParaList.add(cmd.substring(start,end));
				break;
//			case "STOU":
//				break;
			case "STOR": //上传文件
				end = findPath(cmd, start);
				if (end <= start) {
					result = ERR_PARAM;
					response = FtpResponse.ERR_COMMAND_PARAMETERS;
					break;
				}
				cmdParaList.add(cmd.substring(start,end));
				break;
			case "APPE": //附加上传文件.
				end = findPath(cmd, start);
				if (end <= start) {
					result = ERR_PARAM;
					response = FtpResponse.ERR_COMMAND_PARAMETERS;
					break;
				}
				cmdParaList.add(cmd.substring(start,end));
				break;
			case "RNFR"://重命名 from
				end = findPath(cmd, start);
				if (end <= start) {
					result = ERR_PARAM;
					response = FtpResponse.ERR_COMMAND_PARAMETERS;
					break;
				}
				cmdParaList.add(cmd.substring(start,end));
				break;
			case "RNTO": //重命名 to
				end = findPath(cmd, start);
				if (end <= start) {
					result = ERR_PARAM;
					response = FtpResponse.ERR_COMMAND_PARAMETERS;
					break;
				}
				cmdParaList.add(cmd.substring(start,end));
				break;
			case "ABOR": //放弃上次的操作.
				break;
			case "DELE": //删除文件.
				end = findPath(cmd, start);
				if (end <= start) {
					result = ERR_PARAM;
					response = FtpResponse.ERR_COMMAND_PARAMETERS;
					break;
				}
				cmdParaList.add(cmd.substring(start,end));
				break;
			case "RMD": //删除目录
				end = findPath(cmd, start);
				if (end <= start) {
					result = ERR_PARAM;
					response = FtpResponse.ERR_COMMAND_PARAMETERS;
					break;
				}
				cmdParaList.add(cmd.substring(start,end));
				break;
			case "MKD": //创建目录
				end = findPath(cmd, start);
				if (end <= start) {
					result = ERR_PARAM;
					response = FtpResponse.ERR_COMMAND_PARAMETERS;
					break;
				}
				cmdParaList.add(cmd.substring(start,end));
				break;
			case "PWD": //打印当前路径
			case "XPWD":
				break;
			case "TYPE": //结构设置
				end = findParam(cmd, start);
				if (end <= start) {
					result = ERR_PARAM;
					response = FtpResponse.ERR_COMMAND_PARAMETERS;
					break;
				}
				cmdParaList.add(cmd.substring(start, end));
				break;
			case "STRU":
				end = findParam(cmd, start);
				if (end <= start) {
					result = ERR_PARAM;
					response = FtpResponse.ERR_COMMAND_PARAMETERS;
					break;
				}
				cmdParaList.add(cmd.substring(start, end));
				break;
			case "MODE":
				end = findParam(cmd, start);
				if (end <= start) {
					result = ERR_PARAM;
					response = FtpResponse.ERR_COMMAND_PARAMETERS;
					break;
				}
				cmdParaList.add(cmd.substring(start, end));
				break;
			case "PASV": //数据链接模式
				break;
			case "PORT":
				end = findPath(cmd, start);
				if (end <= start) {
					result = ERR_PARAM;
					response = FtpResponse.ERR_COMMAND_PARAMETERS;
					break;
				}
				String paras = cmd.substring(start, end);
				String paraArray[]  = null;
				try{
					paraArray = paras.split(",");
				}catch (Exception e) {
					e.printStackTrace();
				}
				if (paraArray == null || paraArray.length != 6) {
					result = ERR_PARAM;
					response = FtpResponse.ERR_COMMAND_FORMAT;
					break;
				}
				for(String para : paraArray){
					cmdParaList.add(para);
				}
				break;
			case "LIST": //目录列表
				end = findPath(cmd, start);
				if (end <= start) {
					cmdParaList.add(".");
				}else {
					cmdParaList.add(cmd.substring(start, end));
				}
				break;
			case "NLST": //目录名称列表.
				end = findPath(cmd, start);
				if (end <= start) {
					cmdParaList.add(".");
				}else {
					cmdParaList.add(cmd.substring(start, end));
				}
				break;
			case "SYST": //返回操作系统信息
				break;
			case "STAT": //返回控制连接状态或者文件信息.
				end = findPath(cmd, start);
				if (end > start) {
					cmdParaList.add(cmd.substring(start, end));
				}
				break;
			case "HELP": //这条命令我们在平常系统中得到的帮助没有什么区别，响应类型是211或214。建议在使用USER命令前使用此命令。
				end = findPath(cmd, start);
				if (end > start) {
					cmdParaList.add(cmd.substring(start, end));
				}
				break;
			case "NOOP":
			case "QUIT":
				break;
			case "SIZE": //get the size of file or 550
				end = findPath(cmd, start);
				if (end <= start) {
					result = ERR_PARAM;
					response = FtpResponse.ERR_COMMAND_PARAMETERS;
					break;
				}
				cmdParaList.add(cmd.substring(start, end));
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
	
	public int getParamCount(){
		return cmdParaList.size();
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
