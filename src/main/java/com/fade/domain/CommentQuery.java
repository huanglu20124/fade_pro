package com.fade.domain;

import java.util.List;

public class CommentQuery {
	private Integer start;
	private List<Comment>list;
	
	public Integer getStart() {
		return start;
	}
	public void setStart(Integer start) {
		this.start = start;
	}
	public List<Comment> getList() {
		return list;
	}
	public void setList(List<Comment> list) {
		this.list = list;
	}
	
	
}
