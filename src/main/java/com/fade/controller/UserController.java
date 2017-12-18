package com.fade.controller;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.fade.domain.TokenModel;
import com.fade.domain.User;
import com.fade.exception.FadeException;
import com.fade.service.UserService;

@Controller
public class UserController {
	
	@Resource(name = "userService")
	private UserService userService;
	
	@RequestMapping(value = "/getUserById.action", method = RequestMethod.POST)
	@ResponseBody
	public String getUserById(Integer user_id) throws FadeException{
		return userService.getUserById(user_id);
	}
	
	@RequestMapping(value = "/loginWechat.action", method = RequestMethod.POST)
	@ResponseBody
	public String loginWechat(String wechat_id) throws FadeException{
		//微信登录，返回最新用户信息，同时进行token处理（token先不设置过期）
		return userService.loginWechat(wechat_id);
	}
	
	@RequestMapping(value = "/registerWechat.action", method = RequestMethod.POST)
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
	
	@RequestMapping(value = "/registerQueryTel.action", method = RequestMethod.POST)
	@ResponseBody
	public String registerQueryTel(String telephone){
		//查询手机号是否被注册
		return userService.registerQueryTel(telephone);
	}
	
	@RequestMapping(value = "/registerByName.action", method =  RequestMethod.POST)
	@ResponseBody
	public String registerByName(HttpServletRequest request) throws FadeException{
		User user = new User();
		try {
			BeanUtils.populate(user, request.getParameterMap());
		} catch (Exception e) {
			throw new FadeException("包装User类出错");
		}
		//安卓端昵称密码的注册方式
		return userService.registerByName(user);
	}
	
	@RequestMapping(value = "/loginUser.action", method =  RequestMethod.POST)
	@ResponseBody
	public String loginUser(HttpServletRequest request) throws FadeException{
		//安卓端登录
		User user = new User();
		try {
			BeanUtils.populate(user, request.getParameterMap());
		} catch (Exception e) {
			throw new FadeException("包装User类出错");
		}
		//安卓端昵称密码的注册方式
		return userService.loginUser(user);
	}

	@RequestMapping(value = "/updateUserById.action", method =  RequestMethod.POST)
	@ResponseBody
	public String updateUserById(HttpServletRequest request) throws FadeException{
		//编辑用户信息
		User user = new User();
		try {
			BeanUtils.populate(user, request.getParameterMap());
		} catch (Exception e) {
			throw new FadeException("包装User类出错");
		}
		return userService.updateUserById(user);
	}

	@RequestMapping(value = "/logoutByToken.action", method =  RequestMethod.POST)
	@ResponseBody
	public String logoutUserByToken(HttpServletRequest request)throws FadeException{
		//注销登录
		TokenModel model = JSON.parseObject(request.getParameter("tokenModel"),TokenModel.class);
		return userService.logoutUserByToken(model);
	}
}
