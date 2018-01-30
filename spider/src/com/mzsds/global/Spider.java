package com.mzsds.global;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mzsds.util.ConfigUtil;
import com.mzsds.util.DBUtil;
import com.mzsds.util.HttpUtil;
import com.mzsds.util.IOUtil;
import com.mzsds.util.LogUtil;

public class Spider {
	/**
	 * 通过正则表达式得到URL中想要的信息
	 * 
	 * @param regex
	 *            信息的正则表达式， eg：String regex = "[\\s]*([\\S]+)[\\s]*</div>";
	 * @param fileFromPath
	 *            临时存放包含具体信息的html文件路径，eg："C:/spider/htmlUrl.txt"
	 * @param groupNum
	 *            正则表达式的group信息
	 * @return
	 * @author mengzhimin
	 * @throws Exception
	 */
	@SuppressWarnings("resource")
	public static List<String> getListFromRegex(String regex,
			String fileFromPath, int groupNum) {
		BufferedReader br;
		List<String> list = new ArrayList<String>();
		try {
			br = new BufferedReader(new FileReader(fileFromPath));
			String line = null;
			Pattern pattern = Pattern.compile(regex);
			// 读响应数据到.TXT文件中
			try {
				while (!br.ready()) {
				}
			} catch (IOException e) {
				e.printStackTrace();
			} // 阻塞，等待一段时间
			try {
				while ((line = br.readLine()) != null) {
					// 匹配正则表达式
					Matcher matcher = pattern.matcher(line);
					while (matcher.find()) {
						// 输出匹配内容
						list.add(matcher.group(groupNum));
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * 访问URL，并写入相应文件中
	 * 
	 * @param fileGetNewsPath
	 *            文件目录，eg："C:/spider/htmlUrl.txt"
	 * @param Url
	 *            访问的URL，eg："http://bbs.csdn.net/forums/Windows"
	 * @author mengzhimin
	 */
	public static void visitUrl(String fileGetNewsPath, String Url) {
		try {
			String htmlString = HttpUtil.callWebPage(Url, null);
			File file = new File(fileGetNewsPath);
			file.createNewFile();
			IOUtil.writeTextFile(fileGetNewsPath, htmlString, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 查看数据库中是否含有tableName
	 * 
	 * @param tableName
	 *            数据库表名,eg:jktt_cloud
	 * @return true:有；false：无
	 * @author mengzhimin
	 */
	public static boolean findTable(String tableName) {
		LogUtil.log("  方法findTable");
		boolean flag = false;
		try {
			String sql = "select table_name from information_schema.tables where table_schema= '"
					+ ConfigUtil.getProperty(null, "UseDataBase", true)
					+ "' AND table_name = '" + tableName + "'";
			if (DBUtil.getInstance().queryReturnSingleString(sql) != null) {
				flag = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}

}
