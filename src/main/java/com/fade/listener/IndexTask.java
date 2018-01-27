package com.fade.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.fade.domain.Note;
import com.fade.mapper.NoteDao;
import com.fade.service.SolrService;
import com.fade.util.Const;
import com.fade.util.RedisUtil;

public class IndexTask extends TimerTask{
	//将帖子添加到索引数据库的服务
	private RedisUtil redisUtil;
	private Logger logger;
	private NoteDao noteDao;
	private SolrService solrService;
	
	public IndexTask(NoteDao noteDao,SolrService solrService,RedisUtil redisUtil,Logger logger){
		this.redisUtil = redisUtil;
		this.logger = logger;
		this.noteDao = noteDao;
		this.solrService = solrService;
	}
	
	@Override 
	public void run() {
		//查询队列
		List<String>list = redisUtil.listGetAll(Const.INDEX_LIST);
		Note note = null;
		for(String idStr : list){
			note = noteDao.getNoteById(new Integer(idStr));
			if(note.getUuid() != null){
				solrService.solrAddUpdateNote(note);
			}		
			redisUtil.listRemoveValue(Const.INDEX_LIST, idStr);
		}
		//最后清空
		
	}

}
