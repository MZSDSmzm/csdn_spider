package com.mzsds.global;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mzsds.util.DBUtil;
import com.mzsds.util.IOUtil;
import com.mzsds.util.LogUtil;

public class ServletBeginThreadCsdnForum extends Thread {

	private static String regex = "[\\u4e00-\\u9fa5_a-zA-Z0-9\\s，。,.“”‘’！!@#$%^&*\\\\（）\\/~()？:：》《?【】、\\+_\\-]+";
	// 爬取的页数
	private static int pageNum = 500;

	private static String urlBase = "http://bbs.csdn.net";
	private static String fileGetNewsPath = "C:/spider/Forum/ForumHtmlUrl.txt";

	private static String regexPageNum = "<li><span>总数：[0-9]+，</span><span>共([0-9]+)页</span></li>";
	private static String regexFieldUrl = "<dt><a href=\"(/forums[\\S]+)\">([\\S]+)/?</a>[^\\|]";
	private static String regexForumsQuestions = "<strong class=\"[a-z]+\">("
			+ "[\\S]+"
			+ ")</strong>[\\s]+<a href=\"(/topics/[\\S]+)\" target=\"_blank\" title=\""
			+ regex
			+ "\">("
			+ regex
			+ ")</a>[\\s]+<span class=\"forum_link\">\\[<span class=\"parent\"><a href=\"([\\S]+)\">("
			+ "[\\S]+[\\s]?[\\S]+"
			+ ")</a></span> <a href=\"([\\S]+)\">("
			+ "[\\S]+[\\s]?[\\S]+"
			+ ")</a>]</span>[\\s]+</td>[\\s]+<td class=\"tc\">([\\d]+)</td>[\\s]+<td class=\"tc\">[\\s]+<a href=\"([\\S]+)\" rel=\"nofollow\" target=\"_blank\" title=\"("
			+ "[\\S]+[\\s]?[\\S]+"
			+ ")\"[\\S]+</a><br />[\\s]+<span class=\"time\">([\\S]+ [\\S]+)</span></td>[\\s]+<td class=\"tc\">([\\d]+)</td>[\\s]+<td class=\"tc\">[\\s]+<a href=\"([\\S]+)\" rel=\"nofollow\" target=\"_blank\" title=\"("
			+ "[\\S]+[\\s]?[\\S]+"
			+ ")\"[\\S]+</a><br />[\\s]+<span class=\"time\">([\\S]+ [\\S]+)</span>[\\s]+</td>";

	public static void main(String[] args) throws InterruptedException {
		System.out.println(regexForumsQuestions);
		begin();
	}

	/** 启动 */
	public void run() {
		while (true) {
			try {
				// 每天的22点执行线程
				SimpleDateFormat sdfs = new SimpleDateFormat("hh:mm");
				String nowTime = sdfs.format(new Date());

				// 加锁
				int lock = 0;
				if (nowTime == "12:00") {
					lock = 1;
				}
				if (lock == 1) {
					LogUtil.log("---------[ServletBeginThreadCsdnForum] 定时作业执行"
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
	 * begin ,线程run时调用
	 * 
	 * @throws InterruptedException
	 */
	public static void begin() throws InterruptedException {
		Spider.visitUrl(fileGetNewsPath, urlBase);
		Set<String> forumSet = new HashSet<String>();
		List<String> linkLists = Spider.getListFromRegex(regexFieldUrl,
				fileGetNewsPath, 1);
		for (String string : linkLists) {
			if (string.endsWith("/")) {
				string = string.substring(0, string.length() - 1);
			}
			forumSet.add(string);
			System.out.println(string);
		}
		// 未解决的论坛问题
		visitForumsSetUrl(forumSet, false);
		// 已解决的论坛问题
		visitForumsSetUrl(forumSet, true);
	}

	/**
	 * 访问csdn中主要论坛模块， eg：visitForumsSetUrl(forumSet,true);
	 * 
	 * @param forumSet
	 *            ,csdn论坛中主要的论坛模块
	 * @param flagForumClosed
	 *            ,flagForumClosed=false指的是csdn论坛中未解决的问题，反之是已解决的问题
	 * @author mengzhimin
	 * @throws InterruptedException
	 */
	public static void visitForumsSetUrl(Set<String> forumSet,
			boolean flagForumClosed) throws InterruptedException {
		Iterator<String> iterator = forumSet.iterator();
		String iteratorString = null;
		while (iterator.hasNext()) {
			iteratorString = (String) iterator.next();
			if (flagForumClosed == false) {
				// 网站默认为未解决的论坛问题
				LogUtil.log("未解决的论坛问题!");
				Spider.visitUrl(fileGetNewsPath, urlBase + iteratorString);
				System.out.println(urlBase + iteratorString);
			} else {
				// 这是已解决的论坛问题
				LogUtil.log("已解决的论坛问题!");
				Spider.visitUrl(fileGetNewsPath, urlBase + iteratorString
						+ "/closed");
				iteratorString += "/closed";
			}
			List<String> pageList = Spider.getListFromRegex(regexPageNum,
					fileGetNewsPath, 1);
			int pages = 1;
			if (pageList != null && !"".equals(pageList)) {
				pages = Integer.parseInt(pageList.get(0));
			}
			System.out.println("pages=====" + pages);
			visitUrlOneByOnePage(pages, iteratorString);
			System.out.println("===================");
			Thread.sleep(5 * 60 * 1000);// 休息5分钟
		}
	}

	/**
	 * 根据总共的页数一页页的访问URL，eg：visitUrlOneByOnePage(1523,"/forums/Windows");
	 * 
	 * @param pages
	 *            ,该论坛模块一共的页码数量
	 * @param iteratorString
	 *            ,URL主要部分，eg：/forums/Windows
	 * @author mengzhimin
	 * @throws InterruptedException
	 */
	public static void visitUrlOneByOnePage(int pages, String iteratorString)
			throws InterruptedException {
		LogUtil.log("方法visitUrlOneByOnePage");
		String tableName = null;
		if (iteratorString.contains("/close")) {
			tableName = "forum_"
					+ iteratorString.substring(
							iteratorString.indexOf("/", 2) + 1,
							iteratorString.lastIndexOf("/")).toLowerCase();
		} else {
			tableName = "forum_"
					+ iteratorString.substring(
							iteratorString.lastIndexOf("/") + 1,
							iteratorString.length()).toLowerCase();
		}

		String yesterdayTime = dealYesterday(tableName);
		int time = 3;
		// 最多爬取论坛的信息
		for (int page = 1; page <= pages && page <= pageNum; page++) {
			String visitUrl = urlBase + iteratorString + "?page=" + page;
			LogUtil.log("visitUrl:" + visitUrl);
			List<ArrayList<String>> forumList = new ArrayList<ArrayList<String>>();
			Spider.visitUrl(fileGetNewsPath, visitUrl);
			forumList = getListFromRegex(regexForumsQuestions, fileGetNewsPath);
			if (forumList.size() != 0) {
				if (!getForumList(forumList, tableName, yesterdayTime)) {
					break;
				}
			} else {
				page--;
				time--;
				if (time == 0) {
					break;
				}
				continue;
			}

			if (page % 20 == 0) {
				Thread.sleep(2 * 60 * 1000);// 睡眠1分钟，防止403
			} else if (page % 199 == 0) {
				Thread.sleep(10 * 60 * 1000);// 睡眠5分钟，防止403
			} else {
				Thread.sleep(3 * 1000);// 睡眠2秒，防止403
			}
		}
	}

	/**
	 * 通过正则表达式得到csdn论坛URL中所有想要的信息
	 * 
	 * @param regex
	 *            信息的正则表达式， eg：String regex = "[\\s]*([\\S]+)[\\s]*</div>";
	 * @param fileFromPath
	 *            临时存放包含具体信息的html文件路径，eg："C:/spider/htmlUrl.txt"
	 * @author mengzhimin
	 * @throws Exception
	 */
	@SuppressWarnings("resource")
	public static List<ArrayList<String>> getListFromRegex(String regex,
			String fileFromPath) {
		BufferedReader br;
		List<ArrayList<String>> forumList = new ArrayList<ArrayList<String>>();
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
				List<String> list = null;
				while ((line = br.readLine()) != null) {
					// 匹配正则表达式
					Matcher matcher = pattern.matcher(line);
					while (matcher.find()) {
						list = new ArrayList<String>();
						// 输出匹配内容
						for (int i = 1; i <= 15; i++) {
							list.add(matcher.group(i));
						}
						forumList.add((ArrayList<String>) list);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return forumList;
	}

	/**
	 * 处理URL关键词(eg:/forums/Windows)，提取数据库表中想要的信息(windows),将信息添加到数据库
	 * 
	 * @param list
	 *            存放所得信息的集合
	 * @param tableName
	 *            数据库的表名
	 * @param yesterdayTime
	 *            数据库最新的时间（一般是前一晚的最后更新时间）
	 * @return true:继续，false：停止
	 * @author mengzhimin
	 */
	public static boolean getForumList(List<ArrayList<String>> list,
			String tableName, String yesterdayTime) {
		if (!Spider.findTable(tableName)) {
			makeForumTable(tableName);
		}
		for (ArrayList<String> arrayList : list) {
			try {
				if (!putDataBase(arrayList, tableName, yesterdayTime)) {
					return false;
				} else {
					IOUtil.writeTextFile("C:/spider/Forum/" + arrayList.get(4)
							+ ".txt", arrayList.toString(), true);
				}
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	/**
	 * 将得到的信息存放到数据库中，数据库中有数据即为更新到最新日期，没有数据就是得到所有模块中的数据
	 * 
	 * @param dataArrayList
	 *            存放数据的list集合
	 * @param tableName
	 *            要存放数据的表名，eg：forum_windows
	 * @param yesterdayTime
	 *            数据库中最新的更新数据日期（一般到昨天）
	 * @return true：继续存放，false：结束存放
	 * @throws ParseException
	 */
	public static boolean putDataBase(ArrayList<String> dataArrayList,
			String tableName, String yesterdayTime) throws ParseException {
		LogUtil.log("方法putDataBase");
		LogUtil.log("使用数据库：" + tableName);

		/*if (yesterdayTime != null && !"".equals(yesterdayTime)) {
			if (dataArrayList.get(14).length() == "yyyy-MM-dd HH:mm".length()) {
				Date now = new SimpleDateFormat("yyyy-MM-dd HH:mm")
						.parse(dataArrayList.get(14));
				Date yesterday = new SimpleDateFormat("yyyy-MM-dd HH:mm")
						.parse(yesterdayTime);
				long nowMS = now.getTime();
				long yesterdayMS = yesterday.getTime();
				// 更新数据到数据库以前的最新日期
				if (nowMS <= yesterdayMS) {
					return false;
				}
			}
		}*/

		// 1 Status 2 Link 3 Title 4 TypeLink 5 Type 6 Field 7 Score 8
		// AskMenLink 9 AskMen 10 AskTime 11 ReplyNum 12 LastUpdateManLink 13
		// LastUpdateMan 14 LastUpdateTime 15 AddTime
		String sql = "INSERT INTO  "
				+ tableName
				+ "(ID,Status,Link,Title,TypeLink,Type,FieldLink,Field,Score,AskMenLink,AskMen,AskTime,ReplyNum,"
				+ "LastUpdateManLink,LastUpdateMan,LastUpdateTime,AddTime) VALUES(null,'"
				+ dataArrayList.get(0) + "','" + urlBase + dataArrayList.get(1)
				+ "','" + dataArrayList.get(2) + "','" + urlBase
				+ dataArrayList.get(3) + "','" + dataArrayList.get(4) + "','"
				+ urlBase + dataArrayList.get(5) + "','" + dataArrayList.get(6)
				+ "'," + dataArrayList.get(7) + ",'" + dataArrayList.get(8)
				+ "','" + dataArrayList.get(9) + "','" + dataArrayList.get(10)
				+ "'," + dataArrayList.get(11) + ",'" + dataArrayList.get(12)
				+ "','" + dataArrayList.get(13) + "','" + dataArrayList.get(14)
				+ "' ,now())";
		int n = 0;
		try {
			n = DBUtil.getInstance().execute(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return n > 0;
	}

	/**
	 * 创建数据库表
	 * 
	 * @param tableName
	 *            数据库表名,eg :forum_windows
	 * @author mengzhimin
	 */
	public static void makeForumTable(String tableName) {
		LogUtil.log("方法makeForumTable");
		String sql = "create table "
				+ tableName
				+ "(ID	int PRIMARY KEY auto_increment,Title varchar(1024),Link varchar(1024),Type varchar(255),TypeLink varchar(1024),Field varchar(255),FieldLink varchar(255),Score int,AskMen varchar(255),AskMenLink varchar(1024),AskTime varchar(255),ReplyNum int,LastUpdateMan varchar(255),LastUpdateManLink varchar(1024),LastUpdateTime varchar(255),Status varchar(255),AddTime varchar(255))";
		try {
			DBUtil.getInstance().execute(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 论坛：根据日期更新数据，数据库中的最新时间（LastUpdateTime）到现在时间
	 * 
	 * @param tableName
	 *            极客头条数据库，eg：forum_windows
	 * @return
	 */
	public static String dealYesterday(String tableName) {
		LogUtil.log("方法dealYesterday");
		String yesterdayTime = null;
		try {
			if (Spider.findTable(tableName) == false) {
				makeForumTable(tableName);
			}
			String sqlSelect = "SELECT LastUpdateTime FROM " + tableName
					+ " ORDER BY LastUpdateTime DESC ";
			yesterdayTime = DBUtil.getInstance().queryReturnSingleString(
					sqlSelect);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return yesterdayTime;
	}

}
