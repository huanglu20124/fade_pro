package com.fade.domain;

import java.io.Serializable;
import java.util.List;

public class Comment implements Serializable{
	//一级评论

	private static final long serialVersionUID = -2152733214028496361L;
	private Integer comment_id;           
	private Integer user_id;             
	private String  nickname;            
	private String  head_image_url;      	 
	private Integer note_id;          
	private String comment_time;      
	private String comment_content;
	private List<SecondComment>comments;//二级评论列表
	private Integer type; //0代表增秒评论，1代表减秒评论
	
	//以下是用于通知显示的属性
	private String exampleImage;//示例图片
	private String note_content;//文字内容

	public String getExampleImage() {
		return exampleImage;
	}
	public void setExampleImage(String exampleImage) {
		this.exampleImage = exampleImage;
	}
	public String getNote_content() {
		return note_content;
	}
	public void setNote_content(String note_content) {
		this.note_content = note_content;
	}
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
	public void setComment_time(String comment_time) {
		this.comment_time = comment_time;
	}
	public String getComment_content() {
		return comment_content;
	}
	public void setComment_content(String comment_content) {
		this.comment_content = comment_content;
	}
	public List<SecondComment> getComments() {
		return comments;
	}
	public void setComments(List<SecondComment> comments) {
		this.comments = comments;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	@Override
	public String toString() {
		return "comment_id="+comment_id + ",comment_content="+comment_content;
	}
}
