package com.fade.domain;

import java.io.Serializable;

public class Comment implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2152733214028496361L;
	private Integer comment_id;           
	private Integer user_id;             
	private String  nickname;            
	private String  head_image_url;      
	private Integer to_comment_id;          
	 
	private Integer note_id;          
	private String comment_time;      
	private String comment_content;     
	private Integer comment_good_num;  
	

	public Integer getComment_id() {
		return comment_id;
	}
	public void setComment_id(Integer comment_id) {
		this.comment_id = comment_id;
	}
	public Integer getUser_id() {
		return user_id;
	}
	public void setUser_id(Integer user_id) {
		this.user_id = user_id;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getHead_image_url() {
		return head_image_url;
	}
	public void setHead_image_url(String head_image_url) {
		this.head_image_url = head_image_url;
	}
	public Integer getNote_id() {
		return note_id;
	}
	public void setNote_id(Integer note_id) {
		this.note_id = note_id;
	}
	public String getComment_time() {
		return comment_time;
	}
	public Integer getTo_comment_id() {
		return to_comment_id;
	}
	public void setTo_comment_id(Integer to_comment_id) {
		this.to_comment_id = to_comment_id;
	}
	public void setComment_time(String comment_time) {
		this.comment_time = comment_time;
	}
	public String getComment_content() {
		return comment_content;
	}
	public void setComment_content(String comment_content) {
		this.comment_content = comment_content;
	}
	public Integer getComment_good_num() {
		return comment_good_num;
	}
	public void setComment_good_num(Integer comment_good_num) {
		this.comment_good_num = comment_good_num;
	}

	
}
