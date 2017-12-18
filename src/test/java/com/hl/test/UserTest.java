package com.hl.test;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import com.fade.domain.User;
import com.fade.mapper.UserDao;
import com.fade.service.UserService;
import com.fade.util.TokenUtil;

public class UserTest extends BaseTest {
	@Resource(name="userService")
	private UserService userService;
	
	@Resource(name="userDao")
	private UserDao userDao;

	@Resource(name = "tokenUtil")
	private TokenUtil tokenUtil;
	
	@Test
	public void testAdd() throws Exception {
		User user = new User();
		user.setFade_name("test3");
		user.setConcern_num(0);
		user.setFade_num(0);
		user.setFans_num(0);
		user.setRegister_time(com.fade.util.TimeUtil.getCurrentTime());
		userDao.addUser(user);
		System.out.println(user.getUser_id());//自增主键自动设置到对象中
	}

	@Test
	public void testEditNickname() throws Exception {
		User user = new User();
		user.setUser_id(1);
		user.setNickname("aaaa");
		System.out.println(userDao.updateNickname(user));	//返回更新的行数
	}

	@Test
	public void testEditHead() throws Exception {
		System.out.println(userDao.updateHeadUrl("11111", 8));
	}

	@Test
	public void testGetStarUser() throws Exception {
		List<User>users = userDao.getStarUser(1);
		for(User user : users){
			System.out.println(user.getNickname() + "--" + user.getUser_id());
		}
	}
	
	@Test
	public void testGetUserByTelephone() throws Exception {
		User user = userDao.getUserByTel("1233");
		System.out.println(user);
	}
	
	@Test
	public void testToken() throws Exception {
		
	}

}
