package com.fade.controller;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.fade.domain.ErrorMessage;
import com.fade.domain.User;
import com.fade.exception.FadeException;
import com.fade.service.UserService;

@Controller
public class UserController {
	
	@Resource(name = "userService")
	private UserService userService;
	
	@RequestMapping(value = "/getUserById.action", method = RequestMethod.POST)
	@ResponseBody
	public String getUserById(Integer user_id) throws FadeException {
		ErrorMessage errorMessage = new ErrorMessage();
		errorMessage.setErr("错误测试");
		throw new FadeException(errorMessage);
		//return userService.getUserById(user_id);
	}
	
	@RequestMapping(value = "/loginWechat.action", method = RequestMethod.POST)
	@ResponseBody
	public String loginWechat(String wechat_id){
		return userService.loginWechat(wechat_id);
	}
	
	@RequestMapping(value = "/registerWechat.action", method = RequestMethod.POST)
	@ResponseBody
	public String registerWechat(String wechat_id){
		return null;
	}
	
	
	
}
