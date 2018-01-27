package com.fade.domain;

import java.io.Serializable;
import java.util.List;

/**
 * 用户类
 * @author huanglu
 *
 */
public class User implements Serializable{
	private static final long serialVersionUID = -2838701809575809429L;
	private Integer user_id;
	private String nickname;
	private String telephone;
	private String password;
	private String fade_name;
	
	private String sex;
	private String mail;
	private String head_image_url;
	private String register_time;
	private String summary;
	
	private Integer school_id;//学校id
	private String school_name;//学校名称
	
	private Integer relation_id; //后端用到的，用于得到start
	
	private TokenModel tokenModel;
	
	//第三方提供的id
	private String wechat_id;
	public Integer getConcern_num() {
		return concern_num;
	}
	public void setConcern_num(Integer concern_num) {
		this.concern_num = concern_num;
	}
	public Integer getFans_num() {
		return fans_num;
	}
	public void setFans_num(Integer fans_num) {
		this.fans_num = fans_num;
	}

	private String weibo_id;
	private String qq_id;	
	
	//7月22日新增属性
	private Integer concern_num;
	private Integer fans_num;
	private String area;

	//fade数量
	private Integer fade_num;
	
	//安全性新增：盐，登录时md5计算用
	private String salt;
	
	//uuid，数据库索引主键
	private String uuid;
	
	//推荐算法生成的
	private List<User>recommendUser;//推荐用户
	private List<Note>recommendNote;//推荐帖子
	
	//融云聊天需要的Token
	private String MessageToken; 
	
	//是否已经关注了这个人
	private Integer isConcern;
	
	public Integer getFade_num() {
		return fade_num;
	}
	public void setFade_num(Integer fade_num) {
		this.fade_num = fade_num;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public Integer getUser_id() {
		return user_id;
	}
	public void setUser_id(Integer user_id) {
		this.user_id = user_id;
	}
	public String getHead_image_url() {
		return head_image_url;
	}
	public void setHead_image_url(String head_image_url) {
		this.head_image_url = head_image_url;
	}
	public String getFade_name() {
		return fade_name;
	}
	public void setFade_name(String fade_name) {
		this.fade_name = fade_name;
	}
	public String getWeibo_id() {
		return weibo_id;
	}
	public void setWeibo_id(String weibo_id) {
		this.weibo_id = weibo_id;
	}
	public String getQq_id() {
		return qq_id;
	}
	public void setQq_id(String qq_id) {
		this.qq_id = qq_id;
	}
	
	

	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getTelephone() {
		return telephone;
	}
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getMail() {
		return mail;
	}
	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getRegister_time() {
		return register_time;
	}
	public void setRegister_time(String register_time) {
		this.register_time = register_time;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getWechat_id() {
		return wechat_id;
	}
	public void setWechat_id(String wechat_id) {
		this.wechat_id = wechat_id;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "nickname="+nickname
				+"user_id="+user_id;
	}
	
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	public String getSalt() {
		return salt;
	}
	public void setSalt(String salt) {
		this.salt = salt;
	}
	public TokenModel getTokenModel() {
		return tokenModel;
	}
	public void setTokenModel(TokenModel tokenModel) {
		this.tokenModel = tokenModel;
	}
	public Integer getRelation_id() {
		return relation_id;
	}
	public void setRelation_id(Integer relation_id) {
		this.relation_id = relation_id;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public Integer getSchool_id() {
		return school_id;
	}
	public void setSchool_id(Integer school_id) {
		this.school_id = school_id;
	}
	public String getSchool_name() {
		return school_name;
	}
	public void setSchool_name(String school_name) {
		this.school_name = school_name;
	}
	public List<User> getRecommendUser() {
		return recommendUser;
	}
	public void setRecommendUser(List<User> recommendUser) {
		this.recommendUser = recommendUser;
	}
	public List<Note> getRecommendNote() {
		return recommendNote;
	}
	public void setRecommendNote(List<Note> recommendNote) {
		this.recommendNote = recommendNote;
	}
	public String getMessageToken() {
		return MessageToken;
	}
	public void setMessageToken(String messageToken) {
		MessageToken = messageToken;
	}
	public Integer getIsConcern() {
		return isConcern;
	}
	public void setIsConcern(Integer isConcern) {
		this.isConcern = isConcern;
	}

	
}
