package com.fade.listener;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

public class DataTimer {
	private ServletContext servletContext;
	private Logger logger;
	private Timer timer;
	
	public DataTimer(ServletContext servletContext, Logger logger) {
		this.logger = logger;
		this.servletContext = servletContext;
	}
	
	public void startTimer(){
		timer = new Timer();
		GregorianCalendar gc = new GregorianCalendar();
		//系统启动10秒后开始执行任务
		gc.setTime(new Date());
		gc.add(Calendar.SECOND, 10);
		//每15分钟执行一次
		timer.schedule(new DataTask(servletContext,logger), gc.getTime(),900000);
		logger.info("数据集更新器启动");
	}
	
	public void stopTimer(){
		if(timer != null){
			timer.cancel();
			logger.info("数据集更新器关闭");
		}
	}
}
