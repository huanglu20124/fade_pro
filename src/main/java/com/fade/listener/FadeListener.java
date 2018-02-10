package com.fade.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.fade.mapper.NoteDao;
import com.fade.service.NoteService;
import com.fade.service.SolrService;
import com.fade.util.RedisUtil;


public class FadeListener implements ServletContextListener{

	private static Logger logger = Logger.getLogger(FadeListener.class);
	private DataTimer dataTimer;//数据集更新器，1分钟一次
	private IndexTimer indexTimer;//帖子索引数据插入器，1分钟一次
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		logger.info("Fade总监听器启动");
		ApplicationContext applicationContext = WebApplicationContextUtils.
				getRequiredWebApplicationContext(sce.getServletContext());
		RedisUtil redisUtil = (RedisUtil) applicationContext.getBean("redisUtil");
		NoteDao noteDao = (NoteDao) applicationContext.getBean("noteDao");
		SolrService solrService = (SolrService) applicationContext.getBean("solrService");
		NoteService noteService = (NoteService) applicationContext.getBean("noteService");
		
		indexTimer = new IndexTimer(noteDao,solrService, redisUtil, logger);
		indexTimer.startTimer();
		dataTimer = new DataTimer(noteService,redisUtil);
		dataTimer.startTimer();

	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		indexTimer.stopTimer();
		dataTimer.stopTimer();
	}

}
