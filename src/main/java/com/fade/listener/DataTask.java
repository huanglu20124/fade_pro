package com.fade.listener;

import java.util.TimerTask;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

import com.fade.domain.UpdateMessage;
import com.fade.service.NoteService;
import com.fade.util.Const;
import com.fade.util.RedisUtil;

public class DataTask extends TimerTask {

	private Logger logger = Logger.getLogger(DataTask.class);
	private RedisUtil redisUtil;
	private NoteService noteService;
	
	public DataTask(NoteService noteService, RedisUtil redisUtil) {
		this.redisUtil = redisUtil;
		this.noteService = noteService;
	}
	
	@Override
	public void run() {
		 //一个个取出reference_list中的数据，并更新到reference表
		while(redisUtil.listGetSize(Const.PREFERENCE_LIST) > 0l){
			UpdateMessage message = (UpdateMessage) redisUtil.listLeftPop(Const.PREFERENCE_LIST);
			if(message != null){
				noteService.updateReference(message);
			}
		}

	}

}
