package com.fade.domain;

public class PersonPage {
	//个人界面所需要数据
	private User user;//不包括密码
	private Integer isConcern; //0为没关注，1为已关注
	private NoteQuery query;//活着的帖子，首次加载最多十条(即动态)
	
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
	public NoteQuery getQuery() {
		return query;
	}
	public void setQuery(NoteQuery query) {
		this.query = query;
	}
	
	
}
