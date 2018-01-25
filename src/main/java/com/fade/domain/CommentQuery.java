package com.fade.domain;

import java.util.List;

public class CommentQuery {
	private Integer start;
	private List<Comment>list;
	
	private String point;//通知页面查询时间点
	
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
	public String getPoint() {
		return point;
	}
	public void setPoint(String point) {
		this.point = point;
	}
	
	
}
