package com.fade.listener;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;

import org.apache.log4j.Logger;

import com.fade.service.NoteService;
import com.fade.util.RedisUtil;

public class DataTimer {
	private RedisUtil redisUtil;
	private NoteService noteService;
	private Timer timer;
	private Logger logger = Logger.getLogger(DataTimer.class);
	
	public DataTimer(NoteService noteService, RedisUtil redisUtil) {
		this.redisUtil = redisUtil;
		this.noteService = noteService;
	}
	
	public void startTimer(){
		timer = new Timer();
		GregorianCalendar gc = new GregorianCalendar();
		//系统启动10秒后开始执行任务
		gc.setTime(new Date());
		gc.add(Calendar.SECOND, 10);
		//每1分钟执行一次
		timer.schedule(new DataTask(noteService, redisUtil), gc.getTime(),60000);
		logger.info("数据集更新器启动");
	}
	
	public void stopTimer(){
		if(timer != null){
			timer.cancel();
			logger.info("数据集更新器关闭");
		}
	}
}
