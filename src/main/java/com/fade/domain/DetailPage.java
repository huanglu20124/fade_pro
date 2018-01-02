package com.fade.domain;

import java.util.List;

public class DetailPage {
	//帖子详情页
	private List<Note>second_list; //10条增减秒列表
	private List<Comment>comment_list; //10条评论列表
	private Integer comment_num; //这三个数量用于更新
	private Integer add_num;
	private Integer sub_num;
	private Long fetchTime; 
	
	public List<Note> getSecond_list() {
		return second_list;
	}
	public void setSecond_list(List<Note> second_list) {
		this.second_list = second_list;
	}
	public List<Comment> getComment_list() {
		return comment_list;
	}
	public void setComment_list(List<Comment> comment_list) {
		this.comment_list = comment_list;
	}
	public Integer getComment_num() {
		return comment_num;
	}
	public void setComment_num(Integer comment_num) {
		this.comment_num = comment_num;
	}
	public Integer getAdd_num() {
		return add_num;
	}
	public void setAdd_num(Integer add_num) {
		this.add_num = add_num;
	}
	public Integer getSub_num() {
		return sub_num;
	}
	public void setSub_num(Integer sub_num) {
		this.sub_num = sub_num;
	}
	
	public Long getFetchTime() {
		return fetchTime;
	}
	public void setFetchTime(Long fetchTime) {
		this.fetchTime = fetchTime;
	}
	
	
}
