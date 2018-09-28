package com.newind.ftp;

public class FtpResponse {
//	 110
//  重新启动标记应答。在这种情况下文本是确定的，它必须是：MARK yyyy=mmmm，其中yyy
//y是用户进程数据流标记，mmmm是服务器标记。
//
//
//125
//  数据连接已打开，准备传送
//  
//150
//  文件状态良好，打开数据连接
	public static final String RD_FILE_AND_CONNECTION = "150 File status okay;\r\n";
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
	public static final String OK_FILE_SIZE = "213 %d\r\n";
//  
//214
//  帮助信息，信息仅对人类用户有用
//  
//215
//  名字系统类型
	public static final String OK_SYSTEM_INFO = "215 UNIX Type: TS\r\n";
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
	public static final String OK_PASV_MODE = "227 Entering Passive Mode (%s,%d,%d).\r\n";
//230
//  用户登录成功
	public static final String OK_USER_LOGON = "230 User logged in, proceed.\r\n";
//  
//250
//  请求的文件操作完成
	public static final String OK_FILE_OPERATION = "250 Requested file action okay, completed.\r\n";
	public static final String OK_FILE_OPERATION_EX = "250 %s %s.\r\n";
//  
//257
//  创建"PATHNAME"
	public static final String OK_FILE_CREATED = "257 \"%s\" created\r\n";
//  
//331
//  用户名正确，需要口令
	public static final String PEND_PASS_WORD = "331 User name okay, need password.\r\n";
//  
//332
//  登录时需要帐户信息
//  
//350
//  请求的文件操作需要进一步命令
	public static final String PEND_NEXT_FILE_ACTION = "350 Requested file action pending further information.\r\n";
//  
//421
//  不能提供服务，关闭控制连接
	public static final String WAR_NOT_IN_SERVICE = "421 Service not available, closing control connection.\r\n";
//  
//425
//  不能打开数据连接
//  
	public static final String FAIL_OPEN_DATA_CONNECTION = "425 Can't open data connection.\r\n";
//426
//  关闭连接，中止传输
	public static final String FAIL_ABOR_DATA_TRANSPORT = "426 Connection closed; transfer aborted.\r\n";
//  
//450
//  请求的文件操作未执行
	public static final String FAIL_NOT_TAKEN_ACTION = "450 equested file action not taken.\r\n";
//  
//451
//  中止请求的操作：有本地错误
	public static final String FAIL_ON_IO_EXCEPTION = "451 Requested action aborted: local error in processing.\r\n";
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
