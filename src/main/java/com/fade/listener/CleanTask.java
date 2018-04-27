package com.fade.listener;

import java.util.List;
import java.util.TimerTask;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import com.fade.domain.Note;
import com.fade.mapper.NoteDao;
import com.fade.mapper.UserDao;
import com.fade.service.NoteService;
import com.fade.service.SolrService;
import com.fade.util.Const;
import com.fade.util.RedisUtil;

public class CleanTask extends TimerTask{
	
	private RedisUtil redisUtil;
	private NoteDao noteDao;
	private SolrService solrService;
	private NoteService noteService;
	private UserDao userDao;
	
	private Logger logger;
	private ApplicationContext applicationContext;
	

	public CleanTask(ApplicationContext applicationContext,Logger logger) {
		this.logger = logger;
		this.applicationContext = applicationContext;
	}
	
	@Override 
	public void run() {
		this.noteService = (NoteService) applicationContext.getBean("noteService");
		this.noteDao = (NoteDao) applicationContext.getBean("noteDao");
		this.solrService = (SolrService) applicationContext.getBean("solrService");
		this.redisUtil = (RedisUtil) applicationContext.getBean("redisUtil");
		this.userDao = (UserDao) applicationContext.getBean("userDao");
		
		//一次遍历1000条活的原帖，重新判断生死,得到的note仅包含id，时间,续减秒数量
		List<Note>list = null;
		while((list = noteDao.getNoteJudgeDie(1000)) != null &&list.size() > 0){
			long time = System.currentTimeMillis();
			for(Note note : list){
				if(noteService.judgeDie(note, time)){
					//该原贴已死亡
					//更新原贴以及转发帖的is_die
					noteDao.updateNoteDieSingle(note.getNote_id());
					noteDao.updateRelayNoteDie(note.getNote_id());
					//删除缓存中的数据
					redisUtil.deleteKey("note_" + note.getNote_id());
					//从热门榜中清除
					redisUtil.zsetDeleteKey(Const.HOT_NOTES, "note_"+note.getNote_id());
					//删除其续秒及减秒的缓存
					redisUtil.deleteKey("add_" + note.getNote_id());
					redisUtil.deleteKey("sub_" + note.getNote_id());
					//原作者的动态-1
					userDao.updateDynamicNumMinus(note.getUser_id());
					//转发者的动态减-1
					userDao.updateDynamicNumRelayUsers(note.getNote_id());
					//确认死亡后，得到完整bean，更新索引库的状态
					Note temp = noteDao.getNoteById(note.getNote_id());
					if(temp != null && temp.getUuid() != null){
						temp.setIs_die(0);//设置死亡
						solrService.solrAddUpdateNote(temp);
					}	
					logger.info("清除原贴" + note.getNote_id());
				}
			}			
		}
				;

	}
	
	
	

}
