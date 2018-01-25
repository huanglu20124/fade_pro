 package com.fade.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.fade.domain.Comment;
import com.fade.domain.CommentQuery;
import com.fade.domain.DetailPage;
import com.fade.domain.Image;
import com.fade.domain.Note;
import com.fade.domain.NoteQuery;
import com.fade.domain.SimpleResponse;
import com.fade.exception.FadeException;
import com.fade.mapper.CommentDao;
import com.fade.mapper.NoteDao;
import com.fade.mapper.UserDao;
import com.fade.util.Const;
import com.fade.util.RedisUtil;
import com.fade.util.TimeUtil;
import com.fade.websocket.MessageWebSocketHandler;

@Service("noteService")
public class NoteServiceImpl implements NoteService {
	@Resource(name = "noteDao")
	private NoteDao noteDao;
	
	@Resource(name = "userDao")
	private UserDao userDao;
	
	@Resource(name = "redisUtil")
	private RedisUtil redisUtil;
	
	@Resource(name = "commentDao")
	private CommentDao commentDao;
	
	@Resource(name = "commentService")
	private CommentService commentService;
	
	@Resource(name = "messageWebSocketHandler")
	private MessageWebSocketHandler webSocketHandler;
	
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
		note.setBaseComment_num(0);
		//存储到数据库
		noteDao.addNote(note);
		//个人fade数量加1
		userDao.updateFadeNumPlus(note.getUser_id());
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
				System.out.println("k=" + k + " images=" + note.getImages().size());
				note.getImages().get(k).setImage_url(dir_path + file_name);
				note.getImages().get(k).setNote_id(note.getNote_id());
				image_urls.add(dir_path + file_name);
				k++;
			}
			//把帖子链接返还给前端
			extra.put("imageUrls", image_urls);
		}else {
			//添加默认的图片队列，保证不为null
			List<Image>list = new ArrayList<>();
			note.setImages(list);
		}
		//添加图片
		if(note.getImages() != null && note.getImages().size() != 0)
		noteDao.addNoteImageBatch(note);
		//添加到redis中，初始时间为15分钟,注意key的格式 ，转发贴的key和原贴的key不同
		String key = "note_" + note.getNote_id();
		redisUtil.addKey(key, JSON.toJSONString(note), Const.DEFAULT_LIFE, TimeUnit.MINUTES);
		//添加到热门推送排行榜
		redisUtil.zsetAddKey(Const.HOT_NOTES, key, 0d);
		// 找到所有在线的粉丝，然后将它们的list2更新(这个后期要考虑优化，因为粉丝数量可能很大)
		List<Integer>all_fans_ids = userDao.getAllFansId(note.getUser_id()); 
		String temp = "note_"+note.getNote_id();
		for(Integer fans_id : all_fans_ids){
			if(redisUtil.setIsMember(Const.ONLINE_USERS, "user_" + fans_id.toString())){
				redisUtil.listRightPush("list2_"+fans_id,temp);
			}
		}
		//同时，自己的list2队列也要更新
		//redisUtil.listRightPush("list2_"+note.getUser_id(), temp);
		//返回部分信息
		extra.put("post_time", post_time);		
		extra.put("note_id", note.getNote_id());
		SimpleResponse response = new SimpleResponse("添加成功", null, extra);
		response.setExtra(extra);
		logger.info("用户" + note.getUser_id() + "添加帖子 " + note.getNote_id() + "成功");
		return JSON.toJSONString(response);
	}

	@Override
	public String getTenNoteByTime(Integer user_id, Integer start, Integer concern_num) {
		Long time = System.currentTimeMillis();
		String array_name = "list1_"+user_id;//队列名字		
		//先直接查找前100 条，直到凑成10条 (仿票圈)，start=0的时候，定义为初次加载，先要清除redis缓存
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
		if((start != 0) && (start >= length)) {
			NoteQuery query = new NoteQuery();
			query.setList(new ArrayList<>());
			query.setStart(start);
			return JSON.toJSONString(query);//防止重复添加到redis队列
		}
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
						//原贴存在的话，从数据库查询转发贴，并加上原贴，这里要考虑缓存
						Note note = noteDao.getNoteById(new Integer(strs[1]));
						Note origin = JSON.parseObject(origin_str,Note.class);
						origin.setFetchTime(time);
						note.setOrigin(origin);
						note.setFetchTime(time);
						ans_list.add(note);
						count++; 
						flag++;
					}
				}else { 
					//原创帖，直接从redis中获取
					String note_str = (String)redisUtil.getValue(key);
					if(note_str != null){
						Note temp = JSON.parseObject(note_str, Note.class);
						temp.setFetchTime(time);
						ans_list.add(temp);
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
			List<Note>notes = null;
			if(concern_num == 0){
				//关注数量为0，只查找自己的
				notes = noteDao.getMuchMyNoteId(user_id,search_id);
			}else {
				notes = noteDao.getMuchNoteId(user_id,search_id);
			}
			for(Note note : notes){
				if(note.getTarget_id() == 0){
					//说明是原创帖
					String note_str = (String)redisUtil.getValue("note_" + note.getNote_id());
					if(note_str != null){
						//帖子还没挂，加入到队列里(count小于10的时候)
						if(count < 10){
							Note temp = JSON.parseObject(note_str, Note.class);
							temp.setFetchTime(time);
							ans_list.add(temp);
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
						Note origin = JSON.parseObject(origin_str, Note.class);
						origin.setFetchTime(time);
						relay.setOrigin(origin);
						relay.setFetchTime(time);
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
		if(die_ids.size() > 0) redisUtil.setAddKeyMul(Const.DIE_LIST, die_ids.toArray());
		if(count < 10){
			//最后一次检查，如果数量小于10，热门推送进行补充
			//List<Note>hot_notes = getHotNote(user_id,ans_list,null);
			//if(hot_notes != null) ans_list.addAll(hot_notes);
		}
		//最后一个切面
		checkAction(ans_list, user_id);
		System.out.println("帖子数量=" + ans_list.size());
		Map<String, Object>map = new HashMap<>();
		map.put("list", ans_list);
		map.put("start", flag);
		return JSON.toJSONString(map);
	}

	@Override
	public String getMoreNote(Integer user_id, List<Note>updateList) {
		//顶部下拉刷新
		NoteQuery query = new NoteQuery();
		List<Note>add_notes = getAddNote(user_id);
		//返回更新信息
		checkIsDie(updateList);
		if(add_notes.size() < 10){
/*			List<Note>hot_notes = getHotNote(user_id,add_notes,updateList);
			if(hot_notes != null) add_notes.addAll(hot_notes);*/
		}
		//检查帖子是否点过赞
		checkAction(add_notes, user_id);
		query.setUpdateList(updateList);
		query.setList(add_notes);
		return JSON.toJSONString(query);
	}
	 
	private List<Note> getHotNote(Integer user_id,List<Note>add_notes,List<Note>updateList) {
		int num = 10 - add_notes.size();
		//热门推送帖,先选取头1000条，随机用不重复的补
		//添加到集合中，方便对比
		Set<Note>set = new HashSet<>();
		set.addAll(add_notes);
		if(updateList != null) set.addAll(updateList);
		List<Note>ansNotes = new ArrayList<>();
		List<String>list = redisUtil.zsetRange(Const.HOT_NOTES, 0, 1000);
		if(list.size() > 0){
			int[]randomArray = null;
			if(list.size() >= num) {
				randomArray = randomArray(0, list.size() -1, num);
			}else {
				randomArray = randomArray(0, list.size() -1, list.size());
			}
			for(int randomNum : randomArray){
				String orgin_str = (String) redisUtil.getValue(list.get(randomNum));
				if(orgin_str != null){
					Note origin = JSON.parseObject(orgin_str, Note.class);
					if(set.size() > 0){
					    while (!set.contains(origin)) {
					    	ansNotes.add(origin);
						}
					}else {
						ansNotes.add(origin);
					}
				}else {
					//从推送榜中删除
					redisUtil.zsetDeleteKey(Const.HOT_NOTES, list.get(randomNum));
				}
			}
		}	
		return ansNotes;
	}

	private List<Note> getAddNote(Integer user_id) {
		//从list2队列中取出新增的
		String array_name = "list2_" + user_id;
		List<Note>notes = new ArrayList<>();
		List<String>keys = redisUtil.listGetAll(array_name);
		List<Integer>die_ids = new ArrayList<>();
		//增加此刻时间
		Long time = System.currentTimeMillis();
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
					origin.setFetchTime(time);
					Note note = noteDao.getNoteById(new Integer(strs[1]));
					note.setOrigin(origin);
					note.setFetchTime(time);
					notes.add(note);
					count++;
				}
			}else {
				//如果是原创帖
				String note_str = (String)redisUtil.getValue(key);
				if(note_str != null){
					Note temp = JSON.parseObject(note_str, Note.class);
					temp.setFetchTime(time);
					notes.add(temp);
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
		if(note == null || note.getTarget_id() == null || note.getTarget_id() == 0) throw new FadeException("请求格式不正确！");
		String origin_str = (String)redisUtil.getValue("note_" + note.getTarget_id());
		if(origin_str == null) throw new FadeException("操作失败，原贴已失效！");
		//续秒前检查是否已经续过
		Integer noteTempId = noteDao.getNoteQueryChangeSecond(note.getUser_id(),note.getTarget_id());
		if(noteTempId != null)  throw new FadeException("操作失败，已经续过秒");
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
			origin.setSub_num(sub_sum);
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
				//从排行榜中移除
				redisUtil.zsetDeleteKey(Const.HOT_NOTES, "note_" + origin.getNote_id());
				redisUtil.deleteKey("note_" + origin.getNote_id());
				return JSON.toJSONString(new SimpleResponse("0",null));//代表原贴已消失
			}
		}else {
			if(time_left <= (5l - past)) time_left = 5l - past;
		}
		logger.info("续一秒成功，原贴" + origin.getNote_id() + "的剩余时间为" + time_left + "分钟");
		//原主人的通知数量+1
		userDao.updateContributePlus(origin.getUser_id());
		redisUtil.addKey("note_"+origin.getNote_id(), JSON.toJSONString(origin),time_left, TimeUnit.MINUTES);
		//websocket通知原主人
		webSocketHandler.sendMessageToUser(origin.getUser_id(), JSON.toJSONString(new SimpleResponse("00", null)));
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
		//redisUtil.listRightPush("list2_"+note.getUser_id(),key);
		Map<String, Object>extra = new HashMap<>();
		//更新热门推送排行榜的排名
		redisUtil.zsetAddKey(Const.HOT_NOTES, "note_" + origin.getNote_id(), (double)(add_sum - sub_sum));
		//返回部分信息
		extra.put("post_time", post_time);		
		extra.put("note_id", note.getNote_id());
		extra.put("add_num", add_sum);
		extra.put("sub_num", sub_sum);
		extra.put("comment_num", origin.getComment_num());
		extra.put("fetchTime", System.currentTimeMillis());
		SimpleResponse response = new SimpleResponse("1", null, extra);
		logger.info("用户" + note.getUser_id() + "添加帖子 " + note.getNote_id() + "成功");
		return JSON.toJSONString(response);
	}
  
	public void checkAction(List<Note>notes, Integer user_id){
		//增加是否续或者减的属性
		for(Note note : notes){
			Integer type = null;
			if(note.getTarget_id() != null && note.getTarget_id() != 0){
				//转发帖，查询是否对原贴点赞
				type = noteDao.getNoteCheckAction(user_id,note.getTarget_id());
			}else {
				type = noteDao.getNoteCheckAction(user_id,note.getNote_id());
			}
			if(type == null){
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
	
	private void checkIsDie(List<Note>updateList){
		Long time = System.currentTimeMillis();
		//更新已加载过的数据，若不存在的话，is_die设置为0
		Note note = null;
		String origin_str = null;
		Note origin = null;
		for(Note temp : updateList){
			 if(temp.getTarget_id() != null && temp.getTarget_id() != 0){
				 //假如是转发的,更新的是origin的信息
				 origin_str = (String) redisUtil.getValue("note_" + temp.getTarget_id());
					if(origin_str != null){
						note = new Note();
						//还存活
						origin = JSON.parseObject(origin_str, Note.class);
						note.setAdd_num(origin.getAdd_num());
						note.setSub_num(origin.getSub_num());
						note.setComment_num(origin.getComment_num());
						note.setIs_die(1);
						note.setFetchTime(time);
						temp.setOrigin(note);
						temp.setIs_die(1);
						temp.setFetchTime(time);
					}else {
						//已死亡
						temp.setIs_die(0);
					}
			 }else {
				 origin_str = (String)redisUtil.getValue("note_" + temp.getNote_id());
				 if(origin_str != null){
					 origin = JSON.parseObject(origin_str, Note.class);
					 temp.setNote_id(origin.getNote_id());
					 temp.setAdd_num(origin.getAdd_num());
					 temp.setSub_num(origin.getSub_num());
					 temp.setComment_num(origin.getComment_num());
					 temp.setIs_die(1);
					 temp.setFetchTime(time);
				 }else {
					temp.setIs_die(0);
				}
			}
		}
	}
	
	@Override
	public String getNotePage(Integer note_id) throws FadeException {
		String note_str = (String) redisUtil.getValue("note_" + note_id);
		if(note_str == null) {
			redisUtil.setAddKey(Const.DIE_LIST, note_id.toString());
			throw new FadeException("该帖子已消失");
		}
		//详情页加载，10条续减一秒记录，10个评论
		CommentQuery commentQuery = commentService.getTenComment(note_id, 0);
		List<Comment>comments = commentQuery.getList();
		List<Note>second_list = getTenRelayNote(note_id,0*10);
		DetailPage page = new DetailPage();
		page.setComment_list(comments); 
		page.setSecond_list(second_list);
		//更新三个数量
		Note note = JSON.parseObject(note_str, Note.class);
		page.setComment_num(note.getComment_num());
		page.setAdd_num(note.getAdd_num());
		page.setSub_num(note.getSub_num());
		page.setFetchTime(System.currentTimeMillis());
		return JSON.toJSONString(page);
	}

	private List<Note> getTenRelayNote(Integer note_id, Integer page) {
		//获取十条增减秒,page为第几次加载，第一次填0
		return noteDao.getTenRelayNote(note_id,page);
	}
	
	@Override
	public String deleteNote(Integer note_id, Integer user_id) {
		//删除帖子
		SimpleResponse response = new SimpleResponse();
		//mysql删除
		if(noteDao.deleteNote(note_id) != null){
			response.setSuccess("删除成功");
		}else {
			response.setErr("删除失败");
		}	
		//redis删除
		redisUtil.deleteKey("note_" + note_id);
		//评论队列删除
		redisUtil.deleteKey("comment_" + note_id);
		return JSON.toJSONString(response);
	}

	@Override
	public String getMyNote(Integer user_id, Integer start) {
		NoteQuery query = new NoteQuery();
		List<Note>notes = noteDao.getMyNote(user_id,start);
		//图片数据
		for(Note note : notes){
			List<Image>images = noteDao.getNoteImage(note.getNote_id());
			note.setImages(images);
		}
		checkAction(notes, user_id);
		query.setList(notes);
		int size = notes.size();
		if(size == 0) query.setStart(0);
		else {
			query.setStart(notes.get(size -1).getNote_id());
		}	
		return JSON.toJSONString(query);
	}
	
	public int[] randomArray(int min,int max,int n){  
	    int len = max-min+1;  
	      
	    if(max < min || n > len){  
	        return null;  
	    }  
	      
	    //初始化给定范围的待选数组  
	    int[] source = new int[len];  
	       for (int i = min; i < min+len; i++){  
	        source[i-min] = i;  
	       }  
	         
	       int[] result = new int[n];  
	       Random rd = new Random();  
	       int index = 0;  
	       for (int i = 0; i < result.length; i++) {  
	        //待选数组0到(len-2)随机一个下标  
	           index = Math.abs(rd.nextInt() % len--);  
	           //将随机到的数放入结果集  
	           result[i] = source[index];  
	           //将待选数组中被随机到的数，用待选数组(len-1)下标对应的数替换  
	           source[index] = source[len];  
	       }  
	       return result;  
	}

	
	@Override
	public String getOtherPersonNote(Integer user_id, Integer my_id, Integer start) {
		NoteQuery query = new NoteQuery();
		List<Note>notes = noteDao.getMyNote(user_id,start);
		//图片数据
		for(Note note : notes){
			List<Image>images = noteDao.getNoteImage(note.getNote_id());
			note.setImages(images);
		}
		checkAction(notes, my_id);
		query.setList(notes);
		int size = notes.size();
		if(size == 0) query.setStart(0);
		else {
			query.setStart(notes.get(size -1).getNote_id());
		}	
		return JSON.toJSONString(query);
	}

	
	@Override
	public void addImage(List<Note> list) {
		//添加图片
		for(Note note : list){
			note.setImages(noteDao.getNoteImage(note.getNote_id()));
		}
	}

	@Override
	public String getFullNote(Integer note_id, Integer user_id) {
		Note note = noteDao.getNoteById(note_id);
		if(note != null){
			Integer type = null;
			if(note.getTarget_id() != null && note.getTarget_id() != 0){
				//转发帖，查询是否对原贴点赞
				type = noteDao.getNoteCheckAction(user_id,note.getTarget_id());
				//添加原贴
				Note origin = noteDao.getNoteById(note.getTarget_id());
				origin.setImages(noteDao.getNoteImage(origin.getNote_id()));
				note.setOrigin(origin);
			}else {
				type = noteDao.getNoteCheckAction(user_id,note.getNote_id());
				note.setImages(noteDao.getNoteImage(note_id));
			}
			if(type == null){
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
		return JSON.toJSONString(note);
	}  
}
