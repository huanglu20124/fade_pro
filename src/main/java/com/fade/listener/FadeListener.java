package com.fade.listener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.fade.mapper.NoteDao;
import com.fade.util.RedisUtil;


public class FadeListener implements ServletContextListener{

	private static Logger logger = Logger.getLogger(FadeListener.class);
	private CleanTimer cleanTimer;//死亡帖子清理器，1分钟一次
	private DataTimer dataTimer;//数据集更新器，15分钟一次
	private ServletContext servletContext; 
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		logger.info("Fade总监听器启动");
		servletContext = sce.getServletContext();
		ApplicationContext applicationContext = WebApplicationContextUtils.
				getRequiredWebApplicationContext(sce.getServletContext());
		RedisUtil redisUtil = (RedisUtil) applicationContext.getBean("redisUtil");
		NoteDao noteDao = (NoteDao) applicationContext.getBean("noteDao");
		cleanTimer = new CleanTimer(noteDao,redisUtil, logger);
		cleanTimer.startTimer();
		dataTimer = new DataTimer(servletContext, logger);
		dataTimer.startTimer();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		cleanTimer.stopTimer();
		dataTimer.stopTimer();
	}

}
