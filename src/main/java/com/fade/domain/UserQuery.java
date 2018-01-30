package com.fade.domain;

import java.util.List;

public class UserQuery {
	private List<User>list;
	private Integer start;
	
	private String point;//通知页面查询时间点
	private Integer sum;//搜索页用（返回的搜索结果总数量，上限50条）
	
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
	public String getPoint() {
		return point;
	}
	public void setPoint(String point) {
		this.point = point;
	}
	public Integer getSum() {
		return sum;
	}
	public void setSum(Integer sum) {
		this.sum = sum;
	}
	
	
}
