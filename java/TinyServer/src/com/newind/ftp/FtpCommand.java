package com.newind.ftp;

import java.util.ArrayList;
import java.util.List;

import com.newind.util.TextUtil;

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
	
	public void parse(String cmd) {
		int end = -1;
		int pos = findCmdName(cmd,0);
		if (!cmd.endsWith(CRLF) || pos < 0 || pos > 4) {
			result = ERR_FORMAT;
			response = FtpResponse.ERR_COMMAND_FORMAT;
			return;
		}
		result = OK; // INIT the result to okay.
		cmdName = cmd.substring(0, pos);
		int start = pos + 1;
		switch (cmdName.toUpperCase()) {
			case "USER"://用户名
			case "CWD": //改变工作目录
			case "RETR": //下载文件
			case "STOR": //上传文件
// 			case "STOU": // 唯一存储 暂不支持
//				break;
			case "APPE": //附加上传文件.
			case "RNFR"://重命名 from
			case "RNTO": //重命名 to
			case "DELE": //删除文件.
			case "RMD": //删除目录
			case "MKD": //创建目录
			case "SIZE": //get the size of file or 550
				pasePath(cmd, start, null);
				break;
			case "TYPE": //结构设置
			case "STRU":
			case "MODE":
				end = findParam(cmd, start);
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
				case "CDUP":
				cmdName = "CWD";
				cmdParaList.add("..");
				break;
			case "ABOR": //放弃上次的操作.
				break;
				case "PWD": //打印当前路径
			case "XPWD":
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
				try{
					String[] paraArray = paras.split(",");
					if (paraArray == null || paraArray.length != 6) {
						result = ERR_PARAM;
						response = FtpResponse.ERR_COMMAND_FORMAT;
						break;
					}
					for(String para : paraArray){
						cmdParaList.add(para);
					}
				}catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case "LIST": //目录列表
			case "NLST": //目录名称列表.
				pasePath(cmd, start, ".");
				break;
				case "SYST": //返回操作系统信息
				break;
			case "STAT": //返回控制连接状态或者文件信息.
			case "HELP": //这条命令我们在平常系统中得到的帮助没有什么区别，响应类型是211或214。建议在使用USER命令前使用此命令。
				end = findPath(cmd, start);
				if (end > start) {
					cmdParaList.add(cmd.substring(start, end));
				}
				break;
				case "NOOP":
			case "QUIT":
				break;
				case "OPTS":
				end = findParam(cmd, start);
				if (end <= start) {
					result = ERR_FORMAT;
					response = FtpResponse.ERR_COMMAND_FORMAT;
					break;
				}
				String codeType = cmd.substring(start, end);
				start = end + 1;
				end = findParam(cmd, start);
				if (end <= start) {
					result = ERR_PARAM;
					response = FtpResponse.ERR_COMMAND_PARAMETERS;
					break;
				}
				String onOff = cmd.substring(start, end);
				if (TextUtil.equal(codeType.toLowerCase(), "utf8") && TextUtil.equal(onOff.toLowerCase(), "on")) {
					result = ERR;
					response = FtpResponse.OK_COMMAND;
				}else {
					result = ERR_PARAM;
					response = FtpResponse.ERR_COMMAND_NOT_IMPLEMENT_PARAMETER;
				}
				break;
			default:
				result = -2;
				response = FtpResponse.ERR_COMMAND_NOT_IMPLEMENT;
				break;
		}
	}

	protected void pasePath(String cmd, int start, String defaultPath) {
		int end = findPath(cmd, start);
		if (end <= start) { // not null able
			if (defaultPath == null) {
				result = ERR_PARAM;
				response = FtpResponse.ERR_COMMAND_PARAMETERS;
				return;
			} else {
				cmdParaList.add(defaultPath);
			}
		}
		cmdParaList.add(cmd.substring(start, end));
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
