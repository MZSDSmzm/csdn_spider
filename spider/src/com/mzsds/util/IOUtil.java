package com.mzsds.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;


/**
 * IO读写工具集
 */
public class IOUtil {
	
	
	
	/**
	 * 读取文本文件
	 * eg: 	IOUtil.writeTextFile("d:/temp/aa.txt", "hello world 1", false);
	 *		System.out.println(IOUtil.readTextFile("d:/temp/aa.txt"));
	 *
	 * @param fileName 文件全路径名称
	 * @return 字符串
	 * @throws Exception 异常
	 */
	public static String readTextFile(String fileName) throws Exception{
		BufferedReader br=new BufferedReader(new FileReader(fileName));
		String line="";
		StringBuffer buffer = new StringBuffer();
		while((line=br.readLine())!=null){
			buffer.append(line+"\r\n");
		}
		br.close();
		return buffer.toString();
	}
  
	
	
	/**
	 * 写入文本文件
	 * eg: 	IOUtil.writeTextFile("d:/temp/aa.txt", "hello world 1", false);
	 *		System.out.println(IOUtil.readTextFile("d:/temp/aa.txt"));
	 *
	 * @param fileName 文本文件全路径名
	 * @param fileData 欲写入的文本内容
	 * @param appendFlag 为True时表示在文档最后面追加。为False时，为覆写
	 * @throws Exception
	 */
	public static void writeTextFile(String fileName, String fileData, boolean appendFlag) throws Exception{
		File file = new File(fileName);
		if(appendFlag){
			BufferedWriter ow = new BufferedWriter(new FileWriter(file, true));
			ow.append(fileData);
			ow.newLine();
			ow.close();
		}else{
			BufferedWriter ow = new BufferedWriter(new FileWriter(file));
			ow.write(fileData);
			ow.newLine();
			ow.close();
		}
	}
	
	
	/**
	 * 读取二进制文件
	 * eg: 	byte[] barr = IOUtil.readBinFile("d:/temp/NewPlatform.rar");
	 * 		IOUtil.writeBinFile("d:/temp/NewPlatform1.rar", barr);
	 * 
	 * @param fileName 二进制文件全路径名称
	 * @return 返回文件二进制数组
	 * @throws Exception
	 */
	public static byte[] readBinFile(String fileName) throws Exception{  
		File file =new File(fileName);  
		long len = file.length();  
		byte[] bytes = new byte[(int)len];
		BufferedInputStream bufferedInputStream=new BufferedInputStream(new FileInputStream(file));  
		int r = bufferedInputStream.read(bytes);  
		 if (r != len){ LogUtil.exception("IOUtil.readBinFile("+fileName+") 读写文件异常"); } 
        bufferedInputStream.close();  
        return bytes;  
    }
	
	
	/**
	 * 写入二进制文件
	 * eg: 	byte[] barr = IOUtil.readBinFile("d:/temp/NewPlatform.rar");
	 * 		IOUtil.writeBinFile("d:/temp/NewPlatform1.rar", barr);
	 * 
	 * @param fileName 二进制文件全路径名称
	 * @param data 二进制数组
	 * @throws Exception
	 */
	public static void writeBinFile(String fileName, byte[] data) throws Exception{
		FileOutputStream fos = new FileOutputStream(fileName);  
		fos.write(data);
		fos.close();  
	}
	
	 
	/**
	 * 根据文件扩展名获得文件MIME类型。失败返回空“”。
	 * eg: System.out.println(IOUtil.getMimeType("a.xml"));
	 * 
	 * @param fileName 文件名，带不带路径均可
	 * @return MIME类型
	 * @throws Exception
	 */
	public static String getMimeType(String fileName)  throws Exception {
		
		//提取扩展名，若扩展名为空，则直接返回失败
		String extendStr = null;
		int dindex = fileName.trim().lastIndexOf(".");
		if(dindex>=0) 
			extendStr = fileName.substring(dindex).toLowerCase();
		else
			return "";
		//匹配mime类别（匹配特定扩展名，相当于是补充使用）
		//参考：http://tool.oschina.net/commons
		switch(extendStr){
			case ".jpg": return "image/jpeg";
			case ".png": return "image/png";
			case ".bmp": return "application/x-bmp";
			case ".jpeg": return "image/jpeg";
			case ".ico": return "image/x-icon";
			case ".tiff": return "image/tiff";
			
			case ".txt": return "text/plain";
			case ".html": return "text/html";
			case ".htm": return "text/html";
			case ".js":   return "application/x-javascript";
			case ".jsp": return "application/x-internet-signup";
			case ".java": return "java/*";
			case ".css": return "text/css";
			case ".xml": return "text/xml";
			case ".wml": return "text/vnd.wap.wml";
			
			case ".mp3": return "audio/mp3";
			case ".wav": return "audio/x-wav";
			case ".mp2": return "audio/mp2";
			case ".mid": return "audio/mid";
			case ".wma": return "audio/x-ms-wma";
			
			case ".mp4": return "video/mpeg4";
			case ".swf": return "application/x-shockwave-flash";
			case ".mpeg": return "video/mpg";
			case ".rmvb": return "application/vnd.rn-realmedia-vbr";
			
			case ".apk": return "application/vnd.android.package-archive";
			case ".ipa": return "application/vnd.iphone";
			case ".vsd": return "application/x-vsd";
			case ".xls": return "application/x-xls";
			case ".pdf": return "application/pdf";
			case ".wps": return "application/vnd.ms-works";
			case ".ppt": return "application/vnd.ms-powerpoint";
			case ".doc": return "application/msword"; 
			case ".docx": return "application/msword"; 
			case ".exe": return "application/x-msdownload";
			
		}
		//匹配mime类别（匹配普通扩展名）
		FileNameMap fileNameMap = URLConnection.getFileNameMap();   
		String type = fileNameMap.getContentTypeFor(fileName);   
		if(type==null) type = "";
		return type;
	} 
	
	
	

	/** 
     * 将String转换成InputStream 
     * 
     * @param in 
     * @return 
     * @throws Exception 
     */  
    public static InputStream covertString2InputStream(String inStr, String encoding) throws Exception{  
    	if(encoding==null || encoding.equals("")) encoding = "utf8";
        ByteArrayInputStream is = new ByteArrayInputStream(inStr.getBytes(encoding));
        return is;  
    }  
    
    

    /**
     *  将InputStream转换成某种字符编码的String 
     * @param in
     * @param encoding
     * @return
     * @throws Exception
     */
    public static String covertInputStream2String(InputStream in, String encoding) throws Exception{ 
    	//读取内容
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();  
        byte[] data = new byte[4096];  
        int count = -1;  
        while((count = in.read(data,0,4096)) != -1){
        	outStream.write(data, 0, count);  
        }
        //编码返回
        if(encoding==null){
        	return new String(outStream.toByteArray());  
        }else{
        	if(encoding.equals("")) encoding = "utf8";
        	return new String(outStream.toByteArray(), encoding);  
        }
    }
    
	
    /**
     * 获取文件的扩展名
     * eg: System.out.println(IOUtil.getFileExtension("/Users/hbbc/Temp/aa.jpg"));
     * 
     * @param filename 文件名字符串，可以带路径，也可以不带
     * @return 文件的扩展名，如"jpg"，没有扩展名，或者解析错误返回空字符串“”
     */
	public static String getFileExtension(String filename) {
        if ((filename != null) && (filename.length() > 0)) {   
            int dot = filename.lastIndexOf('.');   
            if ((dot >-1) && (dot < (filename.length() - 1))) {   
                return filename.substring(dot + 1);   
            }   
        }   
        return "";   
    }
	
	
	
	/**
	 * 拷贝文件，从sourceFileFullName拷贝到targetFileFullName。拷贝后检查两个文件大小是否一致，一致才返回True。
	 * eg： 
	 * 
	 * @param sourceFileFullName  源文件
	 * @param targetFileFullName 目标文件
	 * @return 拷贝成功返回True，否则，返回False
	 */
	public static boolean copyFile(String sourceFileFullName, String targetFileFullName) {  
		try {  
			//拷贝文件
			FileInputStream in = new java.io.FileInputStream(sourceFileFullName);  
			FileOutputStream out = new FileOutputStream(targetFileFullName);  
			byte[] bt = new byte[1024];  
			int count;  
			while ((count = in.read(bt)) > 0) {  
				out.write(bt, 0, count);  
			}  
			in.close();  
			out.close();
			//完成拷贝后，比较一下两个文件的尺寸是否一致，一致的话，才返回成功
			if((new File(sourceFileFullName)).length()==(new File(targetFileFullName)).length())
				return true;
			else
				return false;  
		} catch (Exception e) {
			LogUtil.exception(e);
			return false;
		}  
	}  
	
	
	

	 /**
   * 递归查找指定文件夹下含部分文件名的文件清单（文件名大小写敏感！）。filePartName 为文件的部分名称，如: .java src 等。
   * onlyFileFlag 为true时，只检索文件，否则，连文件夹一并检索。
   * eg: String[] farr = IOUtil.findFileList("d:/temp/aa", ".java", true);
   */
	public static String[] findFileList(String tarDirName, String filePartName, boolean onlyFileFlag) {
		String[] rArr = null;
		ArrayList<String> flist = new ArrayList<String>();
		findFileListOfName(tarDirName, filePartName, onlyFileFlag, flist);
		rArr = new String[flist.size()];
		for(int i=0; i<rArr.length; i++){
			rArr[i] = (String) flist.get(i);
		}
		return rArr;
  }
	private static void findFileListOfName(String tarDirName, String filePartName, boolean onlyFileFlag, ArrayList<String> fileListObj){
		File tarDir = new File(tarDirName);
      if (tarDir.isDirectory()) {
	        String[] children = tarDir.list(); 					//递归删除目录中的子目录下
          for (int i=0; i<children.length; i++) {
              findFileListOfName(new File(tarDir, children[i]).getAbsolutePath(), filePartName, onlyFileFlag, fileListObj);
          }
      }
      //只搜索文件，但本文件为路径的时候，跳过
      if(onlyFileFlag==true &&  tarDir.isDirectory()==true) return;
      //通过文件名进行匹配，大小写敏感！
      //System.out.println(tarDir.getName()+" == "+filePartName);
      if(tarDir.getName().indexOf(filePartName)>=0){
      	fileListObj.add(tarDirName);
      }
	}
	
	
	
	/**
	 * 替换文本文件中指定字符串。若存在多处，一并替换。
	 * eg: boolean bFlag = IOUtil.replaceTextFileContent("d:/temp/aa.txt", "heLo", "hello");
	 */
	public static boolean replaceTextFileContent(String fileName, String oldStr, String newStr){
		try {
			String filestr = IOUtil.readTextFile(fileName);
			filestr = filestr.replaceAll(oldStr, newStr);
			IOUtil.writeTextFile(fileName, filestr, false);
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
		return true;
	}
	
	
	
  /**
   * 递归删除目录下的所有文件及子目录下所有文件
   * eg: boolean bFlag = IOUtil.deleteDir("d:/temp/aa");
   */
	public static boolean deleteDir(String tarDirName) {
		File tarDir = new File(tarDirName);
      if (tarDir.isDirectory()) {
	        String[] children = tarDir.list(); 					//递归删除目录中的子目录下
          for (int i=0; i<children.length; i++) {
              boolean success = deleteDir(new File(tarDir, children[i]).getAbsolutePath());
              if (!success) {
                  return false;
              }
          }
      }
      // 目录此时为空，可以删除
      return tarDir.delete();
  }

  
	
  /**
   * 递归复制文件夹及子文件夹下所有内容
   * eg:  IOUtil.copyDir("d:/temp/aa", "d:/temp/aa_copy");
   */
  public static void copyDir(String srcDirName, String tarDirName) throws IOException {  
  	File srcDir = new File(srcDirName);
  	File tarDir = new File(tarDirName);
      if (srcDir.isDirectory()) {  
          if (!tarDir.exists()) {  
              tarDir.mkdir();
          }  
          String files[] = srcDir.list();  
          for (String file : files) {  
              File srcFile = new File(srcDir, file);  
              File destFile = new File(tarDir, file);  
              // 递归复制  
              copyDir(srcFile.getAbsolutePath(), destFile.getAbsolutePath());  
          }  
      } else {  
          InputStream in = new FileInputStream(srcDir);  
          OutputStream out = new FileOutputStream(tarDir);  
          byte[] buffer = new byte[1024];  
          int length;
          while ((length = in.read(buffer)) > 0) {  
              out.write(buffer, 0, length);  
          }  
          in.close();  
          out.close();  
      }  
  }  
}
