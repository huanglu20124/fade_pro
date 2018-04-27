package com.fade.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.fade.domain.AddMessage;
import com.fade.domain.CommentMessage;
import com.fade.domain.Department;
import com.fade.domain.Note;
import com.fade.domain.NoteQuery;
import com.fade.domain.PersonPage;
import com.fade.domain.PushMessage;
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
import com.fade.util.RongCloudHelper;
import com.fade.util.TimeUtil;
import com.fade.util.TokenUtil;
import com.fade.websocket.MessageWebSocketHandler;
import com.gexin.rp.sdk.base.IPushResult;
import com.gexin.rp.sdk.base.impl.SingleMessage;
import com.gexin.rp.sdk.base.impl.Target;
import com.gexin.rp.sdk.exceptions.RequestException;
import com.gexin.rp.sdk.http.IGtPush;
import com.gexin.rp.sdk.template.NotificationTemplate;
import com.gexin.rp.sdk.template.style.Style0;

@SuppressWarnings("deprecation")
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
	
	@Resource(name = "noteService")
	private NoteService noteService;
	
	@Resource(name = "messageWebSocketHandler")
	private MessageWebSocketHandler webSocketHandler;
	
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
		logger.info("准备登录的微信wechat_id="+wechat_id);
		//User user = userDao.getUserByOpenIdQuery(wechat_id, 0);
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
			redisUtil.setAddKey(Const.ONLINE_USERS, "user_"+user.getUser_id());
			return JSON.toJSONString(map);
		}
	}

	@Override
	public String registerWechat(String js_code, User user) throws FadeException {
		// 如果客户端本地没有检测到，则发送小程序code、以及一些用户信息到服务器，服务器请求得到openid，返回给小程序
		String str = null;
		logger.info("js_code=" + js_code + "请求注册账号,用户信息为=" + JSON.toJSONString(user));
		Map<String, Object>ansMap = new HashMap<>();
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
			str = baos.toString("utf-8");
		} catch (Exception e) {
			e.printStackTrace();
			throw new FadeException("向腾讯服务器请求数据失败");
		}
		logger.info("向腾讯服务器请求的结果是：" + str);
		JSONObject temp = new JSONObject(str);
		String wechat_id = temp.getString("openid");
		user.setWechat_id(wechat_id);
		//学校id，没有的话设置为默认的中大
		if(user.getSchool_id() == null){
			user.setSchool_id(Const.DEFAULT_SCHOOL_ID);
		}
		//院系id，没有就设置默认
		if(user.getDepartment_id() == null){
			user.setDepartment_id(Const.DEFAULT_DEPARTMENT_ID);
		}
		String url = null;
		if((url = user.getHead_image_url()) != null){
			//将url保存到本地
			String dir_path = "image/head/" + TimeUtil.getYearMonth() + "/";
			File dir = new File(dir_path);
			if(!dir.exists()) dir.mkdirs();
			String file_name = wechat_id + ".jpg";
			downloadPic(url,Const.DATA_PATH + dir_path + file_name);
			//再设置给user
		    user.setHead_image_url(dir_path + file_name);
		}
		// 生成fade_name 注册时间
		String uuid = UUID.randomUUID().toString();
		String fade_name = "fade_" + uuid.substring(30);
		user.setFade_name(fade_name);
		user.setRegister_time(TimeUtil.getCurrentTime());
		user.setUuid(uuid);
		//设置盐
		user.setSalt(uuid.substring(0, 5));
		//logger.info("准备注册的微信用户信息="+JSON.toJSONString(user));
		User userQuery = null;
		if ((userQuery = userDao.getUserByOpenId(wechat_id, 0)) == null) {
			userDao.addUser(user);// user_id设置到user对象中
			userDao.addMessage(user.getUser_id());//到新增消息队列里“注册”
			//添加到索引数据库
			user.setUuid(uuid);
			solrService.solrAddUpdateUser(user);
			// 新建tokenModel并返回
			TokenModel model = tokenUtil.createTokenModel(user.getUser_id());
			user.setTokenModel(model);
			//补充一些默认信息
			user.setFade_num(0);
			user.setFans_num(0);
			user.setConcern_num(0);
			logger.info("wechat_id=" + wechat_id + ",user_id=" + user.getUser_id() + " 注册登录成功");
			//redis上线
			redisUtil.setAddKey(Const.ONLINE_USERS, "user_"+user.getUser_id());
			//返回全部信息	
			ansMap.put("user", user);
			return JSON.toJSONString(ansMap);
		}else {
			//因为是首次登陆，所以要返回tokenModel
			TokenModel model = tokenUtil.createTokenModel(userQuery.getUser_id());
			userQuery.setTokenModel(model);
			//redis上线
			redisUtil.setAddKey(Const.ONLINE_USERS, "user_"+user.getUser_id());
			logger.info("user_id=" + userQuery.getUser_id() + ",nickname=" + userQuery.getNickname() + " 登录成功");
			ansMap.put("user", userQuery);
			ansMap.put("success", "该账号已经注册！");
			return JSON.toJSONString(ansMap);
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
		user.setUuid(uuid);
		//学校id，没有的话设置为默认的中大
		if(user.getSchool_id() == null){
			user.setSchool_id(Const.DEFAULT_SCHOOL_ID);
		}
		//院系id，没有就设置默认
		if(user.getDepartment_id() == null){
			user.setDepartment_id(Const.DEFAULT_DEPARTMENT_ID);
		}
		// 设置盐，MD5加密，散列一次
		String salt = UUID.randomUUID().toString().substring(0, 5);
		String password_md5 = new Md5Hash(user.getPassword(), salt, 1).toString();
		user.setPassword(password_md5);
		user.setSalt(salt);
		userDao.addUser(user);// 主键属性被自动赋予到user中
		userDao.addMessage(user.getUser_id());//到新增消息队列里“注册”
		//添加到solr数据库中
		solrService.solrAddUpdateUser(user);
		if(user.getUser_id() == null) throw new FadeException("注册失败");
		logger.info("user_id=" + user.getUser_id() + ",nickname=" + user.getNickname() + " 注册成功");
		// 新建tokenModel并返回,key为user_id
		TokenModel model = tokenUtil.createTokenModel(user.getUser_id());
		//redis上线
		redisUtil.setAddKey(Const.ONLINE_USERS, "user_"+user.getUser_id());
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
			if (user.getSchool_name() != null)
				origin.setSchool_name(user.getSchool_name());
			if (user.getSex() != null)
				origin.setSex(user.getSex());
			if (user.getSummary() != null)
				origin.setSummary(user.getSummary());
			if(user.getPassword() != null){
				String salt = userDao.getSaltById(user.getUser_id());
				String password_md5 = new Md5Hash(user.getPassword(), salt, 1).toString();
				origin.setPassword(password_md5);
			}
			if(user.getDepartment_id() != null)
				origin.setDepartment_id(user.getDepartment_id());
			if(user.getSchool_id() != null)
				origin.setSchool_id(user.getSchool_id());
			if (userDao.updateUserById(origin) == 1) {
				//更新solr数据库
				solrService.solrAddUpdateUser(origin);
				//更新所有缓存中有关信息，假如改变了头像或昵称的话
				if((!origin_nickname.equals(origin.getNickname())) 
						|| (!origin_head_url.equals(origin.getHead_image_url()))){
					//找到用户所有活帖子的id
					List<Integer>live_ids = noteDao.getUserLiveNote(user.getUser_id());
					String key = null;
					Note temp = null;
					for(Integer note_id : live_ids){
						key = "note_" + note_id;
						temp = (Note) redisUtil.getValue(key);
						if(temp != null){
							//说明该帖子的确是活的
							temp.setNickname(origin.getNickname());
							temp.setHead_image_url(origin.getHead_image_url());
							long time = redisUtil.getKeyTime(key, TimeUnit.MINUTES);
							redisUtil.addKey(key, temp, time, TimeUnit.MINUTES);
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
		tokenUtil.deleteToken(model);
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
			redisUtil.setRemove(Const.ONLINE_USERS, "user_" + user_id);
		}
		return JSON.toJSONString(new SimpleResponse("下线成功！", null));
	}
	
	@Override
	public String concern(Integer fans_id, Integer star_id) {
		SimpleResponse response = new SimpleResponse();
		if(userDao.addConcern(fans_id,star_id) != null){
			response.setSuccess("关注成功！");
			//关注数量+1
			userDao.updateConcernNum(fans_id,1);
			//粉丝数量加一
			userDao.updateFansNum(star_id,1);
			//未读通知粉丝数量+1
			userDao.updateAddFansPlus(star_id);
			//通知前端更新
			SimpleResponse message = new SimpleResponse("01", null);
			Map<String, Object>temp = new HashMap<>();
			User fans = userDao.getUserById(fans_id);
			temp.put("user", fans);
			message.setExtra(temp);
			//websocket推送
			webSocketHandler.sendMessageToUser(star_id, JSON.toJSONString(message));
			//个推推送到安卓前端
			pushMessage(star_id, "你有一位新粉丝", new PushMessage(null, 3));
		}else {
			response.setErr("关注失败");
		}
		return JSON.toJSONString(response);
	}
	
	@Override
	public String cancelConcern(Integer fans_id, Integer star_id) {
		SimpleResponse response = new SimpleResponse();
		if(userDao.cancelConcern(fans_id,star_id) != null){
			//关注数量-1
			userDao.updateConcernNum(fans_id,2);
			//粉丝数量-1
			userDao.updateFansNum(star_id,2);
		}
		response.setSuccess("取消关注成功！");
		return JSON.toJSONString(response);
	}

	@Override
	public String getPersonPage(Integer user_id, Integer my_id) {
		PersonPage page = new PersonPage();
		NoteQuery query = noteService.getLiveNote(user_id, my_id, 0);
		page.setQuery(query);
		boolean isMy = user_id == my_id ? true : false;
		User user = userDao.getSimpleUserById(user_id);//获取部分用户信息
		page.setUser(user);
		if(!isMy){
			if(userDao.getRelation(user_id,my_id) != null){
				page.setIsConcern(1);
			}else {
				page.setIsConcern(0);
			}
		}		
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
	public String getAddContribute(Integer user_id,Integer start,String point) {
		//start=0,首次查询，返回时间点； 之后请求必须携带该时间点
		NoteQuery query = new NoteQuery();
		List<Note>list = null;
		if(start == 0){
			//查询时间点
			point = userDao.getAddPoint(user_id,0);
			query.setPoint(point);
			list = userDao.getAddContribute(user_id,start,point);
			//贡献队列初始化
			userDao.initAddMessage(user_id,0);
		}else {
			list = userDao.getAddContribute(user_id,start,point);
		}
		//添加示例图片
		for(Note note : list){
			//因为这些都是转发帖啊
			note.setExampleImage(noteDao.getOneImage(note.getTarget_id()));
		}
		query.setList(list);
		if(list.size() > 0) query.setStart(list.get(list.size() -1).getNote_id());
		else {
			query.setStart(0);
		}
		return JSON.toJSONString(query);
	}

	@Override
	public String getAddFans(Integer user_id,Integer start,String point) {
		//start=0,首次查询，返回时间点； 之后请求必须携带该时间点
		UserQuery query = new UserQuery();
		List<User>list = null;
		if(start == 0){
			//查询时间点
			point = userDao.getAddPoint(user_id,1);
			query.setPoint(point);
			list = userDao.getAddFans(user_id,start,point);
			//粉丝队列初始化
			userDao.initAddMessage(user_id,1);
		}else {
			list = userDao.getAddFans(user_id,start,point);
		}
		//检查对粉丝的关注情况
		for(User user : list){
			if(userDao.getRelation(user.getUser_id(),user_id) != null){
				user.setIsConcern(1);
			}else {
				user.setIsConcern(0);
			}
		}
		
		query.setList(list);
		if(list.size() > 0) query.setStart(list.get(list.size() -1).getUser_id());
		else {
			query.setStart(0);
		}
		return JSON.toJSONString(query);
	}
	
	@Override
	public String getAddComment(Integer user_id,Integer start,String point) {
		//start=0,首次查询，返回时间点； 之后请求必须携带该时间点
		Map<String,Object>ansMap = new HashMap<>();
		List<CommentMessage>list = null;
		if(start == 0){
			//查询时间点
			point = userDao.getAddPoint(user_id,2);
			list = userDao.getAddComment(user_id,start,point);
			//粉丝队列初始化
			userDao.initAddMessage(user_id,2);
		}else {
			list = userDao.getAddComment(user_id,start,point);
		}
		ansMap.put("point", point);
		for(CommentMessage commentMessage : list){
			//查询图片
			commentMessage.setExampleImage(noteDao.getOneImage(commentMessage.getNote_id()));
		}
		ansMap.put("list", list);
		if(list.size() > 0) {
			ansMap.put("start", list.get(list.size() -1).getMessage_id());
		}else {
			ansMap.put("start", 0);
		}
		return JSON.toJSONString(ansMap);
	}

	@Override
	public String searchUser(String keyword, Integer page) {
		//调用solr数据库，分页查询
		return JSON.toJSONString(solrService.getTenUserKeyword(keyword,page));
	}
	
	@Override
	public String getTenRecommendUser(Integer user_id, Integer start) {
		UserQuery query = new UserQuery();
		List<User>list = new ArrayList<>();
		List<Integer>getIds = null;
		//推荐用户是一个字段
		String recommendIdStr = userDao.getRecommendUser(user_id);
		int flag = start*10;
		int end = flag + 10;
		if(recommendIdStr != null){
			List<Integer>ids = JSON.parseArray(recommendIdStr, Integer.class);
			int size = ids.size();
			if(flag < size){
				if(end <= size){
					getIds = ids.subList(flag, end);
				}else {
					getIds = ids.subList(flag, size);
				}	
			}
		}
		if(getIds != null){
			for(Integer id : getIds){
				list.add(userDao.getUserById(id));
			}
		}
		start++;
		query.setList(list);
		query.setStart(start);
		return JSON.toJSONString(query);
	}

	@Override
	public String getMessageToken(Integer user_id) throws FadeException{
		//先查询用户信息
		User user = userDao.getSimpleUserById(user_id);//获取用户昵称，用户头像
		//向融云发起请求
		try {
			//5位随机数
/*			String randomNum = UUID.randomUUID().toString().substring(0,5);
			Long timestamp = System.currentTimeMillis();
			String signature = DigestUtils.sha1Hex(Const.RONG_APP_SECRET + randomNum + timestamp);*/
			String randomNum = RongCloudHelper.getRandNum();
			String timestamp = RongCloudHelper.getCurTime();
			String signature = RongCloudHelper.getSignature(randomNum, timestamp);
			HttpClient client = new DefaultHttpClient();
			HttpPost postRequest = new HttpPost(Const.RONG_URL + "/user/getToken.json");
			postRequest.addHeader("App-Key", Const.RONG_APP_KEY);
			postRequest.addHeader("Nonce", randomNum);
			postRequest.addHeader("Timestamp", timestamp.toString());
			postRequest.addHeader("Signature", signature);
			List<NameValuePair>list = new ArrayList<>();
			list.add(new BasicNameValuePair("userId", user_id.toString()));
			list.add(new BasicNameValuePair("name", user.getNickname()));
			list.add(new BasicNameValuePair("portraitUri", Const.BASE_IP + user.getHead_image_url()));
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, "utf-8");
			postRequest.setEntity(entity);
			
			HttpResponse response = client.execute(postRequest);
			String content = EntityUtils.toString(response.getEntity());
			if(response.getStatusLine().getStatusCode() == 200){
				logger.info("用户" + user_id + "请求融云token成功");
				//直接返回token信息
				com.alibaba.fastjson.JSONObject object = JSON.parseObject(content);
				System.out.println("token=" + object.getString("token"));
				com.alibaba.fastjson.JSONObject ans = new com.alibaba.fastjson.JSONObject();
				ans.put("token", object.getString("token"));
				return ans.toJSONString();			
			}else {
				System.out.println(response.getStatusLine().getStatusCode());
				System.out.println(content);
				throw new FadeException("向融云服务器请求token失败！--" + content);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new FadeException("向融云服务器请求token失败！");
		}
	}

	@Override
	public String getOldContribute(Integer user_id, Integer start) {
		NoteQuery query = new NoteQuery();
		List<Note>list = userDao.getOldContribute(user_id,start);
		//添加示例图片
		for(Note note : list){
			note.setExampleImage(noteDao.getOneImage(note.getTarget_id()));
		}
		query.setList(list);
		if(list.size() > 0) query.setStart(list.get(list.size() - 1).getNote_id());
		else {
			query.setStart(0);
		}	
		return JSON.toJSONString(query);
	}

	@Override
	public String getOldFans(Integer user_id, Integer start) {
		UserQuery query = new UserQuery();
		List<User>list = userDao.getOldFans(user_id,start);
		//检查对粉丝的关注情况
		for(User user : list){
			if(userDao.getRelation(user.getUser_id(),user_id) != null){
				user.setIsConcern(1);
			}else {
				user.setIsConcern(0);
			}
		}
		
		query.setList(list);
		if(list.size() > 0) query.setStart(list.get(list.size() - 1).getUser_id());
		else {
			query.setStart(0);
		}	
		return JSON.toJSONString(query);
	}

	@Override
	public String getOldComment(Integer user_id, Integer start) {
		//start=0,首次查询，返回时间点； 之后请求必须携带该时间点
		Map<String,Object>ansMap = new HashMap<>();
		List<CommentMessage>list = null;
		list = userDao.getOldComment(user_id,start);
		for(CommentMessage commentMessage : list){
			//查询图片
			commentMessage.setExampleImage(noteDao.getOneImage(commentMessage.getNote_id()));
		}
		ansMap.put("list", list);
		if(list.size() > 0) {
			ansMap.put("start", list.get(list.size() -1).getMessage_id());
		}else {
			ansMap.put("start", 0);
		}
		return JSON.toJSONString(ansMap);
	}
	
	public void downloadPic(String url, String localPath){
		logger.info("准备保存文件" + url);
		File file = new File(localPath);
		if(file.exists()){
			file.delete();
		}
	    FileOutputStream fs = null;
	    URLConnection conn = null;
	    InputStream inStream = null;
        try {
 	       int byteread = 0;
            conn = new URL(url).openConnection();
            inStream = conn.getInputStream();
            fs = new FileOutputStream(localPath);
            byte[] buffer = new byte[1204];
            while ((byteread = inStream.read(buffer)) != -1) {
                fs.write(buffer, 0, byteread);
            }
            logger.info("保存文件成功，路径为" + localPath );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
			try {
				if(fs != null) fs.close();
				if(inStream != null) inStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public String getFans(Integer user_id, Integer my_id, Integer start) {
		//个人页，分页查询20条粉丝
		UserQuery query = new UserQuery();
		List<User>list = userDao.getFans(user_id,start*20);
		//检查对粉丝的关注情况
		for(User user : list){
			if(userDao.getRelation(user.getUser_id(),my_id) != null){
				user.setIsConcern(1);
			}else {
				user.setIsConcern(0);
			}
		}
		query.setList(list);
		query.setStart(++start);
		return JSON.toJSONString(query);
	}
	
	@Override
	public String getConcerns(Integer user_id,Integer my_id, Integer start) {
		//个人页，分页查询20条关注者
		UserQuery query = new UserQuery();
		List<User>list = userDao.getConcerns(user_id,start*20);
		for(User user : list){
			if(userDao.getRelation(user.getUser_id(),my_id) != null){
				user.setIsConcern(1);
			}else {
				user.setIsConcern(0);
			}
		}
		query.setList(list);
		query.setStart(++start);
		return JSON.toJSONString(query);
	}

	@Override
	public String getOriginRecommendUsers(Integer user_id, Integer start) {
		List<User>list = userDao.getAllUsers();
		User my = userDao.getUserById(user_id);
		UserQuery query = new UserQuery();
		for(User user : list){
			double originScore = 0;
			if(user.getFans_num() <= 10) originScore += 1;
			else if (user.getFans_num() <= 100) originScore += 2;
			else originScore += 3;
			
			//根据学校院系加分
			if(my.getSchool_id() == user.getSchool_id()) originScore += 1;
			if(my.getDepartment_id() == user.getDepartment_id()) originScore += 1;
			
			//根据性别加分
			if(my.getSex().equals("男") && user.getSex().equals("女"))  originScore += 1;
			else if(my.getSex().equals("女") && user.getSex().equals("男"))  originScore += 1;
			user.setRecommendScore(originScore);
		}
		
		list.sort(new RecommendUserComparator());
		if(start < list.size()){
			if(start + 9 < list.size()) query.setList(list.subList(start, start + 9));
			else query.setList(list.subList(start, list.size()));
		}
		else query.setList(new ArrayList<>());
		query.setStart(start + 10);
		return JSON.toJSONString(query);
	}

	class RecommendUserComparator implements Comparator<User>{
		@Override
		public int compare(User user1, User user2) {
			return user1.getRecommendScore().compareTo(user2.getRecommendScore())*-1;
		}
		
	}

	@Override
	public String getSchoolDepartment(Integer school_id) {
		//注返回一个学校所有院系
		List<Department>list = userDao.getSchoolDepartment(school_id);
		Map<String, Object>map = new HashMap<>();
		map.put("list", list);
		return JSON.toJSONString(map);
	}

	@Override
	public String changePasswordTel(String telephone, String password) {
		//客户端手机验证过后，修改密码
		User user = userDao.getUserByTel(telephone);
		SimpleResponse response = new SimpleResponse();
		if(user == null)  response.setErr("用户不存在");
		else {
			String salt = userDao.getSaltById(user.getUser_id());
			String password_md5 = new Md5Hash(password, salt, 1).toString();
			userDao.updateUserPass(telephone, password_md5);
			response.setSuccess("修改密码成功");
		}
		return JSON.toJSONString(response);
	}

	@Override
	public String addClientId(Integer user_id, String clientid) {
		if(user_id != null && clientid != null){
			redisUtil.hashAdd(Const.GETUI_CIDS, user_id.toString(), clientid);
		}
		return "{}";
	}

	@Override
	public void pushMessage(Integer user_id, String msg,  PushMessage pushMessage) {
		//个推通知原主人
		 IGtPush push = new IGtPush("http://sdk.open.api.igexin.com/apiex.htm", 
				 Const.GETUI_APPKEY, Const.GETUI_MASTERSECRET);
		
	    NotificationTemplate template = new NotificationTemplate();
        // 设置APPID与APPKEY
        template.setAppId(Const.GETUI_APPID);
        template.setAppkey(Const.GETUI_APPKEY);
        // 透传消息设置，1为强制启动应用，客户端接收到消息后就会立即启动应用；2为等待应用启动
        template.setTransmissionType(1);
        if(pushMessage != null) template.setTransmissionContent(JSON.toJSONString(pushMessage));
        // 设置定时展示时间
        // template.setDuration("2015-01-16 11:40:00", "2015-01-16 12:24:00");

        Style0 style = new Style0();
        // 设置通知栏标题与内容
        style.setTitle("Fade");
        style.setText(msg);
        // 配置通知栏图标
        style.setLogo("icon.png");
        // 配置通知栏网络图标
        style.setLogoUrl("");
        // 设置通知是否响铃，震动，或者可清除
        style.setRing(true);
        style.setVibrate(true);
        style.setClearable(true);
        template.setStyle(style);
	        
        SingleMessage message = new SingleMessage();
        message.setOffline(true);
        // 离线有效时间10天，单位为毫秒，可选
        message.setOfflineExpireTime(24 * 3600 * 1000 * 10);
        message.setData(template);
        // 可选，1为wifi，0为不限制网络环境。根据手机处于的网络情况，决定是否下发
        message.setPushNetWorkType(0);
        Target target = new Target();
        target.setAppId(Const.GETUI_APPID);
        String clientid = (String) redisUtil.hashGet(Const.GETUI_CIDS, user_id.toString());  
        target.setClientId(clientid);
        //target.setAlias(Alias);
        IPushResult ret = null;
        try {
            ret = push.pushMessageToSingle(message, target);
            logger.info("个推成功发送消息");
        } catch (RequestException e) {
            e.printStackTrace();
            ret = push.pushMessageToSingle(message, target, e.getRequestId());
        }
        if (ret != null) {
            System.out.println(ret.getResponse().toString());
        } else {
            System.out.println("服务器响应异常");
        }
		
	}
	
	
}
