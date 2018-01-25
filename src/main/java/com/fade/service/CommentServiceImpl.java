package com.fade.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.fade.domain.Comment;
import com.fade.domain.CommentQuery;
import com.fade.domain.Note;
import com.fade.domain.SecondComment;
import com.fade.domain.SimpleResponse;
import com.fade.exception.FadeException;
import com.fade.mapper.CommentDao;
import com.fade.mapper.NoteDao;
import com.fade.mapper.UserDao;
import com.fade.util.RedisUtil;
import com.fade.util.TimeUtil;
import com.fade.websocket.MessageWebSocketHandler;

@Service("commentService")
public class CommentServiceImpl implements CommentService{
	@Resource(name = "noteDao")
	private NoteDao noteDao;
	
	@Resource(name = "userDao")
	private UserDao userDao;
	
	@Resource(name = "redisUtil")
	private RedisUtil redisUtil;
	
	@Resource(name = "commentDao")
	private CommentDao commentDao;
	
	@Resource(name = "messageWebSocketHandler")
	private MessageWebSocketHandler webSocketHandler;
	
	private static Logger logger = Logger.getLogger(CommentService.class);
	
	@Override
	public CommentQuery getTenComment(Integer note_id, Integer start) throws FadeException {
		//加载十条评论，核心：找到比start大的数据，加到list3中，队列key为'list3_'+note_id, 评论的key为'comment_'+comment_id
		//start为id， 不是索引！start一开始为0
		//comment_num用来判断是否还需要查询数据库
		CommentQuery commentQuery = new CommentQuery();
		List<Comment>ans_list = new ArrayList<>();
		Boolean isNeed = true; //是否需要再查询数据库
		String search_key = null;
		int search_id = 0; //下次查询数据库的起点
		int num = 10; //要从数据库补充的评论条数的数量
		String note_str = (String) redisUtil.getValue("note_" + note_id);
		Integer comment_num = 0;
		String array_name = "list3_" + note_id;
		List<String>all_keys = redisUtil.listGetAll(array_name);
		int size = all_keys.size();
		List<Comment>list = null;
		if(note_str == null) throw new FadeException("原贴已消失！");
		else {
			//优化，先查询帖子的一级评论数量，再判断是否需要查询数据库
			Note note = JSON.parseObject(note_str, Note.class);
			comment_num = note.getBaseComment_num();
			if(comment_num == 0){
				commentQuery.setStart(0);
				commentQuery.setList(ans_list);
				return commentQuery;//评论数量为0
			}else if (comment_num == size) {
				//数量绝对够，不用再查询数据库
				isNeed = false;
			}
		}

		if(isNeed){
			if(start == 0){
				//第一次加载,从队列里取10条，不够从数据库补
				if(size == 0){
					search_id = 0; //代表数据库查询直接取头10条
					num = 10;
					isNeed = true;
				}else if (size < 10) {
					//为队列最后一个id
					search_key = all_keys.get(0);
					String[] strs = search_key.split("_");
					search_id = new Integer(strs[1]);
					num = 10 - size;
					//将已经有的加到ans_list
					List<String>temps = all_keys.subList(0, size);
					for(String temp : temps){
						String comment_str = (String) redisUtil.getValue(temp);
						if(comment_str != null){
							ans_list.add(JSON.parseObject(comment_str, Comment.class));
						}
					}
					isNeed = true;
				}else {
					//数据量已够
					isNeed = false;
				}			
			}else{
				//找到该id在队列中的位置
				int position = 0;
				String compare_key = "comment_"+start;
				for(int i = 0; i < size; i++){
					if(all_keys.get(i).equals(compare_key)){
						position = i;
						break;
					}
				}
				if(position + 10 <= size){
					//数量足够
					isNeed = false;
				}else {
					//数量不足
					search_key = all_keys.get(0);
					String[] strs = search_key.split("_");
					search_id = new Integer(strs[1]);
					num = 10 - (size - position);//补充数量
					//将已经有的加到ans_list
					List<String>temps = all_keys.subList(0, position);
					for(String temp : temps){
						String comment_str = (String) redisUtil.getValue(temp);
						if(comment_str != null){
							ans_list.add(JSON.parseObject(comment_str, Comment.class));
						}
					}
					isNeed = true;
				}
			}
		}
		
		if(isNeed){
			//反过来
			Collections.reverse(ans_list);
			list = commentDao.getTenComment(note_id,search_id,num); 
			getSecondComment(list);//添加二级评论
			if(list.size() > 0) {
				ans_list.addAll(list);
				//加入到队列里
				List<String>keys = new ArrayList<>();
				for(Comment comment : list){
					keys.add("comment_" + comment.getComment_id());
					//单个key的持续时间为15分钟
					redisUtil.addKey("comment_" + comment.getComment_id(), JSON.toJSONString(comment),15l,TimeUnit.MINUTES);
				}
				logger.info("将以下评论加入队列:" +  keys.toArray());
				redisUtil.listLeftPushAll(array_name, keys.toArray());
				//重新设置队列持续时间为15分钟
				redisUtil.setKeyTime(array_name, 15l, TimeUnit.MINUTES);
			}
		}else {
			List<String>ten_keys = null;
			if(size >= 10){
				ten_keys = all_keys.subList(0, 10);
			}else {
				ten_keys = all_keys.subList(0, size);
			}
			//要反过来，从早到晚
			Collections.reverse(ten_keys);
		    for(String key: ten_keys){
				String comment_str = (String) redisUtil.getValue(key);
				if(comment_str != null){
					Comment temp = JSON.parseObject(comment_str, Comment.class);
					ans_list.add(temp);
				}else {
					//为空的话，从数据库查询，并加到缓存里
					String[]strs = key.split("_");
					Comment temp = commentDao.getCommentById(new Integer(strs[1]));
					if(temp != null){
						//假如还在的话
						temp.setComments(commentDao.getSecondComment(temp.getComment_id()));
						redisUtil.addKey(key, JSON.toJSONString(temp), 15l, TimeUnit.MINUTES);
					}
				}
		    }
		}
		int flag = 10;
		if(ans_list.size() > 0){
			flag = ans_list.get(ans_list.size() - 1).getComment_id();		
		}
		commentQuery.setStart(flag);
		commentQuery.setList(ans_list);
		return commentQuery;
	}
	
	private void getSecondComment(List<Comment>comments){
		//为所有评论添加二级评论
		for(Comment comment : comments){
			comment.setComments(commentDao.getSecondComment(comment.getComment_id()));
		}
		
	}
	
	@Override
	public String addComment(Comment comment) {
		comment.setComment_time(TimeUtil.getCurrentTime());
		commentDao.addComment(comment);
		SimpleResponse response = new SimpleResponse();
		if(comment.getComment_id() != null){
			response.setSuccess("添加评论成功");
			//更新帖子的评论数量
			noteDao.updateCommentNum(comment.getNote_id());
			//redis对应也要更新
			String key = "note_" + comment.getNote_id();
			String note_str = (String) redisUtil.getValue(key);
			if(note_str != null){
				Note note = JSON.parseObject(note_str,Note.class);
				note.setComment_num(note.getComment_num() + 1);
				note.setBaseComment_num(note.getBaseComment_num() +1);
				long time = redisUtil.getKeyTime("note_" + comment.getNote_id(), TimeUnit.MINUTES);
				redisUtil.addKey(key, JSON.toJSONString(note), time, TimeUnit.MINUTES);
				//websocket通知主人更新
				//更新该主人数据库通知数量
				userDao.updateAddCommentPlus(note.getUser_id());
				webSocketHandler.sendMessageToUser(note.getUser_id(),JSON.toJSONString(new SimpleResponse("02",null)));
			}
				
			//添加其他信息
			Map<String, Object>extra = new HashMap<>();
			extra.put("comment_id", comment.getComment_id());
			extra.put("comment_time", comment.getComment_time());
			response.setExtra(extra);
		}else {
			response.setErr("添加失败");
		}
		return JSON.toJSONString(response);
	}
	
	@Override
	public String addSecondComment(SecondComment secondComment) {
		//发过来的secondComment必须包含note_id
		secondComment.setComment_time(TimeUtil.getCurrentTime());
		commentDao.addSecondComment(secondComment);
		SimpleResponse response = new SimpleResponse();
		if(secondComment.getComment_id() != null){
			response.setSuccess("添加评论成功");
			//更新帖子的评论数量
			noteDao.updateCommentNum(secondComment.getNote_id());
			//redis更新帖子信息
			String key = "note_" + secondComment.getNote_id();
			String note_str = (String) redisUtil.getValue(key);
			if(note_str != null){
				Note note = JSON.parseObject(note_str,Note.class);
				note.setComment_num(note.getComment_num() + 1);
				long time = redisUtil.getKeyTime("note_" + secondComment.getNote_id(), TimeUnit.MINUTES);
				redisUtil.addKey(key, JSON.toJSONString(note), time, TimeUnit.MINUTES);
				//更新该主人数据库通知数量
				userDao.updateAddCommentPlus(note.getUser_id());
				//websocket通知主人更新
				webSocketHandler.sendMessageToUser(note.getUser_id(),JSON.toJSONString(new SimpleResponse("02",null)));
			}
			//redis更新一级评论的信息
			key = "comment_" + secondComment.getComment_id();
			String comment_str = (String) redisUtil.getValue(key);
			if(comment_str != null){
				Comment comment = JSON.parseObject(comment_str, Comment.class);
				comment.getComments().add(secondComment);
				redisUtil.addKey(key, JSON.toJSONString(comment), 15l, TimeUnit.MINUTES);
			}
			//添加其他信息
			Map<String, Object>extra = new HashMap<>();
			extra.put("second_id", secondComment.getSecond_id());
			extra.put("comment_time",secondComment.getComment_time());
			response.setExtra(extra);
		}else {
			response.setErr("添加失败");
		}
		return JSON.toJSONString(response);
	}

}
