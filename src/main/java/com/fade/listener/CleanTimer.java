package com.fade.listener;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import org.apache.log4j.Logger;

import com.fade.mapper.NoteDao;
import com.fade.util.RedisUtil;

public class CleanTimer{
	private RedisUtil redisUtil;
	private Logger logger;
	private Timer timer;
	private NoteDao noteDao;
	
	public CleanTimer(NoteDao noteDao,RedisUtil redisUtil,Logger logger) {
		this.redisUtil = redisUtil;
		this.logger = logger;
		this.noteDao = noteDao;
	}
	public void startTimer(){
		timer = new Timer();
		GregorianCalendar gc = new GregorianCalendar();
		//系统启动10秒后开始执行任务
		gc.setTime(new Date());
		gc.add(Calendar.SECOND, 10);
		//每60s执行一次
		timer.schedule(new CleanTask(noteDao,redisUtil,logger), gc.getTime(),60000);
	}
	
	public void stopTimer(){
		if(timer != null){
			timer.cancel();
			System.out.println("CleanTimer定时器关闭了");
		}
	}

}
