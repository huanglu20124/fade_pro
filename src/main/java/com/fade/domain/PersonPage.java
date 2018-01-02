package com.fade.domain;

public class PersonPage {
	//个人界面所需要数据
	private User user;
	private Integer isConcern; //0为没关注，1为已关注
	
	
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Integer getIsConcern() {
		return isConcern;
	}
	public void setIsConcern(Integer isConcern) {
		this.isConcern = isConcern;
	}
	
	
}
