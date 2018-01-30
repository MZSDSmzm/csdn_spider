package com.mzsds.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

public class HttpUtil {

	
	
	/**
	 * 发起网页调用，获取网页调用返回值。调用可以是Post方式也可以是Get方式。当为Get方式时，第二个参数postData设置为null。
	 * 若发生调用失败，返回null ! eg:
	 * System.out.println(HttpUtil.callWebPage("http://www.baidu.com", null));
	 * System.out.println(HttpUtil.callWebPage(
	 * "http://market.aliyun.com/image/?spm=5176.383338.201.27.US4LIi", null));
	 * System.out.println(HttpUtil.callWebPage(
	 * "http://127.0.0.1:8080/demobo/global.Login.doLogin.hf?userAccount=admin&type=11"
	 * , "userPassword=haha&userAge=12"));
	 * 
	 * @param urlStr  HTTP Url 带参数。如：http://www.xxx.com/a.jsp?a=1
	 * @param postData  Post数据，形如：userPassword=haha&userAge=12
	 * @return String WEB页面调用后返回的HTML代码。调用失败，返回null。
	 * @author mengzhimin
	 */
	public static String callWebPage(String urlStr, String postData) {
		return callWebPage(urlStr, postData, "UTF-8");
	}
	
	
	/**
	 * 发起网页调用，获取网页调用返回值。调用可以是Post方式也可以是Get方式。当为Get方式时，第二个参数postData设置为null。
	 * 若发生调用失败，返回null ! eg:
	 * System.out.println(HttpUtil.callWebPage("http://www.baidu.com", null, "GBK"));
	 * 
	 * @param urlStr  HTTP Url 带参数。如：http://www.xxx.com/a.jsp?a=1
	 * @param postData Post数据，形如：userPassword=haha&userAge=12
	 * @param encoding 强制输入输出流的编码格式，为空或NULL的时候取默认值
	 * @return String WEB页面调用后返回的HTML代码。调用失败，返回null。
	 * @author mengzhimin
	 */
	public static String callWebPage(String urlStr, String postData, String encoding) {

		String rStr = null;
		BufferedReader in = null;
		PrintWriter out = null;
		try {
			// 打开和URL之间的连接
			URLConnection conn = (new URL(urlStr)).openConnection();
			StringBuffer sb = new StringBuffer();
			// 设置通用的请求属性
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			if (postData != null) {
				conn.setDoOutput(true);
				conn.setDoInput(true);
			}
			// 建立实际的连接
			conn.connect();
			// Post数据（如果非空的话， 获取URLConnection对象对应的输出流并输出参数）
			if (postData != null) {
				if(encoding==null || "".equals(encoding)){
					out = new PrintWriter(new OutputStreamWriter(conn.getOutputStream()), true);
				}else{
					out = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(),  encoding), true);
				}
				out.print(postData);
				out.flush();
			}
			// 获取所有响应头字段
			// Map<String, List<String>> map = connection.getHeaderFields();
			// for (String key : map.keySet()) { System.out.println(key +
			// " <== " + map.get(key)); }
			// 定义 BufferedReader输入流来读取URL的响应
			if(encoding==null || "".equals(encoding)){
				in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			}else{
				in = new BufferedReader(new InputStreamReader(conn.getInputStream(),  encoding));
			}
			String line;
			while ((line = in.readLine()) != null) {
				sb.append(line);
			}
			rStr = sb.toString();
		} catch (Exception e) {
			LogUtil.exception(e);
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (Exception ee) {
			}
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception ee) {
			}
		}
		return rStr;
	}
}
