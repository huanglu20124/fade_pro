package com.fade.listener;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;

import org.apache.log4j.Logger;

import com.fade.mapper.NoteDao;
import com.fade.service.SolrService;
import com.fade.util.RedisUtil;

public class IndexTimer {
	private RedisUtil redisUtil;
	private Logger logger;
	private Timer timer;
	private NoteDao noteDao;
	private SolrService solrService;
	
	public IndexTimer(NoteDao noteDao,SolrService solrService,RedisUtil redisUtil,Logger logger) {
		this.redisUtil = redisUtil;
		this.logger = logger;
		this.noteDao = noteDao;
		this.solrService = solrService;
	}
	public void startTimer(){
		timer = new Timer();
		GregorianCalendar gc = new GregorianCalendar();
		//系统启动10秒后开始执行任务
		gc.setTime(new Date());
		gc.add(Calendar.SECOND, 10);
		//每60s执行一次
		timer.schedule(new IndexTask(noteDao,solrService,redisUtil,logger), gc.getTime(),60000);
		logger.info("帖子注册器启动");
	}
	
	public void stopTimer(){
		if(timer != null){
			timer.cancel();
			logger.info("帖子注册器关闭");
		}
	}
}
