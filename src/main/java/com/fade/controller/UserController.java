package com.fade.controller;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.fade.domain.TokenModel;
import com.fade.domain.User;
import com.fade.exception.FadeException;
import com.fade.service.UserService;

@Controller
public class UserController {
	
	@Resource(name = "userService")
	private UserService userService;
	 
	@RequestMapping(value = "/getUserById/{user_id}", method = RequestMethod.GET)
	@ResponseBody
	public String getUserById(@PathVariable("user_id")Integer user_id) throws FadeException{
		return userService.getUserById(user_id);
	}
	
	@RequestMapping(value = "/loginWechat/{wechat_id}", method = RequestMethod.GET)
	@ResponseBody
	public String loginWechat(@PathVariable("wechat_id")String wechat_id) throws FadeException{
		//微信登录，返回最新用户信息，同时进行token处理（token先不设置过期）
		return userService.loginWechat(wechat_id);
	}
	
	@RequestMapping(value = "/registerWechat", method = RequestMethod.POST)
	@ResponseBody
	public String registerWechat(String js_code,HttpServletRequest request) throws FadeException{
		//微信注册
		User user = new User();
		try {
			BeanUtils.populate(user, request.getParameterMap());
		} catch (Exception e) {
			throw new FadeException("包装User类出错");
		}
		return userService.registerWechat(js_code,user);
	}
	
	@RequestMapping(value = "/registerQueryTel/{telephone}", method = RequestMethod.GET)
	@ResponseBody
	public String registerQueryTel(@PathVariable("telephone")String telephone){
		//查询手机号是否被注册
		return userService.registerQueryTel(telephone);
	}
	
	@RequestMapping(value = "/registerByName", method =  RequestMethod.POST)
	@ResponseBody
	public String registerByName(HttpServletRequest request,@RequestParam("file")MultipartFile file) throws FadeException{
		//包括了上传头像
		User user = new User();
		try {
			BeanUtils.populate(user, request.getParameterMap());
		} catch (Exception e) {
			throw new FadeException("包装User类出错");
		}
		//安卓端昵称密码的注册方式
		return userService.registerByName(user,file);
	}
	
	@RequestMapping(value = "/loginUserByName/{fade_name}/{password}",method =  RequestMethod.GET)
	@ResponseBody
	public String loginUserByName(@PathVariable("fade_name")String fade_name, 
			@PathVariable("password")String password) throws FadeException{
		//安卓端通过fade_name登录，使用post请求，安全一点
		User user = new User();
		user.setFade_name(fade_name);
		user.setPassword(password);
		//安卓端昵称密码的注册方式
		return userService.loginUser(user);
	}
	
	@RequestMapping(value = "/loginUserByTel/{telephone}/{password}",method =  RequestMethod.GET)
	@ResponseBody
	public String loginUserByTel(@PathVariable("telephone")String telephone,
			@PathVariable("password")String password) throws FadeException{
		//安卓端通过telephone登录，使用post请求，安全一点
		User user = new User();
		user.setTelephone(telephone);
		user.setPassword(password);
		//安卓端昵称密码的注册方式
		return userService.loginUser(user);
	}
	
	@RequestMapping(value = "/updateUserById", method =  RequestMethod.POST)
	@ResponseBody
	public String updateUserById(HttpServletRequest request,@RequestParam("file")MultipartFile file) throws FadeException{
		//包括了上传头像
		//编辑用户信息
		User user = new User();
		try {
			BeanUtils.populate(user, request.getParameterMap());
		} catch (Exception e) {
			throw new FadeException("包装User类出错");
		}
		return userService.updateUserById(user,file);
	}

	@RequestMapping(value = "/logoutByToken/{tokenModel}", method =  RequestMethod.DELETE)
	@ResponseBody
	public String logoutUserByToken(@PathVariable("tokenModel")String tokenModel)throws FadeException{
		//注销登录
		TokenModel model = JSON.parseObject(tokenModel,TokenModel.class);
		return userService.logoutUserByToken(model);
	}

	@RequestMapping(value = "/online",method =  RequestMethod.POST)
	@ResponseBody
	public String online(Integer user_id){
		//上线请求
		return userService.online(user_id);
	}
	
	@RequestMapping(value = "/offline",method =  RequestMethod.POST)
	@ResponseBody
	public String offline(Integer user_id){
		//上线请求
		return userService.offline(user_id);
	}	
}
