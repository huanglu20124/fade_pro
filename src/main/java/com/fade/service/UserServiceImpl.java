package com.fade.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.fade.domain.SimpleResponse;
import com.fade.domain.TokenModel;
import com.fade.domain.User;
import com.fade.exception.FadeException;
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

	@Resource(name = "redisUtil")
	private RedisUtil redisUtil;

	@Resource(name = "tokenUtil")
	private TokenUtil tokenUtil;

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
		SimpleResponse response = new SimpleResponse("注册成功",null,extra);
		//返回部分有用信息
		return JSON.toJSONString(response);
	}

	@Override
	public String loginUser(User user){
		// 安卓端昵称密码的注册方式
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
		if (ans_user == null)
			return null;
		else {
			// 新建token并返回
			Map<String, Object> map = new HashMap<>();
			TokenModel model = tokenUtil.createTokenModel(ans_user.getUser_id());
			map.put("tokenModel", model);
			// 最后要还原密码
			if (origin_password != null)
				ans_user.setPassword(origin_password);
			map.put("user", ans_user);
			//redis上线
			redisUtil.addKey(Const.ONLINE_USERS, "user_"+user.getUser_id());
			logger.info("user_id=" + ans_user.getUser_id() + ",nickname=" + ans_user.getNickname() + " 登录成功");
			return JSON.toJSONString(map);
		}
	}

	@Override
	public String updateUserById(User user,MultipartFile file) throws FadeException {
		// 先得到原本的用户信息
		User origin = userDao.getUserById(user.getUser_id());
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
			user.setHead_image_url(head_url);
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
			if (userDao.updateUserById(user) == 1) {
				logger.info("user_id=" + user.getUser_id() + ",nickname=" + user.getNickname() + "修改个人信息成功");
				Map<String, Object>extra = new HashMap<>();
				extra.put("head_image_url", user.getHead_image_url());
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
    	redisUtil.setAddKey(Const.ONLINE_USERS,"user_"+user_id);
		return JSON.toJSONString(new SimpleResponse("上线成功",null));
	}

	@Override
	public String offline(Integer user_id) {
		//清除redis队列的缓存
		redisUtil.deleteKey("list1_" + user_id);//首页队列
		redisUtil.deleteKey("list2_" + user_id);//首页更新队列
		return JSON.toJSONString(new SimpleResponse("下线成功！", null));
	}
}
