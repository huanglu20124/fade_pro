package com.fade.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.fade.domain.User;
import com.fade.mapper.UserDao;

@Service("userService")
public class UserServiceImpl implements UserService {
	@Autowired
	private UserDao userDao;

	@Override
	public String getUserById(Integer user_id) {
		User user = userDao.getUserById(user_id);
		if(user == null) return "{}";
		else return JSON.toJSONString(user);
	}

	@Override
	public String loginWechat(String wechat_id) {
		User user = userDao.getUserByOpenId(wechat_id, 0);
		if(user == null) return "{}";
		else return JSON.toJSONString(user);
	}
	
	
}
