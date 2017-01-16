package com.newind.ftp;

public class FtpResponse {
//	 110
//  重新启动标记应答。在这种情况下文本是确定的，它必须是：MARK yyyy=mmmm，其中yyy
//y是用户进程数据流标记，mmmm是服务器标记。
//  
//120
//  服务在nnn分钟内准备好
//  
//125
//  数据连接已打开，准备传送
//  
//150
//  文件状态良好，打开数据连接
//  
//200
//  命令成功
	public static final String OK_COMMAND = "200 Command okay.\r\n";
//  
//202
//  命令未实现 //积极回复, 避免客户端关闭连接
	public static final String OK_COMMAND_NOT_IMPLEMENT = "202 Command not implemented, superfluous at this site.\r\n";
//  
//211
//  系统状态或系统帮助响应
//  
//212
//  目录状态
//  
//213
//  文件状态
//  
//214
//  帮助信息，信息仅对人类用户有用
//  
//215
//  名字系统类型
//  
//220
//  对新用户服务准备好 //用户连接后,系统回复给客户端.
	public static final String OK_SERVER_READY = "220 Service ready for new user.\r\n";
//  
//221
//  服务关闭控制连接，可以退出登录
	public static final String OK_QUIT = "221 Service closing control connection.\r\n";
//  
//225
//  数据连接打开，无传输正在进行
	
//  
//226
//  关闭数据连接，请求的文件操作成功
//  
//227
//  进入被动模式
//  
//230
//  用户登录
//  
//250
//  请求的文件操作完成
//  
//257
//  创建"PATHNAME"
//  
//331
//  用户名正确，需要口令
//  
//332
//  登录时需要帐户信息
//  
//350
//  请求的文件操作需要进一步命令
//  
//421
//  不能提供服务，关闭控制连接
//  
//425
//  不能打开数据连接
//  
//426
//  关闭连接，中止传输
//  
//450
//  请求的文件操作未执行
//  
//451
//  中止请求的操作：有本地错误
//  
//452
//  未执行请求的操作：系统存储空间不足
//  
//500
//  格式错误，命令不可识别
//  
	public static final String ERR_COMMAND_FORMAT = "500 Syntax error, command unrecognized.\r\n";
//501
//  参数语法错误
	public static final String ERR_COMMAND_PARAMETERS = "501 Syntax error in parameters or arguments.\r\n";
//  
//502
//  命令未实现
	public static final String ERR_COMMAND_NOT_IMPLEMENT = "502 Command not implemented.\r\n";
//  
//503
//  命令顺序错误
	public static final String ERR_COMMAND_SEQUENCE = "503 Bad sequence of commands.\r\n";
//  
//504
//  此参数下的命令功能未实现
	public static final String ERR_COMMAND_NOT_IMPLEMENT_PARAMETER = "504 Command not implemented for that parameter.\r\n";
//  
//530
//  未登录
	public static final String ERR_NOT_LOGIN = "530 Not logged in.\r\n";
//  
//532
//  存储文件需要帐户信息
//  
//550
//  未执行请求的操作 
	public static final String ERR_NOT_TAKEN_ACTION = "550 Requested action not taken.\r\n";
//  
//551
//  请求操作中止：页类型未知
//  
//552
//  请求的文件操作中止，存储分配溢出
//  
//553
//  未执行请求的操作：文件名不合法

}
