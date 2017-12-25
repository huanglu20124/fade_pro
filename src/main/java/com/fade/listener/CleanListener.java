package com.fade.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.fade.mapper.NoteDao;
import com.fade.util.RedisUtil;


public class CleanListener implements ServletContextListener{

	private static Logger logger = Logger.getLogger(CleanListener.class);
	private CleanTimer timer;
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		logger.info("帖子清理监听器启动");
		ApplicationContext applicationContext = WebApplicationContextUtils.
				getRequiredWebApplicationContext(sce.getServletContext());
		RedisUtil redisUtil = (RedisUtil) applicationContext.getBean("redisUtil");
		NoteDao noteDao = (NoteDao) applicationContext.getBean("noteDao");
		timer = new CleanTimer(noteDao,redisUtil, logger);
		timer.startTimer();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		timer.stopTimer();
	}

}
