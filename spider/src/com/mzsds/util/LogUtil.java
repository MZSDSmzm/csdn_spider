package com.mzsds.util;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


/**
 * 日志工具类
 */
public class LogUtil {

	
	//Log4j日志类句柄。配合：/WEB-INF/lib/log4j-1.2.15.jar。
	private static Logger logObj = null;
	
	
	//Log4j 的配置参数
	static {
		
		try {
			String loglevel = ConfigUtil.getProperty(null, "LogUtil.LogLevel", false);
			
			String logbasedir = ConfigUtil.getProperty(null, "LogUtil.LogFileBaseDir", false);
			if(logbasedir.indexOf("{DefaultLogPath}")>=0){
				String curClassPath = (new IOUtil()).getClass().getResource("/").getFile().toString();
				String curWebRootPath = (new File(curClassPath)).getParentFile().getParentFile().toString();
				String logDir = curWebRootPath+File.separator+"Log";
				logbasedir = logbasedir.trim().replace("{DefaultLogPath}",  logDir);		///Users/hbbc/Project/Tomcat7.0.57/webapps/sdvrbo
			}
			if(logbasedir.indexOf("{SystemName}")>=0){
				String curClassPath = (new IOUtil()).getClass().getResource("/").getFile().toString();
				String curWebRootPath = (new File(curClassPath)).getParentFile().getParentFile().toString();
				String systemname = curWebRootPath.substring(curWebRootPath.lastIndexOf(File.separator)+1);
				logbasedir = logbasedir.trim().replace("{SystemName}", systemname);
			}
			
			Properties pro = new Properties();
	        pro.put("log4j.rootLogger", "DEBUG, Console, FileLog, FileError");
			pro.put("log4j.appender.Console","org.apache.log4j.ConsoleAppender");
			pro.put("log4j.appender.Console.Threshold", loglevel);
			pro.put("log4j.appender.Console.Target","System.out");
			pro.put("log4j.appender.Console.layout", "org.apache.log4j.PatternLayout");
			pro.put("log4j.appender.Console.layout.ConversionPattern", "[%p][%-d{yyyy-MM-dd HH:mm:ss.SSS}] %m%n");
			
			pro.put("log4j.appender.FileLog", "org.apache.log4j.DailyRollingFileAppender");
			pro.put("log4j.appender.FileLog.Threshold", loglevel); 
			pro.put("log4j.appender.FileLog.File", logbasedir+File.separator+"log.log");
			pro.put("log4j.appender.FileLog.DatePattern", "'.'yyyy-MM-dd");
			pro.put("log4j.appender.FileLog.Append", "true");
			pro.put("log4j.appender.FileLog.Encoding", "UTF-8");
			pro.put("log4j.appender.FileLog.layout", "org.apache.log4j.PatternLayout");
			pro.put("log4j.appender.FileLog.layout.ConversionPattern", "[%p][%-d{yyyy-MM-dd HH:mm:ss.SSS}] %m%n");
	
			pro.put("log4j.appender.FileError", "org.apache.log4j.DailyRollingFileAppender");
			pro.put("log4j.appender.FileError.Threshold", "ERROR");
			pro.put("log4j.appender.FileError.File", logbasedir+File.separator+"error.log");
			pro.put("log4j.appender.FileError.DatePattern", "'.'yyyy-MM-dd");
			pro.put("log4j.appender.FileError.Append", "true");
			pro.put("log4j.appender.FileError.Encoding", "UTF-8");
			pro.put("log4j.appender.FileError.layout", "org.apache.log4j.PatternLayout");
			pro.put("log4j.appender.FileError.layout.ConversionPattern", "[%p][%-d{yyyy-MM-dd HH:mm:ss.SSS}] %m%n");
			
	        PropertyConfigurator.configure(pro);
	        logObj = Logger.getLogger(LogUtil.class);
	        
	        System.out.println("[LogUtil] logbasedir = "+logbasedir);
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	
	
	/**
	 * 判断写Debug日志的功能是否开启。返回True，表示开启，False表示没有开启。
	 * 可以在Debug类日志输出之前用本方法做一下判断，以减少不必要的Debug日志的相关运算。
	 */
	public static Boolean isDebugEnabled(){
		return logObj.isDebugEnabled();
	}
	
	
	/**
	 * 日志信息。 
	 * eg: LogUtil.log("hello everyone!");
	 */
	public static void log(String msg){
		logObj.info(msg);
	}
	
	
	/**
	 * 调试信息日志。
	 * eg: LogUtil.debug("date 2013001"); 
	 */
	public static void debug(String msg){
		logObj.debug(msg);
	}
	
	
	/**
	 * 错误日志信息。包括：异常和错误。
	 * eg: LogUtil.exception(e.toString()); 
	 */
	public static void exception(String msg){
		logObj.error(getExceptionSubjectStr(null) + " " + msg);
	}
	
	
	/**
	 * 错误日志信息。包括：异常和错误。
	 * 本方法，会保持打印保存完整的 printStackTrace() 的异常堆栈信息。
	 * eg: LogUtil.exception(e);
	 */
	public static void exception(Exception e){
		exception(null, e, null);
	}
	
	

	/**
	 * 错误异常日志输出方法。用于输出错误描述，异常堆栈 到控制台、日志文件 和 JSP页面。
	 * eg: LogUtil.exception("[HF框架] 接收到新的访问请求，但解析失败！", e, response);
	 * 
	 * @param msg 编程人员自定义输出错误或异常的描述信息
	 * @param e	Exception对象，当为空时，不输出错误堆栈信息
	 * @param response 输出到JSP页面的对象，但本对象非空时，会自动格式化适合浏览器展示的HTML页面信息。
	 */
	public static void exception(String msg, Exception e, HttpServletResponse response ){
		try{
			//异常摘要信息
			String sujInfo = getExceptionSubjectStr(e);	//错误摘要信息（时间，位置，代码行数）
			String usrInfo = msg;					//用户输入错误信息
			String stakInfo = "";					//异常堆栈信息
			try {
				Throwable tObj = e.getCause();
				while(tObj!=null && tObj.getStackTrace()!=null && tObj.getStackTrace().length>0){
					StackTraceElement[] stackElements = tObj.getStackTrace();	
					sujInfo = sujInfo + "\r\n<br><br> <====== ["+stackElements[0].getClassName()+"."+stackElements[0].getMethodName()+"("+stackElements[0].getFileName()+":"+stackElements[0].getLineNumber()+")] "+e.getCause().toString();
					tObj = tObj.getCause();
				}
				sujInfo = sujInfo + "<br><br>\r\n";
			}catch(Exception ee){  logObj.error(getExceptionSubjectStr(ee) + "\r\n" + ee.toString());  }
			//若堆栈信息非空组织堆栈信息
			if(e!=null){
				StringWriter sw = new StringWriter(); 
				PrintWriter pw = new PrintWriter(sw); 
				e.printStackTrace(pw); 
				stakInfo = sw.toString();
			}
			//输出日志到控制台和日志文件
			logObj.error(sujInfo+" "+usrInfo+" "+stakInfo);
			//输出错误信息到JSP页面，若Response对象非空的话
			if(response!=null){
				String htmlmsg ="<html><head><title></title><style><!--H1 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:22px;} H2 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:16px;} H3 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:14px;} "
								+ "BODY {font-family:Tahoma,Arial,sans-serif;color:black;background-color:white;}  B {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;} P {font-family:Tahoma,Arial,sans-serif;background:white;color:black;font-size:12px;}  A {color : black;}A.name {color : black;}HR {color : #525D76;}--></style> </head>"
			    				+"<body><h1>  系统错误报告</h1><HR size='1' noshade='noshade'><p><b>类别</b> 错误和异常信息</p>"
			    				+"<p><b> 概述</b> "+sujInfo+" "+usrInfo+"</p>"
			    				+"<p><b> 详情</b> "
			    				+ stakInfo.replace("\n", "<br>").replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;").replace(" ", "&nbsp;")
			    				+"<pre></pre></p>"
			    				+"<HR size='1' noshade='noshade'><h3> --- com.hbbc.util.LogUtil report --- </h3></body></html>";
				response.setContentType("text/html;charset=UTF-8"); 
			    response.getWriter().println(htmlmsg); 
			}
		} catch (Exception ee) { 
			logObj.error(getExceptionSubjectStr(ee) + "\r\n" + ee.toString());
		} 
	}
	
	
	
	/**
	 * 返回日志发生的异常摘要信息。
	 * 包括：包名，类名，和代码行数。
	 * 如：[com.hbbc.util.Test.main(13)]。
	 */
	private static String getExceptionSubjectStr(Exception e){
		String classInfo = "[]";
        StackTraceElement[] stackElements = null;
        int stackPos = 0;
        if(e==null){
        	//若是没有传过异常过来，则在本方法抛出一个异常，然后回溯错误信息
        	stackElements = (new Throwable()).getStackTrace();	
        	stackPos = 2;
        }else{
        	//直接获取异常的错误信息
        	stackElements = e.getStackTrace();
        	stackPos = 0;
        }
        //组织异常摘要信息
		if (stackElements != null && stackElements.length>stackPos) {
			classInfo = "["+stackElements[stackPos].getClassName()+"."+stackElements[stackPos].getMethodName()+"("+stackElements[stackPos].getFileName()+":"+stackElements[stackPos].getLineNumber()+")]";
        }
		return classInfo;
	}
	
	
}





