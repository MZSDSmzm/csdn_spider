package com.mzsds.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * 数据表对象
 */
public class DataTable {
	
	//行记录
	private ArrayList<Object[]> rows = null;
	
	//列记录
	private ArrayList<String> columns = null;
	
	
	/**
	 * 没有参数的构造器，默认构造一个一列的DataTable。
	 * eg: DataTable dt=new DataTable();
	 */
	protected DataTable(){
		columns = new ArrayList<String>(1);
		columns.add("Column1");
		rows = new ArrayList<Object[]>();
	}
	
	
	
	/**
	 * 通过定义列数来构造一个空的DataTable。
	 * eg: DataTable dt = new DataTable(3);
	 * 
	 * @param columnCount 列数
	 */
	public DataTable(int columnCount){
		columns = new ArrayList<String>(columnCount);
		for(int i=0; i<columnCount; i++){
			columns.add("Column"+(i+1));
		}
		rows = new ArrayList<Object[]>();
	}
	
	
	
	/**
	 * 通过列名数组 和 数据数组快速构造DataTable
	 * eg: DataTable dt  = new DataTable(new String[]{"id", "name", "age"}, new Object[][]{{"1", "张三", "19"}, {"1", "李四", "26"}});
	 * 
	 * @param columnNames 列名
	 * @param rowDataArr	数据
	 */
	public DataTable(String[] columnNames, Object[][] rowDataArr){
		if(columnNames!=null && columnNames.length>0){
			columns = new ArrayList<String>(columnNames.length);
			for(int i=0; i<columnNames.length; i++){
				columns.add(columnNames[i]);
			}
		}
		rows = new ArrayList<Object[]>();
		if(rowDataArr!=null){
			for(int r=0; r<rowDataArr.length; r++){
				this.addRow(rowDataArr[r]);
			}
		}
	}
	
	
	/**
	 * 用实体类列表来构造DataTable。用类的属性名做列名，用字段的值做DataTable的值。
	 * eg: 	List<com.hbbc.domain.ACLUserDO> list = (List<com.hbbc.domain.ACLUserDO>)DBUtil.getInstance().query("select * from acl_user").getDOList(com.hbbc.domain.ACLUserDO.class, true);
	 *			DataTable dt = new DataTable(list); 
	 *	
	 * @param list 实体对象的List列表
	 * @throws Exception 
	 */
	public DataTable(List<?> list) throws Exception{
		//反射获取对象属性作为列名
		String[] colArr = null;
		{
		     Class<?> ownerClass = list.get(0).getClass();  
		     Field[] fields = ownerClass.getFields();
		     colArr = new String[fields.length];
		     for(int c=0; c<fields.length; c++){
		    	 colArr[c] = fields[c].getName();
		     }
		}
		//反射获取对象属性值作为DataTable数据
	    Object[][] dataArr = new String[list.size()][colArr.length];
		for(int r=0; r<list.size(); r++){
			Object curObj = list.get(r);
			Class<?> ownerClass = curObj.getClass();  
		     Field[] fields = ownerClass.getFields();
		     for(int c=0; c<fields.length; c++){
		    	 String value = "";
		    	 if(curObj!=null) {
		    		 if(fields[c].get(curObj)!=null) value = fields[c].get(curObj).toString();
		    	 }
		    	 dataArr[r][c] = value;
		     }
		}
		//开始构造
		if(colArr!=null && colArr.length>0){
			columns = new ArrayList<String>(colArr.length);
			for(int i=0; i<colArr.length; i++){
				columns.add(colArr[i]);
			}
		}
		rows = new ArrayList<Object[]>();
		if(dataArr!=null){
			for(int r=0; r<dataArr.length; r++){
				this.addRow(dataArr[r]);
			}
		}
	}
	
	
	
	/**
	 * 得到表的数据行数（具体的行数，非下标，从1开始）
	 * eg: System.out.println((new DataTable()).getRowsCount());
	 * 
	 * @return int 行数
	 */
	public int getRowsCount(){
		return rows.size();
	}
	
	
	
	/**
	 * 得到表的数据列数（列数，非下标，从1开始）
	 * eg: System.out.println((new DataTable()).getColumnsCount());
	 * @return int 列数
	 */
	public int getColumnsCount(){
		return columns.size();
	}
	
	
	
	/**
	 * 查看本DataTable是否为空（通过检查DataTable 的列和行的长度是否为0进行判断）
	 * eg: System.out.println((new DataTable()).isNull());
	 * 
	 * @return 返回True表示为空，返回False表示非空。
	 */
	public boolean isNull(){
		if(columns==null || columns.size()<=0) return true;
		if(rows==null || rows.size()<=0) return true;
		return false;
	}
	
	
	
	/**
	 * 在DataTable的最后追加行数据。数据量大时，尽量用addRow，而非insertRow。前者性能要高2个数量级。
	 * eg: (new DataTable(3)).addRow("12", "weihua", "男");
	 * 
	 * @param values 行数据
	 */
	public void addRow(Object... values){
		if(values.length==columns.size()){
			//若插入的行数据跟列数一致，则直接赋值
			this.rows.add(values);
		}else{
			//若行数据跟列数不一致，截取超出的列，补齐不足的列
			Object[] arr= new Object[columns.size()];
			for(int i=0; i<columns.size(); i++){
				if(i<values.length){
					arr[i] = values[i];
				}else{
					arr[i]="";
				}
			}
			this.rows.add(arr);
		}
	}
	
	
	
	/**
	 * 在DataTable的数据行中插入数据。插入位置为index参数所指定下标的数据的前面，比如：下标0，则表示插入在最前面。
	 * 数据量大时，尽量用addRow，而非insertRow。前者性能要高2个数量级。
	 * eg: dt.insertColumn("中文", 2, "男");
	 * 
	 * @param index 插入数据的下标，从0开始。但数值小于0，或者大于最大下标时，自动插入在最前面或最后面，并不报错。
	 * @param values 插入的行数据
	 */
	public void insertRow(int index, Object... values){
		if(index<0) index=0;
		if(index>rows.size()) index = rows.size();
		if(values.length==columns.size()){
			//若插入的行数据跟列数一致，则直接赋值
			this.rows.add(index, values);
		}else{
			//若行数据跟列数不一致，截取超出的列，补齐不足的列
			Object[] arr= new Object[columns.size()];
			for(int i=0; i<columns.size(); i++){
				if(i<values.length){
					arr[i] = values[i];
				}else{
					arr[i]="";
				}
			}
			this.rows.add(index, arr);
		}
	}
	
	
	
	/**
	 * 删除指定下标的行数据（注意是下标，而非行数！）
	 * eg: dt.deleteColumn(2);
	 * 
	 * @param index 预删除的行数据下标
	 */
	public void deleteRow(int index){
		if(index<0) index=0;
		if(index>rows.size()) index = rows.size();
		rows.remove(index);
	}
	
	
	
	/**
	 * 拷贝指定的行到指定的下标处。拷贝方式为插入，而非移动，旧数据并不删除。
	 * 方法有做参数检查和容错，超过下标，或取值异常，不会报错。
	 * eg: dt.copyRows(0, 3, 1);
	 * 
	 * @param fromRowIndex 从某个下标开始拷贝
	 * @param toRowIndex 目标数据行的下标
	 * @param rowsNum 拷贝的行数，最小1，最大所有行数。
	 */
	public void copyRows(int fromRowIndex, int toRowIndex, int rowsNum){
		//检查参数，容错
		if(fromRowIndex<0) fromRowIndex=0;
		if(fromRowIndex>rows.size()) fromRowIndex = rows.size()-1;
		if(toRowIndex<0) toRowIndex=0;
		if(toRowIndex>rows.size()) toRowIndex = rows.size();
		if(fromRowIndex+rowsNum>rows.size()) rowsNum = rows.size()-fromRowIndex;
		if(rowsNum<=1)rowsNum=1;
		//先把数据拷贝出来（逐项复制，浅复制）
		Object[][] cArr = new Object[rowsNum][columns.size()];
		for(int r=0; r<rowsNum; r++){
			for(int c=0; c<columns.size(); c++){
				cArr[r][c] = rows.get(r+fromRowIndex)[c];	
			}
		}
		//逐条插入到新的地方（插入的时候需要倒过来插，以保证顺序插入）
		for(int r=rowsNum-1; r>=0; r--){
			this.insertRow(toRowIndex, cArr[r]);
		}
	}
	
	
	
	/**
	 * 删除数据列。注意参数为列的下标，而非列数。遇到数据量大时，本方法的执行效率不高！
	 * eg: dt.deleteColumn(2);
	 * 
	 * @param columnIndex 预删除的列的下标
	 */
	public void deleteColumn(int columnIndex){
		//检查参数，容错
		if(columnIndex<0) columnIndex=0;
		if(columnIndex>this.columns.size()) columnIndex=this.columns.size();
		//更改列名称
		this.columns.remove(columnIndex);
		//调整数据，每行数据均删除一列
		for(int r=0; r<rows.size(); r++){
			int k=0;
			Object[] oldRowDataArr = rows.get(r);
			Object[] newRowDataArr = new Object[columns.size()];
			for(int c=0; c<newRowDataArr.length; c++){
				if(c==columnIndex) k++;	//若是要删除的列，跳过
				newRowDataArr[c] = oldRowDataArr[k];
				k++;
			}
			rows.set(r, newRowDataArr);
		}
	}
	
	
	
	/**
	 * 插入一列。该列的初始化数值，由 defaultValue 指定。
	 * eg: dt.insertColumn("性别", 2, "男");
	 * 
	 * @param newColumnName 新列的名称
	 * @param columnIndex 新列插入的位置，注意是：下标，而非列数
	 * @param defaultValue 新列的初始化数值
	 */
	public void insertColumn(String newColumnName, int columnIndex, Object defaultValue){
		//检查参数，容错
		if(columnIndex<0) columnIndex=0;
		if(columnIndex>this.columns.size()) columnIndex=this.columns.size();
		if(newColumnName==null)newColumnName="";
		//更改列名称
		this.columns.add(columnIndex, newColumnName);
		//调整数据，每行数据均加入一列！
		for(int r=0; r<rows.size(); r++){
			int k=0;
			Object[] oldRowDataArr = rows.get(r);
			Object[] newRowDataArr = new Object[columns.size()];
			for(int c=0; c<newRowDataArr.length; c++){
				if(c==columnIndex){
					newRowDataArr[c] = defaultValue;
				}else{
					newRowDataArr[c] = oldRowDataArr[k];
					k++;
				}
			}
			rows.set(r, newRowDataArr);
		}
	}
	
	
	/**
	 * 复制DataTable中某列的数据来插入新列。注意是复制而非移动，是插入而非追加。
	 * eg: dt.copyColumn("登录密码", 0, 2);
	 * 
	 * @param newColumnName
	 * @param fromColumnIndex
	 * @param toColumnIndex
	 */
	public void copyColumn(String newColumnName, int fromColumnIndex, int toColumnIndex){
		//检查参数，容错
		if(fromColumnIndex<0) fromColumnIndex=0;
		if(fromColumnIndex>this.columns.size()) fromColumnIndex=this.columns.size()-1;
		if(toColumnIndex<0) toColumnIndex=0;
		if(toColumnIndex>this.columns.size()) toColumnIndex=this.columns.size();
		if(newColumnName==null)newColumnName="";
		//更改列名称
		this.columns.add(toColumnIndex, newColumnName);
		//调整数据，每行数据均加入一列！
		for(int r=0; r<rows.size(); r++){
			int k=0;
			Object[] oldRowDataArr = rows.get(r);
			Object[] newRowDataArr = new Object[columns.size()];
			for(int c=0; c<newRowDataArr.length; c++){
				if(c==toColumnIndex){
					newRowDataArr[c] = oldRowDataArr[fromColumnIndex];
				}else{
					newRowDataArr[c] = oldRowDataArr[k];
					k++;
				}
			}
			rows.set(r, newRowDataArr);
		}
		
	}
	
	
	
	/**
	 * 通过列的名字查询列的下标。出错返回-1（没有找到，或者有相同名称的列）。
	 * 
	 * @param columnName 列名称
	 * @return 返回列的下标（以0开始）
	 */
	public int getColumnIndexByName(String columnName){
		return columns.indexOf(columnName);
	}
	
	
	
	/**
	 * 通过列的下标获得列的名称。
	 * eg: String cname = dt.getColumnNameByIndex(0);
	 * 
	 * @param columnIndex 列的下标（从0开始）
	 * @return 返回列名称
	 */
	public String getColumnNameByIndex(int columnIndex){
		return columns.get(columnIndex);
	}
	
	
	
	/**
	 * 通过列的下标设置列的名称。
	 * dt.setColumnNameByIndex(0, "编号");
	 * 
	 * @param columnIndex 预设置的列的下标
	 * @param columnName 列的名称
	 */
	public void setColumnNameByIndex(int columnIndex, String columnName){
		columns.set(columnIndex, columnName);
	}
	
	
	/**
	 * 得到某行数据，以对象数组方式返回。
	 * Object[] rowData = dt.getCells(1);
	 * 
	 * @param rowIndex 行的下标
	 * @return 对象数组
	 */
	public Object[] getCells(int rowIndex){
		return rows.get(rowIndex);
	}
	
	
	
	/**
	 * 获得某单元格的数据对象
	 * System.out.println(dt.getCell(1, "编号"));
	 * 
	 * @param rowIndex 单元格的行下标
	 * @param columnName 列名
	 * @return 返回数据对象
	 */
	public Object getCell(int rowIndex, String columnName){
		int columnIndex = columns.indexOf(columnName);
		return rows.get(rowIndex)[columnIndex];
	}
	
	
	
	/**
	 * 获得某单元格的数据对象
	 * System.out.println(dt.getCell(0, 0));
	 * 
	 * @param rowIndex 单元格的行下标
	 * @param columnIndex 列下标
	 * @return 返回数据对象
	 */
	public Object getCell(int rowIndex, int columnIndex){
		return rows.get(rowIndex)[columnIndex];
	}
	
	
	/**
	 * 设置单元格的数值。若没有此数据，会抛出异常。
	 * eg: dt.setCell(1, "age", 12);
	 * 
	 * @param rowIndex 行下标
	 * @param columnName 列名
	 * @param valueObj 预设置的数值
	 */
	public void setCell(int rowIndex, String columnName, Object valueObj){
		int columnIndex = columns.indexOf(columnName);
		rows.get(rowIndex)[columnIndex] = valueObj;
	}
	
	
	
	/**
	 * 设置单元格的数值。若没有此数据，会抛出异常。
	 * eg: dt.setCell(1, 1, 12);
	 * 
	 * @param rowIndex 行下标
	 * @param columnIndex 列下标
	 * @param valueObj 预设置的数值
	 */
	public void setCell(int rowIndex, int columnIndex, Object valueObj){
		rows.get(rowIndex)[columnIndex] = valueObj;
	}
	
	
	
	/**
	 * 从DataTable提取某列数据出来
	 * Object[] arr = new DataTable(new String[]{"id", "name", "age"}, new Object[][]{{"1", "张三", "19"}, {"1", "李四", "26"}}).pickColumnDataArray("name");
	 *
	 * @param columnName
	 * @return
	 */
	public Object[] pickColumnDataArray(String columnName){
		Object[] rArr = new Object[rows.size()];
		for(int r=0; r<rArr.length; r++){
			rArr[r] = this.getCell(r, columnName);
		}
		return rArr;
	}
	
	
	
	/**
	 * 提取当前DataTable的某些行组成新的DataTable（浅复制），本方法做了容错处理，错误的参数不会报错。
	 * eg: System.out.println((new DataTable(new String[]{"id", "name", "age"}, new Object[][]{{"1", "张三", "19"}, {"2", "李四", "26"}, {"3", "李四2", "26"}})).pickChildDataTable(1, 2).serialize("\r\n", "", false));
	 * 
	 * @param rowIndex 开始复制的行下标
	 * @param rowNum 要复制的行数
	 * @return 新的DataTable
	 */
	public DataTable pickChildDataTable(int rowIndex, int rowNum){
		String[] columnArr =  (String[])this.columns.toArray(new String[0]); 	
		DataTable newDT = new DataTable(columnArr, null);
		for(int i=0; i<rowNum; i++){
			if(rowIndex+i<0 || rowIndex+i>=this.rows.size()) continue;		//若要复制的数据下标超过正常范围，返回
			Object[] oriRow = this.rows.get(rowIndex+i);
			Object[] newRow = new Object[oriRow.length];
			for(int c=0; c<newRow.length; c++){
				newRow[c] = oriRow[c];
			}
			newDT.addRow(newRow);
		}
		return newDT;
	}
	
	
	
	/**
	 * 检索DataTable，锁定某列为特定值的数据行，返回数据行中指定列的数据。常用于类似，通过“编号”获取“用户名”之类的用法。
	 * 需要注意的是，此方法只检索并返回第一条记录！ 没有找到返回NULL。
	 * eg: System.out.println((new DataTable(new String[]{"id", "name", "age"}, new Object[][]{{"1", "张三", "19"}, {"2", "李四", "26"}, {"3", "李四2", "26"}})).findValue("id", "2", "name"));
	 * 
	 * @param condColName 过滤条件的列名
	 * @param condColValue 过滤列的取值
	 * @param targetColName 找到确定的行后，返回那列的数据值
	 * @return
	 */
	public Object findValue(String condColName, String condColValue, String targetColName){
		for(int i=0; i<rows.size(); i++){
			Object valueObj = this.getCell(i, condColName);
			if(valueObj!=null && valueObj.toString().equals(condColValue)){
				return this.getCell(i, targetColName);
			}
		}
		return null;
	}
	
	
	/**
	 * 按过滤条件检索DataTable。将符合结果的行组成新的DataTable并返回。
	 * eg: System.out.println((new DataTable(new String[]{"id", "name", "age"}, new Object[][]{{"1", "张三", "19"}, {"2", "李四", "26"}, {"2", "王五", "21"}})).findValueDT("id", "2").findValueDT("name", "王五").serialize(";", ",", false));
		
	 * @param condColName 查询条件列名
	 * @param condColValue 查询条件列的取值
	 * @return DataTable
	 */
	public DataTable findValueDT(String condColName, String condColValue){
		String[] columnArr =  (String[])this.columns.toArray(new String[0]); 	
		DataTable newDT = new DataTable(columnArr, null);
		for(int i=0; i<rows.size(); i++){
			Object valueObj = this.getCell(i, condColName);
			if(valueObj!=null && valueObj.toString().equals(condColValue)){
				Object[] oriRow = this.rows.get(i);
				Object[] newRow = new Object[oriRow.length];
				for(int c=0; c<newRow.length; c++){
					newRow[c] = oriRow[c];
				}
				newDT.addRow(newRow);
			}
		}
		return newDT;
	}	
	
	
	
	/**
	 * 将所有DataTable数据逐行转换成DAO实体类。数组方式返回。
	 * 自动赋值时忽略大小写的名称进行匹配，自动赋值到指定的实体类中。
	 * eg:
	 * DataTable dt = (new DBUtil(null)).query("SELECT  * FROM acl_user");
	 * Object[] arr = dt.getDOs(LoginDO.class, false);
	 * for(int k=0; k<arr.length; k++){
	 * 		LoginDO loginObj = (LoginDO)arr[k]; 
	 * 		System.out.print("\r\n==="+loginObj.userAccount);
	 * }
	 * 
	 * @param classObj classObj 实体类的类别，一般通过.class或.getClass()方式获得
	 * @param nullSafeFlag nullSafeFlag 但返回数据为空时，是否做赋予安全值的操作，True表示操作，False表示原样返回null
	 * @return 返回实体对象实例数组Object[]
	 * @throws Exception
	 */
	public Object[] getDOs(Class<?> classObj, Boolean nullSafeFlag) throws Exception{
		Object[] arr = new Object[rows.size()];
		for(int i=0; i<rows.size(); i++){
			Object newObject = null;
			if(i==0) {
				//第一行打印Debug日志
				LogUtil.debug("[DataTable] 方法 getDAOs 一共需创建 '"+rows.size()+"' 个对象（条记录），为更好的可读性，仅打印第一个对象的创建日志，如下：");
				newObject = getDO(i, classObj, nullSafeFlag, true) ;	
			}else{
				//其他行不打印Debug日志信息
				newObject = getDO(i, classObj, nullSafeFlag, false) ;
			}
			arr[i] = newObject;
		}
		return arr;
	}

	
	
	/**
	 * 将所有DataTable数据逐行转换成DAO实体类。ArrayList方式返回。
	 * 自动赋值时忽略大小写的名称进行匹配，自动赋值到指定的实体类中。
	 * eg:
	 *	DataTable dt = (new DBUtil(null)).query("SELECT * FROM acl_user");
	 *	ArrayList arr = dt.getDOArrayList(com.hbbc.sys.ACLUserBO.class, true);
	 *	com.hbbc.sys.ACLUserBO firstUser = (com.hbbc.sys.ACLUserBO)arr.get(0);
	 *	System.out.println("firstUser name = "+firstUser.userName);
	 * 
	 * @param classObj classObj 实体类的类别，一般通过.class或.getClass()方式获得
	 * @param nullSafeFlag nullSafeFlag 但返回数据为空时，是否做赋予安全值的操作，True表示操作，False表示原样返回null
	 * @return 返回实体对象实例ArrayList。
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<Object> getDOArrayList(Class<?> classObj, Boolean nullSafeFlag) throws Exception{
		return (ArrayList<Object>)getDOList(classObj, nullSafeFlag);
	}
	
	
	/**
	 * 将所有DataTable数据逐行转换成DAO实体类。List方式返回。
	 * 自动赋值时忽略大小写的名称进行匹配，自动赋值到指定的实体类中。
	 * eg: List<ACLUserBO> userList = (List<ACLUserBO>)((new DBUtil(null)).query(sql).getDOList(ACLUserBO.class, false));
	 * 
	 * @param classObj classObj 实体类的类别，一般通过.class或.getClass()方式获得
	 * @param nullSafeFlag nullSafeFlag 但返回数据为空时，是否做赋予安全值的操作，True表示操作，False表示原样返回null
	 * @return 返回实体对象实例ArrayList。
	 * @throws Exception
	 */
	public List<?> getDOList(Class<?> classObj, Boolean nullSafeFlag) throws Exception{
		ArrayList<Object> rArr = new ArrayList<Object>();
		for(int i=0; i<rows.size(); i++){
			Object newObject = null;
			if(i==0) {
				//第一行打印Debug日志
				LogUtil.debug("[DataTable] 方法 getDOArrayList 一共需创建 '"+rows.size()+"' 个对象（条记录），为更好的可读性，仅打印第一个对象的创建日志，如下：");
				newObject = getDO(i, classObj, nullSafeFlag, true) ;	
			}else{
				//其他行不打印Debug日志信息
				newObject = getDO(i, classObj, nullSafeFlag, false) ;
			}
			rArr.add(newObject);
		}
		return rArr;
	}
	
	
	/**
	 * 从DataTable中抽取指定行的数据，根据忽略大小写的名称进行匹配，自动赋值到指定的实体类中，获得实体类的实例。
	 * 需要特别说明的是第三个参数nullSafeFlag，用于指定对数据库中返回NULL对象是否做自动赋值操作。若选择Ture，将对
	 * 字符型变量赋值“”，对数值型变量赋值 0，对日期型变量赋值“1900-1-1”。
	 * eg: DataTable dt = (new DBUtil(null)).query("SELECT  * FROM acl_user ");
	 *       LoginDO obj = (LoginDO)dt.getDAO(0, LoginDO.class, true);
	 * 
	 * @param rowIndex 行数据的下标
	 * @param classObj 实体类的类别，一般通过.class或.getClass()方式获得
	 * @param nullSafeFlag 但返回数据为空时，是否做赋予安全值的操作，True表示操作，False表示原样返回null
	 * @return 返回实体对象实例
	 * @throws Exception
	 */
	public Object getDO(int rowIndex, Class<?> classObj, Boolean nullSafeFlag) throws Exception{
		return getDO(rowIndex, classObj, nullSafeFlag,  true);
	}
	
	
	
	/**
	 * 从DataTable中抽取指定行的数据，根据忽略大小写的名称进行匹配，自动赋值到指定的实体类中，获得实体类的实例。
	 * 最后一个参数：debugFlag ， 控制是否打印Debug日志信息，以避免产生过量的Debug日志信息。
	 */
	private Object getDO(int rowIndex, Class<?> classObj, Boolean nullSafeFlag, Boolean debugFlag) throws Exception{
		//若DataTable数据为空，直接返回NULL
		if(rows.size()<=0 || rowIndex>=rows.size()){
			LogUtil.debug("[DataTable] 发现DataTable数据为空 或者 所有指定的行没有数据，直接返回 null ！");
			return null;
		}
		//新创对象实例
		Object newObj = ReflectUtil.instanceClass(classObj.getName(), null);
		String className = classObj.getSimpleName();
		if(debugFlag) LogUtil.debug("[DataTable] 方法 getDO 创建数据 DO 对象 "+className+" : "+newObj.toString()+"。开始匹配数据。");
		//复制出一份用于搜索的列名和一份用于记录列是否匹配使用的列名（实现忽略大小写！）
		ArrayList<String> columnsFinder = new ArrayList<String>(columns.size());
		LinkedList<String> columnsUnused = new LinkedList<String>();
		for(int k=0; k<columns.size(); k++){  
			columnsFinder.add((columns.get(k).toString()).toLowerCase()); 
			columnsUnused.add(columns.get(k).toString()); 
		}
		
		
		//循环该对象的所有属性，逐个匹配复制
	    Field[] fieldArr = classObj.getFields();
	    for(int i=0; i<fieldArr.length; i++){
	    	String fieldName = fieldArr[i].getName();
	    	try{
		    	//自动匹配相同名字的数值
				int columnIndex = columnsFinder.indexOf(fieldName.toLowerCase());
				if(columnIndex>=0){
					//在DataTable的行中找到对应名称的列
					Object fieldValue = rows.get(rowIndex)[columnIndex];
				    Field field = classObj.getField(fieldName); 
				    columnsUnused.set(columnIndex, ""); 		//标志本字段已被使用		
				    if(fieldValue!=null){
				    	//若从JDBC中传过来的是string, float, int, double, date ....这些基础类型，直接赋值，否则，尝试转换成字符串后再赋值
				    	if(fieldValue.getClass()==String.class || fieldValue.getClass()==Integer.class  || fieldValue.getClass()==Long.class  || fieldValue.getClass()==Double.class || fieldValue.getClass()==Float.class  || fieldValue.getClass()==Boolean.class || fieldValue.getClass()==java.sql.Date.class || fieldValue.getClass()==java.sql.Timestamp.class){ 
				    		field.set(newObj,  fieldValue); 
				    	}else{
				    		//尝试转换成字符串后再进行转换赋值
				    		LogUtil.debug("[DataTable] "+fieldName+"="+fieldValue+" ("+fieldValue.getClass().toString()+")  进行转换复制。");
				    		String fieldValueStr = fieldValue.toString();
				    		if(fieldArr[i].getType()==String.class)																					fieldValue=fieldValueStr;
				    		else if(fieldArr[i].getType()==Integer.class || fieldArr[i].getType().toString().equals("int")) 			fieldValue = Integer.parseInt(fieldValueStr);
				    		else if(fieldArr[i].getType()==Long.class  || fieldArr[i].getType().toString().equals("long")) 			fieldValue = Long.parseLong(fieldValueStr);
				    		else if(fieldArr[i].getType()==Double.class  || fieldArr[i].getType().toString().equals("double")) 	fieldValue = Double.parseDouble(fieldValueStr);
				    		else if(fieldArr[i].getType()==Float.class  || fieldArr[i].getType().toString().equals("float")) 			fieldValue = Float.parseFloat(fieldValueStr);
				    		else if(fieldArr[i].getType()==Boolean.class  || fieldArr[i].getType().toString().equals("boolean")) fieldValue = Boolean.parseBoolean(fieldValueStr);
				    		else if(fieldArr[i].getType()==java.util.Date.class) 																	fieldValue = DataUtil.parseDate(fieldValueStr);
				    		else if(fieldArr[i].getType()==java.sql.Timestamp.class) 															fieldValue = new java.sql.Timestamp(DataUtil.parseDate(fieldValueStr).getTime());
				    		field.set(newObj,  fieldValue);
				    	}
				    	if(debugFlag) LogUtil.debug("[DataTable] "+className+"."+fieldName+"   <==  "+fieldValue.toString()+"    ("+columns.get(columnIndex)+")");
				    }else{
				    	if(nullSafeFlag){
				    		//若需要做数据安全操作的，自动赋予初值
				    		if(fieldArr[i].getType()==String.class) 																						fieldValue="";
				    		else if(fieldArr[i].getType()==Integer.class || fieldArr[i].getType().toString().equals("int")) 				fieldValue = 0;
				    		else if(fieldArr[i].getType()==Long.class  || fieldArr[i].getType().toString().equals("long")) 				fieldValue = 0L;
				    		else if(fieldArr[i].getType()==Double.class  || fieldArr[i].getType().toString().equals("double")) 		fieldValue = 0.0D;
				    		else if(fieldArr[i].getType()==Float.class  || fieldArr[i].getType().toString().equals("float")) 				fieldValue = 0.0F;
				    		else if(fieldArr[i].getType()==Boolean.class  || fieldArr[i].getType().toString().equals("boolean")) 	fieldValue = false;
				    		else if(fieldArr[i].getType()==java.util.Date.class) 																		fieldValue = DataUtil.parseDate("1900-1-1");
				    		else if(fieldArr[i].getType()==java.sql.Timestamp.class) 																fieldValue = new java.sql.Timestamp(DataUtil.parseDate("1900-1-1").getTime());
				    		else fieldValue = "";
				    		field.set(newObj,  fieldValue);
				    		if(debugFlag) LogUtil.debug("[DataTable] "+className+"."+fieldName+"   <==  [safevalue]["+fieldArr[i].getType().getSimpleName()+"] "+fieldValue+"    ("+columns.get(columnIndex)+")");     
				    	}else{
				    		//若不需要做数据安全操作的，直接返回NULL
				    		if(debugFlag) LogUtil.debug("[DataTable] "+className+"."+fieldName+"   <---  null");
				    	}
				    }
				}else{
					//在DataTable的行中没有找到对应名称的列
					if(debugFlag) LogUtil.debug("[DataTable] "+className+"."+fieldName+"   <---  ");
				}
	    	}catch(Exception e){
	    		LogUtil.exception(e);
	    	}
	    }
	    
	    
    	//将没有匹配上的数据库字段输出出来，以方便调试使用
	    if(LogUtil.isDebugEnabled()){
		    for(int d=0; d<columnsUnused.size(); d++){
		    	String colName = columnsUnused.get(d).toString();
		    	if(colName!=null && colName.equals("")==false){
			    	Object colValue = this.getCell(rowIndex, colName);
			    	String colValueStr = "null";
			    	if(colValue!=null) colValueStr = colValue.toString();
			    	if(debugFlag) LogUtil.debug("[DataTable] "+className+".         <---  "+ colValueStr +"    ("+colName+")");  
		    	}
		    }
	    }
	    
	    return newObj;
	}
	
	
	/**
	 * 将DataTable中的某行转换为JSONObject。
	 * 
	 * @param rowIndex 预转换成Json对象的行数据下标。注意不是行数，而是下标。
	 * @return JSONObject
	 * @throws Exception
	 */
	public JSONObject getJSONObject(int rowIndex) throws Exception{
		//若DataTable数据为空，直接返回NULL
		if(rows.size()<=0 || rowIndex>=rows.size()){
			LogUtil.debug("[DataTable] 发现DataTable数据为空 或者 所有指定的行没有数据，直接返回 null ！");
			return null;
		}
		//转换DataTable为JSONArray
		JSONObject curObj = new JSONObject();
		for(int c=0; c<columns.size(); c++){
			String colName = columns.get(c);
			Object colValue = getCell(rowIndex, c);
			curObj.put(colName, colValue);
		}
		return curObj;
	}
	
	
	
	/**
	 * 将DataTable所有行数据转换为JSONArray。
	 * 
	 *eg:	JSONObject rObj = new JSONObject();
	 *		rObj.put("Result", true);
	 *		rObj.put("Notice", "");
	 *		rObj.put("ProjectList", dt.getJSONArray());
	 *		String resStr = rObj.toString();
	 *		
	 * @return
	 * @throws Exception
	 */
	public JSONArray getJSONArray() throws Exception{
		//若DataTable数据为空，直接返回NULL
		if(rows.size()<=0 ){
			LogUtil.debug("[DataTable] 发现DataTable数据为空 或者 所有指定的行没有数据，直接返回 null ！");
			return null;
		}
		//转换DataTable为JSONArray
		JSONArray jarr = new JSONArray();
		for(int i=0; i<rows.size(); i++){
			JSONObject curObj = new JSONObject();
			for(int c=0; c<columns.size(); c++){
				String colName = columns.get(c);
				Object colValue = getCell(i, c);
				curObj.put(colName, colValue);
			}
			jarr.put(curObj);
		}
		return jarr;
	}
	
	
	/**
	 * 将DataTable 序列化成JSON字符串
	 * 
	 * eg:
	 * 	DataTable dt = DBUtil.getInstance(null).query("select * from acl_user");
	 *	LogUtil.debug(dt.serializeToJSONString());
	 */
	public String serializeToJSONString() throws Exception{
		
		JSONObject json = new JSONObject();
		//先处理column
		JSONArray cjsonObj = new JSONArray();
		for(int c=0; c<columns.size(); c++){
			String colName = columns.get(c);
			cjsonObj.put(colName);
		}
		json.put("Columns", cjsonObj);
		//若DataTable数据为空，直接返回NULL 
		if(rows.size()>0){
			json.put("Rows", this.getJSONArray());
		}
		return json.toString();
	}
	
	
	/**
	 * 将JSON字符串反序列化为 DataTable （原字符串需是DataTable序列化而来）
	 * 	eg:
	 * 	DataTable dt = DBUtil.getInstance(null).query("select * from acl_user");
	 *	String jsonString = dt.serializeToJSONString();
	 *	System.out.println(jsonString);
	 *	DataTable newDT = (new DataTable()).deserializeFromJSONString(jsonString);
	 *	System.out.println(newDT.serializeToJSONString());
	 *
	 * @param jsonString
	 * @return
	 * @throws Exception
	 */
	public DataTable deserializeFromJSONString(String jsonString) throws Exception{

		DataTable rDT = null;
		JSONObject obj = new JSONObject(jsonString);
		//整理列信息，构造DataTable
		JSONArray colArr = obj.getJSONArray("Columns");
		String[] carr = new String[colArr.length()];
		for(int c=0; c<colArr.length(); c++){
			carr[c] = colArr.getString(c);
		}
		rDT = new DataTable(carr, null);
		//整理行数据
		JSONArray rowArr = obj.getJSONArray("Rows");
		for(int r=0; r<rowArr.length(); r++){
			JSONObject curRowObj = rowArr.getJSONObject(r);
			Object[] cells = new Object[carr.length];
			for(int c=0; c<carr.length; c++){
				String curColName = carr[c];
				Object curColValue = null;
				if(curRowObj.isNull(curColName)==false){
					curColValue = curRowObj.get(curColName);
				}
				cells[c] = curColValue;
			}
			rDT.addRow(cells);
		}
		return rDT;
	}
	
	
	
	/**
	 * 序列化DataTable成字符串（自定义格式），以方便传输或展示。需要注意的是，NULL 会导出成空字符串 "" ！
	 * 需要注意的是：只有当第三个参数 withDataTableInfoFlag 为真时，序列化后的字符串才能被反序列化！
	 * 否则，只输出数据区域的序列化后的数据
	 * eg: System.out.print(dt.serialize("\r\n", ", ", true));
	 * 
	 * @param rowSeparator 行数据分隔符
	 * @param cloumnSeparator 列数据分割符
	 * @param withDataTableInfoFlag 是否包含DataTable的构造信息，包括参数和列名。
	 * @return 字符串。
	 */
	public String serialize(String rowSeparator, String cloumnSeparator, Boolean withDataTableInfoFlag){
		StringBuffer sb = new StringBuffer();
		if(withDataTableInfoFlag){
			//导出序列化的参数
			sb.append("{{{[[");
			sb.append("("+rowSeparator+")("+cloumnSeparator+")");
			sb.append("]]|[[");
			//导出表结构
			for(int c=0; c<columns.size(); c++){
				if(c>0) sb.append("]][[");
				sb.append(columns.get(c));
			}
			sb.append("]]}}}");
		}
		//导出表数据
		for(int r=0; r<rows.size(); r++){
			if(r>0) sb.append(rowSeparator);
			Object[] cells = rows.get(r);
			for(int c=0; c<cells.length; c++){
				if(c>0) sb.append(cloumnSeparator);
				if(cells[c]!=null) sb.append(cells[c].toString());
			}
			
		}
		return sb.toString();
	}
	
	
	
	/**
	 * 反序列化字符串成DataTable对象（自定义格式）
	 * eg:
	 * String sStr = (new DataTable(new String[]{"id", "name", "age"}, new Object[][]{{"1", "张三", "19"}, {"1", "李四", "26"}})).serialize(";", ",", false);
	 * DataTable newDT = (new DataTable()).deserialize(sStr);
	 *	System.out.print(newDT.serialize("|!|", "|@|", true)); 
	 * @param serializeString
	 * @return
	 */
	public DataTable deserialize(String serializeString){
		DataTable rObj = null;
		try{
			serializeString = serializeString.replace("{{{[[(", ""); 	
			String[] arra = DataUtil.splitString(serializeString, "]]}}}");
			String[] arrb = DataUtil.splitString( arra[0],  ")]]|[[");
			
			String[] params = DataUtil.splitString( arrb[0], ")(");
			String rowSeparator = params[0];
			String cloumnSeparator = params[1];
			
			String[] columnsArr = DataUtil.splitString( arrb[1], "]][[");
			String dataStr = arra[1];
			String[][] dataArr = DataUtil.splitStringTo2DArr(dataStr, rowSeparator, cloumnSeparator);
			
			rObj = new DataTable(columnsArr, dataArr);
			
		}catch(Exception e){
			LogUtil.exception(e.toString());
		}
		return rObj;
	}
	
}


