package com.mzsds.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * 数据处理工具类。用作数据转换，数据类别判断...
 */
public class DataUtil {

	
	
	/**
	 * 字符串编码格式转换。常见的有：utf8, gb2312, gbk, ISO-8859-1
	 * eg: DataUtil.covertStringEncoding(a, "ISO-8859-1", "utf8");
	 * 
	 * @param str 待编码转换的字符串
	 * @param fromEncodeStr 从什么编码格式
	 * @param toEncodeStr 转换成什么编码格式
	 * @return 返回编码转换完成后的字符串
	 * @throws Exception
	 */
	public static String covertStringEncoding(String str, String fromEncodeStr, String toEncodeStr) throws Exception{
		return new String(str.getBytes(fromEncodeStr), toEncodeStr);
	}
	
	
	
	/**
	 * 用特殊分割符把数据分割成一个一维数组，如："hello|!|Every|!|body!"用"|!|"分割后就得到数组{"hello", "Every", "body!"}
	 * Java自带的split()函数有正则表达式转义的麻烦，此方法作为一种替代算法。失败返回NULL。
	 * eg: String[] arra = DataUtil.splitString("a,b,c", ",");
	 * 
	 * @param originalStr 用于分割的原始字符串
	 * @param splitStr 分割字符串
	 * @return String[] 分割完成后的字符串，失败返回NULL
	 */
    public static String[] splitString(String originalStr, String splitStr)
    {
        String[] resultArr = null;
        try
        {
            int indexPlace;
            int splitLength = splitStr.length();
            String indexStr = null;
            String tmpStr = originalStr;
            ArrayList<String> oArrayList = new ArrayList<String>();
            while ((indexPlace = tmpStr.indexOf(splitStr)) > -1)
            {
                indexStr = tmpStr.substring(0, indexPlace);
                oArrayList.add(indexStr);
                tmpStr = tmpStr.substring(indexPlace + splitLength);
            }
            oArrayList.add(tmpStr);
            resultArr = (String[])oArrayList.toArray(new String[0]); 	
        }
        catch(Exception e) {
        	LogUtil.exception(e.toString());
        }
        return resultArr;
    }
    
    /**
     * 将数据分割成二维数组。失败返回NULL。
     * eg: String[][] dataArr = DataUtil.splitStringTo2DArr("a,b,c;1,2,3;11,12,13", ";", ",");
     * 
     * @param originalStr
     * @param splitStr1 第一次分割的分割符，如：";"  "|@|"
     * @param splitStr2 第二次分割的分割符，如：","  "|!|"
     * @return 返回分割后的二维数组
     */
    public static String[][] splitStringTo2DArr(String originalStr, String splitStr1, String splitStr2) {
    	
        String[] arr1 = splitString(originalStr, splitStr1);
        if (arr1 != null && arr1.length > 0)
        {
            String[][] arr = new String[arr1.length][];
            for (int i = 0; i < arr1.length; i++)
            {
                arr[i] = splitString(arr1[i], splitStr2);
            }
            return arr;
        }
        return null;
    }
    
    /**
     * 获取UUID。真随机字符串，每次都不同。
     * eg: String a = DataUtil.getUUID();
     * @return 字符串，形如：1d1d3f28-50e4-4661-981f-fb64c034b7c8
     */
	public static String getUUID(){
		UUID uuid = UUID.randomUUID();   
		return uuid.toString();
	}
	
	/**
	 * 根据不同的系统，返回不同转码类型的字符串
	 * */
	public static String getHttpCNSafeString(String str){
		String strchanged = str;
		try { 
			if(str != null) {
				if(!isChineseChar(str)) {
					strchanged = new String (str.getBytes("ISO-8859-1"),"utf-8"); 
				}
			}
		} catch (Exception e) { 
			LogUtil.exception(e);
		}
		return strchanged;
	}
	
	/**
	 * 判断是否是中文
	 * @param str
	 * @return
	 */
	public static boolean isChineseChar(String str) {
		boolean temp = false;
		try {
			Pattern p = Pattern.compile("[\u4e00-\u9fa5]"); 
		       Matcher m = p.matcher(str); 
		       if(m.find()){ 
		           temp =  true;
		       }
		} catch (Exception e) {
			LogUtil.exception(e);
		}
	    return temp;
	}
	
	/**
	 * 获得数据库SQL语句安全编码后的字符串，防止注入式SQL攻击。主要就是把单引号进行转码。
	 * eg: String sql = "UPDATE sys_distsaa SET SessionValue='"+DataUtil.getDBSafeString(sessionValue)+"'  WHERE ClientID='"+clientID+"' ";
	 * 
	 * @param str 待编码的字符串
	 * @return 编码后的字符串
	 */
    public static String getDBSafeString(String str){
    	return str.replace("'", "''");
    }
    
	/**
	 * 解析时间字符串成日期对象。
	 * eg: System.out.println(DataUtil.parseDate("2011-3-1 12:09:00")); 
	 * 		System.out.println(DataUtil.parseDate("2000-1-1"));
	 * 
	 * @param dateStr 支持日期和日期加时间两种格式的解析。如：“2000-1-1”和 "2011-3-1 12:09:00"
	 * @return Date 日期对象
	 * @throws Exception
	 */
	public static Date parseDate(String dateStr) throws Exception{
		java.text.SimpleDateFormat sim = null;
		if(dateStr.indexOf(":")>0) {
			sim=new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		} else  {
			sim=new java.text.SimpleDateFormat("yyyy-MM-dd");
		}
		Date d=sim.parse(dateStr);
		return d;
	}
	
    /**
     * 得到当前时间的字符串表示。
     * eg: System.out.println(DataUtil.getNowTimeString());
     * 
     * @return 字符串，如：2015-02-26 08:23:06
     */
    public static String getNowTimeString(){
    	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");	
    	return df.format(new Date());
    }
    
    /**
     * 计算给出时间计算并格式化后的字符串。第二个参数表示给出时间的时间差。正数表示XX秒以后，负数表示XX秒钟以前。
     * eg: String sessionTimeoutLimitStr = DataUtil.calDatetimeString(new Date(),  -1* 30*60);
     * 
     * @param date 时间格式
     * @param adjustSeconds 时间调整数，单位：秒
     * @return 返回调整时间后的格式化时间字符串
     */
    public static String calDatetimeString(Date date, long adjustSeconds){
    	if(adjustSeconds!=0) date.setTime(date.getTime() + adjustSeconds*1000);
    	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	return df.format(date);
    }
    
    /**
     * 转换Byte[]数组为十六进制字符串。主要用于打印排错或者传输使用。
     * eg: System.out.println(DataUtil.covertByteArrToHexString("您好abc".getBytes()));
     * 
     * @param data Byte[]
     * @return 十六进制字符串，中间用空格分隔
     */
    public static String covertByteArrToHexString(byte[] data) {
    	StringBuffer sb = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            String hex = Integer.toHexString(data[i] & 0xFF);
            if (hex.length() == 1)
            {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase()+" ");
        }
        return sb.toString();
    }
    
    /**
     * 识别给定字符串（中文）的编码格式。返回 GB2312、ISO-8859-1、UTF-8、GBK 中的一种，不能识别的，返回空。
     * eg: String encodingType = DataUtil.getStringEncoding("中文");
     * 
     * @param str 识别的字符串
     * @return GB2312、ISO-8859-1、UTF-8、GBK
     */
    public static String getStringEncoding(String str){
    	String encode = "GB2312";      
    	try {
    		if (str.equals(new String(str.getBytes(encode), encode))) {      
    			String s = encode;      
    			return s;      
    		}  
    	} catch (Exception exception) {}      
    	encode = "ISO-8859-1";      
    	try {
    		if (str.equals(new String(str.getBytes(encode), encode))) {      
    			String s = encode;      
    			return s;      
    		}  
    	} catch (Exception exception) {}    
    	encode = "UTF-8";      
    	try {
    		if (str.equals(new String(str.getBytes(encode), encode))) {      
    			String s = encode;      
    			return s;      
    		}  
    	} catch (Exception exception) {}    
    	encode = "GBK";      
    	try {
    		if (str.equals(new String(str.getBytes(encode), encode))) {      
    			String s = encode;      
    			return s;      
    		}  
    	} catch (Exception exception) {}
    	return "";   
    }
    
    /**
     * 得到当前时间的字符串表示。
     * eg: System.out.println(DataUtil.getNowTimeString());
     * 
     * @return 字符串，如：2015-02-26
     */
    public static String getNowDateString(){
    	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");	
    	return df.format(new Date());
    }
}
