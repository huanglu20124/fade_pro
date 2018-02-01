package com.fade.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.fade.domain.AddMessage;
import com.fade.domain.Comment;
import com.fade.domain.CommentMessage;
import com.fade.domain.Note;
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
	//根据fade_name查询用户
	public User getUserByFadeName(String fade_name);
	//编辑用户信息(部分)
	public Integer updateUserById(User user);
	//根据电话获取用户的盐
	public String getSaltByTel(String telephone);
	//根据fade_name获取用户的盐
	public String getSaltByFadeName(String fade_name);
	//查询所有粉丝
	public List<Integer> getAllFansId(Integer user_id);
	//关注某人
	public Integer addConcern(@Param("fans_id")Integer fans_id, @Param("star_id")Integer star_id);
	//取消关注某人
	public void cancelConcern(@Param("fans_id")Integer fans_id, @Param("star_id")Integer star_id);
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
	//获取十个新增粉丝
	public List<User> getAddFans(@Param("user_id")Integer user_id, @Param("start")Integer start, @Param("point")String point);
	//fade数量加一
	public Integer updateFadeNumPlus(Integer user_id);
	//关注数量+1
	public Integer updateConcernNumPlus(Integer fans_id);
	//粉丝数量加一
	public Integer updateFansNumPlus(Integer star_id);
	//未读贡献数量+1
	public Integer updateContributePlus(Integer user_id);
	//未读通知粉丝数量+1
	public Integer updateAddFansPlus(Integer user_id);
	//未读评论数量+1
	public Integer updateAddCommentPlus(Integer user_id);
	//第三方登录之后，获取全部用户信息
	public User getUserByOpenId(@Param("open_id")String open_id,@Param("type")Integer type);
	//单独加入一条偏好信息
	public void addPreference(@Param("user_id")int user_id,
			@Param("note_id")int note_id, @Param("score")double score);
	//查询时间点,type=0,1,2分别为贡献，粉丝，评论
	public String getAddPoint(@Param("user_id")Integer user_id, @Param("type")int type);
	//初始化队列，type=0,1,2分别为贡献，粉丝，评论
	public void initAddMessage(@Param("user_id")Integer user_id, @Param("type")int type);
	//嵌套查询，得到对该用户有“贡献”的帖子
	List<Note> getAddContribute(@Param("user_id")Integer user_id, 
			@Param("start")Integer start,@Param("point")String point);
	//用于通知页只返回评论通知,一次20条
	List<CommentMessage> getAddComment(@Param("user_id")Integer user_id,
			@Param("start")Integer start,@Param("point")String point);
	//查看更多，查看以前的贡献
	public List<Note> getOldContribute(@Param("user_id")Integer user_id, @Param("start")Integer start);
	//查看更多，查看以前的粉丝
	public List<User> getOldFans(@Param("user_id")Integer user_id, @Param("start")Integer start);
	//查看更多，查看以前的评论
	public List<CommentMessage> getOldComment(@Param("user_id")Integer user_id, @Param("start")Integer start);
	//获得用户昵称
	public String getNickname(Integer to_user_id);
	//添加对自己点赞的记录
	public void addMySecond(@Param("user_id")Integer user_id,
			@Param("note_id")Integer note_id, @Param("type")Integer type, @Param("post_time")String post_time);
	//查询自己是否对自己的帖子续秒
	Integer getIsMySecond(@Param("user_id")Integer user_id, @Param("note_id")Integer note_id);
	//获得推荐用户字段
	public String getRecommendUser(Integer user_id);
	//查询主人对帖子的续秒情况
	public Note getMyAddSecondNote(@Param("note_id")Integer note_id, @Param("user_id")Integer user_id);
	//个人页，分页查询20条粉丝
	public List<User> getFans(@Param("user_id")Integer user_id, @Param("start")int start);
	//个人页，分页查询20条关注者
	public List<User> getConcerns(@Param("user_id")Integer user_id, @Param("start")int start);
	//获得关注的人的id
	public List<Integer> getStarUserId(Integer user_id);
	//仅获取一系列用户的id名字头像
	public List<User> getMulSimpleUser(@Param("resultList")List<Integer> resultList);
	//得到最小用户,仅含名字，id，头像
	public User getMinUser(Integer user_id);
	
	
}
