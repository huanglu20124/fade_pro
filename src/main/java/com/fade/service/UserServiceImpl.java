package com.fade.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fade.domain.User;
import com.fade.mapper.UserDao;

@Service("userService")
public class UserServiceImpl implements UserService {
	@Autowired
	private UserDao userDao;

	@Override
	public User getUserById(Integer user_id) {
		return userDao.getUserById(user_id);
	}
	
}
