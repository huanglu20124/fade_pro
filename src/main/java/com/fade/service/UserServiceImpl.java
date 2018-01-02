package com.fade.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.fade.domain.AddMessage;
import com.fade.domain.Comment;
import com.fade.domain.CommentQuery;
import com.fade.domain.Image;
import com.fade.domain.Note;
import com.fade.domain.NoteQuery;
import com.fade.domain.PersonPage;
import com.fade.domain.SimpleResponse;
import com.fade.domain.TokenModel;
import com.fade.domain.User;
import com.fade.domain.UserQuery;
import com.fade.exception.FadeException;
import com.fade.mapper.CommentDao;
import com.fade.mapper.NoteDao;
import com.fade.mapper.UserDao;
import com.fade.util.Const;
import com.fade.util.RedisUtil;
import com.fade.util.TimeUtil;
import com.fade.util.TokenUtil;

@Service("userService")
public class UserServiceImpl implements UserService {
	public static Logger logger = Logger.getLogger(UserServiceImpl.class);
	
	@Autowired
	private UserDao userDao;

	@Resource(name = "noteDao")
	private NoteDao noteDao;
	
	@Resource(name = "commentDao")
	private CommentDao commentDao;
	
	@Resource(name = "redisUtil")
	private RedisUtil redisUtil;

	@Resource(name = "tokenUtil")
	private TokenUtil tokenUtil;

	@Resource(name = "solrService")
	private SolrService solrService;
	
	@Override
	public String getUserById(Integer user_id) throws FadeException {
		// 返回用户详细信息
		User user = userDao.getUserById(user_id);
		if (user == null)
			throw new FadeException("查询到的用户为不存在");
		else {
			return JSON.toJSONString(user);
		}
	}

	@Override
	public String loginWechat(String wechat_id) throws FadeException {
		User user = userDao.getUserByOpenId(wechat_id, 0);
		if (user == null)
			throw new FadeException("查询到的用户为空");
		else {
			// 新建tokenModel并返回
			Map<String, Object> map = new HashMap<>();
			TokenModel model = tokenUtil.createTokenModel(user.getUser_id());
			map.put("tokenModel", model);
			map.put("user", user);
			// 记录日志
			logger.info("user_id=" + user.getUser_id() + ", user_name=" + user.getNickname() + " 登录成功");
			//redis上线
			redisUtil.addKey(Const.ONLINE_USERS, "user_"+user.getUser_id());
			return JSON.toJSONString(map);
		}
	}

	@Override
	public String registerWechat(String js_code, User user) throws FadeException {
		// 如果客户端本地没有检测到，则发送小程序code、以及一些用户信息到服务器，服务器请求得到openid，返回给小程序
		logger.info("js_code=" + js_code + "请求注册账号");
		try {
			URL url = new URL("https://api.weixin.qq.com/sns/jscode2session?appid=" + Const.APP_ID + "&secret="
					+ Const.AppSecret + "&js_code=" + js_code + "&grant_type=authorization_code");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			// 得到输入流
			InputStream is = connection.getInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len = 0;
			while (-1 != (len = is.read(buffer))) {
				baos.write(buffer, 0, len);
				baos.flush();
			}
			String str = baos.toString("utf-8");
			logger.info("向腾讯服务器请求的结果是：" + str);
			JSONObject temp = new JSONObject(str);
			String wechat_id = temp.getString("openid");
			user.setWechat_id(wechat_id);
			// 生成fade_name 注册时间
			String uuid = UUID.randomUUID().toString();
			String fade_name = "fade_" + uuid.substring(30);
			user.setFade_name(fade_name);
			user.setRegister_time(TimeUtil.getCurrentTime());
			if (userDao.getUserByOpenId(wechat_id, 0) == null) {
				// 设置盐，MD5加密，散列一次
				String salt = UUID.randomUUID().toString().substring(0, 5);
				user.setSalt(salt);
				String password_md5 = new Md5Hash(user.getPassword(), salt, 1).toString();
				user.setPassword(password_md5);
				userDao.addUser(user);// user_id设置到user对象中
				//添加到索引数据库
				user.setUuid(uuid);
				solrService.solrAddUpdateUser(user);
				// 新建tokenModel并返回
				Map<String, Object> map = new HashMap<>();
				TokenModel model = tokenUtil.createTokenModel(user.getUser_id());
				map.put("tokenModel", model);
				logger.info("wechat_id=" + wechat_id + ",user_id=" + user.getUser_id() + " 注册登录成功");
				//redis上线
				redisUtil.addKey(Const.ONLINE_USERS, "user_"+user.getUser_id());
				//返回部分信息
				Map<String, Object> extra = new HashMap<>();
				extra.put("tokenModel", model);
				extra.put("register_time", user.getRegister_time());
				extra.put("fade_name", user.getFade_name());
				SimpleResponse response = new SimpleResponse("注册成功",null,extra);
				return JSON.toJSONString(response);
			} else {
				throw new FadeException("注册失败，该wechat_id的账号已被注册");
			}
		} catch (Exception e) {
			throw new FadeException("与腾讯服务器的连接异常，注册微信账号失败");
		}
	}

	@Override
	public String registerQueryTel(String telephone) {
		// 查询手机号是否被注册
		SimpleResponse response = new SimpleResponse(null, null);
		if (userDao.getUserByTel(telephone) == null) {
			response.setSuccess("0");
		} else {
			response.setSuccess("1");
		}
		return JSON.toJSONString(response);
	}

	@Override
	public String registerByName(User user,MultipartFile file) throws FadeException {
		// 安卓端昵称密码的注册方式，包括上传头像
		if(file != null){
			//先处理文件
			String dir_path = Const.DATA_PATH + "image/head/" + TimeUtil.getYearMonth() + "/";
			File dir = new File(dir_path);
			if(!dir.exists()) dir.mkdirs();
			// 找到后缀名
			String origin_file_name = file.getOriginalFilename();
			int flag = origin_file_name.lastIndexOf(".");
			String tail = origin_file_name.substring(flag, origin_file_name.length());
			String file_name = UUID.randomUUID().toString().substring(0, 10);
			File save_file = new File(dir_path + file_name + tail);
			try {
				file.transferTo(save_file);
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				throw new FadeException("上传头像图片异常！");
			}
			String head_url = "image/head/" + TimeUtil.getYearMonth() + "/" + file_name + tail;
			user.setHead_image_url(head_url);
		}
		// 生成fade_name和注册时间
		String uuid = UUID.randomUUID().toString();
		String fade_name = "fade_" + uuid.substring(30);
		user.setFade_name(fade_name);
		user.setRegister_time(TimeUtil.getCurrentTime());
		// 设置盐，MD5加密，散列一次
		String salt = UUID.randomUUID().toString().substring(0, 5);
		String password_md5 = new Md5Hash(user.getPassword(), salt, 1).toString();
		user.setPassword(password_md5);
		userDao.addUser(user);// 主键属性被自动赋予到user中
		userDao.addMessage(user.getUser_id());//到新增消息队列里“注册”
		//添加到solr数据库中
		user.setUuid(uuid);
		solrService.solrAddUpdateUser(user);
		if(user.getUser_id() == null) throw new FadeException("注册失败");
		logger.info("user_id=" + user.getUser_id() + ",nickname=" + user.getNickname() + " 注册成功");
		// 把新建的盐写入盐表
		userDao.addSalt(user.getUser_id(), salt);
		// 新建tokenModel并返回,key为user_id
		TokenModel model = tokenUtil.createTokenModel(user.getUser_id());
		//redis上线
		redisUtil.addKey(Const.ONLINE_USERS, "user_"+user.getUser_id());
		//返回部分信息
		Map<String, Object> extra = new HashMap<>();
		extra.put("tokenModel", model);
		extra.put("register_time", user.getRegister_time());
		extra.put("fade_name", user.getFade_name());
		extra.put("head_image_url", user.getHead_image_url());
		SimpleResponse response = new SimpleResponse("注册成功",null,extra);
		//返回部分有用信息
		return JSON.toJSONString(response);
	}

	@Override
	public String loginUser(User user){
		// 安卓端昵称密码的登录方式
		User ans_user = null;
		String origin_password = null;
		String md5_password = null;
		// 手机登录
		if (user.getTelephone() != null) {
			// 从数据库查询盐
			String salt = userDao.getSaltByTel(user.getTelephone());
			if (salt != null) {
				origin_password = user.getPassword();
				md5_password = new Md5Hash(origin_password, salt, 1).toString();
				user.setPassword(md5_password);
			}
			if ((ans_user = (User) userDao.getUserByTelPwd(user)) == null) {
				return JSON.toJSONString(new SimpleResponse(null, "账号不存在或密码错误！"));
			}
		}
		// Fade账号登录
		else if (user.getFade_name() != null) {
			// 从数据库查询盐
			String salt = userDao.getSaltByFadeName(user.getFade_name());
			if (salt != null) {
				origin_password = user.getPassword();
				md5_password = new Md5Hash(origin_password, salt, 1).toString();
				user.setPassword(md5_password);
			}
			if ((ans_user = (User) userDao.getUserByFadePwd(user)) == null) {
				return JSON.toJSONString(new SimpleResponse(null, "账号不存在或密码错误！"));
			}
		}

		TokenModel model = tokenUtil.createTokenModel(ans_user.getUser_id());
		ans_user.setTokenModel(model);
		// 最后要还原密码
		if (origin_password != null)
			ans_user.setPassword(origin_password);
		//redis上线
		redisUtil.setAddKey(Const.ONLINE_USERS, "user_"+user.getUser_id());
		logger.info("user_id=" + ans_user.getUser_id() + ",nickname=" + ans_user.getNickname() + " 登录成功");
		return JSON.toJSONString(ans_user);
	}

	@Override
	public String updateUserById(User user,MultipartFile file) throws FadeException {
		// 先得到原本的用户信息
		User origin = userDao.getUserById(user.getUser_id());
		String origin_nickname = origin.getNickname();
		String origin_head_url = origin.getHead_image_url();
		// 编辑用户信息(部分)，包括上传头像
		if(file != null){
			//先处理文件，找到原文件夹和文件，删除原图片，换新名字保存
			String url_suffix = getUrlSuffix(origin.getHead_image_url());
			File old_file = new File(Const.DATA_PATH + url_suffix);
			if(old_file.exists()) old_file.delete();
			//准备保存新文件
			String dir_path = Const.DATA_PATH + "image/head/" + TimeUtil.getYearMonth() + "/";
			File dir = new File(dir_path);
			if(!dir.exists()) dir.mkdirs();
			// 找到后缀名
			String origin_file_name = file.getOriginalFilename();
			int flag = origin_file_name.lastIndexOf(".");
			String tail = origin_file_name.substring(flag, origin_file_name.length());
			String file_name = UUID.randomUUID().toString().substring(0, 10);
			File save_file = new File(dir_path + file_name + tail);
			try {
				file.transferTo(save_file);
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				throw new FadeException("上传头像图片异常！");
			}
			String head_url = "image/head/" + TimeUtil.getYearMonth() + "/" + file_name + tail;
			origin.setHead_image_url(head_url);
		}
		if (user.getUser_id() == null)
			throw new FadeException("user_id不能为空");
		else {
			if (user.getArea() != null)
				origin.setArea(user.getArea());
			if (user.getNickname() != null)
				origin.setNickname(user.getNickname());
			if (user.getSchool() != null)
				origin.setSchool(user.getSchool());
			if (user.getSex() != null)
				origin.setSex(user.getSex());
			if (user.getSummary() != null)
				origin.setSummary(user.getSummary());
			if (userDao.updateUserById(origin) == 1) {
				//更新solr数据库
				solrService.solrAddUpdateUser(origin);
				//更新所有缓存中有关信息，假如改变了头像或昵称的话
				if((!origin_nickname.equals(origin.getNickname())) 
						|| (!origin_head_url.equals(origin.getHead_image_url()))){
					//找到用户所有活帖子的id
					List<Integer>live_ids = noteDao.getUserLiveNote(user.getUser_id());
					String note_str = null;
					String key = null;
					for(Integer note_id : live_ids){
						key = "note_" + note_id;
						note_str = (String) redisUtil.getValue(key);
						if(note_str != null){
							//说明该帖子的确是活的
							Note temp = JSON.parseObject(note_str, Note.class);
							temp.setNickname(origin.getNickname());
							temp.setHead_image_url(origin.getHead_image_url());
							long time = redisUtil.getKeyTime(key, TimeUnit.MINUTES);
							redisUtil.addKey(key, JSON.toJSONString(temp), time, TimeUnit.MINUTES);
						}
					}
					//删除redis中所有该用户的一级评论
					List<Integer>comment_ids = commentDao.getUserAllComment(origin.getUser_id());
					for(Integer comment_id : comment_ids){
						redisUtil.deleteKey("comment_" + comment_id) ;
					}
				}
				logger.info("user_id=" + user.getUser_id() + ",nickname=" + user.getNickname() + "修改个人信息成功");
				Map<String, Object>extra = new HashMap<>();
				extra.put("head_image_url", origin.getHead_image_url());
				//同时返回头像信息
				return JSON.toJSONString(new SimpleResponse("修改成功！",null,extra));
			} else {
				throw new FadeException("修改个人信息失败！");
			}
		}
	}

	@Override
	public String logoutUserByToken(TokenModel model) throws FadeException {
		// 先从登录队列中移除
		redisUtil.listRemoveValue("user_" + model.getUser_id(), model.getToken());
		// 然后删除key
		redisUtil.deleteKey(model.getToken());
		// 清除相关缓存
		offline(model.getUser_id());
		return JSON.toJSONString(new SimpleResponse("退出登录成功", null));
	}

    private  String getUrlSuffix(String url){
    	//获取url的“后缀”
    	int start = 0;
    	int count = 0;
    	for(int i = 0; i < url.length(); i++){
    		if(url.charAt(i) == '/') count ++;
    		if(count == 4){
    			start = i;
    			break;
    		}
    	}
    	return url.substring(start, url.length());
    }
	
    @Override
	public String online(Integer user_id) {
		//用户上线后，加入到online_user里
    	if(user_id != null){ 
    		System.out.println(user_id + "请求上线");
    		redisUtil.setAddKey(Const.ONLINE_USERS,"user_"+user_id.toString());
    		return JSON.toJSONString(new SimpleResponse("上线成功",null));
    	}else {
    		return JSON.toJSONString(new SimpleResponse(null,"上线失败"));
		}
    	
	}

	@Override
	public String offline(Integer user_id) {
		//清除redis队列的缓存
		if(user_id != null){
			redisUtil.deleteKey("list1_" + user_id);//首页队列
			redisUtil.deleteKey("list2_" + user_id);//首页更新队列
		}
		return JSON.toJSONString(new SimpleResponse("下线成功！", null));
	}
	
	@Override
	public String concern(Integer fans_id, Integer star_id) {
		SimpleResponse response = new SimpleResponse();
		if(userDao.addConcern(fans_id,star_id) != null){
			response.setSuccess("关注成功！");
			//关注数量+1
			userDao.updateConcernNumPlus(fans_id);
			//粉丝数量加一
			userDao.updateFansNumPlus(star_id);
		}else {
			response.setErr("关注失败");
		}
		return JSON.toJSONString(response);
	}
	
	@Override
	public String cancelConcern(Integer fans_id, Integer star_id) {
		SimpleResponse response = new SimpleResponse();
		if(userDao.cancelConcern(fans_id,star_id) != null){
			response.setSuccess("取消关注成功！");
		}else {
			response.setErr("取消关注失败");
		}
		return JSON.toJSONString(response);
	}

	@Override
	public String getPersonPage(Integer user_id, Integer my_id) {
		PersonPage page = new PersonPage();
		User user = userDao.getSimpleUserById(user_id);//获取部分用户信息
		page.setUser(user);
		if(userDao.getRelation(user_id,my_id) != null){
			page.setIsConcern(1);
		}else {
			page.setIsConcern(0);
		}
		NoteQuery query = new NoteQuery();
		List<Note>notes = noteDao.getMyNote(user_id,0);
		//图片数据
		for(Note note : notes){
			List<Image>images = noteDao.getNoteImage(note.getNote_id());
			note.setImages(images);
		}
		checkAction(notes, user_id);
		query.setList(notes);
		query.setStart(notes.get(notes.size() -1).getNote_id());
		return JSON.toJSONString(page);
	}

	@Override
	public String getHeadImageUrl(User user) {
		String head_image_url = null;
		SimpleResponse response = new SimpleResponse();
		if((head_image_url = userDao.getHeadImageUrl(user)) == null){
			response.setErr("获取头像失败，账号可能不存在");
		}else{
			Map<String, Object>extra = new HashMap<>();
			extra.put("head_image_url", head_image_url);
			response.setExtra(extra);
		}
		return JSON.toJSONString(response);
	}
	
	@Override
	public String getAddMessage(Integer user_id) {
		//获取通知的简要概括信息，增加的消息数量，用于消息页显示
		AddMessage addMessage = userDao.getAddMessage(user_id);
		if(addMessage != null) return JSON.toJSONString(addMessage);
		return "{}";
	}
	
	@Override
	public String getAddContribute(Integer user_id,Integer start) {
		//一次10条
		List<Note>list = noteDao.getAddContribute(user_id,start);
		//更新通知点
		userDao.updateContributePoint(user_id,TimeUtil.getCurrentTime());
		//初始通知数量为0
		userDao.updateContributeZero(user_id);
		NoteQuery query = new NoteQuery(); 
		query.setList(list);
		query.setStart(list.get(list.size() -1).getNote_id());
		return JSON.toJSONString(query);
	}

	@Override
	public String getAddFans(Integer user_id, Integer start) {
		//一次10条
		List<User>list = userDao.getAddFans(user_id,start);
		//把数据库的该通知数量改为0,更新通知点
		userDao.updateAddFans(user_id,TimeUtil.getCurrentTime());
		UserQuery query = new UserQuery();
		query.setStart(list.get(list.size() - 1).getRelation_id());
		query.setList(list);
		return JSON.toJSONString(query);
	}
	
	@Override
	public String getAddComment(Integer user_id, Integer start) {
		//只返回一级评论,一次10条
		List<Comment>list = commentDao.getAddComment(user_id,start);
		//把数据库的该通知数量改为0,更新通知点
		userDao.updateAddComment(user_id,TimeUtil.getCurrentTime());
		CommentQuery query = new CommentQuery();
		query.setList(list);
		query.setStart(list.get(list.size() -1).getComment_id());
		return JSON.toJSONString(query);
	}

	@Override
	public String searchUser(String keyword, Integer page) {
		//调用solr数据库，分页查询
		List<User>users = solrService.getTenUserKeyword(keyword,page);
		UserQuery query = new UserQuery();
		query.setStart(++page);
		query.setList(users);
		return JSON.toJSONString(query);
	}
	
	private void checkAction(List<Note>notes, Integer user_id){
		//增加是否续或者减的属性
		for(Note note : notes){
			Integer type = null;
			if(note.getTarget_id() != 0){
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
}
