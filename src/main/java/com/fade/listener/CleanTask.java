package com.fade.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.fade.mapper.NoteDao;
import com.fade.util.Const;
import com.fade.util.RedisUtil;

public class CleanTask extends TimerTask{
	
	private RedisUtil redisUtil;
	private Logger logger;
	private NoteDao noteDao;
	
	public CleanTask(NoteDao noteDao,RedisUtil redisUtil,Logger logger){
		this.redisUtil = redisUtil;
		this.logger = logger;
		this.noteDao = noteDao;
	}
	
	@Override 
	public void run() {
		List<String>die_list = new ArrayList<>();
		die_list.addAll(redisUtil.setGetAll(Const.DIE_LIST));
		redisUtil.deleteKey(Const.DIE_LIST);
		List<Integer>die_ids = new ArrayList<>();
		for(String id_str : die_list){
			die_ids.add(new Integer(id_str));
			//从热门榜中清除
			redisUtil.zsetDeleteKey(Const.HOT_NOTES, "note_"+id_str);
		}
		if(die_ids.size() > 0) noteDao.updateNoteDie(die_ids);
		//logger.info("清理完成，清理的帖子有" + die_list);
	}

}
