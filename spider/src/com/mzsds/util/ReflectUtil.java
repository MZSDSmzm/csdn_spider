package com.mzsds.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 反射工具类。动态调用各种类和方法。
 */
public class ReflectUtil {

	
	/**
	 * 反射创建类的实例，然后调用其方法。
	 * 先创建类的实例，然后调用指定的方法。方法调用返回值类别为Object。需执行转换。
	 * eg:	Object[] argsA = new Object[]{new String("hel"), new Integer(12)};
	 * 		Object resultStr = ReflectUtil.invokeMethod("com.hbbc.sys.Login", null, "doLogin", argsA);
	 * 
	 * @param className 类名（包名+类名），大小写敏感
	 * @param classArgs 实例化类时（构造函数），需要传入的参数。无参构造函数，传入null即可。
	 * @param methodName 调用的方法名称，大小写敏感
	 * @param methodArgs 调用方法传入的参数，没有的时候传入null。
	 * @return Object，返回调用方法返回的对象
	 * @throws Exception 不会处理异常，遇到异常会抛出
	 */
	@SuppressWarnings("rawtypes")
	public static Object invokeMethod(String className, Object[] classArgs, String methodName, Object[] methodArgs) throws Exception {  
		//实例化类
		Class<?> ownerClass = Class.forName(className); 
		Object instanceObj = instanceClass(className, classArgs);
		//准备方法调用的参数（各个参数的类别数组），然后调用
		Class[] margsArr = null;
		if(methodArgs!=null && methodArgs.length>0){
			margsArr = new Class[methodArgs.length];  
			for (int i = 0, j = methodArgs.length; i < j; i++) margsArr[i] = methodArgs[i].getClass();  
		}
		Method method = ownerClass.getMethod(methodName,margsArr);  
		return method.invoke(instanceObj, methodArgs);
	}  
	
	

	/**
	 * 反射调用某个类的静态方法。
	 * 不会创建类的实例，而是直接调用类的静态方法。
	 * eg: ReflectUtil.invokeStaticMethod("com.hbbc.util.LogUtil", "debug", new Object[]{new String("hello world!")});
	 * 
	 * @param className 类名（包名+类名），大小写敏感
	 * @param methodName 调用的方法名称，大小写敏感
	 * @param args  调用方法传入的参数，没有的时候传入null。
	 * @return Object，返回调用方法返回的对象
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public static Object invokeStaticMethod(String className, String methodName, Object[] args) throws Exception {  
		Class<?> ownerClass = Class.forName(className); 
		Class[] argsClass = new Class[args.length];  
		for (int i = 0, j = args.length; i < j; i++) {  
			argsClass[i] = args[i].getClass();  
		}
		Method method = ownerClass.getMethod(methodName,argsClass);  
		return method.invoke(null, args);
	}  
	
	
	

	/**
	 *  实例化类（可带参数），返回实例化后的类的实例，以Object形态返回，使用前需进行转换。
	 *  eg: DBUtil dbcon = (DBUtil)ReflectUtil.instanceClass("com.hbbc.util.DBUtil", new Object[]{new String("")});
	 *  
	 * @param className 类名（包名+类名），大小写敏感
	 * @param classArgs 实例化类时（构造函数），需要传入的参数。无参构造函数，传入null即可。
	 * @return 实例化后的类的实例
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public static Object instanceClass(String className, Object[] classArgs) throws Exception {
		Class<?> classObj = Class.forName(className);
		Class[] cargsArr = null;
		if(classArgs!=null && classArgs.length>0){
			cargsArr = new Class[classArgs.length];
			for(int i=0; i<classArgs.length; i++) cargsArr[i] = classArgs[i].getClass();
		}
		Constructor cons = classObj.getConstructor(cargsArr);
		return cons.newInstance(classArgs);
	}  
	
	
	
	/**
	 * 得到某个类实例下的某个特定名字的属性的属性值
	 * eg: System.out.println(getProperty(classObj, "userID").toString());
	 * 
	 * @param owner 对象指针（实例化后的类对象）
	 * @param fieldName 属性名字
	 * @return 返回属性的数值
	 * @throws Exception
	 */
	public static Object getProperty(Object owner, String fieldName) throws Exception {  
	     Class<?> ownerClass = owner.getClass();  
	     Field field = ownerClass.getField(fieldName);  
	     Object property = field.get(owner);  
	     return property;  
	}
	
	
	
}
