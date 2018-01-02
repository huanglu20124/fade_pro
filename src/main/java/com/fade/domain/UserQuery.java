package com.fade.domain;

import java.util.List;

public class UserQuery {
	private List<User>list;
	private Integer start;
	
	public List<User> getList() {
		return list;
	}
	public void setList(List<User> list) {
		this.list = list;
	}
	public Integer getStart() {
		return start;
	}
	public void setStart(Integer start) {
		this.start = start;
	}
	
	
}
