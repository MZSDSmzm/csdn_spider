package com.mzsds.global;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONObject;

import com.mzsds.util.DBUtil;
import com.mzsds.util.DataUtil;
import com.mzsds.util.HttpUtil;
import com.mzsds.util.IOUtil;
import com.mzsds.util.LogUtil;

public class ServletBeginThreadCsdnJktt extends Thread {

	// 业界 news 云计算 cloud 大数据 bigdata 人工智能 AI 物联网 iot 语言 language 数据库 database 前端
	// frontend
	// 移动开发 mobile 系统网络安全 os 游戏与图像 game 研发工具 tools 软件工程 se 程序人生 career 开源项目
	// osproject
	private static String[] module = { "news", "cloud", "bigdata", "AI", "iot",
			"language", "database", "frontend", "mobile", "os", "game",
			"tools", "se", "career", "osproject" };
	private static String[] moduleName = { "业界", "云计算", "大数据 ", "人工智能", "物联网 ",
			"语言", "数据库", "前端", "移动开发", "系统网络安全", "游戏与图像", "研发工具", "软件工程",
			"程序人生", "开源项目" };

	// 将要爬取具体信息的相关正则表达式
	private static String regex = "[\\s]*([\\S]+)[\\s]*</div>";
	private static String regex2 = "<a href=\"((http|ftp|https)://[\\s|\\S]*?)\" class=\"title\" target=\"_blank\">(.*)</a>";
	private static String regex3 = "data-target>([\\S]*)</a></li>";
	private static String regex4 = "<li class=\"read_num\"><span>[\\S]*</span><em>([\\S]*)</em></li>";
	private static String regex5 = "<li>([0-9][\\S|\\s]*)</li>";
	private static String urlBase = "http://geek.csdn.net/service/news/get_category_news_list?category_id=";
	private static String fileGetNewsPath = "C:/spider/Jktt/JkttHtmlUrl.txt";

	/** 启动 */
	public void run() {
		while (true) {
			try {
				// 每天的10点执行线程
				SimpleDateFormat sdfs = new SimpleDateFormat("hh:mm");
				String nowTime = sdfs.format(new Date());

				// 加锁
				int lock = 0;
				if (nowTime == "22:00") {
					lock = 1;
				}
				if (lock == 1) {
					LogUtil.log("---------[ServletAgainThreadCsdnJktt] 定时作业执行"
							+ lock);
					begin();
				}
				// 每分钟检查一次
				Thread.sleep(1 * 60 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 开始线程,run调用的方法
	 * 
	 * @author menghzimin
	 */
	public static void begin() {
		LogUtil.log("方法begin");
		// 将极客头条中的【业界 云计算 大数据 人工智能 物联网 语言 数据库 前端 移动开发 系统网络安全 游戏与图像 研发工具 软件工程
		// 程序人生 开源项目】(编号1-15)放入map中
		Map<Integer, String> jkttMap = new HashMap<Integer, String>();
		for (int i = 0; i < module.length; i++) {
			jkttMap.put(i, module[i]);
		}
		if (!jkttMap.equals("") && jkttMap != null) {
			LogUtil.log("====开始模块下载！");
			try {
				upload(jkttMap, moduleName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			LogUtil.log("====没有模块进行下载！");
		}
	}

	/**
	 * 进行极客头条模块的信息下载
	 * 
	 * @param jkttMap
	 *            存放极客头条模块的url信息
	 * @param moduleName
	 *            存放极客头条模块的模块信息
	 * @author mengzhimin
	 */
	public static void upload(Map<Integer, String> jkttMap, String[] moduleName) {
		LogUtil.log("方法upload");
		// 定义一个大数，为了抓包
		String yesterdayTime = null;
		for (int num = 0; num < jkttMap.size(); num++) {
			long j = 15888098;
			yesterdayTime = dealYesterday(jkttMap.get(num));
			int time = 3;
			long from = 0;
			for (long i = 0; i > -1; i++) {

				String htmlString = getJkttJquaryUrlHtml(urlBase
						+ jkttMap.get(num)
						+ "&jsonpcallback=jQuery203027978063627208827_15888097&username=&from="
						+ from + "&size=20&type=category&_=" + j++);
				LogUtil.log("==== from = " + from);
				if (htmlString == null || "".equals(htmlString)) {
					time--;
					if (time > 0) {
						LogUtil.log("==== time = " + time);
						if (from > 0) {
							from -= 20;
						}
						continue;
					} else {
						LogUtil.log(moduleName[num] + "结束");
						break;
					}
				}
				try {
					IOUtil.writeTextFile(fileGetNewsPath, htmlString, false);
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					if (!putAllListTo(fileGetNewsPath, moduleName[num],
							jkttMap.get(num), yesterdayTime)) {
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				from += 20;
				LogUtil.log(moduleName[num] + ":" + i);
			}
		}
	}

	/**
	 * 极客头条：删除以前的类似发布日期为：5小时前 的数据，并获取以前最新发表日期
	 * 
	 * @param jktt
	 *            极客头条数据库(不包含jktt_)，eg：cloud
	 * @return
	 */
	public static String dealYesterday(String jktt) {
		LogUtil.log("方法dealYesterday");
		String yesterdayTime = null;
		try {
			if (Spider.findTable("jktt_" + jktt) == false) {
				makeJkttTable("jktt_" + jktt);
			}
			String sqlDelete = "DELETE FROM jktt_" + jktt
					+ " WHERE ReportTime LIKE '%"
					+ DataUtil.getHttpCNSafeString("前") + "'";
			DBUtil.getInstance().execute(sqlDelete);
			String sqlSelect = "SELECT ReportTime FROM jktt_" + jktt
					+ " ORDER BY ReportTime DESC ";
			yesterdayTime = DBUtil.getInstance().queryReturnSingleString(
					sqlSelect);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return yesterdayTime;
	}

	/**
	 * 将爬取到的信息以
	 * "阿里iDST华先胜：城市大脑，对城市的全量、实时认知和搜索|http://geek.csdn.net/news/detail/237643|云计算|2303|3|2017-09-22 11:02"
	 * 的形式保存在相应文件中 eg:putAllListTo("C:/spider/htmlUrl.txt"','"数据库");
	 * 
	 * @param fileGetNewsPath
	 *            临时存放包含具体信息的html文件路径 ，比如："C:/spider/htmlUrl.txt"
	 * @param moudle
	 *            极客头条的模块名称，比如："数据库"
	 * @param moudleUrlKey
	 *            极客头条的模块URL的关键词，eg：sjk
	 * @param yesterdayTime
	 *            数据库昨天的最新日期
	 * @author mengzhimin
	 * @throws Exception
	 */
	public static boolean putAllListTo(String fileGetNewsPath, String moudle,
			String moudleUrlKey, String yesterdayTime) throws Exception {
		LogUtil.log("方法putAllListTo");

		List<String> listGoodNum = Spider.getListFromRegex(regex,
				fileGetNewsPath, 1);
		List<String> listLink = Spider.getListFromRegex(regex2,
				fileGetNewsPath, 1);
		List<String> listTitle = Spider.getListFromRegex(regex2,
				fileGetNewsPath, 3);
		List<String> listField = Spider.getListFromRegex(regex3,
				fileGetNewsPath, 1);
		List<String> listReadNum = Spider.getListFromRegex(regex4,
				fileGetNewsPath, 1);
		List<String> listReportTime = Spider.getListFromRegex(regex5,
				fileGetNewsPath, 1);
		List<String> listAll = new ArrayList<String>();

		// 创建新 PrintWriter,路径filePath
		for (int i = 0; i < listTitle.size(); i++) {
			try {
				listAll.add(listTitle.get(i) + "|" + listLink.get(i) + "|"
						+ listField.get(i) + "|" + listReadNum.get(i) + "|"
						+ listGoodNum.get(i) + "|" + listReportTime.get(i));
				LogUtil.log("  " + listAll.get(i));
				String[] listString = DataUtil.splitString(listAll.get(i), "|");
				IOUtil.writeTextFile("C:/spider/Jktt/" + moudle + ".txt",
						listAll.get(i), true);
				if (!putDataBase(listString, moudle, moudleUrlKey,
						yesterdayTime)) {
					return false;
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		LogUtil.log("====爬取完成！");
		return true;
	}

	/**
	 * 将得到的信息存放到数据库中，数据库中有数据即为更新到最新日期，没有数据就是得到所有模块中的数据 eg：putDataBase(new
	 * String[]{"短视频图像处理 OpenGL ES 实践",
	 * "https://tech.upyun.com/article/232/OpenGL.html"
	 * ,"云计算","894","8","2017-09-21 11:22"},"云计算");
	 * 
	 * @param listString
	 *            分割后的字符串数组，包含爬取信息
	 * @param moudle
	 *            极客头条的模块名称，比如："数据库"
	 * @param dbKey
	 *            数据库的关键字(不包含jktt_)，eg：news
	 * @param yesterdayTime
	 *            数据库昨天的最新日期
	 * @return 数据库增加操作是否执行成功
	 * @throws ParseException
	 * @throws Exception
	 */
	public static boolean putDataBase(String[] listString, String moudle,
			String dbKey, String yesterdayTime) throws ParseException {
		LogUtil.log("方法putDataBase");
		LogUtil.log("使用数据库：jktt_" + dbKey);
		if (listString.length != 6) {
			return true;
		}
		for (int i = 0; i < listString.length; i++) {
			if (listString[i].contains(",") || listString[i].contains("'")) {
				return true;
			}
		}

		if (yesterdayTime != null && !"".equals(yesterdayTime)) {
			if (listString[5].length() == "yyyy-MM-dd HH:mm".length()) {
				Date now = new SimpleDateFormat("yyyy-MM-dd HH:mm")
						.parse(listString[5]);
				Date yesterday = new SimpleDateFormat("yyyy-MM-dd HH:mm")
						.parse(yesterdayTime);
				long nowMS = now.getTime();
				long yesterdayMS = yesterday.getTime();
				// 更新数据到数据库以前的最新日期
				if (nowMS <= yesterdayMS) {
					return false;
				}
			}
		}

		String sql = "INSERT INTO  jktt_"
				+ dbKey
				+ "(ID,Type,Title,Link,Field,ReadNum,GoodNum,ReportTime,AddTime) VALUES(null,'"
				+ moudle + "','" + listString[0] + "','" + listString[1]
				+ "','" + listString[2] + "'," + listString[3] + ","
				+ listString[4] + ",'" + listString[5] + "' ,now())";
		int n = 0;
		try {
			n = DBUtil.getInstance().execute(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return n > 0;
	}

	/**
	 * 获得极客头条Jquary对应的URL中json数据的html值
	 * 
	 * @param url
	 * @author mengzhimin
	 * @return String的html
	 */
	public static String getJkttJquaryUrlHtml(String url) {
		LogUtil.log("方法getJkttJquaryUrlHtml");
		LogUtil.log("URl：" + url);
		JSONObject jsonObject = null;
		String htmlStr = "";
		String result = HttpUtil.callWebPage(url, null);
		if (result != null && !"".equals(result)) {
			result = result.substring(result.indexOf("{"), result.length() - 1);
			jsonObject = JSONObject.fromObject(result);
			if (jsonObject.containsKey("html")) {
				htmlStr = jsonObject.getString("html");
			}
		}
		return htmlStr;
	}

	/**
	 * 创建数据库表
	 * 
	 * @param tableName
	 *            数据库表名,eg :jktt_os
	 * @author mengzhimin
	 */
	public static void makeJkttTable(String tableName) {
		LogUtil.log("方法makeJkttTable");
		String sql = "create table "
				+ tableName
				+ "(ID	int PRIMARY KEY auto_increment,Type varchar(255),Title varchar(1024),Link varchar(1024),Field varchar(255),ReadNum int,GoodNum int,ReportTime varchar(255),AddTime varchar(255))";
		try {
			DBUtil.getInstance().execute(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 将得到的HTML信息放到TXT文件中
	 * 
	 * @param regex
	 *            信息的正则表达式， eg：String regex = "[\\s]*([\\S]+)[\\s]*</div>";
	 * @param fileFromPath
	 *            临时存放包含具体信息的html文件路径，eg："C:/spider/htmlUrl.txt"
	 * @param fileToPath
	 *            文件保存的地方（路径），eg："C:/spider/mzm.txt"
	 * @author mengzhimin
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	public static void getHtmlFileToTXT(String regex, String fileFromPath,
			String fileToPath) throws IOException {
		LogUtil.log("方法getHtmlFileToTXT");
		BufferedReader br = new BufferedReader(new FileReader(fileFromPath));
		String line = null;
		PrintWriter out = null;
		Pattern pattern = Pattern.compile(regex);
		// 创建新 PrintWriter,路径filePath
		out = new PrintWriter(new FileWriter(fileToPath), true);
		Set<String> set = new HashSet<String>();
		// 读响应数据到.TXT文件中
		while ((line = br.readLine()) != null) {
			// 匹配正则表达式
			Matcher matcher = pattern.matcher(line);
			if (matcher.find()) {
				// 输出匹配内容
				set.add(matcher.group());
			}
		}
		for (String str : set) {
			// 写入爬取内容
			out.println(str);
		}
		out.close();
	}
}
