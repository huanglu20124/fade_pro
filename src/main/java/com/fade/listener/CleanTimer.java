package com.fade.listener;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import com.fade.mapper.NoteDao;
import com.fade.service.SolrService;
import com.fade.util.RedisUtil;

public class CleanTimer{
	private Logger logger;
	private Timer timer;
	private ApplicationContext applicationContext;
	
	public CleanTimer(ApplicationContext applicationContext,Logger logger) {
		this.logger = logger;
		this.applicationContext = applicationContext;
	}
	
	public void startTimer(){
		timer = new Timer();
		GregorianCalendar gc = new GregorianCalendar();
		//系统启动10秒后开始执行任务
		gc.setTime(new Date());
		gc.add(Calendar.SECOND, 10);
		//每60s执行一次
		timer.schedule(new CleanTask(applicationContext,logger), gc.getTime(),60000);
		logger.info("帖子清理器启动");
	}
	
	public void stopTimer(){
		if(timer != null){
			timer.cancel();
			logger.info("帖子清理器关闭");
		}
	}

}
