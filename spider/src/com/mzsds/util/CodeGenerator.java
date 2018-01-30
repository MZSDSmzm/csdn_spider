package com.mzsds.util;

import java.io.File;


/**
 * 代码生成工具
 */
public class CodeGenerator {

	
	/**
	 * 利用单表的数据表结构，自动参数Template1模板样式的代码，代码存储在 projectBasePath 为根的目录下（遇到已存在的文件，自动跳过，不进行覆盖！）
	 * 
	 * @param tablename 数据表名称，如：base_sn
	 * @param namespace 命名空间，如：base, acl, project, ...
	 * @param mainName 页面主名称，如：商品。 自动产生“商品详情”和 “商品管理” 等页面
	 * @param projectBasePath 项目的根路径
	 */
	public static void createTemplate1FilesOfSingleTable ( String tablename, String namespace, String mainName, String projectBasePath) {
		/**
		 算法思路：
		 1）首先解析 tablename 数据表，获得表的： 字段名称、字段类别、字段备注  信息。留存备用。
		 2）将Template1 的必要文件，做成样板，放在 /asset/template1/template/ 下面。应该包括4个文件 templatelist.jsp  templatespec.jsp  TemplateList.java  TemplateSpec.java
		      将里面的需要替换的变量，用 {{的特殊字符串进行替换}} ，如“{{Title}}”，
		 3）准备生成代码时，从Template1的四个文件逐个取出成字符串，先查找替换静态变量，如{{Title}}。再生成动态变量。
		 4）动态变量，即是跟数据库字段相关的部分。根据字段类型来生成相应的表单元素。此外，在列表和新建、编辑表单，均是对单表的所有字段进行生成！
		 5）将生成好的字符串存入对应的文件夹（改换成正确的文件名称），保存前，先判断文件是否存在，对于文件已经存在的，报错后跳过！
		 */
	}
	
	
	
	

	/**
	 * 生成MySQL当前数据库（所有表或者特定表）的数据库操作实体类代码。包括：DO 和 BO两类。生成的代码含有数据库的字段的说明作注释，并自动加上set get  方法
	 * eg: String code = CodeGenerator.createMySQLDBCode("acl_user", "com.mzsds.domain", "/Users/mzsds/Project/MyEclipseWS/sjkpi/src/com/mzsds/domain", true);			//Linux, MAC下生成acl_user的代码
	 * eg: String code = CodeGenerator.createMySQLDBCode(null, "com.mzsds.domain", "d:\\temp\\testcode", true);		//WIN下生成整个类的代码
	 * 
	 * @param dbTableName  数据表名。但此参数为NULL的时候，表示针对整个数据库的所有表生成对象实体类。
	 * @param basePackage 生成实体类所带的包名。
	 * @param saveFilePath 生成的实体类文件存放的文件夹位置。当此字段非空时，会自动将生成的代码存放在该文件夹下。否则，若此参数为空，则不进行存储。
	 * @param withBOFlag 除去生成DO层代码，是否生成BO层代码。一般BO继承自DO。BO中扩展用户自己的代码。所以此参数为True 时，会检查BO文件是否存在，若已存在，则不再创建。
	 * @return DO源代码。当生成整个数据库代码时候，返回值为“”
	 * @throws Exception
	 */
	public  static String createMySQLDBCode(String dbTableName, String basePackage, String saveFilePath, Boolean withBOFlag) throws Exception{
		
		//当没有给出具体的表名的时，遍历所有数据库，逐表进行代码生成
		if(dbTableName==null){
			StringBuffer sb = new StringBuffer();
			DataTable dt = DBUtil.getInstance().query("show tables;");
			if(dt.isNull()==false){
				for(int t = 0; t<dt.getRowsCount(); t++){
					String curTableName = dt.getCell(t, 0).toString();
					LogUtil.log(" ("+(t+1)+"/"+dt.getRowsCount()+") Create code of table '"+curTableName+"'");
					String curCodeStr = createMySQLDBCode(curTableName, basePackage, saveFilePath, withBOFlag);
					sb.append(curCodeStr);
				}
			}
			return "";		//针对整个数据库操作的，此处直接返回""，且不在执行下面的代码
		}
		
		//对特定表 dbTableName 生成代码 ================
		StringBuffer code = new StringBuffer();
		
		//获取目标数据表的所有列
		String sql = "SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT FROM INFORMATION_SCHEMA.COLUMNS WHERE table_schema = database() AND table_name = '"+dbTableName+"'"; 
		DataTable dt = DBUtil.getInstance().query(sql);
		if(dt.isNull()) return "";		// 若列数据为空，则返回错误
		
		// === 生成DO代码 ===
		//格式化类名。 acl_user ==> ACLUser ,  some ==> Some
		String formateClassName = "";
		if(dbTableName.indexOf("_")>0){
			String[] nArr = dbTableName.split("_");
			for(int n=0; n<nArr.length; n++){
				if(n==0) {
					formateClassName = nArr[n].toUpperCase();
				}else{
					formateClassName += nArr[n].substring(0, 1).toUpperCase()+nArr[n].substring(1);
				}
			}
		}else{
			formateClassName = dbTableName.substring(0, 1).toUpperCase()+dbTableName.substring(1);
		}
		// DO、BO 的类名
		String doClassName = formateClassName+"DO";
		String boClassName = formateClassName+"BO";
		
		// 生成 DO 代码的头部信息
		code.append("package com.mzsds.domain;  \r\n\r\n");
		code.append("public class "+doClassName +" {\r\n\r\n");
				
		// 生成 DO 代码的字段申明 和 get set 方法
		for(int c=0; c<dt.getRowsCount(); c++){
			String colName = dt.getCell(c, "COLUMN_NAME").toString();
			String colUpperName = colName.substring(0, 1).toUpperCase()+colName.substring(1);		//首字母大写
			String colLowerName = colName.substring(0, 1).toLowerCase()+colName.substring(1);		//首字母小写
			String colType = dt.getCell(c, "DATA_TYPE").toString();
			String colComment = dt.getCell(c, "COLUMN_COMMENT").toString();
			
			String tStr="String";
			String fColType="["+colType+"]";
			if("[int] [tinyint] [smallint] [mediumint] [integer] [bigint]".indexOf(fColType)>=0){
				tStr="int";
			}else if("[real] [double] [float] [decimal] [numeric]".indexOf(fColType)>=0){
				tStr = "double";
			}else if("[bit]".indexOf(fColType)>=0){
				tStr = "boolean";
			}else if("[date] [time] [datetime] [timestamp] [year]".indexOf(fColType)>=0){
				tStr = "java.util.Date";
			}
			//字段申明
			String cStr = colComment.replace('\r', ' ').replace('\n', ' ');	
			code.append("	/** "+cStr+" */ \r\n");										
			code.append("	public "+tStr+" "+colLowerName+";  \r\n\r\n");			
			//set方法
			code.append("	/** 设置，"+cStr+" */ \r\n");							
			code.append("	public void set"+colUpperName+"("+tStr+" o"+colUpperName+") { "+colLowerName+" = o"+colUpperName+"; } \r\n\r\n");		
			//get方法
			code.append("	/** 获取，"+cStr+" */ \r\n");					
			code.append("	public "+tStr+" get"+colUpperName+"() { return "+colLowerName+"; } \r\n");															
			//本字段处理结束，留出点儿空间，准备下一字段。
			code.append("	\r\n\r\n\r\n");		
		}
				
		//组织类结束的代码
		code.append("\r\n\r\n}\r\n");
		
		//准备输出DO代码
		String codeStr = code.toString();
		if(saveFilePath!=null && saveFilePath.equals("")==false){
			IOUtil.writeTextFile(saveFilePath+File.separator+doClassName+".java", codeStr, false);
			
			//若需要存储BO代码的
			if(withBOFlag){
				String boCode = "package com.mzsds.domain;  \r\n\r\n";
				boCode = boCode+ "public class "+boClassName +"  extends  "+doClassName+" {\r\n\r\n\r\n}\r\n";
				if((new File(saveFilePath+File.separator+boClassName+".java")).exists()==false){
					//若BO文件不存在，则创建，否则，跳过！
					IOUtil.writeTextFile(saveFilePath+File.separator+boClassName+".java", boCode, false);
				}
			}
		}
		return codeStr;
	}
	
	
}
