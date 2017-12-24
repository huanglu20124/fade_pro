package com.fade.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
				image_urls.add(Const.BASE_IP +  dir_path + file_name);
				k++;
			}
			//把帖子链接返还给前端
			extra.put("image_urls", image_urls);
		}
		//添加图片
		if(note.getImages() != null && note.getImages().size() != 0)
		noteDao.addNoteImageBatch(note);
		//添加到redis中，初始时间为15分钟,注意key的格式		
		redisUtil.addKey("note_" + note.getNote_id(), JSON.toJSONString(note), 15l, TimeUnit.MINUTES);
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
		//先直接查找前100 条，直到凑成10条 (仿票圈)
		List<Note>ans_list = new ArrayList<>();
		int count = 0;
		//找出起点之后的全部帖子,起始是0
		List<String>note_ids = redisUtil.listGetRange(array_name,start.longValue(),-1l);
		Boolean isNeed = false; //是否需要查询mysql数据库
		List<Integer>die_ids = new ArrayList<>();//收集死亡的帖子，用于批处理更新数据库
		String search_key = null;//用于数据库查询的起点，查找比这个key小的帖子
		if(note_ids != null && note_ids.size() > 0) search_key = note_ids.get(note_ids.size() -1);
		int flag = start;//一个flag，用来找到下次的start索引，最后作为start返回
		if(note_ids.size() == 0) isNeed = true;
		else {
			for(String key : note_ids){
				//这里的key为"note_"+真正的note_id，为一个帖子在redis中的唯一key
				String note_str = (String)redisUtil.getValue(key);
				if(note_str != null){					
					ans_list.add(JSON.parseObject(note_str, Note.class));
					count++; 
					flag++;
					if(count == 10){
						break;
					}
				}else {
					String[]strs = key.split("_");
					die_ids.add(new Integer(strs[1]));
					//从队列中移除
					redisUtil.listRemoveValue(array_name, key);
					flag--;
				}
			}
		}
		if(count < 10) isNeed = true;
		//再更新死亡帖子
		if(die_ids.size() > 0)
		noteDao.updateNoteDie(die_ids);
		//开始数据库查询
		Integer search_id = 0;
		if(isNeed){
			if(search_key != null){
				String[] strs = search_key.split("_");
				search_id = new Integer(strs[1]);
			}
		}
		while(isNeed){
			//一次查找一百条,活的就加入到队列里
			List<Integer>add_ids = noteDao.getMuchNoteId(user_id,search_id);
			for(Integer add_id : add_ids){
				String note_str = (String)redisUtil.getValue("note_" + add_id);
				if(note_str != null){
					//帖子还没挂，加入到队列里(count小于10的时候)
					if(count < 10){
						ans_list.add(JSON.parseObject(note_str, Note.class)); 
						flag++;
					}
					//也加入到redis队列中
					redisUtil.listRightPush(array_name, "note_" + add_id);					
					count++;
				}
			}
			if(count >= 10) break;
			if(add_ids.size() > 0) {
				search_id = add_ids.get(add_ids.size() - 1);
			}else {
				break;
			}
		}
		Map<String, Object>map = new HashMap<>();
		map.put("list", ans_list);
		map.put("start", flag);
		return JSON.toJSONString(map);
	}

	
}
