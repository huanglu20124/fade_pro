package com.fade.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.fade.domain.User;

public interface UserDao {
	//通过id获取用户 
	public User getUserById(Integer user_id);
	//插入新用户，返回主键 
	public Integer addUser(User user);
	//编辑用户昵称
	public Integer updateNickname(User user);
	//保存或更新用户头像
	public Integer updateHeadUrl(@Param("head_image_url")String head_image_url,
			@Param("user_id")Integer user_id);
	//获取关注的全部用户
	public List<User> getStarUser(Integer user_id);
	//查询手机号是否已被注册，只返回user_id
	public User getUserByTel(String telephone);
	//通过手机和密码登录 
	public User getUserByTelPwd(User user);
	//通过fade账号和密码登录
	public User getUserByFadePwd(User user);
	//查询openid是否被注册,只返回user_id
	public User getUserByOpenId(@Param("open_id")String open_id,@Param("type")Integer type);
	//根据fade_name查询用户
	public User getUserByFadeName(String fade_name);
	//编辑用户信息(部分)
	public Integer updateUserById(User user);
	//根据电话获取用户的盐
	public String getSaltByTel(String telephone);
	//根据fade_name获取用户的盐
	public String getSaltByFadeName(String fade_name);
	//新增盐
	public void addSalt(@Param("user_id")Integer user_id, @Param("salt")String salt);
	//查询所有粉丝
	public List<Integer> getAllFansId(Integer user_id);
	
}
