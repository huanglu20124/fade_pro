package com.fade.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.fade.domain.Note;
import com.fade.domain.SimpleResponse;
import com.fade.exception.FadeException;
import com.fade.mapper.NoteDao;
import com.fade.mapper.UserDao;
import com.fade.util.Const;
import com.fade.util.RedisUtil;
import com.fade.util.TimeUtil;

@Service("noteService")
public class NoteServiceImpl implements NoteService {
	@Resource(name = "noteDao")
	private NoteDao noteDao;
	
	@Resource(name = "userDao")
	private UserDao userDao;
	
	@Resource(name = "redisUtil")
	private RedisUtil redisUtil;
	
	private Logger logger = Logger.getLogger(NoteServiceImpl.class);
	
	@Override
	public String addNote(Note note, MultipartFile[] files) throws FadeException{
		//设置帖子时间以及其他参数，用户名是前端发来的 
		String post_time = TimeUtil.getCurrentTime();
		note.setPost_time(post_time);
		note.setAdd_num(0);
		note.setSub_num(0);
		note.setComment_num(0);
		note.setTarget_id(0);
		note.setType(0);
		//存储到数据库
		noteDao.addNote(note);
		if(note.getNote_id() == null) throw new FadeException("添加帖子失败！");
		//保存图片文件
		Map<String, Object>extra = new HashMap<>();
		if(files != null && note.getImages() != null){
			//--分目录存储防止一个文件夹中文件过多
			String hash = Integer.toHexString(UUID.randomUUID().toString().hashCode());
			StringBuilder save_path_builder = new StringBuilder("image/note/");
			for(char c : hash.toCharArray()){
				save_path_builder.append(c + "/");
			}
			String dir_path = save_path_builder.toString();
			new File(Const.DATA_PATH +  dir_path).mkdirs();
			
			int k = 0;
			List<String>image_urls = new ArrayList<>();//记录url返回给前端的
			for(MultipartFile file : files){
				// 找到后缀名
				String origin_file_name = file.getOriginalFilename();
				int flag = origin_file_name.lastIndexOf(".");
				String tail = origin_file_name.substring(flag, origin_file_name.length());
				String file_name = UUID.randomUUID().toString().substring(0, 10) + tail;
				File save_file = new File(Const.DATA_PATH + dir_path + file_name);
				try {
					file.transferTo(save_file);
				} catch (IllegalStateException e) {
					e.printStackTrace();
					throw new FadeException("上传图片异常！");
				} catch (IOException e) {
					e.printStackTrace();
					throw new FadeException("上传图片异常！");
				}
				note.getImages().get(k).setImage_url(dir_path + file_name);
				note.getImages().get(k).setNote_id(note.getNote_id());
				image_urls.add(dir_path + file_name);
				k++;
			}
			//把帖子链接返还给前端
			extra.put("image_urls", image_urls);
		}
		//添加图片
		if(note.getImages() != null && note.getImages().size() != 0)
		noteDao.addNoteImageBatch(note);
		//添加到redis中，初始时间为15分钟,注意key的格式 ，转发贴的key和原贴的key不同
		String key = "note_" + note.getNote_id();
		redisUtil.addKey(key, JSON.toJSONString(note), Const.DEFAULT_LIFE, TimeUnit.MINUTES);
		// 找到所有在线的粉丝，然后将它们的list2更新(这个后期要考虑优化，因为粉丝数量可能很大)
		List<Integer>all_fans_ids = userDao.getAllFansId(note.getUser_id()); 
		String temp = "note_"+note.getNote_id();
		for(Integer fans_id : all_fans_ids){
			if(redisUtil.setIsMember(Const.ONLINE_USERS, "user_" + fans_id)){
				redisUtil.listRightPush("list2_"+fans_id,temp);
			}
		}
		//同时，自己的list2队列也要更新
		redisUtil.listRightPush("list2_"+note.getUser_id(), temp);
		//返回部分信息
		extra.put("post_time", post_time);		
		extra.put("note_id", note.getNote_id());
		SimpleResponse response = new SimpleResponse("添加成功", null, extra);
		logger.info("用户" + note.getUser_id() + "添加帖子 " + note.getNote_id() + "成功");
		return JSON.toJSONString(response);
	}

	@Override
	public String getTenNoteByTime(Integer user_id, Integer start) {
		String array_name = "list1_"+user_id;//队列名字		
		//先直接查找前100 条，直到凑成10条 (仿票圈)，start=0的时候，定义为顶部下拉刷新+初次加载，先要清除redis缓存
		if(start == 0){
			//清除缓存先
			redisUtil.deleteKey(array_name);
			redisUtil.deleteKey("list2_" + user_id);
		}
		List<Note>ans_list = new ArrayList<>();
		int count = 0;
		//找出起点之后的全部帖子,起始是0
		List<String>note_ids = redisUtil.listGetRange(array_name,start.longValue(),-1l);
		//总长度
		long length = redisUtil.listGetSize(array_name);
		if((start != 0) && (start >= length)) return "{'list':[],'start':0}";//防止重复添加到redis队列
		Boolean isNeed = false; //是否需要查询mysql数据库
		List<String>die_ids = new ArrayList<>();//收集死亡的帖子，用于批处理更新数据库
		String search_key = null;//用于数据库查询的起点，查找比这个key小的帖子
		if(note_ids != null && note_ids.size() > 0) search_key = note_ids.get(note_ids.size() -1);
		int flag = start;//一个flag，用来找到下次的start索引，最后作为start返回
		if(note_ids.size() == 0) isNeed = true;
		else {
			for(String key : note_ids){
				String[]strs = key.split("_");
				if(strs.length == 3){
					//说明这是转发贴，还要判断原贴存不存在
					String origin_key = "note_"+strs[2];
					String origin_str = (String)redisUtil.getValue(origin_key);
					if(origin_str == null){
						//原贴不存在，则都销毁原贴以及这个贴
						redisUtil.deleteKey(origin_key);
						die_ids.add(strs[1]);
						die_ids.add(strs[2]);
						//从队列中移除
						redisUtil.listRemoveValue(array_name, key);
					}else {
						//原贴存在的话，从数据库查询转发贴，并加上原贴
						Note note = noteDao.getNoteById(new Integer(strs[1]));
						Note origin = JSON.parseObject(origin_str,Note.class);
						note.setOrigin(origin);
						ans_list.add(note);
						count++; 
						flag++;
					}
				}else {
					//原创帖，直接从redis中获取
					String note_str = (String)redisUtil.getValue(key);
					if(note_str != null){
						ans_list.add(JSON.parseObject(note_str, Note.class));
						count++;
						flag++;
					}else {
						die_ids.add(strs[1]);
						//从队列中移除
						redisUtil.listRemoveValue(array_name, key);
					}
				}
				if(count == 10){
					break;
				}				
			}
		}
		if(count < 10) isNeed = true;
		//开始数据库查询
		Integer search_id = 0;
		if(isNeed){
			if(search_key != null){
				String[] strs = search_key.split("_");
				search_id = new Integer(strs[1]);
			}
		}
		
		while(isNeed){
			//一次查找一百条,活的就加入到队列里,最多一次加载100条到缓存里
			List<Note>notes = noteDao.getMuchNoteId(user_id,search_id);
			for(Note note : notes){
				if(note.getTarget_id() == 0){
					//说明是原创帖
					String note_str = (String)redisUtil.getValue("note_" + note.getNote_id());
					if(note_str != null){
						//帖子还没挂，加入到队列里(count小于10的时候)
						if(count < 10){
							ans_list.add(JSON.parseObject(note_str, Note.class));
							count++;
							flag++;
						}
						//加入到redis队列中
						redisUtil.listRightPush(array_name, "note_" + note.getNote_id());	
						logger.info("增加帖子"+note.getNote_id()+"到队列中");
					}else {
						//清除
						die_ids.add(note.getNote_id().toString());
					}
				}else {
					//转发帖，还需要先判断原创帖在不在
					String origin_str = (String)redisUtil.getValue("note_" + note.getTarget_id());
					if(origin_str == null){
						//该帖加入到死亡列表
						die_ids.add(note.getNote_id().toString());
					}else {
						//获得该帖的信息，加入到队列
						Note relay = noteDao.getNoteById(note.getNote_id());
						relay.setOrigin(JSON.parseObject(origin_str, Note.class));
						if(count < 10){
							ans_list.add(relay); 
							count++; 
							flag++;
						}
						//加入redis队列，key格式要区分开
						redisUtil.listRightPush(array_name, "note_" + note.getNote_id() + "_" + note.getTarget_id());
						logger.info("增加帖子"+note.getNote_id()+"到队列中");
					}
				}
							
			}
			if(count >= 10) break;
			if(notes.size() > 0) {
				search_id = notes.get(notes.size() - 1).getNote_id();
			}else {
				break;
			}
		}
		//将死亡贴加入到set
		if(die_ids.size() > 0) redisUtil.setAddKey(Const.DIE_LIST, die_ids.toArray());
		if(count < 10){
			//最后一次检查，如果数量小于10，热门推送进行补充
			List<Note>hot_notes = getHotNote(user_id);
			if(hot_notes != null) ans_list.addAll(hot_notes);
		}
		//最后一个切面
		checkAction(ans_list, user_id);
		Map<String, Object>map = new HashMap<>();
		map.put("list", ans_list);
		map.put("start", flag);
		return JSON.toJSONString(map);
	}

	@Override
	public String getMoreNote(Integer user_id) {
		//顶部下拉刷新
		List<Note>add_notes = getAddNote(user_id);
		if(add_notes.size() < 10){
			List<Note>hot_notes = getHotNote(user_id);
			if(hot_notes != null) add_notes.addAll(hot_notes);
		}
		//最后一个切面
		checkAction(add_notes, user_id);
		return JSON.toJSONString(add_notes);
	}
	 
	private List<Note> getHotNote(Integer user_id) {
		//热门推送帖
		return null;
	}

	private List<Note> getAddNote(Integer user_id) {
		//从list2队列中取出新增的
		String array_name = "list2_" + user_id;
		List<Note>notes = new ArrayList<>();
		List<String>keys = redisUtil.listGetAll(array_name);
		List<Integer>die_ids = new ArrayList<>();
		int count = 0;
		for(String key : keys){
			String[]strs = key.split("_");
			if(strs.length == 3){
				//如果是转发帖
				String origin_str = (String)redisUtil.getValue("note_" + strs[2]);
				if(origin_str == null){
					die_ids.add(new Integer(strs[1])); 
				}else {
					Note origin = JSON.parseObject(origin_str, Note.class);
					Note note = noteDao.getNoteById(new Integer(strs[1]));
					note.setOrigin(origin);
					notes.add(note);
					count++;
				}
			}else {
				//如果是原创帖
				String note_str = (String)redisUtil.getValue(key);
				if(note_str != null){
					notes.add(JSON.parseObject(note_str, Note.class));
					count++;
				}
			}
			redisUtil.listLeftPop(array_name);//弹出队列，以免下次又刷新到
			if(count == 10){
				break;
			}
		}
		return notes;
	}

	@Override
	public String changeSecond(Note note) throws FadeException{
		//增和减秒的请求，实则是增加帖子
		if(note == null || note.getTarget_id() == null) throw new FadeException("请求格式不正确！");
		String origin_str = (String)redisUtil.getValue("note_" + note.getTarget_id());
		if(origin_str == null) throw new FadeException("操作失败，原贴已失效！");
		//设置帖子时间以及其他参数，用户名是前端发来的 
		String post_time = TimeUtil.getCurrentTime();
		note.setPost_time(post_time);
		note.setAdd_num(0);
		note.setSub_num(0);
		note.setComment_num(0);
		//存储到数据库
		noteDao.addNote(note);	
		//原贴加秒和减秒数量要更新，原贴生存时间更新
		Note origin = JSON.parseObject(origin_str, Note.class);
		Integer add_sum = origin.getAdd_num();
		Integer sub_sum = origin.getSub_num();
		if(note.getType() == 1){
			//加秒
			add_sum++;
			noteDao.updateNoteAddNum(origin.getNote_id(),add_sum);
			origin.setAdd_num(add_sum);
		}else if(note.getType() == 2){
			//减秒
			sub_sum++;
			noteDao.updateNoteSubNum(origin.getNote_id(),sub_sum);
			origin.setAdd_num(sub_sum);
		}
		//计算剩余时间,覆盖原key
		Date post_date = TimeUtil.getTimeDate(origin.getPost_time());
		Date current_date = new Date(); 
		// 当前时间减去发帖时间，得到分钟数
		long past = ((current_date.getTime() - post_date.getTime()) / (1000 * 60));
		long time_left =  Const.DEFAULT_LIFE + add_sum*5 -sub_sum -past;
		//保证，一条信息，存在5分钟
		if(past >= 5l){
			if(time_left <= 0){
				//直接删除原贴
				redisUtil.setAddKey(Const.DIE_LIST, origin.getNote_id().toString());
				redisUtil.deleteKey("note_" + origin.getNote_id());
				return JSON.toJSONString(new SimpleResponse("0",null));//代表原贴已消失
			}
		}else {
			if(time_left <= (5l - past)) time_left = 5l - past;
		}
		logger.info("续一秒成功，原贴" + origin.getNote_id() + "的剩余时间为" + time_left + "分钟");
		redisUtil.addKey("note_"+origin.getNote_id(), JSON.toJSONString(origin),time_left, TimeUnit.MINUTES);
		//转发帖只存入数据库，不添加到redis中
		String key = "note_" + note.getNote_id() + "_" + note.getTarget_id();
		// 找到所有在线的粉丝，然后将它们的list2更新(这个后期要考虑优化，因为粉丝数量可能很大)
		List<Integer>all_fans_ids = userDao.getAllFansId(note.getUser_id()); 
		System.out.println(all_fans_ids);
		for(Integer fans_id : all_fans_ids){
			if(redisUtil.setIsMember(Const.ONLINE_USERS, "user_" + fans_id)){
				redisUtil.listRightPush("list2_"+fans_id,key);
			}
		}
		//同时，自己的list2队列也要更新
		redisUtil.listRightPush("list2_"+note.getUser_id(),key);
		Map<String, Object>extra = new HashMap<>();
		//返回部分信息
		extra.put("post_time", post_time);		
		extra.put("note_id", note.getNote_id());
		SimpleResponse response = new SimpleResponse("1", null, extra);
		logger.info("用户" + note.getUser_id() + "添加帖子 " + note.getNote_id() + "成功");
		return JSON.toJSONString(response);
	}
  
	private void checkAction(List<Note>notes, Integer user_id){
		//增加是否续或者减的属性
		for(Note note : notes){
			Integer type = null;
			if((type = noteDao.getNoteCheckAction(user_id,note.getNote_id())) == null){
				//还没操作过
				note.setAction(0);
			}else if (type == 1) {
				//增
				note.setAction(1);
			}else if (type == 2) {
				//减
				note.setAction(2);
			}
		}
	}
}
