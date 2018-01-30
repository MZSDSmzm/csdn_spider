package com.mzsds.global;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.mzsds.util.LogUtil;

/**
 * 定时任务作业调度服务。当服务器启动时候启动线程。 监听 ServletContext 变化，如服务器启动时 ServletContext
 * 被创建，服务器关闭时 ServletContext 将要被销毁。
 */
@WebListener("这是一个定时检查用户服务有效期的任务")
public class ServletBeginThread implements ServletContextListener {

	/**
	 * 服务器启动时调用本初始化方法，本方法主要用于管理定时作业线程。
	 */
	public void contextInitialized(ServletContextEvent arg0) {
		try {
			Thread servletBeginThreadCsdnJktt = new ServletBeginThreadCsdnJktt();
			servletBeginThreadCsdnJktt.start();
			LogUtil.log(" 作业线程（ServletBeginThreadCsdnJktt）启动...");
			Thread servletBeginThreadCsdnForum = new ServletBeginThreadCsdnForum();
			servletBeginThreadCsdnForum.start();
			LogUtil.log(" 作业线程（ServletBeginThreadCsdnForum）启动...");
		} catch (Exception e) {
			LogUtil.exception(e);
		}
	}

	/**
	 * 服务器被关闭时调用本方法
	 */
	public void contextDestroyed(ServletContextEvent arg0) {
		LogUtil.log(" 服务器关闭通知...");
	}
}
