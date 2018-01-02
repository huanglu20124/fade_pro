package com.fade.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.fade.domain.AddMessage;
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
	//关注某人
	public Integer addConcern(@Param("fans_id")Integer fans_id, @Param("star_id")Integer star_id);
	//取消关注某人
	public Integer cancelConcern(@Param("fans_id")Integer fans_id, @Param("star_id")Integer star_id);
	//用于详情页，得到部分用户信息
	public User getSimpleUserById(Integer user_id);
	//用于详情页，查询是否已关注某人
	public Integer getRelation(@Param("user_id")Integer user_id, @Param("my_id")Integer my_id);
	//获取头像url
	public String getHeadImageUrl(User user);
	//新增消息数量
	public AddMessage getAddMessage(Integer user_id);
	//新建到新增消息队列里“注册”
	public void addMessage(Integer user_id);
	//贡献队列通知数量改为0,更新通知点
	public Integer updateContributePoint(@Param("user_id")Integer user_id, @Param("addContributePoint")String addContributePoint);
	//获取十个新增粉丝
	public List<User> getAddFans(@Param("user_id")Integer user_id, @Param("start")Integer start);
	//粉丝队列通知数量改为0,更新通知点
	public Integer updateAddFans(@Param("user_id")Integer user_id, @Param("addFansPoint")String addFansPoint);
	//通知队列通知数量改为0,更新通知点
	public Integer updateAddComment(@Param("user_id")Integer user_id, @Param("addCommentPoint")String addCommentPoint);
	//fade数量加一
	public Integer updateFadeNumPlus(@Param("user_id")Integer user_id);
	//关注数量+1
	public Integer updateConcernNumPlus(@Param("user_id")Integer fans_id);
	//粉丝数量加一
	public Integer updateFansNumPlus(@Param("user_id")Integer star_id);
	//通知数量改为0
	public Integer updateContributeZero(@Param("user_id")Integer user_id);
	//原主人的通知数量+1
	public Integer updateContributePlus(Integer user_id);
	
}
