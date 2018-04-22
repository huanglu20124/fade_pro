 package com.fade.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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

import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGSelectQueryBlock.ForClause;
import com.alibaba.fastjson.JSON;
import com.fade.domain.CommentQuery;
import com.fade.domain.DetailPage;
import com.fade.domain.Image;
import com.fade.domain.Note;
import com.fade.domain.NoteQuery;
import com.fade.domain.Preference;
import com.fade.domain.SimpleResponse;
import com.fade.domain.UpdateMessage;
import com.fade.domain.User;
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
	
	@Resource(name = "solrService")
	private SolrService solrService;
	
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
		//设置uuid
		String uuid = UUID.randomUUID().toString();
		note.setUuid(uuid);
		//初始剩余时间
		note.setLiveTime(Const.DEFAULT_LIFE);
		//存储到数据库
		noteDao.addNote(note);
		//redisUtil.listRightPush(Const.INDEX_LIST, note.getNote_id().toString());
		//直接添加到索引库
		solrService.solrAddUpdateNote(note);
		//个人fade数量加1
		userDao.updateFadeNumPlus(note.getUser_id());
		if(note.getNote_id() == null) throw new FadeException("添加帖子失败！");
		//保存图片文件
		Map<String, Object>extra = new HashMap<>();
		if(files != null && note.getImages() != null){
			//--分目录存储防止一个文件夹中文件过多
			String hash = Integer.toHexString(uuid.hashCode());
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
		redisUtil.addKey(key, note, Const.DEFAULT_LIFE, TimeUnit.MINUTES);
		//添加到热门推送排行榜
		redisUtil.zsetAddKey(Const.HOT_NOTES, key, 0d);
		// 找到所有在线的粉丝，然后将它们的list2更新(这个后期要考虑优化，因为粉丝数量可能很大)
		List<Integer>all_fans_ids = userDao.getAllFansId(note.getUser_id()); 
		String temp = "note_"+note.getNote_id();
		for(Integer fans_id : all_fans_ids){
			if(redisUtil.setIsMember(Const.ONLINE_USERS, "user_" + fans_id.toString())){
				redisUtil.listRightPush("list2_"+fans_id,temp);
				//缓存被设置为5l
				redisUtil.setKeyTime("list2_"+fans_id, 5l, TimeUnit.MINUTES);
			}
		}
		//同时，自己的list1队列也要更新
		if(redisUtil.listGetSize("list1_" + note.getUser_id()) != 0){
			//缓存还在，添加到顶部
			redisUtil.listLeftPush("list1_" + note.getUser_id(), "note_" + note.getNote_id());
		}
		//返回部分信息
		extra.put("post_time", post_time);		
		extra.put("note_id", note.getNote_id());
		SimpleResponse response = new SimpleResponse("添加成功", null, extra);
		response.setExtra(extra);
		logger.info("用户" + note.getUser_id() + "添加帖子 " + note.getNote_id() + "成功");
		return JSON.toJSONString(response);
	}

	@Override
	public String getTenNoteByTime(Integer user_id, Integer start, Integer concern_num, List<Note>updateList) {
		//预处理，将已经显示有转发帖的原贴note_id放入checkSet中
		Set<Integer>addCheckSet = new HashSet<>();
		Set<Integer>subCheckSet = new HashSet<>();
		if(updateList != null && updateList.size() > 0){
			for(Note note : updateList){
				if(note.getTarget_id() != null && note.getTarget_id() != 0){
					//说明是转发帖，把原贴id放入
					if(note.getType() == 1){
						addCheckSet.add(note.getTarget_id());
					}else if (note.getType() == 2) {
						subCheckSet.add(note.getTarget_id());
					}			
				}
			}
		}
		
		Long time = System.currentTimeMillis();
		String array_name = "list1_"+user_id;//队列名字		
		//先直接查找前100 条，直到凑成10条 (仿票圈)，start=0的时候，定义为初次加载，先要清除redis缓存
		if(start == 0){
			//首次加载，清除所有缓存先
			redisUtil.deleteKey(array_name);
			//redisUtil.deleteKey("list2_" + user_id);
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
		List<Integer>die_ids = new ArrayList<>();//收集死亡的帖子，用于批处理更新数据库
		String search_key = null;//用于数据库查询的起点，查找比这个key小的帖子
		if(note_ids != null && note_ids.size() > 0) search_key = note_ids.get(note_ids.size() -1);
		int flag = start;//一个flag，用来找到下次的start索引，最后作为start返回
		if(note_ids.size() == 0) isNeed = true;
		else {
			for(String key : note_ids){
				String[]strs = key.split("_");
				if(strs.length == 3){
					//转发帖处理
					String origin_key = "note_"+strs[2];
					Integer origin_id = new Integer(strs[2]);
					if(die_ids.contains(origin_id)){
						//原创帖已死亡
						continue;
					}
					//转发贴，还要判断原贴存不存在
					Note origin = (Note)redisUtil.getValue(origin_key);
					if(origin == null){
						//原贴不存在，则都销毁原贴以及这个贴
						redisUtil.deleteKey(origin_key);
						die_ids.add(origin_id);
						//从队列中移除
						redisUtil.listRemoveValue(array_name, key);
					}else {
						//原贴存在的话，从数据库查询转发贴，并加上原贴，这里要考虑缓存
						Note note = noteDao.getNoteById(new Integer(strs[1]));
						if((note.getType() == 1 && !addCheckSet.contains(origin_id))
							|| (note.getType() == 2 && !subCheckSet.contains(origin_id))){
							origin.setFetchTime(time);
							note.setOrigin(origin);
							note.setFetchTime(time);
							ans_list.add(note);
							count++; 
							flag++;
							//加入，以便后面校验
							if(note.getType() == 1)addCheckSet.add(origin_id);
							else subCheckSet.add(origin_id);
						}
					}
				}else { 
					//原创帖，直接从redis中获取
					Note temp = (Note)redisUtil.getValue(key);
					if(temp != null){
						temp.setFetchTime(time);
						ans_list.add(temp);
						count++;
						flag++;
					}else {
						die_ids.add(new Integer(strs[1]));
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
					Note temp = (Note)redisUtil.getValue("note_" + note.getNote_id());
					if(temp != null){
						//帖子还没挂，加入到队列里(count小于10的时候)
						if(count < 10){
							temp.setFetchTime(time);
							ans_list.add(temp);
							count++;
							flag++;
						}
						//加入到redis队列中
						redisUtil.listRightPush(array_name, "note_" + note.getNote_id());	
						//logger.info("增加帖子"+note.getNote_id()+"到队列中");
					}else {
						//清除
						die_ids.add(note.getNote_id());
					}
				}else {
					//转发帖
					if(die_ids.contains(note.getTarget_id())){
						//原贴已死亡
						continue;
					}
					if((note.getType() == 1 && !addCheckSet.contains(note.getTarget_id()))
							|| (note.getType() == 2 && !subCheckSet.contains(note.getTarget_id()))){
						//转发帖，还需要先判断原创帖在不在
						Note origin = (Note)redisUtil.getValue("note_" + note.getTarget_id());
						if(origin == null){
							//该帖加入到死亡列表
							die_ids.add(note.getTarget_id());
						}else {
							//获得该帖的信息，加入到队列
							Note relay = noteDao.getNoteById(note.getNote_id());
							origin.setFetchTime(time);
							relay.setOrigin(origin);
							relay.setFetchTime(time);
							if(count < 10){
								ans_list.add(relay); 
								count++; 
								flag++;
								//加入，以便后面校验
								if(note.getType() == 1)addCheckSet.add(note.getTarget_id());
								else subCheckSet.add(note.getTarget_id());
							}
							//加入redis队列，key格式要区分开
							redisUtil.listRightPush(array_name, "note_" + note.getNote_id() + "_" + note.getTarget_id());
							//logger.info("增加帖子"+note.getNote_id()+"到队列中");
						}
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
		//最后设置队列的缓存为5l
		redisUtil.setKeyTime(array_name, 5l, TimeUnit.MINUTES);
		cleanDieOriginNote(die_ids);
		if(count < 10){
			//最后一次检查，如果数量小于10，热门推送进行补充
			List<Note>hot_notes = getRecommendNote(user_id,ans_list,updateList);
			if(hot_notes != null) ans_list.addAll(hot_notes);
		}
		//检查行为切面
		checkAction(ans_list, user_id);
		//给转发帖加上addUser以及subUser切面
		addConcernRelayData(ans_list,user_id);
		System.out.println("帖子数量=" + ans_list.size());
		NoteQuery query = new NoteQuery();
		query.setList(ans_list);
		query.setStart(flag);
		return JSON.toJSONString(query);
	}

	private void addConcernRelayData(List<Note> list, Integer user_id) {
		//先得到该用户关注的全部的人的id（后期加入缓存）
		List<Integer>user_ids = userDao.getStarUserId(user_id);
		List<Integer>result = null;
		for(Note temp : list){
			//针对转发帖：把自己关注的人的续秒或减秒记录合并进去，头10条
			if(temp.getTarget_id() != null &&  temp.getTarget_id() != 0){
				Integer origin_id = temp.getTarget_id();
				if(temp.getType() == 1){
					secondCacheReload(origin_id, 1);
					//增秒
					result = redisUtil.zsetGetAllIntList("add_" + origin_id);
				}else if (temp.getType() == 2) {
					//减秒
					secondCacheReload(origin_id, 2);
					//减
					result = redisUtil.zsetGetAllIntList("sub_" + origin_id);		
				}
				result.retainAll(user_ids);//取并集，同时保持原来顺序
				//再查找用户信息，这个方法后期要优化
				//假如第一位不是该转发帖的作者话，调过去
				if(result.size() > 0 && result.get(0) != temp.getUser_id()){
					result.remove(temp.getUser_id());
					result.add(0, temp.getUser_id());
				}
				//返回总数量
				temp.setRelayUserNum(result.size());
				//限制长度最多为10个
				if(result.size() > 10){
					result.subList(0, 10);
				}
				if(temp.getType() == 1){
					temp.setAddUsers(getUsers(result));
				}else {
					temp.setSubUsers(getUsers(result));
				}
			}
		}
	}
	
	private List<User> getUsers(List<Integer> resultList) {
		//由user_id得到用户的方法，后期要考虑缓存
		//List<User>users = userDao.getMulSimpleUser(resultList);
		List<User>users = new ArrayList<>();
		for(Integer user_id : resultList){
			users.add(userDao.getMinUser(user_id));
		}
		return users;
	}
	
	@Override
	public String getMoreNote(Integer user_id, List<Note>updateList) {
		//顶部下拉刷新
		NoteQuery query = new NoteQuery();
		Integer lastId = 0;
		if(updateList.size() > 0){
			int size = updateList.size();
			//取大的为lastId
			if(updateList.get(size - 1).getNote_id() >= updateList.get(0).getNote_id()){
				lastId = updateList.get(size - 1).getNote_id();
			}else {
				lastId = updateList.get(0).getNote_id();
			}
		}
		List<Note>add_notes = getAddNote(user_id,lastId,updateList);
		//返回更新信息
		checkIsDie(updateList);
		//数量还不足的话，添加热门贴和热门贴
		if(add_notes.size() < 10){
			//少于10条的话，从推荐帖子/热门帖子里面补充
			List<Note>hot_notes = getRecommendNote(user_id,add_notes,updateList);
			if(hot_notes != null && hot_notes.size() > 0) add_notes.addAll(hot_notes);
		}
		//检查帖子是否点过赞
		checkAction(add_notes, user_id);
		query.setUpdateList(updateList);
		query.setList(add_notes);
		return JSON.toJSONString(query);
	}
	 
	private List<Note> getRecommendNote(Integer user_id,List<Note>add_notes,List<Note>updateList) {
		int num = 10 - add_notes.size();
		//推荐热门推送帖,先选取头1000条，随机用不重复的补
		//添加到集合中，方便对比
		Set<Integer>set = new HashSet<>();
		for(Note note : add_notes){
			set.add(note.getNote_id());
		}
		for(Note note : updateList){
			set.add(note.getNote_id());
		}
		List<Note>ansNotes = new ArrayList<>();
		//从推荐中补充
		String idStr = userDao.getRecommendNote(user_id);
		if(idStr != null){
			List<Integer>ids = JSON.parseArray(idStr, Integer.class);
			for(Integer id : ids){
				if(!set.contains(id)){
					//找到原贴
					Note origin = (Note) redisUtil.getValue("note_" + id);
					if(origin != null) {
						ansNotes.add(origin);
						//ids.add(origin.getNote_id());
						num--;
					}
				}
				if(num == 0) break;
			}
		}

		if(num > 0){
			//还需要补充
			//从热门推送,头1000条中补充
			List<String>list = redisUtil.zsetRange(Const.HOT_NOTES, 0, 1000);
			if(list.size() > 0){
				int[]randomArray = null;
				if(list.size() >= num) {
					randomArray = randomArray(0, list.size() -1, num);
				}else {
					randomArray = randomArray(0, list.size() -1, list.size());
				}
				for(int randomNum : randomArray){
					Note origin = (Note) redisUtil.getValue(list.get(randomNum));
					if(origin != null){
						if(set.size() > 0){
						    if(!set.contains(origin.getNote_id())) {
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
		}
	
		return ansNotes;
	}

	private List<Note> getAddNote(Integer user_id, Integer lastId,List<Note>updateList) {
		//顶部下拉刷新，获得新帖子
		//这两个集合用于合并重复贴
		Set<Integer>addCheckSet = new HashSet<>();
		Set<Integer>subCheckSet = new HashSet<>();
		//从list2队列中取出新增的
		String array_name = "list2_" + user_id;
		List<Note>notes = new ArrayList<>();
		List<String>keys = redisUtil.listGetAll(array_name);
		List<Integer>die_ids = new ArrayList<>();
		List<String>toListKey = new ArrayList<>();
		//增加此刻时间
		Long time = System.currentTimeMillis();
		int count = 0;
		Set<Note>updateNotes = new HashSet<Note>();//用于检查重复
		updateNotes.addAll(updateList);
		if(keys.size() == 0){
			//list2队列为空，直接到数据库查询，仅查20条
			List<Note>addIds = noteDao.getAddNote(user_id,lastId);//addIds仅仅包含有note_id和target_id以及type,user_id
			for(Note note : addIds){
				if(!updateNotes.contains(note)){
					if(note.getTarget_id() == 0){
						//原创帖
						Note fullNote = (Note)redisUtil.getValue("note_" +note.getNote_id());
						if(fullNote == null){
							die_ids.add(new Integer(note.getNote_id())); 
						}else {
							fullNote.setFetchTime(time);
							notes.add(fullNote);
							toListKey.add("note_" + note.getNote_id());
						}
					}else {
						//转发帖
						if((note.getType() == 1 && !addCheckSet.contains(note.getTarget_id()))
								|| (note.getType() == 2 && !subCheckSet.contains(note.getTarget_id()))){
							Note origin = (Note) redisUtil.getValue("note_" + note.getTarget_id());
							if(origin == null){
								die_ids.add(note.getNote_id()); 
								die_ids.add(note.getTarget_id());
							}else {
								origin.setFetchTime(time); 
								note.setOrigin(origin);
								note.setFetchTime(time);
								notes.add(note);
								toListKey.add("note_" + note.getNote_id() + "_" + note.getTarget_id());
								if(note.getType() == 1) addCheckSet.add(origin.getNote_id());
								else subCheckSet.add(origin.getNote_id());
							}
						}
					}
				}
			}
		}else{
			for(String key : keys){
				String[]strs = key.split("_");
				if(strs.length == 3){
					//如果是转发帖
					Note origin = (Note)redisUtil.getValue("note_" + strs[2]);
					if(origin == null){
						die_ids.add(new Integer(strs[1])); 
						die_ids.add(new Integer(strs[2]));
					}else {
						origin.setFetchTime(time);
						Note note = noteDao.getNoteById(new Integer(strs[1]));
						if((note.getType() == 1 && !addCheckSet.contains(note.getTarget_id()))
								|| (note.getType() == 2 && !subCheckSet.contains(note.getTarget_id()))){
							//查重
							if(!updateNotes.contains(note)){
								note.setOrigin(origin);
								note.setFetchTime(time);
								notes.add(note);
								//toListKey.add("note_" + note.getNote_id() + "_" + note.getTarget_id());
								count++;
								if(note.getType() == 1) addCheckSet.add(origin.getNote_id());
								else subCheckSet.add(origin.getNote_id());
							}
						}
					}
				}else {
					//如果是原创帖
					Note temp = (Note)redisUtil.getValue(key);
					if(temp != null){
						//查重
						if(!updateNotes.contains(temp)){
							temp.setFetchTime(time);
							notes.add(temp);
							//toListKey.add("note_" + temp.getNote_id());
							count++;	
						}
					}
				}
				redisUtil.listLeftPop(array_name);//弹出队列，以免下次又刷新到
				if(count == 10){
					break;
				}
			}
		}
		//将增加的key加到list1(已取消)
/*		if(toListKey.size() > 0)
		redisUtil.listLeftPushAll("list1_" + user_id, toListKey.toArray());*/
		//因为可能出现原创帖，来一波加数据
		addConcernRelayData(notes, user_id);
		return notes;
	}

	@Override
	public String changeSecond(Note note, Integer user_id) throws FadeException{
		//增和减秒的请求，实则是增加帖子
		if(note == null || note.getTarget_id() == null || note.getTarget_id() == 0) throw new FadeException("请求格式不正确！");
		Note origin = (Note)redisUtil.getValue("note_" + note.getTarget_id());
		if(origin == null) throw new FadeException("操作失败，原贴已失效！");
		//自己续自己的情况
		if(origin.getUser_id() == note.getUser_id()){
			return changeSecondWithoutAdd(note);
		}
		//续秒前检查是否已经续过
		Integer noteTempId = noteDao.getNoteQueryChangeSecond(note.getUser_id(),note.getTarget_id());
		if(noteTempId != null)  throw new FadeException("操作失败，已经续过秒");
		//设置帖子时间以及其他参数，用户名是前端发来的 
		String post_time = TimeUtil.getCurrentTime();
		note.setPost_time(post_time);
		note.setAdd_num(0);
		note.setSub_num(0);
		note.setComment_num(0);
		//原贴加秒和减秒数量要更新，原贴生存时间更新
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
				noteDao.updateNoteDieSingle(origin.getNote_id());;
				logger.info("清理贴" + origin.getNote_id());
				//添加到索引数据库更新队列,更新is_die
				redisUtil.listRightPush(Const.INDEX_LIST, origin.getNote_id().toString());
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
		redisUtil.addKey("note_"+origin.getNote_id(), origin,time_left, TimeUnit.MINUTES);
		//设置原贴的流传时间（直接写入数据库）
		noteDao.updateLiveTime(origin.getNote_id(), past + time_left);
		//检查缓存是否需要重新加载
		secondCacheReload(note.getTarget_id(), note.getType());
		//存储到数据库
		noteDao.addNote(note);	
		//添加该记录到缓存
		if(note.getType() == 1){
			redisUtil.zsetAddKey("add_" + note.getTarget_id(), note.getUser_id(), note.getNote_id().doubleValue());
		}else {
			redisUtil.zsetAddKey("sub_" + note.getTarget_id(), note.getUser_id(), note.getNote_id().doubleValue());
		}

		//如果续减秒数量小于11，则需要刷新list4系列缓存
		if(add_sum + sub_sum <= 11)
		redisUtil.deleteKey("list4_" + note.getTarget_id());
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
				//该key的时间被重置为5l
				redisUtil.setKeyTime("list2_"+fans_id, 5l, TimeUnit.MINUTES);
			}
		}
		Map<String, Object>extra = new HashMap<>();
		//更新热门推送排行榜的排名
		redisUtil.zsetAddKey(Const.HOT_NOTES, "note_" + origin.getNote_id(), (double)(add_sum - sub_sum));
		//更新reference表
		createUpdateMessage(2, note.getNote_id(), user_id, origin.getUser_id());
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
	
	@Override
	public void checkAction(List<Note>notes, Integer user_id){
		//检查请求者对于notes的行为
		Integer origin_id;
		//增加是否续或者减的属性
		for(Note note : notes){
			Integer type = null;
			if(note.getTarget_id() != null && note.getTarget_id() != 0){
				//转发帖，查询是否对原贴点赞
				if(note.getOrigin().getAdd_num() == 0 && note.getOrigin().getSub_num() == 0){
					note.setAction(0);
					continue;
				}
				origin_id = note.getTarget_id();
				//type = noteDao.getNoteCheckAction(user_id,note.getTarget_id());
			}else {
				origin_id = note.getNote_id();
				if(note.getAdd_num() == 0 && note.getSub_num() == 0){
					note.setAction(0);
					continue;
				}
				//type = noteDao.getNoteCheckAction(user_id,note.getNote_id());
			}
			//假如续秒或减秒缓存为空，补上
			//检查缓存
			secondCacheReload(origin_id, 1);
			secondCacheReload(origin_id, 2);
			//点赞者作为value
			if(redisUtil.zsetisMember("add_" + origin_id,user_id)){
				//续过秒
				type = 1;
			}else {
				//没续过
				if(redisUtil.zsetisMember("sub_" + origin_id,user_id)){
					//减过秒
					type = 2;
				}else {
					type = 0;
				}
			}
			if(type == null || type == 0){
				//还没操作过
				note.setAction(0);
			}else if (type == 1) {
				//增
				note.setAction(1);
				//顺便把原贴的也设置了
			}else if (type == 2) {
				//减
				note.setAction(2);
			}
			
			if(note.getOrigin() != null){
				note.getOrigin().setAction(note.getAction());
			}
		}
	}
	
	private void checkIsDie(List<Note>updateList){
		Long time = System.currentTimeMillis();
		List<Integer>die_ids = new ArrayList<>();//收集死亡的帖子，用于批处理更新数据库
		//更新已加载过的数据，若不存在的话，is_die设置为0
		//有死的贴则更新到die_list
		Note note = null;
		Note origin = null;
		for(Note temp : updateList){
			 if(temp.getTarget_id() != null && temp.getTarget_id() != 0){
				 //假如是转发的,更新的是origin的信息
				 origin = (Note) redisUtil.getValue("note_" + temp.getTarget_id());
					if(origin != null){
						note = new Note();
						//还存活
						note.setAdd_num(origin.getAdd_num());
						note.setSub_num(origin.getSub_num());
						note.setComment_num(origin.getComment_num());
						note.setIs_die(1);
						note.setFetchTime(time);
						temp.setOrigin(note);
						temp.setIs_die(1);
						temp.setFetchTime(time);
					}else {
						//转发帖已死亡
						temp.setIs_die(0);
						die_ids.add(temp.getTarget_id());
					}
			 }else {
				 origin = (Note)redisUtil.getValue("note_" + temp.getNote_id());
				 if(origin != null){
					 temp.setNote_id(origin.getNote_id());
					 temp.setAdd_num(origin.getAdd_num());
					 temp.setSub_num(origin.getSub_num());
					 temp.setComment_num(origin.getComment_num());
					 temp.setIs_die(1);
					 temp.setFetchTime(time);
				 }else {
					 //原贴已死亡
					temp.setIs_die(0);
					die_ids.add(temp.getNote_id());				
				}
			}
		}
		//处理死亡贴
		cleanDieOriginNote(die_ids);
	}
	
	public void addImages(List<Note>notes) {
		//图片数据
		for(Note note : notes){
			if(note.getTarget_id() != null && note.getTarget_id() != 0){
				List<Image>images = noteDao.getNoteImage(note.getTarget_id());
				note.getOrigin().setImages(images);
			}else {
				List<Image>images = noteDao.getNoteImage(note.getNote_id());
				note.setImages(images);
			}
			
		}
	}
	
	@Override
	public String getNotePage(Integer note_id,Integer user_id,Integer getFull) throws FadeException {
		//实际上note_id应该就为原贴的id
		Note note = (Note) redisUtil.getValue("note_" + note_id);
		if(note == null) {
			//帖子已消失，需要更新数据库
			noteDao.updateNoteDieSingle(note_id);
			logger.info("清理贴" + note_id);
			//从热门榜中清除
			redisUtil.zsetDeleteKey(Const.HOT_NOTES, "note_"+note_id);
			//添加到索引数据库更新队列,更新is_die
			redisUtil.listRightPush(Const.INDEX_LIST,note_id.toString());
			//throw new FadeException("该帖子已消失");
		}
		if(note == null){
			note = noteDao.getNoteById(note_id);
		}
		Integer query_id = null;
		Integer owner_id = null;
		if(note.getTarget_id() == null || note.getTarget_id() == 0) {
			query_id = note.getNote_id();
		    owner_id = note.getUser_id();
		}else {
			query_id = note.getTarget_id();
			owner_id = note.getOrigin().getUser_id();
		}
		//详情页加载，10条续减一秒记录，10个评论
		CommentQuery commentQuery = commentService.getTenComment(query_id, 0);
		NoteQuery noteQuery = getTenRelayNote(query_id,owner_id,0);
		DetailPage page = new DetailPage();
		page.setCommentQuery(commentQuery);
		page.setNoteQuery(noteQuery);
		//更新三个数量
		if(getFull == 1){
			//获取完整note
			if(note != null){
				Integer type = null;
				Note originNote = null;
				if(note.getTarget_id() != null && note.getTarget_id() != 0){
					//转发帖，查询是否对原贴点赞
					type = noteDao.getNoteCheckAction(user_id,note.getTarget_id());
					//添加原贴
					Note origin = noteDao.getNoteById(note.getTarget_id());
					origin.setImages(noteDao.getNoteImage(origin.getNote_id()));
					note.setOrigin(origin);
					originNote = origin;
				}else {
					type = noteDao.getNoteCheckAction(user_id,note.getNote_id());
					note.setImages(noteDao.getNoteImage(note_id));
					originNote = note;
				}
				if(type == null){
					//假如是自己的帖子
					if(originNote != null && originNote.getUser_id() == user_id){
						note.setAction(userDao.getIsMySecond(user_id,originNote.getNote_id()));
					}else {
						//还没操作过
						note.setAction(0);
					}
					if(note.getAction() == null) note.setAction(0);
				}else if (type == 1) {
					//增
					note.setAction(1);
				}else if (type == 2) {
					//减
					note.setAction(2);
				}
				page.setNote(note);
			}
		}else {
			//获取部分
			Note part = new Note();
			part.setComment_num(note.getComment_num());
			part.setAdd_num(note.getAdd_num());
			part.setSub_num(note.getSub_num());
			part.setFetchTime(System.currentTimeMillis());
			page.setNote(part);
		}
		//更新reference表
		createUpdateMessage(1, note_id, user_id, owner_id);
		return JSON.toJSONString(page);
	}

	@Override
	public NoteQuery getTenRelayNote(Integer note_id, Integer owner_id, Integer start) {
		//获取十条增减秒,page为第几次加载，第一次填0
		//头十条记录到缓存里找，没有的话查询数据库，每次还要查帖子主人是否点赞
		List<Note>list = new ArrayList<>();
		Boolean isNeed = false; //需不需要查询主人对帖子的续秒情况
		String array_name = "list4_" + note_id;
		if(start == 0){
			//说明是用于详情页的，需要查询缓存
			List<Object>cacheList = redisUtil.listGetAllObject(array_name);
			if(cacheList == null || (cacheList != null &&cacheList.size() == 0)){
				//没有缓存
				list.addAll(noteDao.getTenRelayNote(note_id,0));//查询对应分页数据
				isNeed = true;
			}else {
				//有缓存
				for(Object object : cacheList){
					list.add((Note)object);
				}
				isNeed = false;
			}
		}else {
			list.addAll(noteDao.getTenRelayNote(note_id,start*10));//查询对应分页数据
			isNeed = true;
		}

		if(isNeed){
			//需要查询主人对帖子的续秒情况
			Note myAddSecondNote = userDao.getMyAddSecondNote(note_id, owner_id);
			if(myAddSecondNote != null){
				//主人自己对自己续过
				Date insertDate = TimeUtil.getTimeDate(myAddSecondNote.getPost_time());
				int flag = 0;
				//插入到原纪录中
				for(int i = 0; i < list.size(); i++){
					//大于返回1
					if(TimeUtil.getTimeDate(list.get(i).getPost_time())
							.compareTo(insertDate) == 1){
						flag = i;
						break;
					}
				}
				//前一条的位置，插入
				int position = flag >= 0 ? flag : 0;
				list.add(position, myAddSecondNote);
			}
		}
		if(start == 0){
			//缓存list，时间10l
			redisUtil.deleteKey(array_name);
			if(list.size() > 0){
				redisUtil.listRightPushAll(array_name, list.toArray());
				redisUtil.setKeyTime(array_name, 10l, TimeUnit.MINUTES);
			}
		}
		NoteQuery noteQuery = new NoteQuery();
		noteQuery.setList(list);
		noteQuery.setStart(start + 1);
		return noteQuery;
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
		//得到自己的原创fade
		NoteQuery query = new NoteQuery();
		List<Note>notes = noteDao.getMyNote(user_id,start);
		long time = System.currentTimeMillis();
		int k = 0;
		//图片数据
		for(Note note : notes){
			if(note.getIs_die() == 1){
				//活帖的话，直接从redis取
				Note cahcheNote = (Note) redisUtil.getValue("note_" + note.getNote_id());
				if(cahcheNote == null){
					//帖子已死亡，按照死贴处理
					note.setIs_die(0);
					List<Image>images = noteDao.getNoteImage(note.getNote_id());
					note.setImages(images);
				}else {
					note.setIs_die(1);
					note.setImages(cahcheNote.getImages());
				}
			}else {
				List<Image>images = noteDao.getNoteImage(note.getNote_id());
				note.setImages(images);
			}
			//设置fetchTime
			note.setFetchTime(time);
			k++;
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
		//获得他人的原创fade
		NoteQuery query = new NoteQuery();
		List<Note>notes = noteDao.getMyNote(user_id,start);
		//设置fetchTime
		long time = System.currentTimeMillis();
		//图片数据
		for(Note note : notes){
			if(note.getIs_die() == 1){
				//活帖的话，直接从redis取
				Note cahcheNote = (Note) redisUtil.getValue("note_" + note.getNote_id());
				if(cahcheNote == null){
					//帖子已死亡，按照死贴处理
					note.setIs_die(0);
					List<Image>images = noteDao.getNoteImage(note.getNote_id());
					note.setImages(images);
				}else {
					note.setIs_die(1);
					note.setImages(cahcheNote.getImages());
				}
			}else {
				List<Image>images = noteDao.getNoteImage(note.getNote_id());
				note.setImages(images);
			}
			note.setFetchTime(time);
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
		Note originNote = null;
		long time = System.currentTimeMillis();
		if(note != null){
			Integer type = null;
			if(note.getTarget_id() != null && note.getTarget_id() != 0){
				//转发帖，查询是否对原贴点赞
				type = noteDao.getNoteCheckAction(user_id,note.getTarget_id());
				//添加原贴
				Note origin = noteDao.getNoteById(note.getTarget_id());
				origin.setImages(noteDao.getNoteImage(origin.getNote_id()));
				note.setOrigin(origin);
				originNote = origin;
			}else {
				type = noteDao.getNoteCheckAction(user_id,note.getNote_id());
				note.setImages(noteDao.getNoteImage(note_id));
				originNote = note;
			}
			note.setFetchTime(time);
			if(type == null){
				//假如是自己的帖子
				if(originNote != null && originNote.getUser_id() == user_id){
					note.setAction(userDao.getIsMySecond(user_id,originNote.getNote_id()));
				}else {
					//还没操作过
					note.setAction(0);
				}
				if(note.getAction() == null) note.setAction(0);
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
	
	@Override
	public String searchNote(String keyword, Integer page, Integer isAlive,Integer user_id) {
		//搜索帖子，可选择活贴或者死贴, 0死贴， 1活帖
		NoteQuery query = solrService.getTenNoteKeyWord(keyword,page,isAlive);
		List<Note>fullList = new ArrayList<>();
		long time = System.currentTimeMillis();
		//补全帖子属性
		for(Note simpleNote : query.getList()){
			if(simpleNote.getIs_die() == 1){
				//活帖，直接从redis中取数据
				Note note = (Note) redisUtil.getValue("note_" + simpleNote.getNote_id());
				if(note != null){
					note.setNote_content(simpleNote.getNote_content());//因为从索引数据库里可能得到带高亮的内容
					//设置fetchTime
					note.setFetchTime(time);
					fullList.add(note);
				}
			}else {
				//从数据库中查询
				Note note = noteDao.getNoteById(simpleNote.getNote_id());
				note.setNote_content(simpleNote.getNote_content());//因为从索引数据库里可能得到带高亮的内容
				//设置fetchTime
				note.setFetchTime(time);
				fullList.add(note);
			}

		}
		if(fullList.size() == 0){
			//出现这种情况，可能是因为输入者输入非关键词的，需要到mysql数据库模糊查询一些记录，补充一下
			//分页查询，得到前50条，再得到分页
		    //List<Note>temps = noteDao.searchLimitNote(keyword,50,isAlive);
		    
		    //query.setSum(temps.size());
		}
		addImage(fullList);
		checkAction(fullList, user_id);
		query.setList(fullList);
		return JSON.toJSONString(query);
	}

	@Override
	public String changeSecondWithoutAdd(Note note) throws FadeException{
		//自己对自己点赞
		if(note == null || note.getTarget_id() == null || note.getTarget_id() == 0) throw new FadeException("请求格式不正确！");
		Note origin = (Note)redisUtil.getValue("note_" + note.getTarget_id());
		if(origin == null) throw new FadeException("操作失败，原贴已失效！");
		//续秒前检查是否已经续过
		Integer noteTempId = noteDao.getNoteQueryChangeSecond(note.getUser_id(),note.getTarget_id());
		if(noteTempId != null)  throw new FadeException("操作失败，已经续过秒");
		//添加记录到myadd_second中
		userDao.addMySecond(note.getUser_id(), note.getTarget_id(), note.getType(),TimeUtil.getCurrentTime());
		secondCacheReload(note.getTarget_id(), note.getType());
		//因为是自己对自己续，没有转发帖，note_id设置为0
		note.setNote_id(0);
		String key = (note.getType() == 1 ? "add_" : "sub_" ) + note.getTarget_id();
		redisUtil.zsetAddKey(key, note.getUser_id(), 0d);
		//原贴加秒和减秒数量要更新，原贴生存时间更新
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
				noteDao.updateNoteDieSingle(origin.getNote_id());;
				logger.info("清理贴" + origin.getNote_id());
				//添加到索引数据库更新队列,更新is_die
				redisUtil.listRightPush(Const.INDEX_LIST, origin.getNote_id().toString());
				//从排行榜中移除
				redisUtil.zsetDeleteKey(Const.HOT_NOTES, "note_" + origin.getNote_id());
				redisUtil.deleteKey("note_" + origin.getNote_id());
				return JSON.toJSONString(new SimpleResponse("0",null));//代表原贴已消失
			}
		}else {
			if(time_left <= (5l - past)) time_left = 5l - past;
		}
		redisUtil.addKey("note_"+origin.getNote_id(), origin,time_left, TimeUnit.MINUTES);
		logger.info("续一秒成功，原贴" + origin.getNote_id() + "的剩余时间为" + time_left + "分钟");
		//如果续减秒数量小于11，则需要刷新list4系列缓存
		if(add_sum + sub_sum <= 11)
		redisUtil.deleteKey("list4_" + note.getTarget_id());
		Map<String, Object>extra = new HashMap<>();
		//设置原贴的流传时间（直接写入数据库）
		noteDao.updateLiveTime(origin.getNote_id(), past + time_left);
		//更新热门推送排行榜的排名
		redisUtil.zsetAddKey(Const.HOT_NOTES, "note_" + origin.getNote_id(), (double)(add_sum - sub_sum));
		//更新reference表
		createUpdateMessage(2, origin.getNote_id(), origin.getUser_id(), origin.getUser_id());
		//返回部分信息	
		extra.put("add_num", add_sum);
		extra.put("sub_num", sub_sum);
		extra.put("comment_num", origin.getComment_num());
		extra.put("fetchTime", System.currentTimeMillis());
		SimpleResponse response = new SimpleResponse("1", null, extra);
		logger.info("用户" + note.getUser_id() + "对自己续秒成功");
		return JSON.toJSONString(response);
	}
	
	@Override
	public NoteQuery getLiveNote(Integer user_id, Integer my_id, Integer start) {
		NoteQuery query = new NoteQuery();
		List<Integer>die_ids = new ArrayList<>();
		long time = System.currentTimeMillis();
		//获取自己活着他人的动态帖子
		List<Note>list = new ArrayList<>();
		List<Note>notes = noteDao.getLiveNote(user_id,start);
		for(Note temp:notes){
			//因为是活帖，从缓存直接拿
			if(temp.getTarget_id() != 0){
				//temp为转发帖
				Note origin = (Note)redisUtil.getValue("note_" + temp.getTarget_id());
				if(origin == null){
					//该帖加入到死亡列表
					die_ids.add(temp.getNote_id());
					die_ids.add(temp.getTarget_id());
				}else {
					//获得该帖的信息，加入到队列
					Note relay = noteDao.getNoteById(temp.getNote_id());
					origin.setFetchTime(time);
					relay.setOrigin(origin);
					relay.setFetchTime(time);
					list.add(relay);
				}
			}else {
				//说明temp是原创帖
				Note note = (Note)redisUtil.getValue("note_" + temp.getNote_id());
				if(note != null){
					note.setFetchTime(time);
					list.add(note);
				}else {
					//清除
					die_ids.add(temp.getNote_id());
				}
			}
		}
		checkAction(list, my_id);
		//清理死亡贴
		cleanDieOriginNote(die_ids);
		query.setList(list);
		int size = notes.size();
		if(size == 0) query.setStart(0);
		else {
			query.setStart(notes.get(size -1).getNote_id());
		}
		return query;
	}
	
	private void secondCacheReload(Integer target_id, int type) {
		//检查续一秒记录以及减一秒记录是否有缓存，没有的话查询数据库重新记载
		Note origin = (Note) redisUtil.getValue("note_" + target_id);
		if(origin == null){
			origin = noteDao.getNoteById(target_id);
		}
		//1为增， 2为减
		String addKey = "add_" + target_id;
		String subKey = "sub_" + target_id;
		if(type == 1){
			if(redisUtil.zsetGetSize(addKey) == 0l){
				//队列为空，读数据库，加载到redis，十五分钟
				//返回全部续秒的人的id
				List<Note>addNotes = noteDao.getAddAll(target_id);//得到全部续秒转发帖
				Note selfNote= null;
				if((selfNote = userDao.getMyAddSecondNote(target_id, origin.getUser_id())) != null
						&& selfNote.getType() == 1){
					selfNote.setNote_id(0);
					addNotes.add(selfNote);
				}
				for(Note note : addNotes){
					redisUtil.zsetAddKey(addKey, note.getUser_id(), note.getNote_id().doubleValue());
				}
			}
			redisUtil.setKeyTime(addKey, 15l, TimeUnit.MINUTES);
		}else if (type == 2) {
			if(redisUtil.zsetGetSize(subKey) == 0l){
				//队列为空，读数据库，加载到redis，十五分钟
				//返回全部续秒的人的id
				List<Note>subNotes = noteDao.getSubAll(target_id);//得到全部续秒转发帖
				Note selfNote= null;
				if((selfNote = userDao.getMyAddSecondNote(target_id, origin.getUser_id())) != null
						&& selfNote.getType() == 2){
					selfNote.setNote_id(0);
					subNotes.add(selfNote);
				}
				for(Note note : subNotes){
					redisUtil.zsetAddKey(subKey, note.getUser_id(), note.getNote_id().doubleValue());
				}
			}
			redisUtil.setKeyTime(subKey, 15l, TimeUnit.MINUTES);
		}
		
	}

	@Override
	public String getConcernSecondNote(Integer user_id, Integer target_id,
			Integer start, Integer type) {
		//点开折叠列表，获取20条记录, type为1和2，分别代表增和减，按时间倒序
		NoteQuery query = new NoteQuery();
		List<Note>list = new ArrayList<>();
		//得到全部关注者的id
		List<Integer>user_ids = userDao.getStarUserId(user_id);
		//检查、重新加载缓存
		secondCacheReload(target_id, type);
		String key = (type == 1 ? "add_" : "sub_") + target_id;
		List<Integer>result = redisUtil.zsetGetAllIntList(key);
		//取交集
		result.retainAll(user_ids);
		List<Note>addNotes = new ArrayList<>();
		for(Integer otherUser_id : result){
			Double temp = redisUtil.zsetScore(key, otherUser_id);
			if(temp != null){
				Note note = noteDao.getRelayNoteById(temp.intValue());
				addNotes.add(note);
			}
		}
		int size = addNotes.size();
		int end = size > start + 20 ? start + 20 : size;
		if(start < size){
			list = addNotes.subList(start, end);
		}
		query.setList(list);
		query.setStart(end);
		return JSON.toJSONString(query);
	}
	
	@Override
	public String getAllSecond(Integer user_id, Integer target_id, Integer start, Integer type) {
		//包括自己，得到20条续秒或者减秒的列表
		//这里的user_id是原帖作者的
		NoteQuery query = new NoteQuery();
		//检查、重新加载缓存
		secondCacheReload(target_id, type);
		String key = (type == 1 ? "add_" : "sub_") + target_id;
		List<Integer>result = redisUtil.zsetRangeInt(key, start, start+20);
		List<Note>addNotes = new ArrayList<>();
		for(Integer otherUser_id : result){
			Double temp = redisUtil.zsetScore(key, otherUser_id);
			if(temp != null && temp != 0){
				Note note = noteDao.getRelayNoteById(temp.intValue());
				addNotes.add(note);
			}
		}
		//查询原帖作者有没有续或者减
		Note myAddNote = userDao.getMyAddSecondNote(target_id, user_id);
		if(myAddNote != null){
			//的确有赞过
			if(addNotes.size() > 0){
				if(myAddNote.getType() == type){
					//主人自己对自己续过
					Date insertDate = TimeUtil.getTimeDate(myAddNote.getPost_time());
					int flag = 0;
					//插入到原纪录中
					for(int i = 0; i < addNotes.size(); i++){
						//大于返回1
						if(TimeUtil.getTimeDate(addNotes.get(i).getPost_time())
								.compareTo(insertDate) == 1){
							flag = i;
							break;
						}
					}
					//前一条的位置，插入
					int position = flag >= 0 ? flag : 0;
					addNotes.add(position, myAddNote);
				}
			}else {
				if(myAddNote.getType() == type){
					addNotes.add(myAddNote);
				}
			}	
		}		
		query.setList(addNotes);
		query.setStart(start + addNotes.size());
		return JSON.toJSONString(query);
	}
	
	private void cleanDieOriginNote(List<Integer>die_ids){
		//清理死亡贴
		if(die_ids.size() > 0){
			noteDao.updateNoteDie(die_ids);
			for(Integer die_id : die_ids){
				logger.info("清理贴及其转发帖" + die_id);
				//更新转发帖的is_die
				noteDao.updateRelayNoteDie(die_id);
				//从热门榜中清除
				redisUtil.zsetDeleteKey(Const.HOT_NOTES, "note_"+die_id);
				//删除其续秒及减秒的缓存
				redisUtil.deleteKey("add_" + die_id);
				redisUtil.deleteKey("sub_" + die_id);
				//添加到索引数据库更新队列,更新is_die
				redisUtil.listRightPush(Const.INDEX_LIST, die_id.toString());
			}
		}
	}

	private void createUpdateMessage(Integer msgId, Integer note_id, Integer user_id,Integer owener_id){
		UpdateMessage updateMessage = new UpdateMessage(msgId, note_id, user_id, owener_id);
		redisUtil.listRightPush(Const.PREFERENCE_LIST, updateMessage);
	}

	@Override
	public void updateReference(UpdateMessage message) {
		//更新用户偏好表
		Preference preference = new Preference(message.getUser_id(), message.getNote_id(),null);
		Double score = 0d;
		switch (message.getMsgId()) {
		case 1:
			//1.打开详情页,+0.5
			score = 0.5;
			break;
		case 2:
			//2.某人对该帖续或减秒，评论一次+1
			score = 1.0;
			break;
		default:
			break;
		}
		if(noteDao.getPreference(message.getNote_id(), message.getUser_id()) == null){
			//检查是否是同校
			//检查是否同系
			//没有的话先插入
			preference.setScore(score);
			logger.info("插入偏好记录note_id=" + message.getNote_id() + ", user_id=" + message.getUser_id());
			noteDao.addPreference(preference);
		}else {
			//更新分数
			logger.info("更新note_id=" + message.getNote_id() + ", user_id=" + message.getUser_id());
			noteDao.updatePreference(message.getNote_id(), message.getUser_id(), score);
		}
		
	}
	

}
