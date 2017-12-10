package com.fade.controller;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.fade.domain.User;
import com.fade.service.UserService;

@Controller
public class UserController {
	
	@Resource(name = "userService")
	private UserService userService;
	
	@RequestMapping(value = "/getUserById.action", method = RequestMethod.POST)
	@ResponseBody
	public String getUserById(Integer user_id) {
		User user = userService.getUserById(user_id);
		if(user != null){
			return JSON.toJSONString(user);
		}else {
			return "{}";
		}
	}
}
