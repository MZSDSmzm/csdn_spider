package com.mzsds.util;

public class CreateData {

	public static void main(String[] args) throws Exception {
		//更新SDVR的WAP和BO项目的代码
		UpdateBOAndWAPDomain();
	}

	
	public static void UpdateBOAndWAPDomain() throws Exception{
		//mac版
		//String bo = CodeGenerator.createMySQLDBCode(null, "com.hbbc.domain", "/Users/wupeng/Desktop/xjkqbo/src/com/hbbc/domain", true);		
		//String wap = CodeGenerator.createMySQLDBCode("vportal_portal", "com.hbbc.domain", "D://phpStudy//WWW//sdvrbo//src//com//hbbc//domain", true);		
		
		//window版
		//String bo = CodeGenerator.createMySQLDBCode(null, "com.mzsds.domain", "D:\\Myeclipse\\workspace\\spider\\src\\com\\mzsds\\domain", true);		
		//String bo = CodeGenerator.createMySQLDBCode("vr_generateapp", "com.hbbc.domain", "C://Users//geek//Desktop//sdvrbo//src//com//hbbc//domain", true);
	}
	
}



