package com.mzsds.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import com.mchange.v2.c3p0.ComboPooledDataSource;


/**
 * 数据库连接工具类。完成数据库的连接，sql语句的执行，返回数据打包等一系列跟数据库JDBC方式访问相关的操作。
 * 包含多种用法：
 * 
 * 用法一：即用即关的方式
 * DataTable dt = DBUtil.getInstance().query(sql);
 * 
 * 
 * 用法二：批量执行若干SQL语句的方式
 * DBUtil dbcon = new DBUtil(null);
 * dbcon.openConnection();
 * ...
 * dbcon.query();
 * dbcon.execute();
 * dbcon.query();
 * ...
 * dbcon.closeConnection();
 * 
 * 
 * 用法三： 带事务的方式
 * DBUtil dbcon = new DBUtil(null);
 * dbcon.transBegin();
 * dbcon.query();
 * dbcon.execute();
 * dbcon.transCommit();
 * dbcon.transRollback();
 * 
 * 
 * 用法四： 直接通过数据库连接参数构造连接
 *  DataTable dt = (new DBUtil("sdvr81", "jdbc:mysql://192.168.1.81:3306/sdvr?useUnicode=true&characterEncoding=utf8|!|com.mysql.jdbc.Driver|!|root|!|geek123|!|false")).query("select * from acl_user");
 *
 */
public class DBUtil {
	
	//数据库连接池。支持指向多个数据库的多个连接池。
	private static java.util.Hashtable<String, ComboPooledDataSource> c3p0Pools = new java.util.Hashtable<String, ComboPooledDataSource>();
	
	//数据库的JDBC名称
	private String gJDBCName = null;
	
	//是否开启数据库连接池
	private boolean useC3P0PoolFlag = false;
	
	//当前数据库连接
	private Connection conn = null;
	
	//数据库连接参数（本参数非空，使用本参数，否则，使用jdbcname对应的配置参数）
	private static String gJDBCURLParams = null;
	
	/**
	 * 本类的SQL语句日志的记录级别。1 表示以INFO级别进行记录； 2 表示以DEBUG级别进行记录；  0 表示不记录日志。 默认值为1。
	 */
	public int SQLLogLevel = 1;
	
	
	
	/**
	 * 以静态调用方式快速获取数据库连接。参数为数据库连接名字。
	 * 参数取值为 null 时，取默认数据库连接为：Default。
	 * eg: DBUtil db = DBUtil.getInstance(null);
	 */
	public static DBUtil getInstance(String jdbcName) throws Exception {
		return new DBUtil(jdbcName);
	}
	
	
	/**
	 * 以静态调用方式快速获取默认数据库连接（默认数据库连接为：Default）
	 * eg: DBUtil db = DBUtil.getInstance();
	 */
	public static DBUtil getInstance() throws Exception {
		return new DBUtil(null);
	}
	
	
	/**
	 * 数据库连接构造函数。JDBC的配置信息配置在 main.properties 文件中。
	 * 默认数据库连接为：Default。支持多个数据库配置。
	 * eg: DataTable dt = (new DBUtil(null)).Query("SELECT  * FROM acl_user where UserID=9999 ");
	 * 
	 * @param jdbcName 连接名称
	 */
	public DBUtil(String jdbcName) throws Exception {
		gJDBCName = jdbcName;
	}
	
	
	
	/**
	 * 数据库连接构造函数。直接通过数据库连接参数，构造数据库连接。连接参数，由 URL+DriverClass+Account+Password+UseC3P0Pool 构成，中间以“|!|”分割。此种方式，不使用连接池。
	 * eg: DataTable dt = (new DBUtil("sdvr81", "jdbc:mysql://192.168.1.81:3306/sdvr?useUnicode=true&characterEncoding=utf8|!|com.mysql.jdbc.Driver|!|root|!|geek123|!|false")).query("select * from acl_user");
	 * 
	 * @param jdbcName 连接名称
	 * @param jdbcURLParams 连接参数
	 */
	public DBUtil(String jdbcName, String jdbcURLParams) throws Exception{
		gJDBCName = jdbcName;
		gJDBCURLParams = jdbcURLParams;
	}
	
	
	
	/**
	 * 开启数据库连接
	 */
	public Connection openConnection() throws Exception {
		
		// 1 获取数据库连接的配置参数
		String jdbcUrl = "";
		String driverClass = "";
		String userStr = "";
		String passwordStr = "";
		
		if(gJDBCURLParams==null){
			// 1.1 若没有带直接构造数据库连接的参数的
			
			//若JDBC的名称为空，设成Default
			if(gJDBCName==null || gJDBCName.equals("")) gJDBCName="Default";
			
			//查看配置项是否使用数据库连接池
			useC3P0PoolFlag = false;
			if(ConfigUtil.getProperty(null, "DBUtil."+gJDBCName+".UseC3P0Pool", true).trim().toUpperCase().equals("TRUE")) useC3P0PoolFlag = true;
			
			jdbcUrl = ConfigUtil.getProperty(null, "DBUtil."+gJDBCName+".JDBCUrl", true);
			driverClass = ConfigUtil.getProperty(null, "DBUtil."+gJDBCName+".DriverClass", true);
			userStr = ConfigUtil.getProperty(null, "DBUtil."+gJDBCName+".User", true);
			passwordStr = ConfigUtil.getProperty(null, "DBUtil."+gJDBCName+".Password", true);
			
		}else{
			
			// 1.2 若带了直接构造数据库连接的参数的
			
			String[] paramArr = DataUtil.splitString(gJDBCURLParams, "|!|");
			jdbcUrl = paramArr[0];
			driverClass = paramArr[1];
			userStr = paramArr[2];
			passwordStr = paramArr[3];
			if(paramArr[3].trim().toUpperCase().equals("TRUE")) useC3P0PoolFlag = true; else useC3P0PoolFlag=false;
		}
		
		// 2 开启数据库连接（区分是否使用连接池两种情况）
		
		//是否使用数据库连接池
		if(useC3P0PoolFlag){
			//从连接池缓存总获取连接池对象
			ComboPooledDataSource cpds = c3p0Pools.get(gJDBCName); 
			//初始化连接池
			if(cpds==null){
				cpds=new ComboPooledDataSource();   
		        cpds.setJdbcUrl(jdbcUrl);  
		        cpds.setDriverClass(driverClass);  
		        cpds.setUser(userStr);  
		        cpds.setPassword(passwordStr);  
		        cpds.setMaxPoolSize(20);  
		        cpds.setMinPoolSize(5);  
		        cpds.setAcquireIncrement(5);  
		        cpds.setInitialPoolSize(5);  
		        cpds.setMaxIdleTime(240);  
		        c3p0Pools.put(gJDBCName, cpds);		//将初始化后的连接池放入连接池缓存中
		        
			}
			//从连接池中获取当前连接
	        conn = cpds.getConnection();
	        
		} else {
			
			//若不使用连接池，则直接用JDBC初始化数据库连接
			Class.forName(driverClass);
			conn = DriverManager.getConnection(jdbcUrl, userStr, passwordStr);
		}
		return conn;
	}
	
	
	
	
	
	/**
	 * 关闭数据库连接
	 */
	public void closeConnection() throws Exception{  
		//若数据库连接非空，且没有使用数据库连接池的，才真正关闭JDBC数据库连接
		if(conn!=null && useC3P0PoolFlag==false){
			conn.close();
		}
		//关闭DBUtil的数据库连接
		conn = null;
	}
	
	
	/**
	 * 执行SQL语句，并返回DataTable
	 * eg: DataTable dt = (new DBUtil(null)).Query("SELECT  * FROM acl_user LIMIT 10");
	 * 
	 * @param sql 执行的SQL语句，查询语句
	 * @return DataTable
	 * @throws SQLException
	 */
	public DataTable  query(String sql) throws Exception {
		return this.query(sql, false);
	}
	
	
	
	/**
	 * 执行SQL语句，并返回不含有NULL的DataTable。
	 * 对于数据库查询为NULL的数据。若是字符型的，自动赋值“”；对于是数字型的自动赋值 0；
	 * 对于布尔型的，自动赋值 false； 对于日期型的，自动赋值 "1900-1-1"。
	 * eg: DataTable dt = DBUtil.getInstance().queryReturnNullSafeDT("select * from acl_user");
	 * 
	 * @param sql 执行的SQL语句，查询语句
	 * @return DataTable
	 * @throws Exception
	 */
	public DataTable queryReturnNullSafeDT(String sql) throws Exception{
		return this.query(sql, true);
	}
	
	
	/**
	 * 执行SQL语句，并返回DataTable
	 */
	private DataTable  query(String sql, boolean nullSafeFlag) throws Exception {

		DataTable rDT = null;
		
		//检测数据库连接，若没有开启，则本方法自动启开，并在方法末尾自动关闭
		boolean openConnHere = false;
		if(conn==null){
			this.openConnection();
			openConnHere = true;
		}
		//准备执行语句
		java.sql.Statement stmt = (java.sql.Statement) conn.createStatement();
		log("[DBUtil] 执行Query语句："+sql);
		ResultSet rs = stmt.executeQuery(sql);
		
		//获得返回数据的列名，构造DataTable
		ResultSetMetaData rsmd = rs.getMetaData();//rs的结构信息
		int colCount=rsmd.getColumnCount();//获取列数
		String[] colArr=new String[colCount];
		for(int c=0; c<colCount; c++) colArr[c]=rsmd.getColumnLabel(c+1); 		
		rDT = new DataTable(colArr, null);
		
		//获取返回数据并放入Datatable
		while(rs.next()){
			Object[] rowdata = new Object[colCount];
			for(int c=0; c<colCount; c++){
				Object curValue = rs.getObject(colArr[c]);
				if(curValue!=null){
					//对于非空对象，进行复制。对于null，不做处理让其保持为null ！
					rowdata[c] = curValue;
				}else{
					//若需要做返回数据的非空处理的
					if(nullSafeFlag){
						String colType = rsmd.getColumnClassName(c+1);//获取指定列的表目录名称
						switch(colType){
							case "java.lang.String":	rowdata[c] = "";			break;
							case "java.lang.Integer":	rowdata[c] = 0;			break;
							case "java.lang.Long":		rowdata[c] = 0L;			break;
							case "java.lang.Float":		rowdata[c] = 0.0F;		break;
							case "java.lang.Double":	rowdata[c] = 0.0D;		break;
							case "java.lang.Boolean":rowdata[c] = false;		break;
							case "java.util.Date":		rowdata[c] = DataUtil.parseDate("1900-1-1");		break;
						}
						if(rowdata[c]==null) rowdata[c] = "";
					}
				}
			}
			rDT.addRow(rowdata);
		}
		//若自动关闭连接开启，本方法结束的时候关闭连接
		if(openConnHere){
			this.closeConnection();
		}
		return rDT;
	}
	
	
	
	/**
	 * 执行SQL语句，并返回第一行第一列的数据。没有数据，或者SQL语句执行失败，均返回NULL。
	 * String usercount =  (new DBUtil(null)).QueryReturnSingleString("SELECT  COUNT(*) FROM acl_user");
	 * 
	 * @param sql 执行的SQL语句，查询语句
	 * @return String
	 * @throws Exception 
	 */
	public String  queryReturnSingleString(String sql) throws Exception  {
		DataTable dt = query(sql);
		if(dt.isNull()==false && dt.getCell(0, 0)!=null){
			return dt.getCell(0, 0).toString();
		}else{
			return null;
		}
	}
	
	
	
	/**
	 * 执行Execute类SQL语句，如：Insert, Update, Delete ...。执行完成返回受影响的条数。
	 * eg: int c = (new DBUtil(null)).execute("update acl_user set username = 'superman2' where UserID=711;");
	 * 
	 * @param sql 执行的SQL语句
	 * @return int 本SQL语句执行后受影响的记录条数 
	 * @throws SQLException
	 */
	public int  execute(String sql) throws Exception {
		
		//检测数据库连接，若没有开启，则本方法自动启开，并在方法末尾自动关闭
		boolean openConnHere = false;
		if(conn==null){
			this.openConnection();
			openConnHere = true;
		}
		
		//准备执行SQL语句
		java.sql.Statement stmt = (java.sql.Statement) conn.createStatement();
		log("[DBUtil] 执行Execute语句："+sql);
		int rValue = stmt.executeUpdate(sql);
		
		//若自动关闭连接开启，本方法结束的时候关闭连接
		if(openConnHere){
			this.closeConnection();
		}
		
		return rValue;
		
	}
	
	
	

	
	
	

	/**
	 * 执行插入类SQL语句，如：Insert。执行完成返回插入本条数据的ID。 
	 * eg: int c = (new DBUtil(null)).executeInsertReturnID("insert acl_user (username) values('superman2');");
	 * 
	 * @param sql 插入的SQL语句
	 * @return int 返回插入本条信息的主键ID
	 * @throws SQLException
	 */
	public int executeInsertReturnID(String sql) throws Exception {

		// 检测数据库连接，若没有开启，则本方法自动启开，并在方法末尾自动关闭
		boolean openConnHere = false;
		if (conn == null) {
			this.openConnection();
			openConnHere = true;
		}

		// 准备执行SQL语句
		java.sql.Statement stmt = (java.sql.Statement) conn.createStatement();
		log("[DBUtil] 执行Execute语句：" + sql);
		
		// 执行Sql语句并返回本条信息的主键ID
		stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		
		int rValue = -1;

		// 获取新插入记录id
		ResultSet results = stmt.getGeneratedKeys();
		if (results.next()) {
			rValue = results.getInt(1);
		}

		// 若自动关闭连接开启，本方法结束的时候关闭连接
		if (openConnHere) {
			this.closeConnection();
		}

		return rValue;
	}
	
	
	
	/**
	 * 开启事务。若数据库连接尚未开启，开启之。
	 * @throws Exception
	 */
	public void transBegin() throws Exception{
		if(conn==null) this.openConnection();
		conn.setAutoCommit(false);
	}
	
	
	
	
	/**
	 * 提交事务。提交事务后，自动关闭连接。
	 * @throws Exception
	 */
	public void transCommit() throws Exception{
		conn.commit();
		this.closeConnection();
	}
	
	
	
	
	/**
	 * 回滚事务。提交事务后，自动关闭连接。
	 * @throws Exception
	 */
	public void transRollback() throws Exception{
		conn.rollback();
		this.closeConnection();
	}
	
	
	/**
	 * 本类的日志记录工具
	 */
	private void log(String logStr){
		if(SQLLogLevel==1){
			LogUtil.log(logStr);
		}else if(SQLLogLevel==2){
			LogUtil.debug(logStr);
		}else{
			//为0或其他值，均不记录日志
		}
	}
	
	
	

	
	
	
}
